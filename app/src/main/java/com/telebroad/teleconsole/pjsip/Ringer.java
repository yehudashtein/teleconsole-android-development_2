package com.telebroad.teleconsole.pjsip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.media.VolumeProviderCompat;

import com.google.common.base.Strings;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;

//import org.linphone.mediastream.video.capture.hwconf.Hacks;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AudioMediaPlayer;
import org.pjsip.pjsua2.Endpoint;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;

public class Ringer {

    ToneGenerator toneGenerator;
    private final Context context;
    private boolean isRinging = false;
    private boolean speakerPhoneOld;
    private int oldMode;
    private boolean ringingSilenced = false;
//    private AudioMedia playbackDevice;
    private MediaPlayer mediaPlayer;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                silenceRinging();
            }
        }
    };
    IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    private boolean isReceiverRegistered = false;

    public Ringer(Context context) {
        this.context = context;
        mAudioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        mVibrator = (Vibrator) AppController.getInstance().getSystemService(Context.VIBRATOR_SERVICE);

//        File root = context.getFilesDir();
//        File dir = new File(root.getAbsolutePath() + File.separator + "TeleConsole" + File.separator + "Ringtone");
//        dir.mkdirs();
//
//        String fileName = dir.getAbsolutePath() + File.separator + "toy_mono.wav";
//        File file = new File(fileName);
//
//        android.util.Log.d("PJSUA_MP3", "filename " + fileName);
//        try{
//            if (!file.exists()) {
//                CopyRAWtoSDCard(R.raw.toy_mono, fileName);
//            }
//            FileInputStream stream = new FileInputStream(fileName);
//            FileDescriptor ringerFD = stream.getFD();\
//            mediaPlayer.setDataSource(ringerFD);
//            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .setUsage(AudioAttributes.USAGE_UNKNOWN)
//                    .setLegacyStreamType(STREAM_MUSIC)
//                    .build());
//            mediaPlayer.prepare();
//
//        } catch (Exception e) {
//            android.util.Log.e("PJSUA_MP3", "error creating player", e);
//            if(Strings.nullToEmpty(e.getMessage()).contains("PJMEDIA_ENOTVALIDWAVE")){
//                try {
//                    CopyRAWtoSDCard(R.raw.toy_mono, fileName);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//            e.printStackTrace();
//        }
    }

//    private void CopyRAWtoSDCard(int id, String path) throws IOException {
//        android.util.Log.d("PJSUA_MP3", "starting copy" );
//        byte[] buff = new byte[1024];
//        int read = 0;
//        try (InputStream in = context.getResources().openRawResource(id); FileOutputStream out = new FileOutputStream(path)) {
//            while ((read = in.read(buff)) > 0) {
//                out.write(buff, 0, read);
//            }
//        } catch (Exception e) {
//            Log.e("PJSUA_MP3", "error copying", e);
//        }
//    }
    @NonNull
    private Uri getRingtoneURL() {

        return Uri.parse("android.resource://com.telebroad.teleconsole/raw/toy_mono_mp3");
    }

//    private MediaPlayer mRingerPlayer;
    private Vibrator mVibrator;
    private AudioManager mAudioManager;

    private boolean shouldBeep(){
        if (CallManager.getInstance().getCallCount() > 1){
            return true;
        }
        if (CallManager.getInstance().hasExternalCalls()){
            return true;
        }
        return false;
    }
    private boolean shouldRing() {
       // android.util.Log.d("Ringer", "Ringer mode " + mAudioManager.getRingerMode());
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            Utils.logToFile("Ringer, should not ring because the ringer mode on the phone is not set to ring ");
            return false;
        }
        String currentSetting = SettingsHelper.getString(R.string.ring_volume_key, R.string.ringtone_vibrate);

//        if (CallManager.getInstance().getCallCount() >= 2) {
//            Utils.logToFile("Ringer, should not ring because there are too many calls ");
//            return false;
//        }

        if (currentSetting.equals(AppController.getAppString(R.string.ringtone_both))
                || currentSetting.equals(AppController.getAppString(R.string.ringtone_ring))){
            Utils.logToFile("Ringer, should ring");
            return true;
        }else {
            Utils.logToFile("Ringer, should not ring because app settings are set not to ring");
            return false;
        }
    }

    private boolean shouldVibrate() {
        if (!(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)) {

            Utils.logToFile("Ringer, should not vibrate because phone settings are set not to vibrate");
            //android.util.Log.d("Vibrate", "Vibrator DND");
            return false;
        }
        if (mVibrator == null) {
            Utils.logToFile("Ringer, should not vibrate because phone was not able to initialize the vibrator ");
           // android.util.Log.d("Vibrate", "Vibrator null");
            return false;
        }
        String currentSetting = SettingsHelper.getString(R.string.ring_volume_key, R.string.ringtone_both);

        if (currentSetting.equals(AppController.getAppString(R.string.ringtone_both))
                || currentSetting.equals(AppController.getAppString(R.string.ringtone_vibrate))){

            Utils.logToFile("Ringer, should vibrate");
            return true;
        }else{

            Utils.logToFile("Ringer, should not vibrate because app settings are set not to vibrate");
            return false;
        }

    }

    public void startRinging() {
        Utils.logToFile("Ringer, start Ringing");
        //android.util.Log.d("Ring02", "Check call amount " + CallManager.getInstance().getCallCount());
        if (shouldBeep()){
            ToneGenerator toneGenerator = new ToneGenerator(STREAM_VOICE_CALL, 100);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING, 300);
            Utils.logToFile("Beeping");
            return;
        }
        try {
            if (shouldVibrate()) {
                AudioAttributes vibrationAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();
                long[] pattern = {0, 1000, 1000};
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    mVibrator.vibrate(pattern, 1, vibrationAttributes);
                } else {
                    //android.util.Log.d("Vibrate", "Vibrating " + mVibrator.hasVibrator());
                    mVibrator.vibrate(VibrationEffect.createWaveform(pattern, 1), vibrationAttributes);
                    //android.util.Log.d("Vibrate", "Still Vibrating ");
                }
                Utils.logToFile(AppController.getInstance(), "Ringer Vibrating");
            }


            if (shouldRing()) {
                startRinger();
            } else {
               // Log.w("Ringer01", "already ringing");
            }
        } catch (Exception e) {
            String stackTrace = "";
            e.printStackTrace();
            for (StackTraceElement ste : e.getStackTrace()) {
                stackTrace += "at " + ste.toString() + "\n";
            }
            if (stackTrace.isEmpty()) {
                stackTrace = "No stack trace";
            }

            Utils.logToFile(e);
            //Log.e("Ringer", " cannot handle incoming call " + e.getMessage() + " " + e.getCause() + " " + stackTrace, e);
        }
        Utils.scheduleTask(() -> {
            silenceRinging();
           // android.util.Log.d("Ringer", "Silencing Ringing");
            }, 35 * SECOND_IN_MILLIS);
        if (SipService.getInstance() != null) SipService.getInstance().registerReceiver(broadcastReceiver, intentFilter);
        isReceiverRegistered = true;
    }

    public void startRinger() {
        Utils.logToFile(context, "Starting ringer");
        if (ringingSilenced){
            Utils.logToFile(context, "Ringer silenced before starting");
            return;
        }
        AndroidAudioManager.getAudioManager().incomingReceived();

        int id = mAudioManager.generateAudioSessionId();
        mediaPlayer = MediaPlayer.create(context, R.raw.toy_mono, new AudioAttributes.Builder()
                .setLegacyStreamType(STREAM_RING)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(), id);
//        toneGenerator = new ToneGenerator(STREAM_VOICE_CALL, 100);
//        toneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE);
//        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        oldMode = audioManager.getMode();
//        audioManager.setMode(AudioManager.MODE_RINGTONE);
//        speakerPhoneOld = audioManager.isSpeakerphoneOn();
//        audioManager.setSpeakerphoneOn(true);
        try {
            if (SipService.getInstance() == null){
                return;
            }
            setRinging(context);
            isRinging = true;
            mediaPlayer.setLooping(true);
//            mediaPlayer.prepare();
//            mediaPlayer.setOnPreparedListener(mp -> {
//                if (!ringingSilenced){
//                    mp.start();
//                }else{
//                    silenceRinging();
//                }
//            });
            mediaPlayer.start();
//            playbackDevice = SipService.getInstance().getEndpointInstance(false).audDevManager().getPlaybackDevMedia();
//            player.startTransmit(playbackDevice);
        } catch (Exception e) {
            Utils.logToFile("Ringer crashed trying to start");
            e.printStackTrace();
        }
        Utils.logToFile(context, "Finished Starting ringer");
        if (ringingSilenced){
            silenceRinging();
        }
       // Log.d("Ringer01", "started");
    }

    @Override
    protected void finalize() throws Throwable {
        if (isReceiverRegistered && SipService.getInstance() != null) {
            try {
                SipService.getInstance().unregisterReceiver(broadcastReceiver);
                isReceiverRegistered = false;
            }catch (IllegalArgumentException illegalArgumentException){
                Utils.logToFile("Unable to deregister receiver");
            }
        }
        super.finalize();
    }

    public void silenceRinging() {


        if (isReceiverRegistered && SipService.getInstance() != null) {
            try {
                SipService.getInstance().unregisterReceiver(broadcastReceiver);
            }catch (IllegalArgumentException iae){
                Utils.logToFile("is receiver registered? " + isReceiverRegistered + " iae " + iae.getMessage() );
            }
            isReceiverRegistered = false;
        }
        Utils.logToFile(context, "Starting Silencing Ringer");
        if (SipService.getInstance() != null) {
           clearRinging();
        }
        ringingSilenced = true;
//        if (toneGenerator != null){
//            toneGenerator.stopTone();
//            toneGenerator.release();
//            toneGenerator = null;
//        }
        if (isRinging && mediaPlayer != null ){
            try {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    mediaPlayer.stop();
//                }
                mediaPlayer.release();
                mediaPlayer = null;
                System.gc();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            audioManager.setMode(oldMode);
//            audioManager.setSpeakerphoneOn(speakerPhoneOld);
//            if (!speakerPhoneOld && BluetoothWrapper.getInstance().canBluetooth()){
//                BluetoothWrapper.getInstance().setBluetoothOn(true);
//            }
            
        }else{

            Utils.logToFile(context, "Unable to silence Ringer, variables are is ringing " + isRinging + " player " + mediaPlayer);
        }

        if (mVibrator != null) {

            //android.util.Log.d("Vibrate", "Vibrating Cancelling");
            mVibrator.cancel();
        }

        Utils.logToFile(context, "Finishing Silencing Ringer");
    }

    private MediaSessionCompat mediaSessionCompat;

    public void setRinging(Context context) {
        mediaSessionCompat = new MediaSessionCompat(context, "Ringing Session");
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 0).build());
        VolumeProviderCompat provider = new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, 100, 50) {
            @Override
            public void onAdjustVolume(int direction) {
                 silenceRinging();
            }
        };
        mediaSessionCompat.setPlaybackToRemote(provider);
        mediaSessionCompat.setActive(true);
    }

    public void clearRinging() {
        if (mediaSessionCompat == null) {
            return;
        }
        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_STOPPED, 0, 0).build());
        mediaSessionCompat.setActive(false);
        mediaSessionCompat.release();
        mediaSessionCompat = null;
    }
}
