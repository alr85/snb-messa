package com.snb.inspect

// ADD: import your three new entity classes
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.snb.inspect.daos.ConveyorDao
import com.snb.inspect.daos.CustomerDAO
import com.snb.inspect.daos.FreefallDao
import com.snb.inspect.daos.MdModelsDAO
import com.snb.inspect.daos.MetalDetectorConveyorCalibrationDAO
import com.snb.inspect.daos.MetalDetectorSystemsDAO
import com.snb.inspect.daos.NoticesDAO
import com.snb.inspect.daos.PipelineDao
import com.snb.inspect.daos.SystemTypeDAO
import com.snb.inspect.daos.UserDao
import com.snb.inspect.dataClasses.ConveyorRetailerSensitivitiesEntity
import com.snb.inspect.dataClasses.CustomerLocal
import com.snb.inspect.dataClasses.FreefallThroatRetailerSensitivitiesEntity
import com.snb.inspect.dataClasses.MdModelsLocal
import com.snb.inspect.dataClasses.MdSystemLocal
import com.snb.inspect.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.snb.inspect.dataClasses.NoticeLocal
import com.snb.inspect.dataClasses.PipelineRetailerSensitivitiesEntity
import com.snb.inspect.dataClasses.SystemTypeLocal
import com.snb.inspect.dataClasses.UserEntity

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
        PipelineRetailerSensitivitiesEntity::class,
        NoticeLocal::class

    ],
    version = 55,
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

    abstract fun noticesDAO(): NoticesDAO


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
