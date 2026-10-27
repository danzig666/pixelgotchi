package hu.pixelgotchi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import hu.pixelgotchi.data.*
import hu.pixelgotchi.engine.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    private val context=ApplicationProvider.getApplicationContext<Context>()
    @Test fun realDatabaseReopenPreservesSnapshotAndAlbumIsIdempotent() {
        val name="persistence-test.db";context.deleteDatabase(name)
        fun open()=Room.databaseBuilder(context,GameDatabase::class.java,name).addMigrations(GameDatabase.MIGRATION_1_2).build()
        var db=open()
        db.runInTransaction {
            db.game().save(Snapshot(json="{\"name\":\"Árvíztűrő\"}"))
            repeat(2){db.game().remember(Memory("one","Lili","{}"))}
        }
        db.close();db=open()
        assertTrue(db.game().read()!!.json.contains("Árvíztűrő"));assertEquals(1,db.game().album("Lili").size);assertEquals(0,db.game().album("Léna").size)
        db.close();context.deleteDatabase(name)
    }
    @Test fun realVersionOneMigratesWithoutLosingSave() {
        val name="migration-test.db";context.deleteDatabase(name)
        val config=SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(object: SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("CREATE TABLE snapshot (`key` INTEGER NOT NULL, json TEXT NOT NULL, PRIMARY KEY(`key`))");db.execSQL("INSERT INTO snapshot VALUES(1,'preserved')") }
            override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase,oldVersion: Int,newVersion: Int) {}
        }).build()
        val helper=FrameworkSQLiteOpenHelperFactory().create(config);helper.writableDatabase;helper.close()
        val db=Room.databaseBuilder(context,GameDatabase::class.java,name).addMigrations(GameDatabase.MIGRATION_1_2).build()
        assertEquals("preserved",db.game().read()!!.json);assertTrue(db.game().album("Lili").isEmpty());db.close();context.deleteDatabase(name)
    }
    @Test fun concurrentWorkerAndUiDoNotLoseActionsAndStalePetCannotChangeReplacement()=runBlocking {
        val repo=GameRepository.get(context);repo.erase();repo.create("Lili","Pötyi")
        repo.transact { w,now ->
            val p=w.profiles["Lili"]!!;p.bornAt=now-1000;p.stage=Stage.BABY
            // A daytime zone independent of the runner's time of day.
            p.schedule=Schedule(bedtime=1,wake=601,zone="Etc/GMT+12")
            val local=java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.of("UTC")).hour*60
            p.schedule=Schedule((local+120)%1440,(local+720)%1440,"UTC");p.sleeping=false
        }
        val id=repo.state.value!!.profiles["Lili"]!!.id
        coroutineScope {
            repeat(8){launch(Dispatchers.Default){repo.action("Lili",id,Action.SNACK)}}
            repeat(8){launch(Dispatchers.Default){repo.transact()}}
        }
        assertEquals(8,repo.state.value!!.profiles["Lili"]!!.snacks)
        repo.transact { w,now -> val p=w.profiles["Lili"]!!;p.diedAt=now;p.deathCause=DeathCause.ILLNESS }
        repo.create("Lili","Új Pötyi")
        val replacement=repo.state.value!!.profiles["Lili"]!!
        repo.action("Lili",id,Action.SNACK)
        assertEquals(replacement.id,repo.state.value!!.profiles["Lili"]!!.id)
        assertEquals(0,repo.state.value!!.profiles["Lili"]!!.snacks)
        assertEquals(1,repo.album("Lili").size);assertTrue(repo.album("Léna").isEmpty())
        repo.erase()
    }
    @Test fun notificationsOffStillAdvancesAndDoesNotStartUnusedProfile()=runBlocking {
        val repo=GameRepository.get(context);repo.erase();repo.create("Lili","Morzsi")
        repo.transact{w,_->w.preferences.notifications=false}
        // The egg's createdAt must also represent the actual historical creation.
        repo.transact{w,now->w.profiles["Lili"]=Pet("Lili",name="Morzsi",createdAt=now-7*GameBalance.DAY)}
        repo.transact()
        assertFalse(repo.state.value!!.profiles["Lili"]!!.alive)
        assertNull(repo.state.value!!.profiles["Léna"])
        repo.erase()
    }
}
