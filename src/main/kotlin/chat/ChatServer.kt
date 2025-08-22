package com.joongho.chat

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*

// 모든 클라이언트에게 메시지를 전달하기 위한 SharedFlow
private val messageFlow = MutableSharedFlow<String>()

fun main() = runBlocking {
    println("코루틴 채팅 서버를 시작합니다...")
    val serverSocket = ServerSocket(12345)

    // 서버 소켓을 IO 디스패처에서 실행하여 메인 쓰레드를 블로킹하지 않음
    withContext(Dispatchers.IO) {
        while (isActive) {
            val clientSocket = serverSocket.accept()
            println("새 클라이언트 접속: ${clientSocket.inetAddress.hostAddress}")
            // 각 클라이언트에 대해 새로운 코루틴을 시작
            launch {
                handleClient(clientSocket)
            }
        }
    }
}

// 각 클라이언트와의 통신을 처리하는 suspend 함수
private suspend fun handleClient(socket: Socket) {
    val reader = Scanner(socket.getInputStream())
    val writer = PrintWriter(socket.getOutputStream(), true)

    // 이 클라이언트에게 메시지를 보내는 코루틴
    val writerJob = CoroutineScope(Dispatchers.IO).launch {
        messageFlow.asSharedFlow().collectLatest { msg ->
            try {
                writer.println(msg)
            } catch (e: Exception) {
                // 클라이언트에게 메시지 전송 실패 시 코루틴 종료
                cancel()
            }
        }
    }

    // 이 클라이언트로부터 메시지를 읽는 코루틴
    val readerJob = CoroutineScope(Dispatchers.IO).launch {
        while (this.isActive) {
            try {
                if (reader.hasNextLine()) {
                    val message = reader.nextLine()
                    println("메시지 수신: $message")
                    // 수신한 메시지를 SharedFlow에 발행
                    messageFlow.emit(message)
                } else {
                    // 클라이언트가 연결을 끊음
                    break
                }
            } catch (e: Exception) {
                break
            }
        }
    }

    // 두 코루틴 중 하나라도 끝나면(접속 종료) 정리 작업 수행
    joinAll(writerJob, readerJob)
    println("클라이언트 접속 끊김: ${socket.inetAddress.hostAddress}")
    socket.close()
}
