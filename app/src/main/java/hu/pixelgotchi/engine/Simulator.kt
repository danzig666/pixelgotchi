package hu.pixelgotchi.engine

import hu.pixelgotchi.engine.GameBalance.VERSION
import hu.pixelgotchi.engine.GameBalance.MINUTE
import hu.pixelgotchi.engine.GameBalance.HOUR
import hu.pixelgotchi.engine.GameBalance.DAY
import hu.pixelgotchi.engine.GameBalance.INCUBATION
import hu.pixelgotchi.engine.GameBalance.HUNGER
import hu.pixelgotchi.engine.GameBalance.HAPPINESS
import hu.pixelgotchi.engine.GameBalance.WASTE
import hu.pixelgotchi.engine.GameBalance.INFECTION
import hu.pixelgotchi.engine.GameBalance.CARE_GRACE
import hu.pixelgotchi.engine.GameBalance.RECOVERY_WAIT
import hu.pixelgotchi.engine.GameBalance.MAX_HEALTH
import hu.pixelgotchi.engine.GameBalance.STARVE_RATE
import hu.pixelgotchi.engine.GameBalance.SICK_RATE
import hu.pixelgotchi.engine.GameBalance.RECOVERY_RATE
import hu.pixelgotchi.engine.GameBalance.LIFESPAN
import hu.pixelgotchi.engine.GameBalance.EVOLUTION_AGES
import hu.pixelgotchi.engine.GameBalance.minimumWeight
import java.time.Instant
import java.time.ZoneId

/** Event driven, fixed-point health. Mutates only the supplied snapshot; no Android dependencies. */
class Simulator {
    fun advance(p: Pet, target: Long): Pet {
        require(p.rulesVersion == VERSION) { "Unsupported rules version ${p.rulesVersion}" }
        if (!p.alive || target < p.processedAt) return p
        settle(p)
        while (p.alive && p.processedAt < target) {
            val now = p.processedAt
            var end = target
            fun due(delta: Long) { if (delta > 0) end = minOf(end, now + delta) }
            if (p.stage == Stage.EGG) {
                due(p.createdAt + INCUBATION - now)
            } else {
                end = minOf(end, nextScheduleBoundary(p, now))
                EVOLUTION_AGES.firstOrNull { it > p.age() }?.let { due(it - p.age()) }
                if (p.sleeping) {
                    if (p.light && !p.nightFault) due(p.sleepSince + CARE_GRACE - now)
                } else {
                    due(HUNGER - p.hungerTimer)
                    due(HAPPINESS - p.joyTimer)
                    due(WASTE - p.poopTimer)
                    if (p.poop >= 2 && !p.sick) due(INFECTION - p.dirtyTime)
                    due(p.nextCall - p.callTimer)
                    val flags = deficits(p)
                    flags.forEachIndexed { i, active -> if (active && !p.deficits[i].counted) due(CARE_GRACE - p.deficits[i].elapsed) }
                    val rate = damage(p)
                    if (rate > 0) due((p.healthUnits + rate - 1) / rate)
                    if (p.good() && p.recoveryTimer < RECOVERY_WAIT) due(RECOVERY_WAIT - p.recoveryTimer)
                }
            }
            p.pendingSchedule?.let { if (it.effectiveAt > now) end = minOf(end, it.effectiveAt) }
            check(end > now) { "Non advancing event" }
            val delta = end - now
            if (p.stage != Stage.EGG && !p.sleeping) {
                val rate = damage(p)
                if (rate > 0) p.healthUnits = (p.healthUnits - rate * delta).coerceAtLeast(0)
                else if (p.good() && p.recoveryTimer >= RECOVERY_WAIT) p.healthUnits = minOf(MAX_HEALTH, p.healthUnits + RECOVERY_RATE * delta)
                p.hungerTimer += delta
                p.joyTimer += delta
                p.poopTimer += delta
                p.callTimer += delta
                if (p.poop >= 2 && !p.sick) p.dirtyTime += delta
                if (p.sick) p.totalSick += delta
                if (p.food == 0 || p.joy == 0) p.totalDeprived += delta
                if (p.good()) p.recoveryTimer = minOf(RECOVERY_WAIT, p.recoveryTimer + delta) else p.recoveryTimer = 0
                deficits(p).forEachIndexed { i, active -> if (active) p.deficits[i].elapsed += delta }
            }
            p.processedAt = end
            // Damage belongs to the interval BEFORE its end, even when a sleep boundary coincides.
            if (p.healthUnits <= 0) die(p, when { p.food == 0 && p.sick -> DeathCause.BOTH; p.sick -> DeathCause.ILLNESS; else -> DeathCause.STARVATION })
            if (p.alive) settle(p)
        }
        return p
    }

    private fun damage(p: Pet) = (if (p.food == 0) STARVE_RATE else 0) + (if (p.sick) SICK_RATE else 0)
    private fun deficits(p: Pet) = listOf(p.food == 0, p.joy == 0, p.sick, p.poop >= 2)
    private fun settle(p: Pet) {
        val now = p.processedAt
        p.pendingSchedule?.let { if (now >= it.effectiveAt) { p.schedule = it.schedule; p.pendingSchedule = null } }
        if (p.stage == Stage.EGG) {
            if (now < p.createdAt + INCUBATION) return
            p.bornAt = p.createdAt + INCUBATION
            p.stage = Stage.BABY
            p.history.add(Evolution(p.bornAt, Stage.BABY, 0))
        }
        if (p.age() >= LIFESPAN) { die(p, DeathCause.AGE); return }
        val stage = when {
            p.age() >= 20 * DAY -> Stage.ELDER
            p.age() >= 120 * HOUR -> Stage.ADULT
            p.age() >= 48 * HOUR -> Stage.TEEN
            p.age() >= HOUR -> Stage.CHILD
            else -> Stage.BABY
        }
        if (stage != p.stage) {
            if (stage == Stage.ADULT) p.variant = EvolutionRules.adult(p)
            p.stage = stage
            p.weight = maxOf(p.weight, minimumWeight(stage))
            p.history.add(Evolution(now, stage, p.variant))
        }
        val asleep = isSleeping(p.schedule, now)
        // Count the previous night's light fault before switching to the next state.
        if (p.sleeping && p.light && !p.nightFault && now - p.sleepSince >= CARE_GRACE) {
            p.nightFault = true; p.mistakes++
        }
        if (asleep && !p.sleeping) { p.sleepSince = now; p.nightFault = false }
        if(!asleep && p.sleeping)p.wokeAt=now
        p.sleeping = asleep
        // Events earned during the preceding awake interval still occur exactly at bedtime.
        while (p.hungerTimer >= HUNGER) { p.hungerTimer -= HUNGER; p.food = (p.food - 1).coerceAtLeast(0) }
        while (p.joyTimer >= HAPPINESS) { p.joyTimer -= HAPPINESS; p.joy = (p.joy - 1).coerceAtLeast(0) }
        while (p.poopTimer >= WASTE) { p.poopTimer -= WASTE; p.poop++ }
        if (p.poop >= 2 && !p.sick && p.dirtyTime >= INFECTION) { p.sick = true; p.sickSince = now }
        if (p.callTimer >= p.nextCall) {
            p.callTimer = 0
            if (p.food >= 3 && p.joy >= 3 && p.poop == 0 && !p.sick) p.falseCall = true
            p.nextCall = (6 + random(p) % 4) * HOUR
        }
        normalize(p)
    }

    private fun normalize(p: Pet) {
        if (!p.good()) p.recoveryTimer = 0
        deficits(p).forEachIndexed { i, active ->
            val d = p.deficits[i]
            if (!active) { d.elapsed = 0; d.counted = false }
            else if (!d.counted && d.elapsed >= CARE_GRACE) { d.counted = true; p.mistakes++ }
        }
    }
    private fun die(p: Pet, cause: DeathCause) {
        p.diedAt = p.processedAt; p.deathCause = cause; p.falseCall = false; p.game = null
    }
    private fun random(p: Pet): Long { p.seed = (p.seed * 1103515245 + 12345).and(0x7fffffff); return p.seed }

    fun act(p: Pet, at: Long, action: Action): Feedback {
        advance(p, at)
        if (!p.alive) return Feedback.DEAD
        if (action == Action.LIGHT) { p.light = !p.light; return Feedback.LIGHT }
        if (p.stage == Stage.EGG) return Feedback.EGG
        if (p.sleeping) return Feedback.SLEEPING
        val feedback = when(action) {
            Action.MEAL -> if (p.food == 4) Feedback.FULL else { p.food++; p.weight++; Feedback.MEAL }
            Action.SNACK -> { p.joy = minOf(4, p.joy + 1); p.weight += 2; p.snacks++; Feedback.SNACK }
            Action.CLEAN -> { p.poop = 0; p.dirtyTime = 0; Feedback.CLEAN }
            Action.MEDICINE -> if (!p.sick) Feedback.HEALTHY else { p.sick = false; p.sickSince = 0; p.dirtyTime = 0; Feedback.CURED }
            Action.TEACH -> if (p.teachable()) { p.training++; p.falseCall = false; Feedback.TAUGHT } else if (p.falseCall) Feedback.NEED_CARE else Feedback.NO_CALL
            Action.START_GAME -> { if (p.game == null || p.game!!.finished) p.game = RoundGame(); Feedback.GAME }
            Action.CANCEL_GAME -> { p.game = null; Feedback.CANCELLED }
            Action.LIGHT -> Feedback.LIGHT
        }
        normalize(p)
        return feedback
    }
    fun guess(p: Pet, at: Long, direction: Int): Feedback {
        advance(p, at)
        if (!p.alive) return Feedback.DEAD
        if (p.sleeping) { p.game = null; return Feedback.SLEEPING }
        val g = p.game ?: return Feedback.CANCELLED
        if (g.finished) return if (g.hits >= 3) Feedback.WON else Feedback.LOST
        require(direction in 0..1)
        g.lastDirection = (random(p) / 65536 % 2).toInt()
        g.round++
        if (direction == g.lastDirection) g.hits++
        if (g.round == 5) {
            g.finished = true
            if (g.hits >= 3 && !g.rewarded) {
                g.rewarded = true; p.wins++; p.joy = minOf(4, p.joy + 1)
                p.weight = maxOf(minimumWeight(p.stage), p.weight - 1)
            }
            normalize(p)
            return if (g.hits >= 3) Feedback.WON else Feedback.LOST
        }
        return if (g.lastDirection == 0) Feedback.LEFT else Feedback.RIGHT
    }
    fun changeSchedule(p: Pet, at: Long, schedule: Schedule) {
        advance(p, at); require(schedule.valid())
        val zone = ZoneId.of(p.schedule.zone)
        val nextDay = Instant.ofEpochMilli(p.processedAt).atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        p.pendingSchedule = PendingSchedule(schedule, nextDay)
    }
    /** The UI may sleep until this event; animation frames never drive game state. */
    fun nextEventAt(p: Pet): Long {
        if (!p.alive) return Long.MAX_VALUE
        val now=p.processedAt
        if(p.stage==Stage.EGG)return p.createdAt+INCUBATION
        val candidates=mutableListOf(nextScheduleBoundary(p,now))
        fun due(delta: Long) { if(delta>0)candidates.add(now+delta) }
        EVOLUTION_AGES.firstOrNull{it>p.age()}?.let{due(it-p.age())}
        p.pendingSchedule?.let{candidates.add(it.effectiveAt)}
        if(p.sleeping) { if(p.light && !p.nightFault)due(p.sleepSince+CARE_GRACE-now) }
        else {
            due(HUNGER-p.hungerTimer);due(HAPPINESS-p.joyTimer);due(WASTE-p.poopTimer)
            if(p.poop>=2 && !p.sick)due(INFECTION-p.dirtyTime)
            due(p.nextCall-p.callTimer)
            deficits(p).forEachIndexed { i,active -> if(active && !p.deficits[i].counted)due(CARE_GRACE-p.deficits[i].elapsed) }
            val rate=damage(p);if(rate>0)due((p.healthUnits+rate-1)/rate)
            if(p.good() && p.recoveryTimer<RECOVERY_WAIT)due(RECOVERY_WAIT-p.recoveryTimer)
        }
        return candidates.filter{it>now}.minOrNull() ?: now+HOUR
    }
    companion object {
        fun isSleeping(s: Schedule, at: Long): Boolean {
            val local = Instant.ofEpochMilli(at).atZone(ZoneId.of(s.zone))
            val minute = local.hour * 60 + local.minute
            return if (s.bedtime > s.wake) minute >= s.bedtime || minute < s.wake else minute >= s.bedtime && minute < s.wake
        }
        fun nextScheduleBoundary(p: Pet, at: Long): Long {
            val zone = ZoneId.of(p.schedule.zone)
            val date = Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
            return (0L..2L).flatMap { day -> listOf(p.schedule.bedtime, p.schedule.wake).map { minute ->
                date.plusDays(day).atTime(minute / 60, minute % 60).atZone(zone).toInstant().toEpochMilli()
            } }.filter { it > at }.min()
        }
    }
}
