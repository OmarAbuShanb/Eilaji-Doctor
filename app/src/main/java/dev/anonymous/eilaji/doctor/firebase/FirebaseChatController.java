package dev.anonymous.eilaji.doctor.firebase;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import dev.anonymous.eilaji.doctor.utils.AppController;

public class FirebaseChatController {
    // messagesImagesRef
//    private final FirebaseStorage storage = FirebaseStorage.getInstance();

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

}
