package hu.pixelgotchi.engine

import hu.pixelgotchi.data.*
import org.junit.Assert.*
import org.junit.Test

class SaveCodecTest {
    @Test fun unusedProfilesSurviveFirstSaveAndReload() {
        val original=World()
        val json=SaveCodec.gson.toJson(original)
        assertTrue(json.contains("\"Lili\":null"));assertTrue(json.contains("\"Léna\":null"))
        val loaded=SaveCodec.decode(json)
        assertEquals(setOf("Lili","Léna"),loaded.profiles.keys)
        assertTrue(loaded.profiles.values.all{it==null})
    }
    @Test fun startingOneProfileDoesNotRemoveTheOtherAndHungarianNamePersists() {
        val w=World();w.profiles["Lili"]=Pet("Lili",id="roundtrip",name="Árvíztűrő",createdAt=1_700_000_000_000,schedule=Schedule(zone="UTC"))
        val loaded=SaveCodec.decode(SaveCodec.gson.toJson(w))
        assertEquals("Árvíztűrő",loaded.profiles["Lili"]!!.name);assertEquals("roundtrip",loaded.profiles["Lili"]!!.id)
        assertTrue(loaded.profiles.containsKey("Léna"));assertNull(loaded.profiles["Léna"])
    }
    @Test(expected=IllegalArgumentException::class) fun invalidHeartsAreRejectedBeforeReachingUi() {
        val w=World();w.profiles["Lili"]=Pet("Lili",name="Pötyi",createdAt=1_700_000_000_000).also{it.food=9}
        SaveCodec.decode(SaveCodec.gson.toJson(w))
    }
    @Test fun fullDeathSnapshotRoundTripDoesNotResurrect() {
        val w=World();val p=Pet("Léna",name="Morzsi",createdAt=1_700_000_000_000,schedule=Schedule(zone="UTC"))
        Simulator().advance(p,p.createdAt+7*GameBalance.DAY);w.profiles["Léna"]=p
        val loaded=SaveCodec.decode(SaveCodec.gson.toJson(w)).profiles["Léna"]!!
        assertFalse(loaded.alive);assertEquals(p.diedAt,loaded.diedAt);assertEquals(p.deathCause,loaded.deathCause)
    }
}
