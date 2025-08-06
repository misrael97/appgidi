package com.example.appgidi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkUtils {
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            
            Log.d("NetworkUtils", "Estado de red: " + (isConnected ? "Conectado" : "Desconectado"));
            return isConnected;
        }
        
        return false;
    }
    
    public static void logNetworkStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                Log.d("NetworkUtils", "Tipo de red: " + activeNetworkInfo.getTypeName());
                Log.d("NetworkUtils", "Estado: " + activeNetworkInfo.getDetailedState());
            }
        }
    }
} 