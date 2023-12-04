package com.telebroad.teleconsole.helpers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.telebroad.teleconsole.controller.AppController;

public class NewCrashHandlerContentProvider extends ContentProvider {
    public static final String TAG = "NewCrashHandler";
    static long PRE_DELAY_MILLIS = 3000L;
    static long POST_DELAY_MILLIS = 3000L;

    public static void initializeAfterFirebaseContentProvider() {
        Thread.setDefaultUncaughtExceptionHandler(new PreFirebaseCrashHandler(Thread.getDefaultUncaughtExceptionHandler()));
    }

    @Override
    public boolean onCreate() {
        try {
           // Log.i(TAG, "+onCreate()");
            Thread.setDefaultUncaughtExceptionHandler(new PostFirebaseCrashHandler(Thread.getDefaultUncaughtExceptionHandler()));
            return true;
        }finally {
           // Log.i(TAG, "-onCreate()");
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


    static class PreFirebaseCrashHandler implements Thread.UncaughtExceptionHandler {
        Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler;
        public static final String TAG = "PreFirebaseCrashHandler";

        public PreFirebaseCrashHandler(Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler) {
            this.previousUncaughtExceptionHandler = previousUncaughtExceptionHandler;
        }

        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            try {
                SettingsHelper.putBoolean(SettingsHelper.DID_CRASH, true);
                // my own logic
//                Thread.sleep(PRE_DELAY_MILLIS);
            }catch (Throwable throwable) {
                throwable.printStackTrace();
//            } finally {
//                previousUncaughtExceptionHandler.uncaughtException(t,e);
//            }
            }
        }
    }

    static class PostFirebaseCrashHandler implements Thread.UncaughtExceptionHandler {
        Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler;
        public static final String TAG = "PostFirebaseCrashHandler";

        public PostFirebaseCrashHandler(Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler) {
            this.previousUncaughtExceptionHandler = previousUncaughtExceptionHandler;
        }

        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            try {
//                Thread.sleep(POST_DELAY_MILLIS);
            }catch (Throwable throwable) {
//                LogWrapper.e(TAG, throwable.getLocalizedMessage());
            }finally {
                previousUncaughtExceptionHandler.uncaughtException(t,e);
            }
        }
    }

}
