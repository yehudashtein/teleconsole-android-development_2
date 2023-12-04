package com.telebroad.teleconsole.pjsip;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.media.AudioManager.MODE_RINGTONE;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import androidx.lifecycle.MutableLiveData;

import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;

import java.util.List;

public class AndroidAudioManager {
    private AudioManager mAudioManager;
    private MediaPlayer mRingerPlayer;
    private final Vibrator mVibrator;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothReceiver mBluetoothReceiver;
    private HeadsetReceiver mHeadsetReceiver;
    private boolean mHeadsetReceiverRegistered;
    private boolean mIsRinging;
    private boolean mAudioFocused;
    private boolean mEchoTesterIsRunning;
    private boolean mIsBluetoothHeadsetConnected;
    private boolean mIsBluetoothHeadsetScoConnected;

    public MutableLiveData<AudioState> getAudioState() {
        return audioState;
    }

    private MutableLiveData<AudioState> audioState = new MutableLiveData<>();

    void incomingReceived() {
        if (CallManager.getInstance().getCallCount() == 1) {
            requestAudioFocus(STREAM_RING);
            startRinging();
        }
        //                        } else if (call == mRingingCall && mIsRinging) {
        //                            // previous state was ringing, so stop ringing
        //                            stopRinging();
        //                        }
    }

    void incomingCallConnected() {
        setAudioManagerInCallMode();
        requestAudioFocus(STREAM_VOICE_CALL);
        if (isBluetoothHeadsetConnected()) {
            routeAudioToBluetooth();
        } else {
            routeAudioToEarPiece();
        }
        callConnected();
    }

    void callConnected() {
        // Only register this one when a call is active
        enableHeadsetReceiver();
    }

    void callEnded() {
        if (CallManager.getInstance().getCallCount() == 0) {
            if (mAudioFocused) {
                int res = mAudioManager.abandonAudioFocus(null);
                mAudioFocused = false;
            }

            if (mHeadsetReceiver != null && mHeadsetReceiverRegistered) {
                //Log.i("Audio Manager", "[Audio Manager] Unregistering headset receiver");
                try {
                    AppController.getInstance().unregisterReceiver(mHeadsetReceiver);
                } catch (IllegalArgumentException iae) {
                }
                mHeadsetReceiverRegistered = false;
            }
            TelephonyManager tm = (TelephonyManager) AppController.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            if (!AppController.getInstance().hasPermissions(READ_PHONE_STATE) || tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
//                Log.d("Audio Manager","[Audio Manager] ---AndroidAudioManager: back to MODE_NORMAL");
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                //   Log.d("Audio Manager", "[Audio Manager] All call terminated, routing back to earpiece");
                routeAudioToEarPiece();
            }
        }
    }

    void outgoingCallStarted() {
        setAudioManagerInCallMode();
        requestAudioFocus(STREAM_VOICE_CALL);
        if (mIsBluetoothHeadsetConnected) {
            routeAudioToBluetooth();
        }
    }

    void streamsRunning() {
        setAudioManagerInCallMode();
        if (mIsBluetoothHeadsetConnected) {
            routeAudioToBluetooth();
        }
    }

    private AndroidAudioManager() {
        mAudioManager = ((AudioManager) AppController.getInstance().getSystemService(Context.AUDIO_SERVICE));
        mVibrator = (Vibrator) AppController.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        mEchoTesterIsRunning = false;
        mHeadsetReceiverRegistered = false;
        // android.util.Log.d("Bluetooth", "Android Audio manager starting");
        startBluetooth();
    }

    public void destroy() {
        if (mBluetoothAdapter != null && mBluetoothHeadset != null) {
            // Log.i("Bluetooth", "[Audio Manager] [Bluetooth] Closing HEADSET profile proxy");
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
        }

        //   Log.i("Bluetooth", "[Audio Manager] [Bluetooth] Unegistering bluetooth receiver");
        if (mBluetoothReceiver != null) {
            AppController.getInstance().unregisterReceiver(mBluetoothReceiver);
        }
    }
    /* Audio routing */

    public void setAudioManagerModeNormal() {
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    public void routeAudioToEarPiece() {
        routeAudioToSpeakerHelper(false);
    }

    public void routeAudioToSpeaker() {
        routeAudioToSpeakerHelper(true);
    }

    public boolean isAudioRoutedToSpeaker() {
        return mAudioManager.isSpeakerphoneOn() && !isUsingBluetoothAudioRoute();
    }

    public boolean isAudioRoutedToEarpiece() {
        return !mAudioManager.isSpeakerphoneOn() && !isUsingBluetoothAudioRoute();
    }

    public boolean onKeyVolumeAdjust(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            adjustVolume(1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            adjustVolume(-1);
            return true;
        }
        return false;
    }

    private void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            // Log.w("AudioMode","[Audio Manager] already in MODE_IN_COMMUNICATION, skipping...");
            return;
        }
        // Log.d("AudioMode","[Audio Manager] Mode: MODE_IN_COMMUNICATION");
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private void requestAudioFocus(int stream) {
        if (!mAudioFocused) {
            int res = mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
        }
    }

    private synchronized void startRinging() {
        if (isBluetoothHeadsetConnected()) {
            routeAudioToBluetooth();
        } else {
            routeAudioToSpeaker();
        }
        mAudioManager.setMode(MODE_RINGTONE);
//        try {
//            if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE
//                    || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
//                    && mVibrator != null) {
//                Compatibility.vibrate(mVibrator);
//            }
//            if (mRingerPlayer == null) {
//                requestAudioFocus(STREAM_RING);
//                mRingerPlayer = new MediaPlayer();
//                mRingerPlayer.setAudioStreamType(STREAM_RING);
//                String ringtone =
//                        LinphonePreferences.instance()
//                                .getRingtone(Settings.System.DEFAULT_RINGTONE_URI.toString());
//                try {
//                    if (ringtone.startsWith("content://")) {
//                        mRingerPlayer.setDataSource(mContext, Uri.parse(ringtone));
//                    } else {
//                        FileInputStream fis = new FileInputStream(ringtone);
//                        mRingerPlayer.setDataSource(fis.getFD());
//                        fis.close();
//                    }
//                } catch (IOException e) {
//                    Log.e(e, "[Audio Manager] Cannot set ringtone");
//                }
//                mRingerPlayer.prepare();
//                mRingerPlayer.setLooping(true);
//                mRingerPlayer.start();
//            } else {
//                Log.w("[Audio Manager] Already ringing");
//            }
//        } catch (Exception e) {
//            Log.e("Audio Manager", "[Audio Manager] Cannot handle incoming call", e);
//        }
        mIsRinging = true;
    }

    private synchronized void stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        mIsRinging = false;
    }

    public void routeToBluetoothOrEarpiece() {
        if (isBluetoothHeadsetConnected()) {
            routeAudioToBluetooth();
        } else {
            routeAudioToEarPiece();
        }
    }

    private void routeAudioToSpeakerHelper(boolean speakerOn) {
        // Log.w("AudioMode", "[Audio Manager] Routing audio to " + (speakerOn ? "speaker" : "earpiece"));
        if (mIsBluetoothHeadsetScoConnected) {
            // Log.w("AudioMode","[Audio Manager] [Bluetooth] Disabling bluetooth audio route");
            changeBluetoothSco(false);
        }
        mAudioManager.setSpeakerphoneOn(speakerOn);
        //android.util.Log.d("SPEAKERUI1", "ratsh is speaker on? " + mAudioManager.isSpeakerphoneOn());
        //android.util.Log.d("SPEAKERUI1", "ratsh calling uas");
        updateAudioState();
    }

    private void adjustVolume(int i) {
        if (mAudioManager.isVolumeFixed()) {
            // Log.e("AudioMode","[Audio Manager] Can't adjust volume, device has it fixed...");
            // Keep going just in case...
        }

        int stream = STREAM_VOICE_CALL;
        if (mIsBluetoothHeadsetScoConnected) {
//            Log.i("AudioMode","[Audio Manager] Bluetooth is connected, try to change the volume on STREAM_BLUETOOTH_SCO");
            stream = 6; // STREAM_BLUETOOTH_SCO, it's hidden...
        }
        // starting from ICS, volume must be adjusted by the application,
        // at least for STREAM_VOICE_CALL volume stream
        mAudioManager.adjustStreamVolume(stream, i < 0 ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    // Bluetooth

    public synchronized void bluetoothHeadetConnectionChanged(boolean connected) {
        mIsBluetoothHeadsetConnected = connected;
        mAudioManager.setBluetoothScoOn(connected);
        // android.util.Log.d("SPEAKERUI1", "bhcc calling uas");
        updateAudioState();
//        if (LinphoneContext.isReady()) LinphoneManager.getCallManager().refreshInCallActions();
    }

    public synchronized void bluetoothHeadetAudioConnectionChanged(boolean connected) {
        // android.util.Log.d("SPEAKERUI1", mAudioManager.isBluetoothScoOn() + " vs " + connected);
        mIsBluetoothHeadsetScoConnected = connected;
        if (mAudioManager.isBluetoothScoOn() != connected) {
            mAudioManager.setBluetoothScoOn(connected);
        }
        updateAudioState();
    }

    public synchronized boolean isBluetoothHeadsetConnected() {
        return mIsBluetoothHeadsetConnected;
    }

    public synchronized void bluetoothHeadetScoConnectionChanged(boolean connected) {
        mIsBluetoothHeadsetScoConnected = connected;
//        if (LinphoneContext.isReady()) LinphoneManager.getCallManager().refreshInCallActions();
    }

    public synchronized boolean isUsingBluetoothAudioRoute() {
        return mIsBluetoothHeadsetScoConnected;
    }

    public synchronized void routeAudioToBluetooth() {
        if (!isBluetoothHeadsetConnected()) {
            // Log.w("Bluetooth","[Audio Manager] [Bluetooth] No headset connected");
            return;
        }
        if (mAudioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
            //Log.w("Bluetooth","[Audio Manager] [Bluetooth] Changing audio mode to MODE_IN_COMMUNICATION and requesting STREAM_VOICE_CALL focus");
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            requestAudioFocus(STREAM_VOICE_CALL);
        }
        changeBluetoothSco(true);
    }

    private synchronized void changeBluetoothSco(final boolean enable) {
        // IT WILL TAKE A CERTAIN NUMBER OF CALLS TO EITHER START/STOP BLUETOOTH SCO FOR IT TO WORK
        if (enable && mIsBluetoothHeadsetScoConnected) {
            // Log.i("Bluetooth","[Audio Manager] [Bluetooth] SCO already enabled, skipping");
            return;
        } else if (!enable && !mIsBluetoothHeadsetScoConnected) {
            // Log.i("Bluetooth","[Audio Manager] [Bluetooth] SCO already disabled, skipping");
            return;
        }

        new Thread() {
            @Override
            public void run() {
                // Log.i("Bluetooth","[Audio Manager] [Bluetooth] SCO start/stop thread started");
                boolean resultAcknowledged;
                int retries = 0;
                do {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // Log.e("Bluetooth", "t", e);
                    }
                    synchronized (AndroidAudioManager.this) {
                        if (enable) {
                            mAudioManager.startBluetoothSco();
                        } else {
                            mAudioManager.stopBluetoothSco();
                        }
                        resultAcknowledged = isUsingBluetoothAudioRoute() == enable;
                        retries++;
                    }
                } while (!resultAcknowledged && retries < 10);
            }
        }.start();
    }

    public void updateAudioState() {
        //  android.util.Log.d("SPEAKERUI1", "uas is speaker on? " + mAudioManager.isSpeakerphoneOn());
        AudioState audioState = getCurrentState();
        //android.util.Log.d("SPEAKERUI", "Updating with " + audioState);
        Utils.updateLiveData(this.audioState, audioState);
    }

    public AudioState getCurrentState() {
        if (mIsBluetoothHeadsetScoConnected) {
            return AudioState.BLUETOOTH;
        }
        //android.util.Log.d("SPEAKERUI1", "gcs is speaker on? " + mAudioManager.isSpeakerphoneOn());
        if (mAudioManager.isSpeakerphoneOn()) {
            return AudioState.SPEAKER;
        }
        return AudioState.EARPIECE;
    }

    public boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT < 31) {
            return AppController.getInstance().hasPermissions(BLUETOOTH);
        } else {
            return AppController.getInstance().hasPermissions(BLUETOOTH_CONNECT);
        }
    }


    public void bluetoothAdapterStateChanged() {
        // android.util.Log.d("Bluetooth", "has bluetooth permission? " + hasBluetoothPermission());
        if (mBluetoothAdapter.isEnabled() && hasBluetoothPermission()) {
            Utils.logToFile("[Audio Manager] [Bluetooth] Adapter enabled");
            mIsBluetoothHeadsetConnected = false;
            mIsBluetoothHeadsetScoConnected = false;
            // android.util.Log.d("SPEAKERUI1", "basc calling uas");
            updateAudioState();
            BluetoothProfile.ServiceListener bluetoothServiceListener = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        Utils.logToFile("[Audio Manager] [Bluetooth] HEADSET profile connected");
                        mBluetoothHeadset = (BluetoothHeadset) proxy;
                        List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
                        if (devices.size() > 0) {
                            Utils.logToFile("[Audio Manager] [Bluetooth] A device is already connected");
                            bluetoothHeadetConnectionChanged(true);
                        }
                        Utils.logToFile("[Audio Manager] [Bluetooth] Registering bluetooth receiver");
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
                        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
                        filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
                        filter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
                        Intent sticky = AppController.getInstance().registerReceiver(mBluetoothReceiver, filter);
                        Utils.logToFile("[Audio Manager] [Bluetooth] Bluetooth receiver registered");
                        int state = sticky.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
                        if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                            Utils.logToFile("[Audio Manager] [Bluetooth] Bluetooth headset SCO connected");
                            bluetoothHeadetScoConnectionChanged(true);
                        } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                            Utils.logToFile("[Audio Manager] [Bluetooth] Bluetooth headset SCO disconnected");
                            bluetoothHeadetScoConnectionChanged(false);
                        } else if (state == AudioManager.SCO_AUDIO_STATE_CONNECTING) {
                            Utils.logToFile("[Audio Manager] [Bluetooth] Bluetooth headset SCO connecting");
                        } else if (state == AudioManager.SCO_AUDIO_STATE_ERROR) {
                            Utils.logToFile("[Audio Manager] [Bluetooth] Bluetooth headset SCO connection error");
                        } else {
                            Utils.logToFile("[Audio Manager] [Bluetooth] Bluetooth headset unknown SCO state changed: " + state);
                        }
                    }
                }

                public void onServiceDisconnected(int profile) {
                    if (profile == BluetoothProfile.HEADSET) {
                        Utils.logToFile("[Audio Manager] [Bluetooth] HEADSET profile disconnected");
                        mBluetoothHeadset = null;
                        mIsBluetoothHeadsetConnected = false;
                        mIsBluetoothHeadsetScoConnected = false;
                        android.util.Log.d("SPEAKERUI1", "osd calling uas");
                        updateAudioState();
                    }
                }
            };
            mBluetoothAdapter.getProfileProxy(AppController.getInstance(), bluetoothServiceListener, BluetoothProfile.HEADSET);
        } else {
            // Log.w("Bluetooth","[Audio Manager] [Bluetooth] Adapter disabled");
        }
    }

    private void startBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            // Log.i("Bluetooth","[Audio Manager] [Bluetooth] Adapter found");
            if (mAudioManager.isBluetoothScoAvailableOffCall()) {
                // Log.i("Bluetooth","[Audio Manager] [Bluetooth] SCO available off call, continue");
            } else {
                // Log.w("Bluetooth","[Audio Manager] [Bluetooth] SCO not available off call !");
            }
            mBluetoothReceiver = new BluetoothReceiver();
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            AppController.getInstance().registerReceiver(mBluetoothReceiver, filter);
            bluetoothAdapterStateChanged();
        } else {
            //  Log.e("Bluetooth", "[Bluetooth adapter is null]");
        }
    }
    // HEADSET

    private void enableHeadsetReceiver() {
        mHeadsetReceiver = new HeadsetReceiver();
        // Log.i("Bluetooth", "[Audio Manager] Registering headset receiver");
        AppController.getInstance().registerReceiver(mHeadsetReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        AppController.getInstance().registerReceiver(mHeadsetReceiver, new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
        mHeadsetReceiverRegistered = true;
    }

    public class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInitialStickyBroadcast()) {
                // Log.i("Headset", "[Headset] Received broadcast from sticky cache, ignoring...");
                return;
            }
            String action = intent.getAction();
            if (action.equals(AudioManager.ACTION_HEADSET_PLUG)) {
                // This happens when the user plugs a Jack headset to the device for example
                // https://developer.android.com/reference/android/media/AudioManager.html#ACTION_HEADSET_PLUG
                int state = intent.getIntExtra("state", 0);
                String name = intent.getStringExtra("name");
                int hasMicrophone = intent.getIntExtra("microphone", 0);
                if (state == 0) {
                    // Log.i("Headset","[Headset] Headset disconnected:" + name);
                } else if (state == 1) {
                    // Log.i("Headset","[Headset] Headset connected:" + name);
                    if (hasMicrophone == 1) {
                        // Log.i("Headset","[Headset] Headset " + name + " has a microphone");
                    }
                } else {
                    //  Log.w("Headset","[Headset] Unknown headset plugged state: " + state);
                }
                getAudioManager().routeAudioToEarPiece();
//                LinphoneManager.getCallManager().refreshInCallActions();
            } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                // This happens when the user disconnect a headset, so we shouldn't play audio loudly
                //  Log.i("Headset","[Headset] Noisy state detected, most probably a headset has been disconnected");
                getAudioManager().routeAudioToEarPiece();
//                LinphoneManager.getCallManager().refreshInCallActions();
            } else {
                //  Log.w("Headset","[Headset] Unknown action: " + action);
            }
        }
    }

    public static class BluetoothReceiver extends BroadcastReceiver {
        public BluetoothReceiver() {
            super();
            // Log.i("Bluetooth","[Bluetooth] Bluetooth receiver created");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Utils.logToFile("[Bluetooth] Bluetooth broadcast received");
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Utils.logToFile("[Bluetooth] Adapter has been turned off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Utils.logToFile("[Bluetooth] Adapter is being turned off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Utils.logToFile("[Bluetooth] Adapter has been turned on");
                        getAudioManager().bluetoothAdapterStateChanged();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //  Log.i("Bluetooth","[Bluetooth] Adapter is being turned on");
                        break;
                    case BluetoothAdapter.ERROR:
                        // Log.e("Bluetooth","[Bluetooth] Adapter is in error state !");
                        break;
                    default:
                        // Log.w("Bluetooth","[Bluetooth] Unknown adapter state: " + state);
                        break;
                }
            } else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    //Log.i("Bluetooth","[Bluetooth] Bluetooth headset connected");
                    getAudioManager().bluetoothHeadetConnectionChanged(true);
                } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                    // Log.i("Bluetooth","[Bluetooth] Bluetooth headset disconnected");
                    getAudioManager().bluetoothHeadetConnectionChanged(false);
                } else {
                    // Log.w("Bluetooth","[Bluetooth] Bluetooth headset unknown state changed: " + state);
                }
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    //  Log.i("Bluetooth","[Bluetooth] Bluetooth headset audio connected");
                    getAudioManager().bluetoothHeadetAudioConnectionChanged(true);
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    // Log.i( "Bluetooth","[Bluetooth] Bluetooth headset audio disconnected");
                    getAudioManager().bluetoothHeadetAudioConnectionChanged(false);
                } else if (state == BluetoothHeadset.STATE_AUDIO_CONNECTING) {
                    // Log.i("Bluetooth","[Bluetooth] Bluetooth headset audio connecting");
                } else {
                    //  Log.w("Bluetooth","[Bluetooth] Bluetooth headset unknown audio state changed: " + state);
                }
            } else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
                if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                    // Log.i("Bluetooth","[Bluetooth] Bluetooth headset SCO connected");
                    getAudioManager().bluetoothHeadetScoConnectionChanged(true);
                } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                    //Log.i("Bluetooth","[Bluetooth] Bluetooth headset SCO disconnected");
                    getAudioManager().bluetoothHeadetScoConnectionChanged(false);
                } else if (state == AudioManager.SCO_AUDIO_STATE_CONNECTING) {
                    // Log.i("Bluetooth","[Bluetooth] Bluetooth headset SCO connecting");
                } else if (state == AudioManager.SCO_AUDIO_STATE_ERROR) {
                    //Log.i("Bluetooth","[Bluetooth] Bluetooth headset SCO connection error");
                } else {
                    // Log.w("Bluetooth","[Bluetooth] Bluetooth headset unknown SCO state changed: " + state);
                }
            } else if (action.equals(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)) {
                String command = intent.getStringExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD);
                int type = intent.getIntExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE, -1);
                String commandType;
                switch (type) {
                    case BluetoothHeadset.AT_CMD_TYPE_ACTION:
                        commandType = "AT Action";
                        break;
                    case BluetoothHeadset.AT_CMD_TYPE_READ:
                        commandType = "AT Read";
                        break;
                    case BluetoothHeadset.AT_CMD_TYPE_TEST:
                        commandType = "AT Test";
                        break;
                    case BluetoothHeadset.AT_CMD_TYPE_SET:
                        commandType = "AT Set";
                        break;
                    case BluetoothHeadset.AT_CMD_TYPE_BASIC:
                        commandType = "AT Basic";
                        break;
                    default:
                        commandType = "AT Unknown";
                        break;
                }
                // Log.i("Bluetooth","[Bluetooth] Vendor action " + commandType + " : " + command);
            } else {
                //Log.w("Bluetooth","[Bluetooth] Bluetooth unknown action: " + action);
            }
        }
    }

    public static AndroidAudioManager getAudioManager() {
        // android.util.Log.d("Bluetooth", "is instance null? " + (instance == null));
        if (instance == null) {
            instance = new AndroidAudioManager();
        }
        return instance;
    }

    private static AndroidAudioManager instance;

    public static enum AudioState {
        UNKNOWN, SPEAKER, BLUETOOTH, EARPIECE
    }
}
