package com.joongho.chat

import javafx.application.Platform
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import kotlinx.coroutines.*
import tornadofx.*
import java.io.PrintWriter
import java.net.Socket
import java.util.*

// TornadoFX App 클래스
class ChatApp : App(ChatView::class)

// 메인 뷰 (UI 정의)
class ChatView : View("코틀린 채팅") {
    private lateinit var messageArea: TextArea
    private lateinit var inputField: TextField
    private val clientController: ClientController by inject()

    override val root = vbox {
        paddingAll = 10.0
        spacing = 10.0

        messageArea = textarea {
            isEditable = false
            prefHeight = 400.0
        }
        hbox {
            spacing = 10.0
            inputField = textfield {
                promptText = "메시지를 입력하세요..."
                prefWidth = 300.0
                action {
                    clientController.sendMessage(inputField.text)
                    inputField.clear()
                }
            }
            button("전송") {
                action {
                    clientController.sendMessage(inputField.text)
                    inputField.clear()
                }
            }
        }
    }

    // 뷰가 생성될 때 컨트롤러를 통해 서버에 연결
    override fun onDock() {
        clientController.connect { message ->
            // UI 스레드에서 messageArea 업데이트
            Platform.runLater {
                messageArea.appendText(message + "\n")
            }
        }
    }

    // 뷰가 닫힐 때 컨트롤러의 리소스를 정리합니다.
    override fun onUndock() {
        clientController.disconnect()
    }
}

// 클라이언트 로직을 처리하는 컨트롤러
class ClientController : Controller() {
    // 코루틴 스코프의 이름을 'clientScope'로 변경하여 충돌을 피합니다.
    private val clientScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var writer: PrintWriter? = null
    private var socket: Socket? = null

    fun connect(onMessageReceived: (String) -> Unit) {
        clientScope.launch {
            try {
                socket = Socket("127.0.0.1", 12345)
                socket?.let {
                    writer = PrintWriter(it.getOutputStream(), true)
                    val reader = Scanner(it.getInputStream())

                    // 서버로부터 메시지를 수신하는 루프
                    while (isActive) {
                        if (reader.hasNextLine()) {
                            val message = reader.nextLine()
                            onMessageReceived(message)
                        } else {
                            // 서버가 연결을 끊었을 때
                            onMessageReceived("서버와의 연결이 종료되었습니다.")
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                onMessageReceived("서버 연결 실패: ${e.message}")
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            clientScope.launch {
                writer?.println(message)
            }
        }
    }

    // 앱 종료 시 호출될 리소스 정리 함수
    fun disconnect() {
        clientScope.cancel()
        writer?.close()
        socket?.close()
    }
}

// GUI 앱을 실행하는 main 함수
fun main() {
    launch<ChatApp>()
}
