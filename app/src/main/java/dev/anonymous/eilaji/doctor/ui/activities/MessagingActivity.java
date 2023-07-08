package dev.anonymous.eilaji.doctor.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import dev.anonymous.eilaji.doctor.adapters.MessagesAdapter;
import dev.anonymous.eilaji.doctor.databinding.ActivityMessagingBinding;
import dev.anonymous.eilaji.doctor.firebase.FirebaseChatManager;
import dev.anonymous.eilaji.doctor.models.MessageModel;
import dev.anonymous.eilaji.doctor.storage.ChatSharedPreferences;
import dev.anonymous.eilaji.doctor.utils.MyScrollToBottomObserver;

public class MessagingActivity extends AppCompatActivity{
    private ActivityMessagingBinding binding;
    private FirebaseChatManager firebaseChatManager;
    private ChatSharedPreferences chatSharedPreferencesManager;
    private MessagesAdapter messagesAdapter;
    private String chatId;
    private String userUid, userFullName, userUrlImage, userToken;
    private String receiverUid, receiverFullName, receiverUrlImage, receiverToken;

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
    }

    private void setupUI() {
        binding.recyclerMessaging.setLayoutManager(getLinearLayoutManager());
    }

    private void fetchUserData() {
        FirebaseUser user = firebaseChatManager.getCurrentUser();
        if (user != null) {
            userUid = user.getUid();
            userFullName = user.getDisplayName();
            userUrlImage = String.valueOf(user.getPhotoUrl());
            userToken = chatSharedPreferencesManager.getToken();

            Intent intent = getIntent();
            if (intent != null) {
                chatId = intent.getStringExtra("chat_id");
                receiverUid = intent.getStringExtra("receiver_uid");
                receiverFullName = intent.getStringExtra("receiver_full_name");
                receiverUrlImage = intent.getStringExtra("receiver_image_url");
                receiverToken = intent.getStringExtra("receiver_token");
            }

            if (receiverUid != null) {
                chatSharedPreferencesManager.putCurrentUserChattingUID(receiverUid);
            }

            if (chatId == null) {
                createNewChat();
            } else {
                loadExistingChat();
            }
        }
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

    private void loadExistingChat() {
        firebaseChatManager.checkChatExist(userUid, receiverUid, (exists, chatIdValue) -> {
            binding.progressMessaging.setVisibility(View.GONE);
            chatId = chatIdValue.toString();
            if (exists) {
                setupMessagesAdapter();
            }
        });
    }

    private void setupMessagesAdapter() {
        DatabaseReference currentChatRef = firebaseChatManager.getMessagingDataReference(chatId);
        FirebaseRecyclerOptions<MessageModel> options = firebaseChatManager.getMessageModelOptions(currentChatRef);
        boolean isRTL = binding.recyclerMessaging.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        messagesAdapter = new MessagesAdapter(options, userUid, isRTL);
        binding.recyclerMessaging.setAdapter(messagesAdapter);
        messagesAdapter.startListening();
        messagesAdapter.registerAdapterDataObserver(new MyScrollToBottomObserver(binding.recyclerMessaging, messagesAdapter, getLinearLayoutManager()));
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true); // Scroll to end of recycler
        return manager;
    }

    private void setupClickListeners() {
        binding.buSendMessage.setOnClickListener(v -> handleSendMessageButtonClick());
        binding.buSendImage.setOnClickListener(v -> handleSendImageButtonClick());
    }

    private void handleSendMessageButtonClick() {
        String message = binding.edMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {
            clearMessageField();
            firebaseChatManager.sendMessage(userUid, receiverUid, chatId, message, null);
        }
    }

    private void handleSendImageButtonClick() {
        PickVisualMediaRequest mediaRequest = firebaseChatManager.getPickVisualMediaRequestSettings();
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                firebaseChatManager.sendMessage(userUid, receiverUid, chatId, null, uri);
            }
        }).launch(mediaRequest);
    }

    private void clearMessageField() {
        binding.edMessage.setText("");
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