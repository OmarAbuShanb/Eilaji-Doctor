package dev.anonymous.eilaji.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dev.anonymous.eilaji.doctor.databinding.FragmentChatsBinding;

public class ChatsFragment extends Fragment {
    FragmentChatsBinding binding;
//    private ChatsViewModel mViewModel;

    private DatabaseReference reference;
    private DatabaseReference chatListRef;

    private ChatsAdapter chatsAdapter;

    private String userUid;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userUid = user.getUid();
            chatListRef = FirebaseDatabase.getInstance()
                    .getReference(Constant.CHAT_LIST_DOCUMENT);

            setupChatsAdapter();
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        if (chatsAdapter != null) {
            chatsAdapter.stopListening();
        }
        super.onDestroy();
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