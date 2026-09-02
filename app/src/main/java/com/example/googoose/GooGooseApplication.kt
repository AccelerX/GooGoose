package com.example.googoose

import android.app.Application
import androidx.room.Room
import com.example.googoose.data.GooGooseRepository
import com.example.googoose.data.db.GooGooseDatabase

class GooGooseApplication : Application() {

    val database: GooGooseDatabase by lazy {
        Room.databaseBuilder(this, GooGooseDatabase::class.java, "googoose.db")
            // No real users yet — a schema change during development just wipes and
            // reseeds rather than needing a hand-written Migration.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val repository: GooGooseRepository by lazy { GooGooseRepository(database) }
}
