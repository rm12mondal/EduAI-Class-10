package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wbbse_master_data")
data class MasterDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val chapterName: String,
    val topicTitle: String,
    val keywords: String,
    val content: String,
    val keyFormulaOrFact: String,
    val examTip: String,
    val isBookmarked: Boolean = false,
    val isUserCreated: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val subjectId: String?,
    val mode: String, // "offline" or "online"
    val sourceLabel: String,
    val latencyMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
