package dev.anonymous.eilaji.doctor.storage;

import android.content.Context;
import android.content.SharedPreferences;

import dev.anonymous.eilaji.doctor.utils.AppController;


public class ChatSharedPreferences {
    private enum SharedPreferencesChatKeys {
        userToken,currentUserChattingUid,
    }
    private static ChatSharedPreferences Instance;
    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ChatSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    public static ChatSharedPreferences getInstance() {
        if (Instance == null) {
            assert AppController.getInstance() != null;
            Instance = new ChatSharedPreferences(AppController.getInstance());
        }
        return Instance;
    }


    public String getToken() {
        return sharedPreferences.getString(SharedPreferencesChatKeys.userToken.name(), null);
    }
    public void putToken(String id) {
        editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesChatKeys.userToken.name(), id);
        editor.apply();
    }
    public void removeToken() {
        editor = sharedPreferences.edit();
        editor.remove(SharedPreferencesChatKeys.userToken.name());
        editor.apply();
    }

    //-------------------------------------------
    public String getCurrentUserChattingUID() {
        return sharedPreferences.getString(SharedPreferencesChatKeys.currentUserChattingUid.name(), "");
    }
    public void putCurrentUserChattingUID(String uid) {
        editor = sharedPreferences.edit();
        editor.putString(SharedPreferencesChatKeys.currentUserChattingUid.name(), uid);
        editor.apply();
    }
    public void removeCurrentUserChattingUID() {
        editor = sharedPreferences.edit();
        editor.remove(SharedPreferencesChatKeys.currentUserChattingUid.name());
        editor.apply();
    }
    //------------------------------------



    // when user logout for instance
    public void clear() {
        editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
