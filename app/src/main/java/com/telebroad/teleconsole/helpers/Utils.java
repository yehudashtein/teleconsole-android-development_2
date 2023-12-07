package com.telebroad.teleconsole.helpers;


import static android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static com.telebroad.teleconsole.helpers.Utils.ConnectionStatus.DISCONNECTED;
import static com.telebroad.teleconsole.helpers.Utils.ConnectionStatus.MOBILE;
import static com.telebroad.teleconsole.helpers.Utils.ConnectionStatus.UNKNOWN;
import static com.telebroad.teleconsole.helpers.Utils.ConnectionStatus.WIFI;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.ContactRecyclerAdapter;
import com.telebroad.teleconsole.controller.ViewContactActivity;
import com.telebroad.teleconsole.controller.dashboard.ContactSaveLocationDialog;
import com.telebroad.teleconsole.controller.login.SignInActivity;
import com.telebroad.teleconsole.model.Contact;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import crl.android.pdfwriter.StandardCharsets;


public class Utils {
    public static String md5(String s) {
        if (s == null) {
            return "";
        }
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                hexString.append(String.format("%02x", 0xFF & aMessageDigest));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static final String[] VIDEO_EXTENSIONS = {
            "video/mp4", "mp4", "video/3gp", "3gp", "video/mkv", "mkv", "video/avi", "avi", "video/3gpp", "3gpp",
            "video/mov", "mov", "video/flv", "flv", "video/wmv", "wmv", "video/webm", "webm", "Video"
            // Add more video extensions if needed
    };

    private static final String[] IMAGE_EXTENSIONS = {
            "image/jpeg", "jpeg", "image/png","png", "image/jpg","jpg", "image/gif","gif", "image/webp","webp", "image/tiff","tiff", "image/raw", "raw", "image/bmp",
            "bmp", "image/heif", "heif", "image/jpeg2000", "jpeg2000", "image/jfif", "jfif", "image/.jfif", "Image"
            // Add more video extensions if needed
    };

    // We push off the permission request to the activity
    @RequiresPermission(Manifest.permission.CALL_PHONE)
    public static void makeCall(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        context.startActivity(intent);
    }

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    public static void callSupport(Context context) {
        makeCall(context, context.getString(R.string.telebroad_support_number));
    }


    public static String formatMilliSeconds(int millis) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss", Locale.getDefault());
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//        Date date = new Date(millis);
        return formatLongMilliSeconds(millis);
    }
    public static String timeConverter(long value){
        String videoTime;
        int duration = (int) value;
        int hrs = (duration/3600000);
        int mns = (duration/60000)%60000;
        int scs = duration%60000/1000;
        if (hrs >0){
            videoTime = String.format("%02d:%02d;%02d",hrs,mns,scs);
        }else {
            videoTime = String.format("%02d:%02d",mns,scs);
        }
        return videoTime;
    }
    public static String formatLongMilliSeconds(long millis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date(millis);
        return dateFormat.format(date);
    }

    public static File getRootFolder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return AppController.getInstance().getApplicationContext().getExternalFilesDir(null);
        } else {
            return AppController.getInstance().getApplicationContext().getFilesDir();
        }
    }

    public static String getPathFromContent(Uri contentUri, Uri externalUri) {
        final String docId = contentUri.getLastPathSegment();
        final String[] split = docId.split(":");
        final String selection = "_id=?";
//        final String[] selectionArgs = new String[]{
//                split[1]
//        };
        return getDataColumn(externalUri, selection, new String[]{docId});
    }

    public static String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = AppController.getInstance().getApplicationContext().getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isPhoneDND() {
        NotificationManager mNotificationManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager == null) {
            return false;
        }
        int dndState = mNotificationManager.getCurrentInterruptionFilter();
        return dndState >= INTERRUPTION_FILTER_PRIORITY;
    }

    public static Network getActiveNetwork(ConnectivityManager mConnectivityManager, NetworkInfo networkInfo) {
        Network activeNetwork = null;
        activeNetwork = mConnectivityManager.getActiveNetwork();
        return activeNetwork;
    }

    public static ConnectionStatus getConnectionStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) AppController.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return UNKNOWN;
        }
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            return DISCONNECTED;
        }
        Network network = getActiveNetwork(connectivityManager, activeNetwork);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? WIFI : MOBILE;
//        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ? ConnectionStatus.WIFI : ConnectionStatus.MOBILE;
    }

    public static boolean isConnectedToInternet() {
        return getConnectionStatus() != DISCONNECTED;
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static <S> void updateLiveData(MutableLiveData<S> toUpdate, S newValue) {
        if (isMainThread()) {
            try {
                toUpdate.setValue(newValue);
            } catch (NullPointerException npe) {
                logToFile("live data is null");
            }
        } else {
            toUpdate.postValue(newValue);
        }
    }

    public static boolean hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return true;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        return false;
    }

    public static void viewContact(List<? extends Contact> contacts, String phoneNumberToSave, @Nullable FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        if (contacts == null || contacts.size() == 0) {
            ContactSaveLocationDialog dialog = new ContactSaveLocationDialog();
            dialog.setDefaultPhoneNumber(phoneNumberToSave);
            dialog.setShowExisting(true);
            dialog.show(activity.getSupportFragmentManager(), "callHistContacts");
        } else if (contacts.size() == 1) {
            Intent viewContactIntent = new Intent(activity, ViewContactActivity.class);
            Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
            viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_ID, contacts.get(0).getID());
            viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_TYPE, contacts.get(0).getType());
            activity.startActivity(viewContactIntent, transitionBundle);
        } else {
            RecyclerView contactRecyclerView = new RecyclerView(activity);
            contactRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            ContactRecyclerAdapter adapter = new ContactRecyclerAdapter();
            Collections.sort(contacts);
            adapter.setContacts(contacts);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.ContactDialogStyle)
                    .setView(contactRecyclerView)
                    .setMessage("Multiple contacts found. Pick one to view.");
            if (contacts.size() > 5) {
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setMessage("Many contacts found. Pick one to view.");
            }

            AlertDialog dialog = builder.create();
            adapter.setOnContactSelected(contact -> {
                Intent viewContactIntent = new Intent(activity, ViewContactActivity.class);
                Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
                viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_ID, contact.getID());
                viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_TYPE, contact.getType());
                activity.startActivity(viewContactIntent, transitionBundle);
                dialog.dismiss();

            });
            contactRecyclerView.setAdapter(adapter);
            float scale = activity.getApplicationContext().getResources().getDisplayMetrics().density;
            int padding = (int) (16 * scale + 0.5f);
            contactRecyclerView.setPadding(padding / 4, 0, padding / 4, padding);
            dialog.show();
            TextView msgTxt = dialog.findViewById(android.R.id.message);
            if (msgTxt != null) msgTxt.setTextSize(14.0f);
        }
    }

    public static boolean checkLoggedOut(Intent intent, Activity activity) {
        if (SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME) == null && SettingsHelper.getString(SettingsHelper.JWT_TOKEN) == null) {
            Intent loginIntent = new Intent(activity, SignInActivity.class);
            copyShareIntent(intent, loginIntent);
            activity.startActivity(loginIntent);
            return true;
        }
        return false;
    }

    public static void copyShareIntent(Intent fromIntent, Intent toIntent) {
        if (fromIntent == null || toIntent == null) {
            return;
        }
        toIntent.setAction(fromIntent.getAction());
        toIntent.setType(fromIntent.getType());
        toIntent.putExtra(Intent.EXTRA_STREAM, (Parcelable) fromIntent.getParcelableExtra(Intent.EXTRA_STREAM));
    }
    public static void compressVideo(File sourceFile, File targetFile) throws Exception {
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(sourceFile);
//        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(targetFile, grabber.getImageWidth(), grabber.getImageHeight());
//
//        recorder.setVideoBitrate(grabber.getVideoBitrate() / 2);
//        recorder.setVideoCodec(grabber.getVideoCodec());
//
//        grabber.start();
//        recorder.start();
//
//        Frame frame;
//        while ((frame = grabber.grabFrame()) != null) {
//            recorder.record(frame);
//        }
//
//        recorder.stop();
//        grabber.stop();
    }
//    public static String getPath(Context context, Uri uri) {
//        String[] projection = { MediaStore.Video.Media.DATA };
//        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//        if (cursor != null) {
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
//            cursor.moveToFirst();
//            String path = cursor.getString(column_index);
//            cursor.close();
//            return path;
//        } else {
//            // Handle the error or return null
//            return null;
//        }
//    }
    public enum ConnectionStatus {
        DISCONNECTED, MOBILE, WIFI, UNKNOWN;
    }

    public static File lastFileModified(File fl) {
        File[] files = fl.listFiles(File::isFile);
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }

    public static String join(CharSequence delimiter,
                              Iterable<? extends CharSequence> elements) {
        StringBuilder sbString = new StringBuilder();
        //iterate through ArrayList
        for (CharSequence language : elements) {
            //append ArrayList element followed by comma
            sbString.append(language).append(",");
        }
        //convert StringBuffer to String
        String strList = sbString.toString();
        //remove last comma from String if you want
        if (strList.length() > 0)
            strList = strList.substring(0, strList.length() - 1);
        return strList;
    }

    public static void deleteLogFiles() {
        File root = AppController.getInstance().getApplicationContext().getFilesDir();
        File dir = new File(root.getAbsolutePath() + File.separator + "TeleConsole" + File.separator + "Logs" + File.separator);
        File[] files = dir.listFiles(File::isFile);
        if (files == null) {
            return;
        }
        long cutOffDate = System.currentTimeMillis();
        cutOffDate = cutOffDate - (DAY_IN_MILLIS);
        for (File file : files) {
           // android.util.Log.d("DEL01", "In for files last Modified " + file.lastModified() + " cutoff " + cutOffDate);
            if (file.lastModified() < cutOffDate) {
              //  android.util.Log.d("DEL01", "file exists " + file.getName());
                if (file.exists()) {
                    //android.util.Log.d("DEL01", "deleting file");
                    URLHelper.uploadLogs(file);
                }
            }
        }
    }

    public static void logToFile(Throwable throwable){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            PrintStream printStream = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
            throwable.printStackTrace(printStream);
            String result = baos.toString();
            logToFile(result);
           // android.util.Log.d("EXCLOG", result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void logToFile(String entry) {
        logToFile(AppController.getInstance(), entry);
    }
    public static void logToFile(Context context, String entry) {
        logToFile(context, entry, true);
    }

    public static File getLogFile(Context context){
        context = context == null ? AppController.getInstance() : context;
        File dir = new File(context.getFilesDir().getAbsolutePath() + File.separator + "TeleConsole" + File.separator + "Logs" + File.separator);
        dir.mkdirs();
        File logFile = lastFileModified(dir);
        return logFile;
    }
    public static void logToFile(Context context, String entry, boolean addTime) {
        if (context == null){
            if (AppController.getInstance() == null){
                return;
            }else{
                context = AppController.getInstance();
            }
        }
        File dir = new File(context.getFilesDir().getAbsolutePath() + File.separator + "TeleConsole" + File.separator + "Logs" + File.separator);
        dir.mkdirs();
        File logFile = lastFileModified(dir);
        boolean append = true;
        if (logFile != null && logFile.length() > 200 * 1024) {
            //android.util.Log.d("UPLOAD", "uploading logs");
            URLHelper.uploadLogs(logFile);
        }
        if (logFile == null || logFile.length() > 200 * 1024) {
            //android.util.Log.d("UPLOAD", "creating new file");
            String dateTime = android.text.format.DateFormat.format("yyyy_MM_dd__HH_mm_SS", new java.util.Date()).toString();
            String filePath = dir.getAbsolutePath() + File.separator + dateTime + ".log";
            logFile = new File(filePath);
            FirebaseCrashlytics.getInstance().setCustomKey("current_log_file_path", filePath);
            FirebaseCrashlytics.getInstance().setCustomKey("can_log", "true");
            append = false;
        }
        try {
            FileWriter fileWriter = new FileWriter(logFile, append);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
            String time = addTime ? sdf.format(new Date()) + " " : "";
            fileWriter.write(android.os.Process.myTid() + " " + time + entry + "\n");
            if (addTime)/// android.util.Log.d("FileLogs ", time + entry);
            fileWriter.close();
        } catch (IOException e) {
           // android.util.Log.d("IOException", "Failed to write to log", e);
            FirebaseCrashlytics.getInstance().setCustomKey("can_log", "false");
            e.printStackTrace();
        }
    }


    public static String formatSeconds(int seconds) {
        return formatMilliSeconds(seconds * 1000);
    }

    public static void showNumberChooser(Context context, MutableLiveData<String> myNumber, ArrayAdapter<String> myNumberAdapter, @StringRes int title) {
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setSingleChoiceItems(myNumberAdapter, myNumberAdapter.getPosition(myNumber.getValue()), (dialog, which) -> {
                    updateLiveData(myNumber, myNumberAdapter.getItem(which));
                    dialog.dismiss();}).create();
         alert.setOnShowListener(dialog -> {
             TextView messageView = (TextView) alert.findViewById(android.R.id.message);
             if (messageView != null) {
                 messageView.setTextSize(18); // Set the size as required
             }
             Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
             Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
             positiveButton.setTextColor(context.getResources().getColor(R.color.black,null));
             negativeButton.setTextColor(context.getResources().getColor(R.color.black,null));
         });alert.show();
    }

    public static void scheduleTask(Runnable runnable, long afterMillis) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, afterMillis);
    }
    public static Bitmap getBitmapFromURI1(ContentResolver resolver, Uri uri) {
        try {
            InputStream inputStream = resolver.openInputStream(uri);
            InputStream exifStream = resolver.openInputStream(uri);
            ExifInterface exif = new ExifInterface(exifStream);
           // android.util.Log.d("EXIF", "orientation " + exif.getAttribute(ExifInterface.TAG_ORIENTATION));
            int rotation = getRotation(exif);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = rotateBitmap(rotation, bitmap);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float aspectRatio = (float) height / width;
            // Aspect ratio of 1080 by 1920 is 1.78, if the actual aspect ratio is less then this, scale by width
            float scale = aspectRatio < 1.78f ? 1080.0f / width : 1920.0f / height;
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            //bitmap = Bitmap.createScaledBitmap(bitmap, 900, 900, false);

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bitmap getBitmapFromURI(ContentResolver resolver, Uri uri) {
        try {
            InputStream inputStream = resolver.openInputStream(uri);
            InputStream exifStream = resolver.openInputStream(uri);
            ExifInterface exif = new ExifInterface(exifStream);
           // android.util.Log.d("EXIF", "orientation " + exif.getAttribute(ExifInterface.TAG_ORIENTATION));
            int rotation = getRotation(exif);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return rotateBitmap(rotation, bitmap);
//            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap rotateBitmap(int rotation, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    public static int getRotation(ExifInterface exif) {
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return  180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    public static String getBase64FromBitmap(Bitmap bitmap) {
        if (bitmap.getByteCount() > 2 * 1024 * 1024) {
            //android.util.Log.d("Base64", "exceeds 2 MB");
            // TODO Compress
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    public static void asyncTask(Runnable task) {
        AsyncTask.execute(task);
    }

//    public static File compressImageUriTo1mb(Uri uri,Context context){
//        byte[] bytes = null;
//        try {
//            InputStream inputStream = context.getContentResolver().openInputStream(uri);
//            bytes = new byte[inputStream.available()];
//            inputStream.read(bytes);
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        byte[] outputBytes = null;
//        int quality = 100;
//        do {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//            outputBytes = outputStream.toByteArray();
//            quality -= Math.round(quality * 0.1);
//        } while (outputBytes.length > 1024 * 1000L && quality > 5);
//
//        File file = new File(context.getCacheDir(), "abc.jpg");
//        try {
//            OutputStream fileOutputStream = new FileOutputStream(file);
//            fileOutputStream.write(outputBytes);
//            fileOutputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return file;
//    }
public static Bitmap drawableToBitmap (Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
        return ((BitmapDrawable)drawable).getBitmap();
    }
    int width = drawable.getIntrinsicWidth();
    width = width > 0 ? width : 1;
    int height = drawable.getIntrinsicHeight();
    height = height > 0 ? height : 1;
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
}
    public static Bitmap drawableToBitmap1 (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public static RippleDrawable getRipple(Context context){
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled }  // enabled state
        };
        int[] colors = new int[] {
                ContextCompat.getColor(context, R.color.colorPrimaryDarkAdaptable)  // ripple color for enabled state
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        return new RippleDrawable(colorStateList, null, null);
    }
    public static RippleDrawable getRippleWithRectangularMask(Context context) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled }  // enabled state
        };
        int[] colors = new int[] {
                ContextCompat.getColor(context, R.color.colorPrimaryDarkAdaptable)  // ripple color for enabled state
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        ShapeDrawable mask = new ShapeDrawable(new RectShape());
        mask.setIntrinsicHeight(30); // height of the mask
        mask.setIntrinsicWidth(30); // width of the mask
        return new RippleDrawable(colorStateList, null, mask);
    }

    public static void setRippleBackground(View view, Context context) {
        RippleDrawable rippleDrawable = getRippleWithRectangularMask(context);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(0f); // Set the corner radius
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{gradientDrawable, rippleDrawable});
        view.setBackground(layerDrawable);
        view.setClipToOutline(true);
    }
    public static ExecutorService getSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }
    public static boolean isImageType(String mimeType) {
        for (String imageExtension : IMAGE_EXTENSIONS) {
            if (imageExtension.equals(mimeType)) return true; // It's a video file
        }
        return false;

    }

    public static boolean isVidoeType(String mimeType) {
        for (String videoExtension : VIDEO_EXTENSIONS) {
            if (videoExtension.equals(mimeType)) return true; // It's a video file
        }
        return false;
    }
}
