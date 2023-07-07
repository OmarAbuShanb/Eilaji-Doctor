package dev.anonymous.eilaji.doctor.storage;

import android.content.Context;
import android.content.SharedPreferences;


public class AppSharedPreferences {
    private enum SharedPreferencesKeys {
        invoked
    }
    private static AppSharedPreferences Instance;
    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private AppSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    public static AppSharedPreferences getInstance(Context context) {
        if (Instance == null) {
            Instance = new AppSharedPreferences(context);
        }
        return Instance;
    }


    /*1 FOR DATABASE NOTIFICATION ID CREATION
    public int getLastNotificationId() {
        return sharedPreferences.getInt("LAST_NOTIFICATION_ID", 0);
    }
    public void putNewNotificationId(int id) {
        editor = sharedPreferences.edit();
        editor.putInt("LAST_NOTIFICATION_ID", id);
        editor.apply();
    }
*/
    //------------------------------------

    public void invokeDummyData() {
        editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferencesKeys.invoked.name(), true);
        editor.apply();
    }

    public boolean isInvoked() {
        return sharedPreferences.getBoolean(SharedPreferencesKeys.invoked.name(), false);
    }


    // when user logout for instance
    public void clear() {
        editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
