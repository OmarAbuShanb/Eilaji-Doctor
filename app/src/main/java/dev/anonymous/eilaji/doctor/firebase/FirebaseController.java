package dev.anonymous.eilaji.doctor.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kotlin.jvm.Volatile;

public class FirebaseController {
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

}
