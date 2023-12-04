package org.pjsip;

import android.content.BroadcastReceiver;
import android.os.Build;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;
//import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.iid.FirebaseInstanceIdReceiver;
//import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.installations.FirebaseInstallations;
import com.telebroad.teleconsole.helpers.SettingsHelper;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class PjCallID {

    private static final String TAG = PjCallID.class.getSimpleName();

    public static String getCallID(){
        return getMD5Hash(getSerial()+ SettingsHelper.getString(SettingsHelper.SIP_USERNAME));
    }

    private static String getSerial() {
        try {
            return Tasks.await(FirebaseInstallations.getInstance().getId());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
//        return FirebaseInstallations.getInstance().getId().getResult();
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            return Build.SERIAL;
//        } else {
//            try {
//                return Build.getSerial();
//            } catch (SecurityException se) {
//                Crashlytics.logException(se);
//                return Build.SERIAL;
//            }
//        }
    }

    private static String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException nax) {
//            FirebaseCrashlytics.getInstance().logException(nax);
            return input;
        }
    }
}
