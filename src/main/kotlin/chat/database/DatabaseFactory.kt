package com.joongho.chat.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

// 채팅 메시지 테이블 스키마 정의
object ChatMessages : Table() {
    val id = integer("id").autoIncrement()
    val content = varchar("content", 255)
    val timestamp = datetime("timestamp")
    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init() {
        // H2 인메모리 데이터베이스에 연결합니다.
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        // 트랜잭션 내에서 테이블이 없으면 생성합니다.
        transaction {
            addLogger(StdOutSqlLogger) // 실행되는 SQL을 콘솔에 로깅
            SchemaUtils.create(ChatMessages)
        }
    }
}
