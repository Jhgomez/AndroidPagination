package com.demo.pagination.db

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver

@Database(
    entities = [
        TvShowEntity::class,
        GenreReference::class,
        OriginCountryReference::class,
        PageIndexesEntity::class
    ],
    version = 1
)
@ColumnTypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tvShowDao(): TvShowDao
}

var database: AppDatabase? = null

fun getAppDatabase(context: Context): AppDatabase {
    return database ?: Room.databaseBuilder<AppDatabase>(context = context, "pagination-demo-db")
        .setDriver(AndroidSQLiteDriver())
        .build()
        .also {
            database = it
        }
}