package hu.pixelgotchi.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName="snapshot")
data class Snapshot(@PrimaryKey val key: Int = 1, val json: String)
@Entity(tableName="memories")
data class Memory(@PrimaryKey val petId: String, val profile: String, val json: String)
@Dao
interface GameDao {
    @Query("SELECT * FROM snapshot WHERE `key`=1") fun read(): Snapshot?
    @Insert(onConflict=OnConflictStrategy.REPLACE) fun save(snapshot: Snapshot)
    @Insert(onConflict=OnConflictStrategy.IGNORE) fun remember(memory: Memory)
    @Query("SELECT * FROM memories WHERE profile=:profile ORDER BY rowid DESC") fun album(profile: String): List<Memory>
    @Query("DELETE FROM snapshot") fun clearSnapshot()
    @Query("DELETE FROM memories") fun clearMemories()
}
@Database(entities=[Snapshot::class,Memory::class],version=2,exportSchema=true)
abstract class GameDatabase : RoomDatabase() {
    abstract fun game(): GameDao
    companion object {
        val MIGRATION_1_2 = object: Migration(1,2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `memories` (`petId` TEXT NOT NULL, `profile` TEXT NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY(`petId`))")
            }
        }
    }
}
