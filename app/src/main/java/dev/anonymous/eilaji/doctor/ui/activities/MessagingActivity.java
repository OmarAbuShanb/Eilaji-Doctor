package dev.anonymous.eilaji.doctor.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import dev.anonymous.eilaji.doctor.Notification.APIService;
import dev.anonymous.eilaji.doctor.Notification.Client;
import dev.anonymous.eilaji.doctor.Notification.Data;
import dev.anonymous.eilaji.doctor.Notification.MyResponse;
import dev.anonymous.eilaji.doctor.Notification.Sender;
import dev.anonymous.eilaji.doctor.adapters.MessagesAdapter;
import dev.anonymous.eilaji.doctor.databinding.ActivityMessagingBinding;
import dev.anonymous.eilaji.doctor.firebase.FirebaseChatManager;
import dev.anonymous.eilaji.doctor.models.ChatModel;
import dev.anonymous.eilaji.doctor.models.MessageModel;
import dev.anonymous.eilaji.doctor.storage.ChatSharedPreferences;
import dev.anonymous.eilaji.doctor.utils.MyScrollToBottomObserver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingActivity extends AppCompatActivity {
    private static final String TAG = "MessagingActivity";
    private ActivityMessagingBinding binding;

    private APIService apiService;

    private FirebaseChatManager firebaseChatManager;
    private ChatSharedPreferences chatSharedPreferencesManager;

    private MessagesAdapter messagesAdapter;

    private String chatId;

    private String userUid,
            userFullName,
            userUrlImage,
            userToken;

    private String receiverUid,
            receiverFullName,
            receiverUrlImage,
            receiverToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeDependencies();
        setupUI();
        fetchUserData();
        setupClickListeners();
    }

    private void initializeDependencies() {
        firebaseChatManager = new FirebaseChatManager();
        chatSharedPreferencesManager = ChatSharedPreferences.getInstance();

        apiService = Client.getClient().create(APIService.class);
    }

    private void setupUI() {
        binding.recyclerMessaging.setLayoutManager(getLinearLayoutManager());
    }

    private void fetchUserData() {
        FirebaseUser user = firebaseChatManager.getCurrentUser();
        if (user != null) {
            userUid = user.getUid();

            SharedPreferences preferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            userFullName = preferences.getString("pharmacy_name", "");
            userUrlImage = preferences.getString("pharmacy_image_url", "default");
            userToken = chatSharedPreferencesManager.getToken();

            Intent intent = getIntent();
            if (intent != null) {
                chatId = intent.getStringExtra("chat_id");
                receiverUid = intent.getStringExtra("receiver_uid");
                receiverFullName = intent.getStringExtra("receiver_full_name");
                receiverUrlImage = intent.getStringExtra("receiver_image_url");
                receiverToken = intent.getStringExtra("receiver_token");
            }

            loadChatIfExist();
        }
    }

    private void setupClickListeners() {
        binding.buSendMessage.setOnClickListener(v -> handleSendMessageButtonClick());
        binding.buSendImage.setOnClickListener(v -> handleSendImageButtonClick());
    }

    private void handleSendMessageButtonClick() {
        String message = binding.edMessage.getText().toString().trim();
        if (chatId != null && !TextUtils.isEmpty(message)) {
            clearMessageField();

            firebaseChatManager.addMessageToChat(
                    chatId, getMessageModel(message, null)
            );

            updateChatListUsers(message, null);

            sendNotification(message, null);
        }
    }

    private void clearMessageField() {
        binding.edMessage.setText("");
    }

    private final PickVisualMediaRequest visualMediaRequest =
            new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build();

    private void handleSendImageButtonClick() {
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (chatId != null && uri != null) {
                firebaseChatManager.uploadImageMessage(
                        userUid,
                        "MessageImage",
                        uri, (imageUrl, success) -> {
                            if (success) {
                                firebaseChatManager.addMessageToChat(
                                        chatId, getMessageModel(null, imageUrl)
                                );

                                updateChatListUsers(null, imageUrl);

                                sendNotification(null, imageUrl);
                            }
                        }
                );
            }
        }).launch(visualMediaRequest);
    }

    void updateChatListUsers(String message, String messageImageUrl) {
        firebaseChatManager.updateChatList(
                userUid,
                receiverUid,
                getChatModelSender(message, messageImageUrl)
        );

        firebaseChatManager.updateChatList(
                receiverUid,
                userUid,
                getChatModelReceiver(message, messageImageUrl)
        );
    }


    private MessageModel getMessageModel(String message, String messageImageUrl) {
        return new MessageModel(
                userUid,
                receiverUid,
                message,
                messageImageUrl,
                null,
                System.currentTimeMillis()
        );
    }

    private ChatModel getChatModelSender(String message, String lastMessageImageUrl) {
        return new ChatModel(
                chatId,
                message,
                lastMessageImageUrl,
                userUid,
                receiverFullName,
                receiverUrlImage,
                receiverToken,
                System.currentTimeMillis()
        );
    }

    private ChatModel getChatModelReceiver(String message, String lastMessageImageUrl) {
        return new ChatModel(
                chatId,
                message,
                lastMessageImageUrl,
                userUid,
                userFullName,
                userUrlImage,
                userToken,
                System.currentTimeMillis()
        );
    }

    private void createNewChat() {
        firebaseChatManager.createChat(userUid, receiverUid, (id, success) -> {
            if (success) {
                chatId = id;
                setupMessagesAdapter();
            } else {
                Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String message, String messageImageUrl) {
        Data data = new Data(userUid, userFullName, message, userUrlImage, messageImageUrl);
        Sender sender = new Sender(data, receiverToken);
        sendNotificationFCM(sender);
    }

    private void sendNotificationFCM(Sender sender) {
        apiService.sendNotification(sender)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                        if (response.code() == 200) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    Toast.makeText(getApplicationContext(), "تم ارسال الاشعار بنجاح", Toast.LENGTH_SHORT).show();
                                } else {
                                    String error = response.body().getResults().get(0).getError();
                                    if (error.equals("NotRegistered")) {
                                        Toast.makeText(getApplicationContext(), "هذا المسخدم غير موجود", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "لم يتم ارسال الاشعار", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "onResponse: " + error);
                                    }
                                }
                            }
                        }
                    }

                    // // {"multicast_id":6143843070518083714,"success":0,"failure":1,"canonical_ids":0,"results":[{"error":"NotRegistered"}]}
                    @Override
                    public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    private void loadChatIfExist() {
        firebaseChatManager.checkChatExist(userUid, receiverUid, (exists, chatIdValue) -> {
            binding.progressMessaging.setVisibility(View.GONE);
            chatId = chatIdValue;
            if (exists) {
                setupMessagesAdapter();
            } else {
                createNewChat();
            }
        });
    }

    private void setupMessagesAdapter() {
        DatabaseReference currentChatRef = firebaseChatManager.getMessagingDataReference(chatId);
        FirebaseRecyclerOptions<MessageModel> options
                = firebaseChatManager.getMessageModelOptions(currentChatRef);

        boolean isRTL = binding.recyclerMessaging.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        messagesAdapter = new MessagesAdapter(options, userUid, isRTL);
        binding.recyclerMessaging.setAdapter(messagesAdapter);

        messagesAdapter.startListening();

        messagesAdapter.registerAdapterDataObserver(
                new MyScrollToBottomObserver(
                        binding.recyclerMessaging,
                        messagesAdapter,
                        getLinearLayoutManager()
                )
        );
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        // Scroll to end of recycler
        manager.setStackFromEnd(true);
        return manager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiverUid != null) {
            chatSharedPreferencesManager.putCurrentUserChattingUID(receiverUid);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiverUid != null) {
            chatSharedPreferencesManager.removeCurrentUserChattingUID();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesAdapter != null) {
            messagesAdapter.stopListening();
        }
    }
}