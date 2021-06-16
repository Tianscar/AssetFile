/*
 * Copyright 2021 Tianscar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tianscar.assetfile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

/**
 * A utility class providing functions about application.
 */
final class ApplicationUtils {

    /**
     * Gets current application object.
     *
     * @return the current application object
     */
    @NonNull
    public static Application getApplication () {
        return CURRENT;
    }

    /**
     * Gets current application context object.
     *
     * @return the current application context object
     */
    @NonNull
    public static Context getApplicationContext () {
        return CURRENT.getApplicationContext();
    }

    /**
     * Gets current app's resources object.
     *
     * @return the current app's resources object
     */
    @NonNull
    public static Resources getResources () {
        return CURRENT.getResources();
    }

    /**
     * Gets current app's asset manager object.
     *
     * @return the current app's asset manager object
     */
    @NonNull
    public static AssetManager getAssets () {
        return CURRENT.getAssets();
    }

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