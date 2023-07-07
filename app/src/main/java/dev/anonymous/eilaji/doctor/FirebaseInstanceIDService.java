package dev.anonymous.eilaji.doctor;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class FirebaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseInstanceIDServi";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.e(TAG, "onNewToken: " + token);
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        preferences.edit().putString("token", token).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String currentUserChattingUid = preferences.getString(Constant.CURRENT_USER_CHATTING_UID, "");

        String userUid = remoteMessage.getData().get("user_uid");
        if (!currentUserChattingUid.equals(userUid)) {
            // remoteMessage.getMessageId() 0:1688335009891464%43383ed843383ed8
            // remoteMessage.getSentTime() 1688335009875

            // 837284011159
            int senderId = Integer.parseInt(Objects.requireNonNull(remoteMessage.getSenderId()));
            String fullName = remoteMessage.getData().get("full_name");
            String message = remoteMessage.getData().get("message");
            String imageUrl = remoteMessage.getData().get("image_url");
            String bigImageUrl = remoteMessage.getData().get("big_image_url");

            // if imageUrl == null => imageBitmap = null
            UtilsNotifications.getBitmapFromUrl(imageUrl, imageBitmap ->
                    // if bigImageUrl == null => imageBitmap2 = null
                    UtilsNotifications.getBitmapFromUrl(bigImageUrl, imageBitmap2 ->
                            showNotification(fullName, message, senderId, imageBitmap, imageBitmap2)
                    )
            );
        }
    }

    private void showNotification(String fullName, String message, int senderId, Bitmap imageUrl, Bitmap bigImageUrl) {
        UtilsNotifications.setUpNotification(
                getApplicationContext(), senderId, fullName, message, imageUrl, bigImageUrl
        );
    }

    @Override
    public void onDeletedMessages() {
        // when there are too many messages (>100) pending for your app on a particular device at
        // the time it connects or if the device hasn't connected to FCM in more than one month
        Log.d(TAG, "onDeletedMessages: ");
    }
}
