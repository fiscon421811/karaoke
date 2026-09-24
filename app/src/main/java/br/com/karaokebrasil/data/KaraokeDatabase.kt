package br.com.karaokebrasil.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Musica::class], version = 2, exportSchema = true)
abstract class KaraokeDatabase : RoomDatabase() {

    abstract fun musicaDao(): MusicaDao

    companion object {
        fun criar(context: Context): KaraokeDatabase =
            Room.databaseBuilder(context, KaraokeDatabase::class.java, "karaoke.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
