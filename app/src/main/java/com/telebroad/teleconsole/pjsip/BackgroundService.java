package com.telebroad.teleconsole.pjsip;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.telebroad.teleconsole.helpers.Utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Service with a background worker thread. This is used mainly for the PJSIP thread. However this
 * class can be used for whenever we want to run things, and ensure they always run on the same thread
 * SipService extends this class, and documentation assumes that we are SipService
 */
abstract class BackgroundService extends Service {
    private HandlerThread mWorkerThread;
    private Handler mHandler;
    private PowerManager.WakeLock mWakeLock;

    @SuppressLint("WakelockTimeout")
    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());

        mWakeLock.acquire();

        mWorkerThread = new HandlerThread(getClass().getSimpleName());
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWorkerThread.quitSafely();
        mWakeLock.release();
    }

    int queuedJobs = 0;

    abstract boolean stopped();

    /**
     * enqueue a job to be done on the background thread
     * @param job The runnable we want to run on this specific thread
     * @param name A tag for this job, for debugging purposes
     *
     * @see #enqueueJob(Runnable, String, boolean)
     * @see #dequeueJob(Runnable)
     * */
    public void enqueueJob(Runnable job, String name){
        enqueueJob(job, name, false);
    }

    /**
     *
     * enqueue a job to be done on the background thread
     * @param job The runnable we want to run on this specific thread
     * @param name A tag for this job, for debugging purposes
     * @param runIfStopped A boolean if we should run the job even if the service is stopped.
     *
     * @see #enqueueJob(Runnable, String)
     * @see #dequeueJob(Runnable)
     * */
    protected void enqueueJob(Runnable job, String name, boolean runIfStopped) {
        Utils.logToFile(this,++queuedJobs + " jobs after enqueuing " + job + " called " + name );
        Runnable loggedJob = () -> {
            Utils.logToFile(BackgroundService.this,"starting " + job + " called " + name );
            FirebaseCrashlytics.getInstance().log("Starting doing " + name);
            FirebaseCrashlytics.getInstance().setCustomKey("LastStartedJob", name);
            if (runIfStopped || !stopped()) {
                job.run();
            }else{
                Utils.logToFile("Skipping " + name + " because service is stopped");
               // android.util.Log.d("SKIPPING", "skipping " + name + " because service is stopped");
            }
            FirebaseCrashlytics.getInstance().setCustomKey("LastStartedJob", "In Between");
            FirebaseCrashlytics.getInstance().log("Finished doing " + name);
            Utils.logToFile(BackgroundService.this,--queuedJobs + " jobs after finishing " + job + " called " + name);
        };
//         After 1 second cancel job, so that the thread doesn't get hung.
        new Timer("Dequeue").schedule(new TimerTask() {
            @Override
            public void run() {
//                android.util.Log.d("Async Run01", "dequeuing " + job);
                dequeueJob(loggedJob);
                Utils.logToFile(BackgroundService.this, " dequeuing " + job + " called " + name);
            }
        }, 3000);
//        AsyncTask.execute(() -> {
//            try {
//                android.util.Log.d("Async", "Sleeping for a second " + job);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
        try {
            if (mHandler.getLooper().getThread().isAlive()) {
                mHandler.post(loggedJob);
            }else{
                //android.util.Log.d("Run01", "dead thread");
            }

        }catch (Exception e){
           // android.util.Log.d("DeadThread","thread is dead is alive? " + mHandler.getLooper().getThread().isAlive());
            e.printStackTrace();
        }
    }

    protected void enqueueDelayedJob(Runnable job, long delayMillis, String name) {
        mHandler.postDelayed(job, delayMillis);
    }

    /**
     * Sometimes a job can take too long, and it hangs the thread, after a job is queued, a timer starts
     * that dequeues the job if it takes too long
     *
     * @param job the job to dequeue
     */
    protected void dequeueJob(Runnable job) {
        mHandler.removeCallbacks(job);
    }
}
