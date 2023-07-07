package dev.anonymous.eilaji.doctor.models

class ChatModel {
    var chatId: String? = null
    var lastMessageText: String? = null
    var lastMessageImage: String? = null
    var lastMessageSenderUid: String? = null
    var userFullName: String? = null
    var userImageUrl: String? = null
    var userToken: String? = null
    var timestamp: Long? = null

    constructor() {}
    constructor(
        chatId: String?,
        lastMessageText: String?,
        lastMessageImage: String?,
        lastMessageSenderUid: String?,
        userFullName: String?,
        userImageUrl: String?,
        userToken: String?,
        timestamp: Long?
    ) {
        this.chatId = chatId
        this.lastMessageText = lastMessageText
        this.lastMessageImage = lastMessageImage
        this.lastMessageSenderUid = lastMessageSenderUid
        this.userFullName = userFullName
        this.userImageUrl = userImageUrl
        this.userToken = userToken
        this.timestamp = timestamp
    }
}