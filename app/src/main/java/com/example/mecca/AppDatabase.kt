package com.example.mecca

// ADD: import your three new entity classes
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mecca.daos.ConveyorDao
import com.example.mecca.daos.CustomerDAO
import com.example.mecca.daos.FreefallDao
import com.example.mecca.daos.MdModelsDAO
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.daos.MetalDetectorSystemsDAO
import com.example.mecca.daos.PipelineDao
import com.example.mecca.daos.SystemTypeDAO
import com.example.mecca.daos.UserDao
import com.example.mecca.dataClasses.ConveyorRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.CustomerLocal
import com.example.mecca.dataClasses.FreefallThroatRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.MdModelsLocal
import com.example.mecca.dataClasses.MdSystemLocal
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.dataClasses.PipelineRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.SystemTypeLocal
import com.example.mecca.dataClasses.UserEntity

@Database(
    entities = [
        CustomerLocal::class,
        UserEntity::class,
        MdModelsLocal::class,
        MdSystemLocal::class,
        MetalDetectorConveyorCalibrationLocal::class,
        SystemTypeLocal::class,
        ConveyorRetailerSensitivitiesEntity::class,
        FreefallThroatRetailerSensitivitiesEntity::class,
        PipelineRetailerSensitivitiesEntity::class

    ],
    version = 48,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDAO
    abstract fun mdModelDao(): MdModelsDAO
    abstract fun mdSystemDAO(): MetalDetectorSystemsDAO
    abstract fun systemTypeDAO(): SystemTypeDAO
    abstract fun metalDetectorConveyorCalibrationDAO(): MetalDetectorConveyorCalibrationDAO

    abstract fun conveyorDao(): ConveyorDao
    abstract fun freefallDao(): FreefallDao
    abstract fun pipelineDao(): PipelineDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = db

                db
            }
        }
    }


}
