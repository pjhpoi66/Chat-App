package com.joongho.chat

import com.joongho.chat.database.DatabaseFactory
import com.joongho.chat.database.ChatHistoryService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*

private val messageFlow = MutableSharedFlow<String>()
// ChatHistoryService 인스턴스 생성
private val chatHistoryService = ChatHistoryService()

fun main() = runBlocking {
    // 서버 시작 시 DB 초기화
    DatabaseFactory.init()
    println("코루틴 채팅 서버를 시작합니다 (DB 연동)...")

    val serverSocket = ServerSocket(12345)

    withContext(Dispatchers.IO) {
        while (isActive) {
            val clientSocket = serverSocket.accept()
            println("새 클라이언트 접속: ${clientSocket.inetAddress.hostAddress}")
            launch {
                handleClient(clientSocket)
            }
        }
    }
}

private suspend fun handleClient(socket: Socket) {
    val reader = Scanner(socket.getInputStream())
    val writer = PrintWriter(socket.getOutputStream(), true)

    // 1. 접속한 클라이언트에게 최근 대화 내용 전송
    try {
        val recentMessages = chatHistoryService.getRecentMessages()
        writer.println("--- 이전 대화 내용 ---")
        recentMessages.forEach { writer.println("[${it.second}] ${it.first}") }
        writer.println("--------------------")
    } catch (e: Exception) {
        println("이전 대화 전송 실패: ${e.message}")
    }


    val writerJob = CoroutineScope(Dispatchers.IO).launch {
        messageFlow.asSharedFlow().collectLatest { msg ->
            try {
                writer.println(msg)
            } catch (e: Exception) {
                cancel()
            }
        }
    }

    val readerJob = CoroutineScope(Dispatchers.IO).launch {
        while (this.isActive) {
            try {
                if (reader.hasNextLine()) {
                    val message = reader.nextLine()
                    println("메시지 수신: $message")

                    // 2. 수신한 메시지를 DB에 저장
                    chatHistoryService.saveMessage(message)

                    // 3. 모든 클라이언트에게 브로드캐스트
                    messageFlow.emit(message)
                } else {
                    break
                }
            } catch (e: Exception) {
                break
            }
        }
    }

    joinAll(writerJob, readerJob)
    println("클라이언트 접속 끊김: ${socket.inetAddress.hostAddress}")
    socket.close()
}
