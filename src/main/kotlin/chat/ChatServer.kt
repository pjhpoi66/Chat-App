package com.joongho.chat

import java.net.ServerSocket
import java.net.Socket
import java.io.PrintWriter
import java.util.Scanner
import java.util.concurrent.Executors

// 모든 연결된 클라이언트의 출력 스트림을 저장하는 리스트
val clientOutputStreams = mutableListOf<PrintWriter>()

fun main() {
    println("Chat server start...")

    //스레드풀 생성
    val executor = Executors.newCachedThreadPool()

    // 12345 포트에서 클라이언트의 접속을 기다립니다.
    val serverSocket = ServerSocket(12345)

    while (true) {
        // 클라이언트의 접속을 수락합니다.
        val clientSocket = serverSocket.accept()
        println("New client connect: ${clientSocket.inetAddress.hostAddress}")

        // 각 클라이언트를 별도의 쓰레드에서 처리합니다.
        executor.submit(ClientHandler(clientSocket))
    }

}

class ClientHandler (private val socket: Socket) : Runnable {

    override fun run() {
        try {
            // 클라이언트로부터 메시지를 읽기 위한 Scanner
            val reader = Scanner(socket.getInputStream())
            // 클라이언트에게 메시지를 보내기 위한 PrintWriter
            val writer = PrintWriter(socket.getOutputStream(), true)

            // 이 클라이언트의 출력 스트림을 공유 리스트에 추가
            synchronized(clientOutputStreams) {
                clientOutputStreams.add(writer)
            }

            // 클라이언트가 보낸 메시지를 계속해서 읽고 모든 클라이언트에게 전송
            while (reader.hasNextLine()) {
                val message = reader.nextLine()
                println("message: $message")
                broadcastMessage(message)
            }

        } catch (e: Exception) {
            println("client disconnect: ${socket.inetAddress.hostAddress}")
        } finally {
            // 클라이언트 접속이 끊겼을 때, 공유 리스트에서 해당 클라이언트의 출력 스트림을 제거
            val writerToRemove = clientOutputStreams.find { it == PrintWriter(socket.getOutputStream(), true) }
            if (writerToRemove != null) {
                synchronized(clientOutputStreams) {
                    clientOutputStreams.remove(writerToRemove)
                }
            }
            socket.close()
        }
    }

    // 모든 클라이언트에게 메시지를 보내는 함수
    private fun broadcastMessage(message: String) {
        synchronized(clientOutputStreams) {
            for (writer in clientOutputStreams) {
                try {
                    writer.println(message)
                } catch (e: Exception) {
                    // 메시지 전송에 실패하면 해당 클라이언트는 접속이 끊긴 것으로 간주
                    e.printStackTrace()
                }
            }
        }
    }

}