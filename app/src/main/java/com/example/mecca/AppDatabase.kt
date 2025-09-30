package com.example.mecca

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mecca.DAOs.CustomerDAO
import com.example.mecca.DAOs.SystemTypeDAO
import com.example.mecca.DataClasses.MdModelsLocal
import com.example.mecca.DataClasses.MdSystemLocal
import com.example.mecca.DataClasses.SystemTypeLocal
import com.example.mecca.CustomerLocal
import com.example.mecca.DAOs.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.DAOs.UserDao
import com.example.mecca.DataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.DataClasses.UserEntity


@Database(entities = [CustomerLocal::class,
    UserEntity::class,
    MdModelsLocal::class,
    MdSystemLocal::class,
    MetalDetectorConveyorCalibrationLocal::class,
    SystemTypeLocal::class],
    version = 34, // Increment the version if needed
    exportSchema = false)

@TypeConverters(Converters::class) // Add your Converters here

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDAO
    abstract fun mdModelDao(): MdModelsDAO
    abstract fun mdSystemDAO(): MetalDetectorSystemsDAO
    abstract fun systemTypeDAO(): SystemTypeDAO
    abstract fun metalDetectorConveyorCalibrationDAO(): MetalDetectorConveyorCalibrationDAO


    // What it does: The main access point for the database. It ties the entity and DAO together, providing a place where you can request the DAO.
    // How it interacts: The AppDatabase class knows about all the entities (tables) and DAOs (operations) and is responsible for managing the actual SQLite database.
    // AppDatabase: Registers the entities (tables like MdSystemLocal) and DAOs (like CalibrationMdSystemsDAO).
    // The getDatabase() method provides a way to access the database. This method will create the database if it doesn’t exist yet and manage it for the app.

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()  // Handles migrations if the database schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
