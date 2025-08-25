package com.joongho.chat.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ChatHistoryService {

    // 메시지를 DB에 저장하는 함수
    fun saveMessage(message: String) {
        transaction {
            ChatMessages.insert {
                it[content] = message
                it[timestamp] = LocalDateTime.now()
            }
        }
    }

    // 최근 메시지를 가져오는 함수
    fun getRecentMessages(limit: Int = 20): List<String> {
        return transaction {
            ChatMessages
                .selectAll()
                .orderBy(ChatMessages.timestamp, SortOrder.DESC)
                .limit(limit)
                .map { it[ChatMessages.content] }
                .reversed() // 시간 순서대로 다시 뒤집음
        }
    }
}
