package com.example.googoose.data.db

import androidx.room.TypeConverter
import com.example.googoose.data.Language
import com.example.googoose.data.TextSizePreset
import com.example.googoose.data.model.TxnType

class Converters {
    @TypeConverter
    fun txnTypeToString(value: TxnType): String = value.name

    @TypeConverter
    fun stringToTxnType(value: String): TxnType = TxnType.valueOf(value)

    @TypeConverter
    fun languageToString(value: Language): String = value.name

    @TypeConverter
    fun stringToLanguage(value: String): Language = Language.valueOf(value)

    @TypeConverter
    fun textSizeToString(value: TextSizePreset): String = value.name

    @TypeConverter
    fun stringToTextSize(value: String): TextSizePreset = TextSizePreset.valueOf(value)
}
