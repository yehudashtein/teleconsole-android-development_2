package com.telebroad.teleconsole;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.telebroad.teleconsole.pjsip.SipManager;

//import static com.telebroad.teleconsole.linphone.LinphoneManager.ACTIVE_CALL_NOTIFICATION_ID;

public class ShutdownService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return  null;
    }

    @Override
    public void onCreate() {
      //  android.util.Log.d("TASK01", "task created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //android.util.Log.d("TASK01", "task started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
       // android.util.Log.d("TASK01", "task removed");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
      //  android.util.Log.d("TASK01", "task removed");
//        NotificationManagerCompat.from(AppController.getInstance()).cancel(ACTIVE_CALL_NOTIFICATION_ID);
        SipManager.getInstance().destroy();
        super.onTaskRemoved(rootIntent);
    }
}
