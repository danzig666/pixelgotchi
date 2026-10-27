package hu.pixelgotchi.engine

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import com.google.gson.Gson

class SimulatorTest {
    private val sim = Simulator()
    private val h = GameBalance.HOUR
    private val m = GameBalance.MINUTE
    private val start = Instant.parse("2026-10-20T07:00:00Z").toEpochMilli()
    private fun pet() = Pet("Lili", id = "fixed-id", name = "Pötyi", createdAt = start - m, schedule = Schedule(zone="UTC")).also { sim.advance(it,start) }
    private fun copy(p: Pet) = Gson().fromJson(Gson().toJson(p), Pet::class.java)
    private fun same(a: Pet,b: Pet) = assertEquals(Gson().toJson(a), Gson().toJson(b))
    private fun awake(p: Pet, amount: Long) {
        var remaining = amount
        while(remaining > 0 && p.alive) {
            val boundary = Simulator.nextScheduleBoundary(p,p.processedAt)
            val dt = minOf(remaining,boundary-p.processedAt)
            val asleep = p.sleeping
            sim.advance(p, p.processedAt+dt)
            if(!asleep) remaining-=dt
        }
    }
    @Test fun exactNeglectDeathAndEightHours() {
        val p=pet();awake(p,8*h);assertTrue(p.alive);assertEquals(0,p.food);assertEquals(100.0,p.health,0.0001)
        awake(p,4*h);assertTrue(p.sick);assertEquals(76.0,p.health,0.0001)
        awake(p,7*h+36*m);assertFalse(p.alive);assertEquals(DeathCause.BOTH,p.deathCause)
        // First day: 13.5 awake hours, then 10.5 hours sleep, then 6h06m.
        assertEquals(start + 30*h+6*m,p.diedAt)
    }
    @Test fun closedAndForegroundAreIdentical() {
        val a=pet();val b=copy(a)
        sim.advance(a,start+48*h)
        for(i in 1..2880)sim.advance(b,start+i*m)
        same(a,b);assertFalse(a.alive)
    }
    @Test fun arbitraryChunksAndMillisecondsAreIdentical() {
        val a=pet();a.healthUnits=80*h;val b=copy(a)
        sim.advance(a,start+9*h+12345)
        val random=java.util.Random(7)
        while(b.processedAt<start+9*h+12345)sim.advance(b,minOf(start+9*h+12345,b.processedAt+random.nextInt(300000)+1))
        same(a,b)
    }
    @Test fun weekAbsenceStopsAtHistoricDeathQuickly() {
        val p=pet();val before=System.nanoTime();sim.advance(p,start+7*24*h)
        assertFalse(p.alive);assertTrue(p.diedAt<start+48*h);assertTrue(System.nanoTime()-before<1_000_000_000)
    }
    @Test fun actionsPreserveRemainders() {
        val p=pet();sim.advance(p,start+2*h+30*m);val remainder=p.hungerTimer
        val w=p.weight;assertEquals(Feedback.MEAL,sim.act(p,p.processedAt,Action.MEAL));assertEquals(w+1,p.weight);assertEquals(remainder,p.hungerTimer)
        assertEquals(Feedback.FULL,sim.act(p,p.processedAt,Action.MEAL));val food=p.food;val joyTimer=p.joyTimer
        sim.act(p,p.processedAt,Action.SNACK);assertEquals(food,p.food);assertEquals(w+3,p.weight);assertEquals(joyTimer,p.joyTimer)
        sim.advance(p,start+4*h);assertEquals(3,p.food)
    }
    @Test fun medicineAndCleaningAreSeparateAndHealthyMedicineCannotResetExposure() {
        val p=pet();p.poop=2;p.dirtyTime=3*h
        assertEquals(Feedback.HEALTHY,sim.act(p,start,Action.MEDICINE));assertEquals(3*h,p.dirtyTime)
        sim.advance(p,start+h);assertTrue(p.sick);val health=p.healthUnits
        sim.act(p,p.processedAt,Action.MEDICINE);assertFalse(p.sick);assertEquals(health,p.healthUnits);assertEquals(2,p.poop)
        sim.advance(p,p.processedAt+3*h);assertFalse(p.sick)
        sim.advance(p,p.processedAt+h);assertTrue(p.sick)
        sim.act(p,p.processedAt,Action.CLEAN);assertTrue(p.sick);assertEquals(0,p.poop);assertEquals(0L,p.dirtyTime)
    }
    @Test fun sleepProtectsWithoutCuringAndDarknessDoesNotPauseDay() {
        val p=pet();val bedtime=start+13*h+30*m;sim.advance(p,bedtime)
        p.sick=true;p.food=0;p.poop=3;val health=p.healthUnits
        sim.advance(p,bedtime+10*h+30*m);assertFalse(p.sleeping);assertTrue(p.sick);assertEquals(0,p.food);assertEquals(3,p.poop);assertEquals(health,p.healthUnits)
        sim.act(p,p.processedAt,Action.LIGHT);sim.advance(p,p.processedAt+h);assertEquals(health-10*h,p.healthUnits)
    }
    @Test fun deathIsPermanentAndActionsCannotSaveRetroactively() {
        val p=pet();assertEquals(Feedback.DEAD,sim.act(p,start+7*24*h,Action.MEAL));val dead=copy(p)
        sim.act(p,start+8*24*h,Action.MEDICINE);sim.advance(p,start-h);same(dead,p)
    }
    @Test fun recoveryWaitIsPersistedAndResetsOnlyOnBadCare() {
        val p=pet();p.healthUnits=80*h;sim.advance(p,start+30*m);val loaded=copy(p)
        sim.advance(loaded,start+h+30*m);assertEquals(81.0,loaded.health,0.00001)
        loaded.poop=1;sim.advance(loaded,start+2*h);assertEquals(0L,loaded.recoveryTimer)
    }
    @Test fun teachingOneCallOnlyAndRealNeedsFirst() {
        val p=pet();p.falseCall=true;p.food=0;assertEquals(Feedback.NEED_CARE,sim.act(p,start,Action.TEACH));assertEquals(0,p.training)
        p.food=4;assertEquals(Feedback.TAUGHT,sim.act(p,start,Action.TEACH));sim.act(p,start,Action.TEACH);assertEquals(1,p.training)
    }
    @Test fun gameRewardExactlyOnceEvenAfterReloadAndAbort() {
        val p=pet();p.joy=2;p.weight=8
        sim.act(p,start,Action.START_GAME)
        for(i in 1..5) {
            val next=(p.seed*1103515245+12345).and(0x7fffffff)
            sim.guess(p,start,(next/65536%2).toInt())
        }
        assertEquals(1,p.wins);assertEquals(3,p.joy);assertEquals(7,p.weight)
        val loaded=copy(p);repeat(5){sim.guess(loaded,start,0)};same(p,loaded)
        sim.act(p,start,Action.START_GAME);sim.guess(p,start,1);sim.act(p,start,Action.CANCEL_GAME);assertEquals(1,p.wins)
    }
    @Test fun continuousDeficitCountsOnceAndNewDeficitCountsAgain() {
        val p=pet();p.food=0;sim.advance(p,start+31*m);assertEquals(1,p.mistakes)
        sim.advance(p,start+h);assertEquals(1,p.mistakes)
        sim.act(p,p.processedAt,Action.MEAL);p.food=0;sim.advance(p,p.processedAt+31*m);assertEquals(2,p.mistakes)
    }
    @Test fun lightFaultOncePerNightAndNoDamage() {
        val p=pet();sim.advance(p,start+14*h);val count=p.mistakes;val health=p.healthUnits
        sim.advance(p,start+18*h);assertEquals(count,p.mistakes);assertEquals(health,p.healthUnits)
    }
    @Test fun eggDoesNotConsumeNeedsAndHatchesOffline() {
        val p=Pet("Léna",name="Bogyó",createdAt=start,schedule=Schedule(zone="UTC"))
        sim.advance(p,start+59_999);assertEquals(Stage.EGG,p.stage)
        sim.advance(p,start+m);assertEquals(Stage.BABY,p.stage);assertEquals(0L,p.hungerTimer);assertEquals(start+m,p.bornAt)
    }
    @Test fun profileSnapshotsAreIndependent() {
        val lili=pet();val lena=Pet("Léna",name="Morzsi",createdAt=start,schedule=Schedule(zone="UTC"))
        sim.advance(lili,start+3*h);sim.advance(lena,start+3*h)
        sim.act(lili,start+3*h,Action.MEAL);assertEquals(4,lili.food);assertEquals(3,lena.food);assertNotEquals(lili.id,lena.id)
    }
    @Test fun naturalDeathAndAllEvolutionThresholds() {
        val p=pet();p.bornAt=start-30*24*h;p.processedAt=start;sim.advance(p,start);assertEquals(DeathCause.AGE,p.deathCause)
        val q=pet();assertEquals(3,EvolutionRules.adult(q));q.training=4;q.wins=6;assertEquals(0,EvolutionRules.adult(q))
        q.training=0;assertEquals(1,EvolutionRules.adult(q));q.wins=0;q.training=3;assertEquals(2,EvolutionRules.adult(q))
        q.snacks=12;assertEquals(4,EvolutionRules.adult(q));q.mistakes=5;assertEquals(5,EvolutionRules.adult(q));q.mistakes=8;assertEquals(6,EvolutionRules.adult(q))
    }
    @Test fun scheduleChangesOnlyNextDayAndDstIsChunkIndependent() {
        val a=pet();a.schedule=Schedule(zone="Europe/Budapest")
        sim.changeSchedule(a,start,Schedule(21*60,7*60,"Europe/Budapest"));assertEquals(20*60+30,a.schedule.bedtime)
        val b=copy(a);val end=Instant.parse("2026-10-27T08:00:00Z").toEpochMilli()
        sim.advance(a,end);while(b.alive&&b.processedAt<end)sim.advance(b,minOf(end,b.processedAt+17*m));same(a,b)
    }
    @Test fun actualAutumnDstNightProtectsForElevenAndHalfHours() {
        val bedtime=Instant.parse("2026-10-24T18:30:00Z").toEpochMilli()
        val p=Pet("Lili",name="Pötyi",createdAt=bedtime-m,schedule=Schedule(zone="Europe/Budapest"))
        sim.advance(p,bedtime);p.sick=true;p.healthUnits=60*h
        val b=copy(p)
        val dawn=Instant.parse("2026-10-25T06:00:00Z").toEpochMilli()
        sim.advance(p,dawn)
        var t=bedtime
        while(t<dawn){t=minOf(dawn,t+13*m);sim.advance(b,t)}
        same(p,b);assertFalse(p.sleeping);assertTrue(p.sick);assertEquals(60.0,p.health,0.0);assertEquals(4,p.food)
        assertEquals(11*h+30*m,dawn-bedtime)
    }
    @Test fun caredPetEvolvesAcrossAllStagesAndEventuallyDiesOfAge() {
        val p=pet()
        val ending=start+31*GameBalance.DAY
        var time=start
        while(p.alive && time<ending) {
            sim.advance(p,time)
            if(p.alive && !p.sleeping) {
                repeat(4){sim.act(p,time,Action.MEAL)}
                if(p.joy<4)repeat(4-p.joy){sim.act(p,time,Action.SNACK)}
                sim.act(p,time,Action.CLEAN);sim.act(p,time,Action.MEDICINE)
            }
            // Extinguish the lamp at night; darkness never pauses the daytime simulation.
            if(p.sleeping && p.light)sim.act(p,time,Action.LIGHT)
            time+=h
        }
        assertEquals(DeathCause.AGE,p.deathCause);assertEquals(start+30*GameBalance.DAY,p.diedAt)
        assertEquals(listOf(Stage.BABY,Stage.CHILD,Stage.TEEN,Stage.ADULT,Stage.ELDER),p.history.map{it.stage})
    }

}
