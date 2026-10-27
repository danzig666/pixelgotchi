package hu.pixelgotchi.engine

import java.time.ZoneId
import java.util.UUID

object GameBalance {
    const val VERSION = 1
    const val MINUTE = 60_000L
    const val HOUR = 60 * MINUTE
    const val DAY = 24 * HOUR
    const val INCUBATION = MINUTE
    const val HUNGER = 2 * HOUR
    const val HAPPINESS = 3 * HOUR
    const val WASTE = 4 * HOUR
    const val INFECTION = 4 * HOUR
    const val CARE_GRACE = 30 * MINUTE
    const val RECOVERY_WAIT = HOUR
    const val MAX_HEALTH = 100 * HOUR
    const val STARVE_RATE = 6L
    const val SICK_RATE = 4L
    const val RECOVERY_RATE = 2L
    const val LIFESPAN = 30 * DAY
    const val NOTIFICATION_GAP = 2 * HOUR
    val EVOLUTION_AGES = listOf(HOUR, 48 * HOUR, 120 * HOUR, 20 * DAY, LIFESPAN)
    fun minimumWeight(stage: Stage) = when(stage) {
        Stage.EGG, Stage.BABY -> 5
        Stage.CHILD -> 10
        Stage.TEEN -> 15
        Stage.ADULT, Stage.ELDER -> 20
    }
}
enum class Stage { EGG, BABY, CHILD, TEEN, ADULT, ELDER }
enum class DeathCause { STARVATION, ILLNESS, BOTH, AGE }
enum class Action { MEAL, SNACK, CLEAN, MEDICINE, LIGHT, TEACH, START_GAME, CANCEL_GAME }
enum class Feedback { MEAL, SNACK, FULL, CLEAN, CURED, HEALTHY, LIGHT, TAUGHT, NO_CALL, NEED_CARE, SLEEPING, EGG, DEAD, GAME, CANCELLED, LEFT, RIGHT, WON, LOST }
data class Schedule(val bedtime: Int = 20 * 60 + 30, val wake: Int = 7 * 60, val zone: String = ZoneId.systemDefault().id) {
    fun valid(): Boolean = ((wake - bedtime + 1440) % 1440) in 480..720
}
data class PendingSchedule(val schedule: Schedule, val effectiveAt: Long)
data class Deficit(var elapsed: Long = 0, var counted: Boolean = false)
data class Evolution(val at: Long, val stage: Stage, val variant: Int)
data class RoundGame(var round: Int = 0, var hits: Int = 0, var lastDirection: Int = -1, var finished: Boolean = false, var rewarded: Boolean = false)
data class Pet(
    val profile: String,
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long,
    var processedAt: Long = createdAt,
    var bornAt: Long = 0,
    var stage: Stage = Stage.EGG,
    var variant: Int = 0,
    var food: Int = 4,
    var joy: Int = 4,
    var healthUnits: Long = GameBalance.MAX_HEALTH,
    var weight: Int = 5,
    var training: Int = 0,
    var poop: Int = 0,
    var dirtyTime: Long = 0,
    var sick: Boolean = false,
    var sickSince: Long = 0,
    var totalSick: Long = 0,
    var totalDeprived: Long = 0,
    var sleeping: Boolean = false,
    var sleepSince: Long = 0,
    var wokeAt: Long = 0,
    var nightFault: Boolean = false,
    var light: Boolean = true,
    var schedule: Schedule = Schedule(),
    var pendingSchedule: PendingSchedule? = null,
    var hungerTimer: Long = 0,
    var joyTimer: Long = 0,
    var poopTimer: Long = 0,
    var recoveryTimer: Long = 0,
    var callTimer: Long = 0,
    var nextCall: Long = 6 * GameBalance.HOUR,
    var falseCall: Boolean = false,
    var mistakes: Int = 0,
    var deficits: MutableList<Deficit> = MutableList(4) { Deficit() },
    var wins: Int = 0,
    var snacks: Int = 0,
    var seed: Long = id.hashCode().toLong().and(0x7fffffff),
    var game: RoundGame? = null,
    var history: MutableList<Evolution> = mutableListOf(),
    var diedAt: Long = 0,
    var deathCause: DeathCause? = null,
    val rulesVersion: Int = GameBalance.VERSION,
    var lastNotice: Long = 0,
    var noticeSeverity: Int = 0
) {
    val alive get() = diedAt == 0L
    val health get() = healthUnits.toDouble() / GameBalance.HOUR
    fun age(at: Long = processedAt) = if (bornAt == 0L) 0L else ((if(alive) at else diedAt) - bornAt).coerceAtLeast(0)
    fun good() = food >= 2 && joy >= 1 && poop == 0 && !sick
    fun teachable() = falseCall && food >= 3 && joy >= 3 && poop == 0 && !sick
    fun severity() = when { !alive || stage == Stage.EGG || sleeping -> 0; sick -> 3; food == 0 -> 2; joy == 0 || poop > 0 || falseCall -> 1; else -> 0 }
}

/** All branches are deterministic; care quality is never randomly selected. */
object EvolutionRules {
    fun adult(p: Pet): Int = when {
        p.mistakes >= 8 || p.totalDeprived >= 24 * GameBalance.HOUR -> 6
        p.mistakes >= 5 || p.totalSick >= 12 * GameBalance.HOUR -> 5
        p.snacks >= 12 || p.weight > 35 -> 4
        p.training >= 4 && p.wins >= 6 && p.mistakes <= 1 -> 0
        p.wins >= 5 && p.mistakes <= 2 -> 1
        p.training >= 3 && p.mistakes <= 3 -> 2
        else -> 3
    }
}
