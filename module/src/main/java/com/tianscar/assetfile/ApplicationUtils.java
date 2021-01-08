package com.tianscar.assetfile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

final class ApplicationUtils {

    @NonNull
    public static Application getApplication () {
        return CURRENT;
    }

    @NonNull
    public static Context getApplicationContext () {
        return CURRENT.getApplicationContext();
    }

    @NonNull
    public static Resources getResources () {
        return CURRENT.getResources();
    }

    @NonNull
    public static AssetManager getAssets () {
        return CURRENT.getAssets();
    }
 
    @SuppressLint("StaticFieldLeak")
    private static final Application CURRENT;
 
    static {
        try {
            Object activityThread = getActivityThread();
            Object app = activityThread.getClass().getMethod("getApplication")
                    .invoke(activityThread);
            CURRENT = (Application) app;
        }
        catch (Throwable e) {
            throw new IllegalStateException("Cannot access application.", e);
        }
    }
 
    private static Object getActivityThread() {
        Object activityThread = null;
        try {
            @SuppressLint("PrivateApi") Method method =
                    Class.forName("android.app.ActivityThread").getMethod("currentActivityThread");
            method.setAccessible(true);
            activityThread = method.invoke(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return activityThread;
    }

}