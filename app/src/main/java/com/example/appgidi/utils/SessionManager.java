package com.example.appgidi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class SessionManager {
    private static final String PREF_NAME = "AppGidiPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_APP_VERSION = "app_version";
    
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    
    /**
     * Guarda la sesión del usuario
     */
    public void saveSession(String token, int userId) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_APP_VERSION, getAppVersion());
        editor.apply();
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public boolean isLoggedIn() {
        String token = prefs.getString(KEY_TOKEN, null);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        return token != null && !token.isEmpty() && userId != -1;
    }
    
    /**
     * Obtiene el token JWT
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }
    
    /**
     * Obtiene el ID del usuario
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * Cierra la sesión
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Verifica si la app fue actualizada o reinstalada
     */
    public boolean isAppUpdated() {
        String savedVersion = prefs.getString(KEY_APP_VERSION, null);
        String currentVersion = getAppVersion();
        
        if (savedVersion == null) {
            // Primera vez que se ejecuta la app
            return false;
        }
        
        return !savedVersion.equals(currentVersion);
    }
    
    /**
     * Obtiene la versión actual de la app
     */
    private String getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName + "_" + packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }
    
    /**
     * Limpia la sesión si la app fue actualizada
     */
    public void checkAndClearSessionIfUpdated() {
        if (isAppUpdated()) {
            logout();
        }
    }
} 