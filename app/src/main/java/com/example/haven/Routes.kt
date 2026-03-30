package com.example.haven

internal object Route {
    const val home = "home"
    const val landing = "landing"
    const val codenameGenerator = "codenameGenerator"
    const val password = "password"
    const val chat = "chat"
    const val logViewer = "logViewer"
}

internal data class Chat(val id: Int, val title: String, val preview: String, val unread: Int)
internal data class Msg(val mine: Boolean, val text: String)
