package dev.anonymous.eilaji.doctor.utils.interfaces;

public interface ChatExistCallback {
    void onSuccess(String chatId);
    void onFailed(String message);
}
