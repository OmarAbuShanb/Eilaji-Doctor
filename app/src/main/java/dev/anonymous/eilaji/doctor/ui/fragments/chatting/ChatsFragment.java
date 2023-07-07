package dev.anonymous.eilaji.doctor.ui.fragments.chatting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dev.anonymous.eilaji.doctor.firebase.FirebaseController;
import dev.anonymous.eilaji.doctor.models.ChatModel;
import dev.anonymous.eilaji.doctor.utils.constants.Constant;
import dev.anonymous.eilaji.doctor.adapters.ChatsAdapter;
import dev.anonymous.eilaji.doctor.databinding.FragmentChatsBinding;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;

    /*private DatabaseReference reference;*/
    private DatabaseReference chatListRef;
    private ChatsAdapter chatsAdapter;
    private String userUid;
    // Hesham Changes
    private FirebaseUser firebaseUser;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseController firebaseController = FirebaseController.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseUser = firebaseController.getCurrentUser();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // now you are all set to display the chat
        executeChatDisplay();
    }

    @Override
    public void onDestroy() {
        if (chatsAdapter != null) {
            chatsAdapter.stopListening();
        }
        super.onDestroy();
    }

    private void executeChatDisplay() {
        if (firebaseUser != null) {
            userUid = firebaseUser.getUid();
            chatListRef = firebaseDatabase
                    .getReference(Constant.CHAT_LIST_DOCUMENT);

            setupChatsAdapter();
        }
    }
    private void setupChatsAdapter() {
        DatabaseReference currentChatRef = chatListRef.child(userUid);
        FirebaseRecyclerOptions<ChatModel> options = new FirebaseRecyclerOptions.Builder<ChatModel>()
                .setQuery(currentChatRef, ChatModel.class)
                .build();

        chatsAdapter = new ChatsAdapter(options, userUid);
        binding.recyclerChats.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerChats.setAdapter(chatsAdapter);

        chatsAdapter.startListening();
    }
}