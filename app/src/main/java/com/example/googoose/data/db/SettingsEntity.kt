package com.example.googoose.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.googoose.data.Language
import com.example.googoose.data.TextSizePreset

/** Always exactly one row (id = 0) — app-wide settings, not a list. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val businessName: String,
    val currency: String,
    val language: Language,
    val textSize: TextSizePreset = TextSizePreset.STANDARD,
)
