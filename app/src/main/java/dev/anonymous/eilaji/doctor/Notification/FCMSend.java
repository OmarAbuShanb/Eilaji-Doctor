package dev.anonymous.eilaji.doctor.Notification;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// https://firebase.google.com/docs/cloud-messaging/send-message#send_using_the_fcm_legacy_http_api
public class FCMSend {
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "key=AAAAwvIEHJc:APA91bGKysFBaOuoltl3Dv9gITBZxDhe8SLpgTC-Nb0oGHtPFVuh2EQU7RZK4_1eumM6bZ8Z3YVF6tGG4gbyhmrSkQ8SHJRCINrR1u7tFX_RYVP88vmnAXMu__q0R4bblm4sdGg1DpDB";
    private static final String TAG = "FCMSend";

    public static void pushNotificationToToken(
            Context context,
            String token,
            String userUid,
            String fullName,
            String message,
            String imageUrl,
            String bigImageUrl
    ) {
        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            JSONObject json = new JSONObject();
            json.put("to", token);
            JSONObject data = new JSONObject();
            data.put("user_uid", userUid);
            data.put("full_name", fullName);

            if (message != null)
                data.put("message", message);

            if (imageUrl != null && !imageUrl.equals("default"))
                data.put("image_url", imageUrl);

            if (bigImageUrl != null)
                data.put("big_image_url", bigImageUrl);

            json.put("data", data);

            JsonObjectRequest jsonObjectRequest =
                    new JsonObjectRequest(Request.Method.POST, BASE_URL, json,
                            response -> {
                                try {
                                    int success = response.getInt("success");
                                    if (success == 1) {
                                        Toast.makeText(context, "تم ارسال الاشعار بنجاح", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // "results" : [{"error" : "NotRegistered"}]
                                        Object errorObj = response.getJSONArray("results").get(0);
                                        JSONObject object = (JSONObject) errorObj;
                                        String errorMessage = object.get("error").toString();
                                        if (errorMessage.equals("NotRegistered")) {
                                            Toast.makeText(context, "هذا المسخدم غير موجود", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "لم يتم ارسال الاشعار (" + errorMessage + "(", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (ClassCastException | JSONException e) {
                                    Log.e(TAG, "pushNotificationToToken:response: " + e.getMessage());
                                }
                            },
                            error -> Log.e(TAG, "pushNotificationToToken:error: " + error.getMessage())) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/json");
                            params.put("Authorization", SERVER_KEY);
                            return params;
                        }
                    };

            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e(TAG, "pushNotificationToToken:catch: " + e.getMessage());
        }
    }
}
