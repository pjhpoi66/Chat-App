package com.joongho.chat
import java.net.Socket
import java.io.PrintWriter
import java.util.Scanner
import kotlin.concurrent.thread

fun main() {
    // 서버 IP와 포트를 지정하여 소켓 생성
    val socket = Socket("127.0.0.1", 12345)
    println("connect to server.")

    // 서버로부터 메시지를 읽는 부분 (별도 쓰레드)
    thread {
        try {
            val reader = Scanner(socket.getInputStream())
            while (reader.hasNextLine()) {
                val message = reader.nextLine()
                println("receive message: $message")
            }
        } catch (e: Exception) {
            println("The connection to the server has been lost.")
        }
    }

    // 사용자 입력을 서버로 보내는 부분 (메인 쓰레드)
    try {
        val writer = PrintWriter(socket.getOutputStream(), true)
        val consoleReader = Scanner(System.`in`)
        print("Please enter your nickname: ")
        val nickname = consoleReader.nextLine()

        while (true) {
            val input = consoleReader.nextLine()
            if (input.equals("exit", ignoreCase = true)) {
                break
            }
            writer.println("[$nickname] $input")
        }
    } catch (e: Exception) {
        println("An error occurred while sending the message.")
    } finally {
        socket.close()
        println("Terminate the connection.")
    }
}
