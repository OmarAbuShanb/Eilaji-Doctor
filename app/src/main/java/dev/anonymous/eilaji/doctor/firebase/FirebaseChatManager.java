package dev.anonymous.eilaji.doctor.firebase;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import dev.anonymous.eilaji.doctor.models.MessageModel;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;

public class FirebaseChatManager {
    private static final String TAG = "FirebaseChatManager";

    private final StorageReference messagesImagesRef;
    private final DatabaseReference chatListRef;
    private final DatabaseReference chatRef;

    public FirebaseChatManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesImagesRef = FirebaseStorage.getInstance().getReference(Constant.MESSAGES_IMAGES_DOCUMENT);
        chatListRef = database.getReference(Constant.CHAT_LIST_DOCUMENT);
        chatRef = database.getReference(Constant.CHATS_DOCUMENT);
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void createChat(String senderUid, String receiverUid, ChatCreationCallback callback) {
        String id = chatRef.push().getKey();
        if (id != null) {
            Map<String, Object> chatData = createChatData(senderUid, receiverUid);
            sendChatDataToServer(id, chatData, callback);
        } else {
            callback.onChatCreated(null, false);
        }
    }

    private void sendChatDataToServer(String id, Map<String, Object> chatData, ChatCreationCallback callback) {
        chatRef.child(id)
                .setValue(chatData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onChatCreated(id, true);
                    } else {
                        Log.e(TAG, "Failed to send chat data: " + task.getException());
                        callback.onChatCreated(null, false);
                    }
                });
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
                    Object chatIdValue = task.getResult().child(Constant.CHAT_LIST_CHILD_CHAT_ID).getValue();
                    callback.onChatExistenceChecked(true, chatIdValue);
                }else {
                    callback.onChatExistenceChecked(false, "");
                }
            } else {
                Log.e(TAG, "Failed to check chat existence: " + task.getException());
                callback.onChatExistenceChecked(false, null);
            }
        });
    }

    public void sendMessage(String senderUid, String receiverUid, String chatId, String message, Uri imageUri) {
        if (TextUtils.isEmpty(chatId)) {
            createNewChatAndSendMessage(senderUid, receiverUid, message, imageUri);
        } else {
            if (imageUri != null) {
                uploadImageMessage(senderUid, receiverUid, chatId, imageUri);
            } else {
                addMessageToChat(senderUid, receiverUid, chatId, message, null);
            }
        }
    }

    private void createNewChatAndSendMessage(String senderUid, String receiverUid, String message, Uri imageUri) {
        createChat(senderUid, receiverUid, (id, success) -> {
            if (success) {
                if (imageUri != null) {
                    uploadImageMessage(senderUid, receiverUid, id, imageUri);
                } else {
                    addMessageToChat(senderUid, receiverUid, id, message, null);
                }
            } else {
                Log.e(TAG, "Failed to create chat: " + id);
            }
        });
    }

    private void uploadImageMessage(String senderUid, String receiverUid, String chatId, Uri imageUri) {
        String imageName = senderUid + "::" + System.currentTimeMillis();
        StorageReference imageRef = messagesImagesRef.child(imageName + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            addMessageToChat(senderUid, receiverUid, chatId, null, imageUrl);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to get image download URL: " + e.getMessage())))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload image: " + e.getMessage()))
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "Upload progress: " + progress);
                });
    }

    private void addMessageToChat(String senderUid, String receiverUid, String chatId, String message, String imageUrl) {
        MessageModel model = new MessageModel(
                senderUid,
                receiverUid,
                message,
                imageUrl,
                null,
                System.currentTimeMillis()
        );

        chatRef.child(chatId)
                .child(Constant.CHATS_CHILD_CHAT)
                .push()
                .setValue(model)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Message sent successfully");
                        // Handle any additional actions after sending a message
                    } else {
                        Log.e(TAG, "Failed to send message: " + task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send message: " + e.getMessage()));
    }

    public DatabaseReference getMessagingDataReference(String chatId) {
        return chatRef.child(chatId).child(Constant.CHATS_CHILD_CHAT);
    }

    public FirebaseRecyclerOptions<MessageModel> getMessageModelOptions(DatabaseReference currentChatRef) {
        return new FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(currentChatRef, MessageModel.class)
                .build();
    }

    public PickVisualMediaRequest getPickVisualMediaRequestSettings() {
        return new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
    }

    public interface ChatCreationCallback {
        void onChatCreated(String chatId, boolean success);
    }

    public interface ChatExistenceCallback {
        void onChatExistenceChecked(boolean exists, Object chatIdValue);
    }
}
