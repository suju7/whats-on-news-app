package com.example.whatsonnews.database

import android.content.Context
import androidx.room.*
import com.example.whatsonnews.news.Article

@Database(
    entities = [Article::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun getArticleDAO(): ArticleDAO

    companion object{
        private const val TAG = "ArticleDatabase"
        @Volatile
        private var instance: ArticleDatabase? = null
        private val LOCK = Object()

        fun getInstance(context: Context): ArticleDatabase {
            if(instance==null){
                synchronized(LOCK){
                    //Log.e(TAG,"Creating new database...")
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ArticleDatabase::class.java,
                        "myRoomDatabase"
                    ).build()
                }
            }
            //Log.e(TAG, "getting the database")
            return instance!!
        }
    }
}