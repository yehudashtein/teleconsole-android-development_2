package com.telebroad.teleconsole.pjsip;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.telebroad.teleconsole.controller.AppController;

import java.util.Set;

public class BluetoothWrapper {
    private static final String THIS_FILE = "BluetoothWrapper";
    private AudioManager audioManager;

    private boolean isBluetoothConnected = false;

    private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

         //   Log.d(THIS_FILE, ">>> BT SCO state changed !!! ");
            if (AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals(action)) {
                int status = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR);
             //   Log.d(THIS_FILE, "BT SCO state changed : " + status + " target is " + targetBt);
                audioManager.setBluetoothScoOn(targetBt);

                if (status == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                    isBluetoothConnected = true;
                } else if (status == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                    isBluetoothConnected = false;
                }

                if (btChangesListener != null) {
                    btChangesListener.onBluetoothStateChanged(status);
                }
            }
        }
    };

    public interface BluetoothChangeListener {
        void onBluetoothStateChanged(int status);
    }


    private static BluetoothWrapper instance;

    protected BluetoothChangeListener btChangesListener;

    public static BluetoothWrapper getInstance() {
        if (instance == null) {
            instance = new BluetoothWrapper();
            instance.setContext(AppController.getInstance());
        }

        return instance;
    }

    protected BluetoothAdapter bluetoothAdapter;

    public void setBluetoothChangeListener(BluetoothChangeListener l) {
        btChangesListener = l;
    }

    private void setContext(Context aContext) {
//        setContext(aContext);
//        context = aContext;
        audioManager = (AudioManager) aContext.getSystemService(Context.AUDIO_SERVICE);
        if (bluetoothAdapter == null) {
            try {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            } catch (RuntimeException e) {
               // Log.w(THIS_FILE, "Cant get default bluetooth adapter ", e);
            }
        }
    }

    public static boolean canBluetooth(AudioManager audioManager) {
        // Detect if any bluetooth a device is available for call
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        }
        boolean hasConnectedDevice = false;
        //If bluetooth is on
        if (bluetoothAdapter.isEnabled()) {

            //We get all bounded bluetooth devices
            // bounded is not enough, should search for connected devices....
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                if (bluetoothClass != null) {
                    int deviceClass = bluetoothClass.getDeviceClass();
                    if (bluetoothClass.hasService(BluetoothClass.Service.RENDER) ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE) {
                        //And if any can be used as a audio handset
                        hasConnectedDevice = true;
                        break;
                    }
                }
            }
        }
        boolean retVal = hasConnectedDevice && audioManager.isBluetoothScoAvailableOffCall();
       // Log.d(THIS_FILE, "Can I do BT ? " + retVal);
        return retVal;
    }

    private boolean targetBt = false;

    public void setBluetoothOn(boolean on) {
      //  Log.d(THIS_FILE, "Ask for " + on + " vs " + audioManager.isBluetoothScoOn());
        targetBt = on;
        if (on != isBluetoothConnected) {
            // BT SCO connection state is different from required activation
            if (on) {
                // First we try to connect
                //Log.d(THIS_FILE, "BT SCO on >>>");
                audioManager.startBluetoothSco();
            } else {
                //Log.d(THIS_FILE, "BT SCO off >>>");
                // We stop to use BT SCO
                audioManager.setBluetoothScoOn(false);
                // And we stop BT SCO connection
                audioManager.stopBluetoothSco();
            }
        } else if (on != audioManager.isBluetoothScoOn()) {
            // BT SCO is already in desired connection state
            // we only have to use it
            audioManager.setBluetoothScoOn(on);
        }
    }

    public boolean isBluetoothOn() {
        return isBluetoothConnected;
    }

    public void register(Context context) {
      //  Log.d(THIS_FILE, "Register BT media receiver");
        context.registerReceiver(mediaStateReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }

    public void unregister(Context context) {
        try {
           // Log.d(THIS_FILE, "Unregister BT media receiver");
            context.unregisterReceiver(mediaStateReceiver);
        } catch (Exception e) {
           // Log.w(THIS_FILE, "Failed to unregister media state receiver", e);
        }
    }

    public boolean isBTHeadsetConnected() {
        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(AppController.getInstance(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;//TODO;
            }
            return (bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothAdapter.STATE_CONNECTED);
        }
        return false;
    }
}
