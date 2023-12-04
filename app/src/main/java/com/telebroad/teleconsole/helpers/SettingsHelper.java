package com.telebroad.teleconsole.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.xml.sax.XMLReader;
//import static io.fabric.sdk.android.services.common.CommonUtils.isNullOrEmpty;

/**
 * Created by yser on 3/13/2018.
 */

public class SettingsHelper {
    Handler handler;
    static CharSequence  sub;


    private static final String BASE_SETTING_STRING = "com.telebroad.settings.";
    public static final String TELEBROAD_USERNAME = BASE_SETTING_STRING + "telebroad.username";
    public static final String TELEBROAD_PASSWORD = BASE_SETTING_STRING + "telebroad.password";
    public static final String SIP_USERNAME = BASE_SETTING_STRING + "sip.username";
    public static final String SIP_PASSWORD = BASE_SETTING_STRING + "sip.password";
    public static final String SIP_DOMAIN = BASE_SETTING_STRING + "sip.domain";
    public static final String LAST_SELECTED_TAB = BASE_SETTING_STRING + "last.selected.tab";
    public static final String DO_NOT_DISTURB = BASE_SETTING_STRING + "do.not.disturb";
    public static final String LAST_CALLED_NUMBER = BASE_SETTING_STRING + "last.called.number";
    public static final String TELECONSOLE_PROFILE = BASE_SETTING_STRING + "teleconsole.profile";
    public static final String TELECONSOLE_SETTINGS = BASE_SETTING_STRING + "teleconsole.settings";
    public static final String PUBNUB_INFO = BASE_SETTING_STRING + "pubnub.info";
    public static final String FULL_PHONE = BASE_SETTING_STRING + "full.phone";
    public static final String AUTO_DOWNLOAD_FAX = BASE_SETTING_STRING + "should.auto.down.fax";
    public static final String CALL_QUALITY = BASE_SETTING_STRING + "call.quality";
    public static final String USE_TLS = BASE_SETTING_STRING + "use.tls";
    public static final String DIRECT_CALLS_ONLY = BASE_SETTING_STRING + "direct.calls.only";
    public static final String DID_CRASH = BASE_SETTING_STRING + "did.crash";
    public static final String JWT_TOKEN = BASE_SETTING_STRING + "jwt.token";
    public static final String CHAT_TOKEN = "token";
    public static final String CHAT_TOPIC = "topic";
    public static final String CHAT_ACTIVITY_ACTIVE = "active";
    public static final String MY_TOPIC = "my_topic";
    public static String getSipDomain() {
        return getString(SIP_DOMAIN, AppController.getAppString(R.string.domain));
    }

    public static boolean setContainsString(String setKey, String entry) {
      //  android.util.Log.d("Notification03", "Set " + PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getStringSet(setKey, new HashSet<>()) +
                //" contains " + entry);
        return PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getStringSet(setKey, new HashSet<>()).contains(entry);
    }

    public static boolean setContainsString(String setKey, @StringRes int entryRes) {
        return setContainsString(setKey, AppController.getAppString(entryRes));
    }

    public static boolean setContainsString(@StringRes int setKeyRes, @StringRes int entryRes) {
        return setContainsString(AppController.getAppString(setKeyRes), AppController.getAppString(entryRes));
    }

    public static String getString(String key, @Nullable String defVaule) {
        return PreferenceManager.getDefaultSharedPreferences(AppController.getInstance().getApplicationContext()).getString(key, defVaule);
    }

    public static int getInt(String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(AppController.getInstance().getApplicationContext()).getInt(key, defaultValue);
    }

    public static String getString(@StringRes int stringResource) {
        return getString(AppController.getInstance().getString(stringResource));
    }

    public static String getString(@StringRes int stringResource, String defValue) {
        return getString(AppController.getInstance().getString(stringResource, defValue));
    }

    public static String getString(@StringRes int stringResource, @StringRes int defValueRes) {
        return getString(AppController.getAppString(stringResource), AppController.getAppString(defValueRes));
    }


    public static String getString(String key) {
        return getString(key, null);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getBoolean(key, defValue);
    }


    public static void putBoolean(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).edit().putBoolean(key, value).apply();
    }




    public static void clear() {
        boolean useTls = getBoolean(USE_TLS, true);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).edit().clear();
        editor.putBoolean(USE_TLS, useTls);
        editor.apply();
    }

    public static void putString(@StringRes int keyRes, String value) {
        putString(AppController.getAppString(keyRes), value);
    }

    public static void putString(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(AppController.getInstance().getApplicationContext()).edit().putString(key, value).apply();
    }

    public static void putInt(String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(AppController.getInstance().getApplicationContext()).edit().putInt(key, value).apply();
    }
    public static String encodeValue(String value) throws UnsupportedEncodingException {
        String encoded = null;
        try {
             encoded =URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return  encoded;
    }


    public static void updateCredentials(String username, String password) {
        putString(TELEBROAD_USERNAME, username);
        putString(TELEBROAD_PASSWORD, password);
        FirebaseCrashlytics.getInstance().setCustomKey("username", username);
    }

    @SuppressLint("HardwareIds")
    @NonNull
    public static String getDevicePhoneNumber() {
        // TO DO return real user's phone number, After all we don't want to get all the customers calls.
        // EDIT Too Late we are already getting the customers' calls :-(
        // EDIT Done leaving previous comments for posterity
        // UPDATE & EDIT separating to do
        //return "13472628201";
        String phoneNumber = getString(R.string.cell_number_key);
        if (isNullOrEmpty(phoneNumber)) {
           // android.util.Log.d("DeviceNumber", "phone number is null");
            if (ActivityCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tMgr = (TelephonyManager) AppController.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
                if (tMgr == null) {
                  //  android.util.Log.d("DeviceNumber", "tmgr is null");
                    return "";
                }
                try {
                    phoneNumber = tMgr.getLine1Number();
                }catch (SecurityException se){
                    return nullToEmpty(phoneNumber);
                }
            }
        }
        return nullToEmpty(phoneNumber);
    }
  public static TextDrawable grtFirstTwoInitialsWithColor(String name){
      String initial = name; //replaceAll("[^a-zA-Z0-9א-ת]", "").trim();
      initial = initial.replace('-', ' ');
      //make sure if there is two spaces its should look on it like onr
      String[] sName = initial.split("\\s+");
      //generate a string if the image url is null
      String initials = "";
      if (sName.length >= 1) {initials += sName[0].charAt(0);}
      if (sName.length >= 2) {initials += sName[sName.length - 1].charAt(0);}
      initials = initials.toUpperCase();
      ColorGenerator generator = ColorGenerator.MATERIAL;
      //generate a color for the avatar if the imageurl is null
      int color1 = generator.getColor(initials);
      //make sure that the image view should except the text avatar
      //int color2 = getBackgroundColor(channelDB.getTopic());
      //String color3 = Integer.toHexString(color2);
      TextDrawable textDrawable = TextDrawable.builder().buildRound(initials, color1);
      return textDrawable;
  }
    public static String getRecordFilePath(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(music,"chatVoiceNote.mp3");
        return file.getPath();
    }
    public static String getRecordFilePathSMS(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(music,"smsVoiceNote.mp3");
        return file.getPath();
    }

    public static String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
    public static String getDateString(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = simpleDateFormat.format(new Date());
        return date;
    }
    public static boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);
        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }
        return hasImage;
    }
    public static String getTimeAgo(long duration,String t,String c){
        Date now = new Date();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);
        if (seconds < 60){
            return "just now";
        }else if (minutes == 1){
            return "a minute ago";
        }else if (minutes > 1 && minutes < 60){
            return minutes + " minutes ago";
        }else if (hours == 1){
            return "an hour ago";
        }else if (hours > 1 && hours < 24){
            return hours +" hours ago";
        }else if (days == 1){
            return t;
            //return "a day ago";
        }else {
            return t;
            //return days+" days ago";
        }
    }
    public static String getTimeAgoInClockTime(long duration,String t,String c){
        Date now = new Date();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);
        if (seconds < 60){
            return "just now";
        }else if (minutes == 1){
            return "a minute ago";
        }else if (minutes > 1 && minutes < 60){
            return minutes + " minutes ago";
        }else if (hours == 1){
            return "an hour ago";
        }else if (hours > 1 && hours < 24){
            return hours +" hours ago";
        }else if (days == 1){
            //return "a day ago";
            return c;
        }else {
            //return days+" days ago";
            return c;
        }
    }
    public static CharSequence reformatHTML(String myHtml){

        try{
            Spanned html = Html.fromHtml(myHtml, Html.FROM_HTML_MODE_COMPACT);
            String descriptionWithOutExtraSpace = html.toString().trim();
            sub  = (html.subSequence(0, descriptionWithOutExtraSpace.length()));

        }catch (Exception e){
            e.printStackTrace();
        }
        return sub;
    }
    public static Date getDate(String ts){
        Instant instant = Instant.parse(ts);
        Date date = Date.from(instant);
        return date;
    }
    public static String[] convert(String... array) {return array;}
}
