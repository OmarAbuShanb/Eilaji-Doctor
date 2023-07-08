package dev.anonymous.eilaji.doctor.firebase;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import dev.anonymous.eilaji.doctor.models.ChatModel;
import dev.anonymous.eilaji.doctor.models.MessageModel;
import dev.anonymous.eilaji.doctor.utils.AppController;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;
import dev.anonymous.eilaji.doctor.utils.interfaces.ChatExistCallback;
import dev.anonymous.eilaji.doctor.utils.interfaces.ListenerCallback;

public class FirebaseChatController {
    // messagesImagesRef
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // ------------------------
    public final FirebaseDatabase database = FirebaseDatabase.getInstance();

    // ------------------------

    // app context
    private final AppController appController = AppController.getInstance();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String TAG = "FirebaseController";

    private volatile static FirebaseChatController instance;

    public static synchronized FirebaseChatController getInstance() {
        if (instance == null) {
            instance = new FirebaseChatController();
        }
        return instance;
    }

    public void signOut() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    //-----------------------------------------------------


    public void uploadImageMassage(String senderUid, final String receiverUid, Uri imageUri, ListenerCallback listenerCallback) {
        final String imageName = senderUid + "::" + System.currentTimeMillis();
        StorageReference pdfRef = storage.getReference(Constant.MESSAGES_IMAGES_DOCUMENT).child(imageName + ".jpg");

        pdfRef.putFile(imageUri).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            System.out.println(progress);
        }).continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            }
            return pdfRef.getDownloadUrl();
        }).addOnSuccessListener(taskSnapshot -> {
            listenerCallback.onSuccess();
//                    Toast.makeText(this, "تم رفع الصورة بنجاح", Toast.LENGTH_SHORT).show();
                   /* addMessageToChat(
                            senderUid,
                            receiverUid,
                            null,
                            taskSnapshot.toString(),
                            null
                    );*/
        }).addOnFailureListener(exception -> {
            listenerCallback.onFailure(exception.getMessage());
//                        Log.e(TAG, "addImageToChat: " + exception.getMessage()
        });
    }

    private void addMessageToChat(String chatId, String senderUid, final String receiverUid, String message,
                                  String imageUrl, String medicineName, ListenerCallback listenerCallback) {
        MessageModel model = new MessageModel(
                senderUid,
                receiverUid,
                message,
                imageUrl,
                medicineName,
                System.currentTimeMillis()
        );

        database.getReference(Constant.CHATS_DOCUMENT)
                .child(chatId)
                .child(Constant.CHATS_CHILD_CHAT)
                .push()
                .setValue(model)
                .addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {
//                        Toast.makeText(this, "تم ارسال الرسالة بنجاح", Toast.LENGTH_SHORT).show();

                        listenerCallback.onSuccess();

                        /*FCMSend.pushNotificationToToken(
                                this,
                                userToken,
                                userUid,
                                userFullName,
                                message,
                                userUrlImage,
                                imageUrl
                        );*/

//                        updateChatList(senderUid, receiverUid, message, imageUrl);
                    }
                }).addOnFailureListener(e -> {
                    listenerCallback.onFailure(e.getMessage());
                });
//                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateChatList(
            String chatId
            , String senderUid
            , String receiverUid
            , String lastMessageText
            , String lastMessageImage
            , String receiverFullName
            , String receiverUrlImage
            , String receiverToken
            , String userFullName
            , String userUrlImage
            , String userToken
    ) {
        var model = new ChatModel(
                chatId,
                lastMessageText,
                lastMessageImage,
                senderUid,
                receiverFullName,
                receiverUrlImage,
                receiverToken,
                System.currentTimeMillis()
        );

        setValue(senderUid, receiverUid, model);

        model.setUserFullName(userFullName);
        model.setUserImageUrl(userUrlImage);
        model.setUserToken(userToken);

        setValue(receiverUid, senderUid, model);
    }

    private void setValue(String user1, String user2, ChatModel model) {
        database.getReference(Constant.CHAT_LIST_DOCUMENT)
                .child(user1)
                .child(user2)
                .setValue(model)
                .addOnCompleteListener(command -> {
                    /*`if (command.isSuccessful()) {

                    }*/
                });
    }

    void checkChatExist(String senderUid, final String receiverUid, ChatExistCallback listenerCallback) {
        final DatabaseReference reference = database.getReference(Constant.CHAT_LIST_DOCUMENT)
                .child(senderUid)
                .child(receiverUid);

        reference.get().addOnCompleteListener(command -> {
//            binding.progressMessaging.setVisibility(View.GONE);
            if (command.isSuccessful()) {
                if (command.getResult().exists()) {
                    Object chatIdValue = command.getResult()
                            .child(Constant.CHAT_LIST_CHILD_CHAT_ID)
                            .getValue();
                    if (chatIdValue != null) {
                        listenerCallback.onSuccess(chatIdValue.toString());
//                        chatId = chatIdValue.toString();
//                        setupMessagesAdapter();
                    }
                } else {
//                    chatId = "";
                    listenerCallback.onSuccess("");
                }
            }
        }).addOnFailureListener(e -> {
            listenerCallback.onFailed(e.getMessage());
        });
    }
}
