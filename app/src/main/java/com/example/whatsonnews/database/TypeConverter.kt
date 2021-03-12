package com.example.whatsonnews.database

import androidx.room.TypeConverter
import com.example.whatsonnews.news.Source

class TypeConverter {

    @TypeConverter
    fun fromSource(source: Source): String{
        return source.name
    }

    @TypeConverter
    fun toSource(name:String): Source{
        return Source(null,name)
    }
}