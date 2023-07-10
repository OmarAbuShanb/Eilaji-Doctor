package dev.anonymous.eilaji.doctor.firebase;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.anonymous.eilaji.doctor.models.ChatModel;
import dev.anonymous.eilaji.doctor.models.MessageModel;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;

public class FirebaseChatManager {
    private static final String TAG = "FirebaseChatManager";

    private final FirebaseStorage storage;
    public final DatabaseReference chatListRef;
    private final DatabaseReference chatRef;

    public FirebaseChatManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        chatListRef = database.getReference(Constant.CHAT_LIST_DOCUMENT);
        chatRef = database.getReference(Constant.CHATS_DOCUMENT);
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public DatabaseReference getMessagingDataReference(String chatId) {
        return chatRef.child(chatId).child(Constant.CHATS_CHILD_CHAT);
    }

    public StorageReference getUserStorageReference(String userUid, String childName) {
        return storage.getReference(userUid).child(childName);
    }

    public interface GetTokenListener {
        void onGetTokenSuccessfully(String token);
    }

    public void getToken(GetTokenListener listener) {
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {
                    Log.d(TAG, "getToken: " + token);
                    listener.onGetTokenSuccessfully(token);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "onCreate:getToken: " + e.getMessage()));
    }

    public void createChat(String senderUid, String receiverUid, ChatCreationCallback callback) {
        String chatId = chatRef.push().getKey();
        if (chatId != null) {
            Map<String, Object> chatData = createChatData(senderUid, receiverUid);

            chatRef.child(chatId)
                    .setValue(chatData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onChatCreated(chatId, true);
                        } else {
                            Log.e(TAG, "Failed to send chat data: " + task.getException());
                            callback.onChatCreated(null, false);
                        }
                    });
        } else {
            callback.onChatCreated(null, false);
        }
    }

    private Map<String, Object> createChatData(String senderUid, String receiverUid) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("sender_uid", senderUid);
        chatData.put("receiver_uid", receiverUid);
        return chatData;
    }

    public void checkChatExist(String senderUid, String receiverUid, ChatExistenceCallback callback) {
        DatabaseReference reference = chatListRef.child(senderUid).child(receiverUid);
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean exists = task.getResult().exists();
                if (exists) {
                    Object chatIdValue = task.getResult().
                            child(Constant.CHAT_LIST_CHILD_CHAT_ID).
                            getValue();
                    if (chatIdValue != null) {
                        callback.onChatExistenceChecked(true, chatIdValue.toString());
                        return;
                    }
                }
                callback.onChatExistenceChecked(false, "");
            } else {
                Log.e(TAG, "Failed to check chat existence: " + task.getException());
                callback.onChatExistenceChecked(false, null);
            }
        });
    }

    public void uploadImageMessage(String userUid, String childName, Uri imageUri, UploadImageCallback callback) {
        StorageReference storageUserReference = getUserStorageReference(userUid, childName)
                .child(UUID.randomUUID().toString() + ".jpg");

        storageUserReference.putFile(imageUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "Upload progress: " + progress);
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return storageUserReference.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    callback.onImageUploaded(uri.toString(), true);
//                  addMessageToChat(senderUid, receiverUid, chatId, null, imageUrl);
                })
                .addOnFailureListener(e -> {
                    callback.onImageUploaded(null, false);
                    Log.e(TAG, "Failed to upload image: " + e.getMessage());
                });
    }

    public void addMessageToChat(String chatId, MessageModel messageModel) {
        chatRef.child(chatId)
                .child(Constant.CHATS_CHILD_CHAT)
                .push()
                .setValue(messageModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Message sent successfully");
                        // Handle any additional actions after sending a message
                    } else {
                        Log.e(TAG, "Failed to send message: " + task.getException());
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to send message: " + e.getMessage()));
    }

    public void updateChatList(
            String user1Uid,
            String user2Uid,
            ChatModel chatModel
//            , UpdateChatListCallback updateChatListCallback
    ) {
        chatListRef.child(user1Uid)
                .child(user2Uid)
                .setValue(chatModel)
                .addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {
//                        updateChatListCallback.onChatListUpdated(true, null);
                    }
                }).addOnFailureListener(e -> {
//                    updateChatListCallback.onChatListUpdated(true, e.getMessage());
                    Log.e(TAG, "updateChatList: " + e.getMessage());
                });
    }

    public FirebaseRecyclerOptions<MessageModel> getMessageModelOptions(DatabaseReference currentChatRef) {
        return new FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(currentChatRef, MessageModel.class)
                .build();
    }

    public interface UpdateChatListCallback {
        void onChatListUpdated(boolean success, String error);
    }

    public interface UploadImageCallback {
        void onImageUploaded(String imageUrl, boolean success);
    }

    public interface ChatCreationCallback {
        void onChatCreated(String chatId, boolean success);
    }

    public interface ChatExistenceCallback {
        void onChatExistenceChecked(boolean exists, String chatIdValue);
    }
}
