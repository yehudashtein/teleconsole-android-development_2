package com.telebroad.teleconsole.helpers;

import android.app.Activity;
import android.graphics.Color;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;

/**
 * Created by yser on 3/26/2018.
 */

public class AlertsHelper {

    public static void makeShort(Activity activity, @StringRes int textResource) {
        try {
            Snackbar snack = Snackbar.make(activity.findViewById(android.R.id.content), textResource, Snackbar.LENGTH_SHORT);
            View view = snack.getView();
            TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextColor(Color.CYAN);
            snack.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Snackbar getShortSnack(Activity context, int textResource) {
        return getSnack(context, textResource, Snackbar.LENGTH_SHORT);
    }

    public static Snackbar getLongSnack(Activity context, int textResource){
        return getSnack(context, textResource, Snackbar.LENGTH_LONG);
    }
    public static Snackbar getSnack(Activity context, int textResource, int length) {
        Snackbar snack = Snackbar.make(context.findViewById(android.R.id.content), textResource, length);
        View view = snack.getView();
        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(Color.CYAN);
        return snack;
    }


    public static void makeLong(Activity _context, int textResource) {
        try {
            Snackbar snack = Snackbar.make(_context.findViewById(android.R.id.content), textResource, Snackbar.LENGTH_LONG);
            View view = snack.getView();
            TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextColor(Color.CYAN);
            snack.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
