package dev.anonymous.eilaji.doctor;

public class ChatModel {
    private String chatId;
    private String lastMessageText;
    private String lastMessageImage;
    private String lastMessageSenderUid;
    private String userFullName;
    private String userImageUrl;
    private String userToken;
    private Long timestamp;

    public ChatModel() {
    }

    public ChatModel(String chatId, String lastMessageText, String lastMessageImage,
                     String lastMessageSenderUid, String userFullName, String userImageUrl, String userToken, Long timestamp) {
        this.chatId = chatId;
        this.lastMessageText = lastMessageText;
        this.lastMessageImage = lastMessageImage;
        this.lastMessageSenderUid = lastMessageSenderUid;
        this.userFullName = userFullName;
        this.userImageUrl = userImageUrl;
        this.userToken = userToken;
        this.timestamp = timestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public String getLastMessageImage() {
        return lastMessageImage;
    }

    public void setLastMessageImage(String lastMessageImage) {
        this.lastMessageImage = lastMessageImage;
    }

    public String getLastMessageSenderUid() {
        return lastMessageSenderUid;
    }

    public void setLastMessageSenderUid(String lastMessageSenderUid) {
        this.lastMessageSenderUid = lastMessageSenderUid;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
