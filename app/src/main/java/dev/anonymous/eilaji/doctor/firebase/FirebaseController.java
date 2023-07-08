package dev.anonymous.eilaji.doctor.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import dev.anonymous.eilaji.doctor.storage.AppSharedPreferences;
import dev.anonymous.eilaji.doctor.utils.AppController;

public class FirebaseController {
    // app context
    private final AppController appController = AppController.getInstance();
    // AppSharedPreferences
    private final AppSharedPreferences appSharedPreferences = AppSharedPreferences.getInstance(appController);

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String  TAG = "FirebaseController";

    private volatile static FirebaseController instance;

    public static synchronized FirebaseController getInstance(){
        if (instance == null) {
            instance = new FirebaseController();
        }
        return instance;
    }

    public void signOut() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser(){
        return auth.getCurrentUser();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }
}
