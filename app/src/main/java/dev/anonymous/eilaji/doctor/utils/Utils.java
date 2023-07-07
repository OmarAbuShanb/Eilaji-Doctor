package dev.anonymous.eilaji.doctor.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Locale;

import dev.anonymous.eilaji.doctor.R;

public class Utils {
    private static final String TAG = "Utils";
    private static Utils instance;

    private Utils() {
    }

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public interface GetTokenListener {
        void onGetTokenSuccessfully(String token);
    }

    public static void getToken(Context context, GetTokenListener listener) {
        Toast.makeText(context, "جار تحديث ال Token", Toast.LENGTH_SHORT).show();
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {
                    Toast.makeText(context, "تم تحديث ال Token", Toast.LENGTH_SHORT).show();
                    System.out.println("token = " + token);
                    SharedPreferences preferences =
                            context.getSharedPreferences("user_info", MODE_PRIVATE);
                    preferences.edit().putString("token", token).apply();
                    listener.onGetTokenSuccessfully(token);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "onCreate:getToken: " + e.getMessage()));
    }

//    public static void saveTokenToUser(Context context, String token, String userUid) {
//        DatabaseReference reference = FirebaseDatabase.getInstance()
//                .getReference(Constant.USERS_DOCUMENT);
//
//        reference.child(userUid)
//                .child(Constant.USERS_CHILD_TOKEN)
//                .setValue(token)
//                .addOnCompleteListener(command -> {
//                    if (command.isSuccessful()) {
//                        Toast.makeText(context, "تم تحديث token بنجاح", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Exception exception = command.getException();
//                        if (exception != null) {
//                            Log.e(TAG, "saveTokenToUser: " + exception.getMessage());
//                        }
//                    }
//                }).addOnFailureListener(e -> Log.e(TAG, "saveTokenToUser:addOnFailureListener: " + e.getMessage()));
//    }

    public static String formatTimeStamp(Long timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return format.format(timeStamp);
    }

    public static RequestBuilder<Drawable> loadImage(Context context, @NonNull String link) {
        return Glide
                .with(context)
                .load(link)
                .placeholder(R.color.place_holder_color)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();
    }

    public void print(String s){
        System.out.println(s);
    }
}
