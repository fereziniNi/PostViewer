package br.edu.ifsp.scl.sc3044025.postviewer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.edu.ifsp.scl.sc3044025.postviewer.data.local.dao.LocalCommentDao
import br.edu.ifsp.scl.sc3044025.postviewer.data.local.entity.LocalCommentEntity

@Database(entities = [LocalCommentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun localCommentDao(): LocalCommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "postviewer_database"
                ).build().also { INSTANCE = it }
            }
    }
}