package com.telebroad.teleconsole.viewmodels;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;

import com.telebroad.teleconsole.controller.AppController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModel;

import static android.content.Intent.ACTION_VIEW;

public class FileViewModel extends ViewModel {

    private File file;
    private String size;
    private CharSequence timestamp;
    private Drawable icon;
    private Intent onClickIntent;

    public void setFile(@NonNull File file) {
        // we need to reset the whole file
        if (this.file == null || !this.file.getAbsolutePath().equals(file.getAbsolutePath())) {
            this.file = file;
        }

        final Intent intent = new Intent(ACTION_VIEW);
        Uri fileUri;
        try {
            fileUri = FileProvider.getUriForFile(AppController.getInstance(), "com.telebroad.teleconsole.fileprovider", file);
        }catch (IllegalArgumentException iae){
            return;
        }
       // android.util.Log.d("Files02", "fileUri " + fileUri);
        String type = AppController.getInstance().getContentResolver().getType(fileUri);
        // android.util.Log.d("Files02", "type " + type);
        intent.setDataAndType(fileUri, type);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        ResolveInfo resolved = findOpenableActivity(intent);
        if (resolved == null) {
            Intent playStoreIntent = new Intent(ACTION_VIEW);


            playStoreIntent.setData(Uri.parse(
                    "https://play.google.com/store/search?q=" + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(type) + " opener"));
            playStoreIntent.setPackage("com.android.vending");
            onClickIntent = playStoreIntent;
        } else {
            icon = resolved.loadIcon(getPackageManager());
            onClickIntent = intent;
        }

        size = Formatter.formatFileSize(AppController.getInstance(), file.length());
        timestamp = DateUtils.getRelativeTimeSpanString(file.lastModified(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
    }

    private PackageManager getPackageManager() {
        return AppController.getInstance().getPackageManager();
    }

    private ResolveInfo findOpenableActivity(Intent intent) {
        int highestPriority = -1;

        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);

        ResolveInfo toReturn = null;

       // android.util.Log.d("Files02", "Has default " + hasDefault(intent.getType()));
        for (ResolveInfo match : resolveInfos) {
            if (match.preferredOrder > highestPriority) {
                highestPriority = match.preferredOrder;
                toReturn = match;
            }
        }

        return toReturn;
    }


    public String getName() {
        return file.getName();
    }

    public String getSize() {
        return size;
    }

    public CharSequence getDate() {
        return timestamp;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Intent getOnClickIntent() {
        return onClickIntent;
    }

    private boolean hasDefault(String dataType) {
        ArrayList<ComponentName> componentNames = new ArrayList<>();
        List<IntentFilter> intentFilters = new ArrayList<>();
        getPackageManager().getPreferredActivities(intentFilters, componentNames, null);

        for (IntentFilter intentFilter : intentFilters) {
            if (intentFilter.matchAction(ACTION_VIEW)){
               // android.util.Log.d("Files02", "matches action has data types " + intentFilter.countDataTypes());
                for (int i = 0 ; i < intentFilter.countDataTypes(); i++){
                    android.util.Log.d("Files02", "after for matches action has data types " + intentFilter.countDataTypes());
                    if (intentFilter.getDataType(i).equals(dataType)){
                      ///  android.util.Log.d("Files02", "matches action");
                        return true;
                    }
                }
            }
            //return false;
        }

        return false;
    }
}
