package dev.anonymous.eilaji.doctor.firebase;

import android.util.Log;

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



    public void forgotPassword(String email, ForgotPasswordListener listener) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onTaskSuccessful();
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            String error = exception.getMessage();
                            Log.e(TAG, "forgotPassword:error:" + error + " , ex :", exception);
                            listener.onTaskFailed(exception);
                        }
                    }
                })
                .addOnFailureListener(listener::onTaskFailed);
    }

    public interface ForgotPasswordListener {
        void onTaskSuccessful();
        void onTaskFailed(Exception exception);
    }
}
