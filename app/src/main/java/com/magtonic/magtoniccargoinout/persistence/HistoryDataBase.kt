package com.magtonic.magtoniccargoinout.persistence

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [History::class], version = 2, exportSchema = true)



abstract class HistoryDataBase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "history.db"
    }

    abstract fun historyDao(): HistoryDao
}