package com.telebroad.teleconsole.pjsip;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.telecom.DisconnectCause;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.telebroad.teleconsole.BuildConfig;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.notification.HistoryNotification;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AudioMediaRecorder;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.CallSetting;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.JbufState;
import org.pjsip.pjsua2.MathStat;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnCallTransferStatusParam;
import org.pjsip.pjsua2.RtcpStat;
import org.pjsip.pjsua2.RtcpStreamStat;
import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.StreamStat;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_flag;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.annotation.Nullable;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.media.AudioManager.STREAM_VOICE_CALL;
import static android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.pjsip.SipService.NONEXSISTENT_PJSIP_ID;


public class TeleConsoleCall extends Call implements CallGroup {

    private static final String TAG = "PJSIP_CALL";

    int callID = NONEXSISTENT_PJSIP_ID;
    Runnable onCallStart;
    Runnable onMediaReady;
    TelebroadSipAccount account;
    private AudioMediaRecorder audioMediaRecorder;
    private boolean recording;
    boolean isSpeaker = false;
    private boolean isActive = false;
    private AudioMedia remoteStream;
    private PhoneNumber name;
    private AudioMedia localStream;
    private ToneGenerator toneGenerator;
    private Ringer ringer;
    private StreamStat streamStat;
    private String recordingDirectoryPath;
    private String recordingFileName;
    private String remoteURI;
    private String finalRecordingPath;
    private String recordingContactName;
    private int connectionID;
    private boolean isIncoming;

    TeleConsoleCall(TelebroadSipAccount acc) {
        super(acc);
        this.account = acc;
    }

    TeleConsoleCall(TelebroadSipAccount acc, int callID) {
        super(acc, callID);
        this.account = acc;
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        handleCallChange(prm);
    }

    private void handleCallChange(OnCallStateParam prm) {
        try {
            CallInfo info = getInfo();
            callID = info.getId();
            remoteURI = info.getRemoteUri();
            int inviteState = info.getState();
            Utils.logToFile(getService(), "State changed " + inviteState);
           // Log.d(TAG + "_TC", "call State changed Call info state " + inviteState + " id " + callID + " call id " + getInfo().getCallIdString() + " role " + getInfo().getRole());
            if (inviteState == pjsip_inv_state.PJSIP_INV_STATE_EARLY && getInfo().getRole() == pjsip_role_e.PJSIP_ROLE_UAC) {
                if (toneGenerator == null) {
                    toneGenerator = new ToneGenerator(STREAM_VOICE_CALL, 50);
                }
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK);
            } else if (inviteState == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                getService().registerPhoneState();
                if(isIncoming){
                    routeStreams();
                    AndroidAudioManager.getAudioManager().incomingCallConnected();
                }else{
                    AndroidAudioManager.getAudioManager().callConnected();
                }
                if (SipService.getInstance() == null){
                    return;
                }
                getService().enqueueJob(() -> {
                    Endpoint endpoint = SipService.getInstance().getEndpointInstance(false);
                    if (endpoint == null){
                        return;
                    }
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && connectionID > 0 && useConnectionService ){
                           // Log.d(TAG + "_TC", "call State changed Call info state setting active");

//                            PJSIPConnection.connections.get(connectionID).setActive();
                        }
                        if(AppController.getInstance().hasPermissions(RECORD_AUDIO)) {
                          //  Log.e("Setting Microphone", "we have microphone permissions");
                            getService().enqueueJob(() -> {
                                try {
                                    routeStreams();
                                    endpoint.audDevManager().setCaptureDev(-1);
                                } catch (Exception e) {

                                    Utils.logToFile(e);
                                    e.printStackTrace();
                                }
                            }, "Setting mic with permission");
                        }else{
                           // Log.e("Setting Microphone", "No microphone permissions");
                        }

//                        Log.d("BluetoothDM", "Output route " + endpoint.audDevManager().getOutputRoute());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.logToFile(e);
                        Utils.logToFile("Exception setting microphone");
                    }
                }, " Setting microphone");
                if (toneGenerator != null) {
                    toneGenerator.stopTone();
                }
                ringer = null;
                if (onCallStart != null) {
                    onCallStart.run();
                }
                CallManager.getInstance().setLiveCall(this);
                isActive = true;
                if (!isIncoming) {
                    callStates.setEarly(false);
                }
                Utils.updateLiveData(liveCallStates, callStates);
                this.getService().startForeground(CallManager.getInstance().getCall(getID()));

            } else if (inviteState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                getService().enqueueJob(this::callDisconnected, "Disconnecting call");
            }
        } catch (Exception e) {
         //   Log.e(TAG + "_TC", "error", e);
            Utils.logToFile(e);
            e.printStackTrace();
        }
    }

    private void moveRecordingToSharedFolder(){
        ContentResolver contentResolver = AppController.getInstance().getApplicationContext().getContentResolver();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Uri audioCollection = MediaStore.Audio.Media.getContentUri(VOLUME_EXTERNAL_PRIMARY);
            ContentValues newSongDetails = new ContentValues();
            newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, recordingContactName + "_" + recordingFileName );
            newSongDetails.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + File.separator + "Call Recordings");
            newSongDetails.put(MediaStore.Audio.Media.ALBUM, "Call Recordings");
            newSongDetails.put(MediaStore.Audio.Media.ARTIST, "Call Recordings");
            newSongDetails.put(MediaStore.Audio.Media.GENRE, "Call Recordings");
            newSongDetails.put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav");
            Uri file = contentResolver.insert(audioCollection, newSongDetails);
            if (file !=  null) {
                try {
                    InputStream inputStream = new FileInputStream(finalRecordingPath);
                    OutputStream outputStream = contentResolver.openOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int read = inputStream.read(buffer);
                    if (outputStream != null) {
                        while (read != -1) {
                            outputStream.write(buffer, 0, read);

                            read = inputStream.read(buffer);
                        }
                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void callDisconnected() {
        if (audioMediaRecorder != null) {
            audioMediaRecorder.delete();
            Utils.asyncTask(this::moveRecordingToSharedFolder);
            audioMediaRecorder = null;
        }
        if (ringer != null) {
            ringer.silenceRinging();
        }
        Utils.logToFile("Call Disconnected, removing call");
        CallManager.getInstance().removeCall(getId());
        getCallController().notifyDisconnected(DisconnectCause.REMOTE);
        if (connectionID > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && useConnectionService){

           // android.util.Log.d("PJSIPConnectionService", "disconnecting " + connectionID);
//            PJSIPConnection.connections.get(connectionID).setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        }
        callStates.setDone(true);
        Utils.updateLiveData(liveCallStates, callStates);

        if (toneGenerator != null) {
            toneGenerator.release();
        }
        delete();
        account.removeCall(this);
    }

    @Override
    public void sendDtmf(String digits) {
        try {
            super.dialDtmf(digits);
        } catch (Exception e) {

            Utils.logToFile(e);
            Utils.logToFile(getService(), e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        handleMediaStateChange(prm);
    }

    Media media = null;
    int mediaStatus;
    private void handleMediaStateChange(OnCallMediaStateParam prm) {
       // android.util.Log.d("State changed", "Media changed");
        try {
            CallMediaInfoVector vector = getInfo().getMedia();
            CallMediaInfo mediaInfo = null;
            for (int i = 0; i < vector.size(); i++) {
                media = getMedia(i);
                mediaInfo = vector.get(i);
                if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO) {
                    mediaStatus = mediaInfo.getStatus();
                    if (media != null && mediaStatus == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
//                    if (CallManager.getInstance().getCallCount() == 1){
//                        CallManager.getInstance().setLiveCall(this);
//                    }
//                        mediaStatus = pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE;
                        if (streamStat == null){
                            streamStat = getStreamStat(0);
                        }
                        callStates.setHold(false);

                        if (onMediaReady != null) {
                            onMediaReady.run();
                           // Log.d("Conf01", "running now " + mediaInfo.getDir());
                            //onMediaReady = null;
                        }
                        break;
                    }
                    if ( mediaStatus == pjsua_call_media_status.PJSUA_CALL_MEDIA_LOCAL_HOLD) {
                        callStates.setHold(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && connectionID > 0 && useConnectionService ){
                            //android.util.Log.d("PJSIPConnectionService", "setting on hold " + connectionID);
                            PJSIPConnection.connections.get(connectionID).setOnHold();
                        }else{

                            //android.util.Log.d("PJSIPConnectionService", "setting on hold " + connectionID);
                        }
                        break;
                    }

                }
            }

         //   Log.d("CallState", "updating call states with " + callStates);
            Utils.updateLiveData(liveCallStates, callStates);
            account.getService().startForeground(this);

            routeStreams();
        } catch (Exception e) {
            Utils.logToFile(e);
            Utils.logToFile(getService(), e.getMessage());
            e.printStackTrace();
        }

        //android.util.Log.e("StartTransmit","calling super");
        super.onCallMediaState(prm);

       // android.util.Log.e("StartTransmit"," super done");
        //mute(callStates.isMute());
    }

    @NonNull
    @Override
    public String toString() {
        return "Remote URI " + remoteURI + " callstates " + liveCallStates.getValue();
    }

    private void routeStreams() {
        try {
            if (!AppController.getInstance().hasPermissions(RECORD_AUDIO)){
               // android.util.Log.e("Routing", "No permission");
                return;
            }else{

                //android.util.Log.e("Routing", "permission granted");
            }
            AudDevManager audDevManager = account.getService().getEndpointInstance(false).audDevManager();

            if (media != null) {
                getService().enqueueJob(() -> {}, "setting media");
                remoteStream = AudioMedia.typecastFromMedia(media);
                localStream = audDevManager.getCaptureDevMedia();
              //  Log.d("AudMang", String.valueOf(localStream));
                if (remoteStream != null) {
                  //  Log.e("StartTransmit","Error");
                    remoteStream.startTransmit(localStream);
                  //  Log.e("StartTransmit","Success");
                    if (!callStates.isMute()) {
                        localStream.startTransmit(remoteStream);
                    }

                   // Log.e("StartTransmit","again");
                }
                AndroidAudioManager.getAudioManager().streamsRunning();
//                if (CallManager.getInstance().getCallCount() == 1) {
//                        if (BluetoothManager.getInstance().isBluetoothHeadsetAvailable()) {
//                            BluetoothManager.getInstance().routeAudioToBluetooth();
//                        }
//                }

            }
        } catch (Exception exc) {
            Utils.logToFile(getService(), exc.getMessage());
            Utils.logToFile(exc);
            exc.printStackTrace();
          //  Log.e(TAG, exc.getMessage(), exc);
            //Logger.error(LOG_TAG, "Error while connecting audio media to sound device", exc);
        }
    }

    public void record(boolean on) {
        try {
            String fileName = android.text.format.DateFormat.format("M_dd_yyyy hh:mm a (ss)", new java.util.Date()).toString() + ".wav";
            String dirPath;

            String filepath;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                ContentResolver contentResolver = AppController.getInstance().getApplicationContext().getContentResolver();
//                Uri audioCollection = MediaStore.Audio.Media.getContentUri(VOLUME_EXTERNAL_PRIMARY);
//                ContentValues newSongDetails = new ContentValues();
//                newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
//                dirPath = Environment.DIRECTORY_MUSIC + File.separator + "TeleConsole" + File.separator + "Recordings" + File.separator + getRemoteNumber().getNameString();
//                newSongDetails.put(MediaStore.Audio.Media.RELATIVE_PATH, dirPath + File.separator + fileName);
//                Uri uri = contentResolver.insert(audioCollection, newSongDetails);
//                filepath = Utils.getPathFromContent(uri, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//
//                android.util.Log.d("FilePath00", "uri = " + filepath);
//            }else{
//                File root = Environment.getExternalStorageDirectory();
//                File dir = new File(root.getAbsolutePath() + File.separator + "TeleConsole" + File.separator + "Recordings" + File.separator + getRemoteNumber().getNameString());
//
//                filepath = dir.getAbsolutePath() + File.separator + 022
//                dir.mkdirs();
//            }

            File root = Utils.getRootFolder();

            this.recordingDirectoryPath = "TeleConsole" + File.separator + "Recordings" + File.separator + getRemoteNumber().getNameString();
            this.recordingFileName = fileName;
            this.recordingContactName = getRemoteNumber().getNameString();
            File dir = new File(root.getAbsolutePath() + File.separator + recordingDirectoryPath);
            filepath = dir.getAbsolutePath() + File.separator + fileName;
            dir.mkdirs();
            recording = on;
            if (audioMediaRecorder == null) {
                audioMediaRecorder = new AudioMediaRecorder();
//                String filePath = dirPath + File.separator + fileName;

               // android.util.Log.d("Filepath00", "filepath " + filepath);
                audioMediaRecorder.createRecorder(filepath);
                this.finalRecordingPath = filepath;
//                this.recordingFilePath = filepath;

            }
            if (audioMediaRecorder != null && remoteStream != null && localStream != null) {
                if (on) {
                    remoteStream.startTransmit(audioMediaRecorder);
                    localStream.startTransmit(audioMediaRecorder);
                } else {
                    remoteStream.stopTransmit(audioMediaRecorder);
                    localStream.stopTransmit(audioMediaRecorder);
                }
            }
        } catch (Exception e) {
            Utils.logToFile(e);
            Utils.logToFile(getService(), e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public PhoneNumber getRemoteNumber() {
        if (name == null || name.getPhoneNumber().isEmpty()) {
            try {
               // android.util.Log.d("ANSWER " , "Trying to get remote");
//                String remoteUri = getInfo().getRemoteUri();
                setNameFromUri(remoteURI);
            } catch (Exception e) {
                Utils.logToFile(e);
                name = new PhoneNumber("");
//                name = PhoneNumber.getPhoneNumber("", "Unknown");
               // android.util.Log.e("PJSIP_NAME", "Name crashed", e);

                Utils.logToFile(getService(), e.getMessage());
            }
        }
        return name;
    }

    private void setNameFromUri(String remoteUri) {
        String displayName = getDisplayName(remoteUri);
        String username = getUsername(remoteUri);
        name = PhoneNumber.getPhoneNumber(username, displayName);
    }

    private String getUsername(String remoteUri) {
        int startUsername = remoteUri.indexOf("sip:");
        int endUserName = remoteUri.indexOf("@");
        return remoteUri.substring(startUsername + 4, endUserName);
    }

    private String getDisplayName(String remoteUri) {
        if (remoteUri == null){
            return "";
        }
        int firstQuote = remoteUri.indexOf("\"");
        // Did we find a quote?
        if (firstQuote != -1) {
            int lastQuote = remoteUri.lastIndexOf("\"");
            if (lastQuote != -1) {
                return remoteUri.substring(firstQuote + 1, lastQuote);
            }
        }
        return "";
    }

    public void setHold(boolean on) {
     //   Log.d(TAG + "_02", "id " + getId() + " should hold " + on);
        CallStates callStates = liveCallStates().getValue();
        if (callStates == null || callStates.isHold() == on) {
            return;
        }
        callStates.setHold(on);
        Utils.updateLiveData(liveCallStates(), callStates);
        // In 3 seconds update the UI to the current hold status, this is done in case the hold didn't work
        new Handler().postDelayed(() -> {
            callStates.setHold(mediaStatus == pjsua_call_media_status.PJSUA_CALL_MEDIA_LOCAL_HOLD);
            Utils.updateLiveData(liveCallStates(), callStates);
        }, 3000);
        try {
            if (on) {
                getService().enqueueJob(() -> {
                    try {
                        if (getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                            setHold(new CallOpParam());
                        }else{
                            try {
                                throw new IllegalArgumentException("Call not yet confirmed, can't hold");
                            }catch (IllegalArgumentException illegalArgumentException){
                                illegalArgumentException.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        Utils.logToFile(e);
                        Utils.logToFile(getService(), e.getMessage());
                        e.printStackTrace();
                    }
                }, "Putting on Hold");
            } else {
                getService().enqueueJob(() -> {
                    CallOpParam unholdParam = new CallOpParam();
                    setMediaParams(unholdParam);
                    CallSetting setting = unholdParam.getOpt();
                    setting.setFlag(pjsua_call_flag.PJSUA_CALL_UNHOLD);
                    try {
                        reinvite(unholdParam);
                    } catch (Exception e) {
                        Utils.logToFile(e);
                        Utils.logToFile(getService(), e.getMessage());
                        e.printStackTrace();
                    }
                }, "Call reinvite");
            }
        } catch (Exception e) {

            Utils.logToFile(e);
            Utils.logToFile(getService(), e.getMessage());
        }
    }

    @Override
    public int duration() {
        return getDuration();
    }

    @Override
    public void hangup() {
        try {
            hangup(new CallOpParam());
        } catch (Exception e) {
            if (ringer != null) {
                ringer.silenceRinging();
            }
            Utils.logToFile("Error hanging up " + e.getMessage());
            CallManager.getInstance().removeCall(getID());
            CallManager.getInstance().stopForeground(account.getService());
            if (e.getMessage().contains("PJ_ETIMEDOUT")) {
               // android.util.Log.e("PJSIP_SERVICE", "Stop");
                getService().stopSelf();
            }

            Utils.logToFile(e);
            Utils.logToFile(getService(), e.getMessage());
            e.printStackTrace();
        }
    }

    public void replyBusy() {
       // android.util.Log.d(TAG, "we are busy");
        CallOpParam callOpParam = new CallOpParam();
        callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
        if (ringer != null) {
            ringer.silenceRinging();
        }
//        if (callStates().isDone()){
//            return;
//        }
        getService().enqueueJob(() -> {
            try {
               // android.util.Log.d("ReplyBusy", "call states " + callStates());

                answer(callOpParam);

            } catch (Exception e) {
                Utils.logToFile("Error replying busy " + e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("PJ_ETIMEDOUT")) {
                  //  android.util.Log.e("PJSIP_SERVICE", "Stop");
                    Utils.logToFile(getService(), "We timed out trying to answer, stopping service");
                    getService().stopSelf();
                }
                Utils.logToFile(e);
                e.printStackTrace();
                Utils.logToFile(getService(), e.getMessage());
            }
        }, "Reply Busy Job ID: " + getSipID());

        Utils.logToFile("Replying busy, remove call " );
        CallManager.getInstance().removeCall(getID());
        CallManager.getInstance().stopForeground(account.getService());
        if (ringer != null) {
            ringer.silenceRinging();
        }
        HistoryNotification.callDeclined = getServerID();
    }

    @Override
    public void answer() {
        CallOpParam callOpParam = new CallOpParam();
        callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_ACCEPTED);
        if (ringer != null) {
            ringer.silenceRinging();
        }
//        try {
        getService().enqueueJob(() -> {
            try {
                if (callStates().isDone()){
                    return;
                }
                answer(callOpParam);
            } catch (Exception e) {

                Utils.logToFile(getService(), e.getMessage());
                if (ringer != null) {
                    ringer.silenceRinging();
                }


                Utils.logToFile(getService(), "Error answering call, removing call ");
                CallManager.getInstance().removeCall(getID());
                CallManager.getInstance().stopForeground(account.getService());
                Utils.logToFile(getService(), "Error answering call: " + e.getMessage());
                Utils.logToFile(e);
                if (e.getMessage().contains("PJ_ETIMEDOUT")) {
                   /// android.util.Log.e("PJSIP_SERVICE", "Stop");
                    getService().stopSelf();
                }
            }
        }, "Call Answer Job ID: " + getSipID());

        if (ringer != null) {
            ringer.silenceRinging();
        }
        callStates.setRinging(false);
        Utils.updateLiveData(liveCallStates, callStates);
//        CallManager.getInstance().setLiveCall(this);
    }

    public void tryRing() {
        if (account == null){
            return;
        }
        if (!account.isRegistered()) {
            ///android.util.Log.d("Test01", "account not registered");
            account.onRegistrationOK = () -> {
                try {
                    ring();
                } catch (Exception e) {
                    Utils.logToFile(getService(), e.getMessage());
                    Utils.logToFile(e);
                    e.printStackTrace();
                } finally {
                    account.onRegistrationOK = null;
                }
            };
            return;
        }
    }

    public void ring() throws Exception {

        //android.util.Log.d("Ring02", "is ringing " + callStates.isRinging() + " is done " + callStates.isDone());
        if (!account.isRegistered()) {
            Utils.logToFile( "Can't ring, account not registered");
            account.onRegistrationOK = () -> {
                try {
                    ring();
                } catch (Exception e) {
                    Utils.logToFile(getService(), e.getMessage());
                    Utils.logToFile(e);
                    e.printStackTrace();
                } finally {
                    account.onRegistrationOK = null;
                }
            };
            return;
        }

        if (getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            Utils.logToFile( "Can't ring, call Disconnected");
            return;
        }
        ringer = new Ringer(getService());
        callStates.setEarly(false);
        callStates.setRinging(true);
        Utils.updateLiveData(liveCallStates, callStates);
       // android.util.Log.d("PJSIP", "adding call");
        if (AppController.getInstance() == null) {
            return;
        }

        CallOpParam callOpParam = new CallOpParam();
        callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_PROGRESS);

        SipHeader uaHeader = new SipHeader();
        uaHeader.setHName("X-TeleConsole");
        uaHeader.setHValue("Android v" + BuildConfig.VERSION_CODE);
        callOpParam.getTxOption().getHeaders().add(uaHeader);

        ringer.startRinging();
        answer(callOpParam);
        Utils.logToFile(getService().getApplicationContext(), "Ringing = Starting foreground");
        account.getService().startForeground(this);
        account.getService().enqueueDelayedJob(() -> {
            if (callStates.isRinging() && !callStates.isDone()) {
                try {
                    replyBusy();
                } catch (Exception e) {
                    Utils.logToFile(getService(), e.getMessage());
                    Utils.logToFile(e);
                    e.printStackTrace();

                }
            }
        }, 30 * 1000, "Stopping ringing");

    }

    public void decline() {
        Utils.logToFile("Declining, remove call");
        try {
            replyBusy();
            CallManager.getInstance().removeCall(getID());
        } catch (Exception e) {
            Utils.logToFile(getService(), "Error declining call " + e.getMessage());
            Utils.logToFile(e);
            //android.util.Log.w(TAG, "decline fail", e);
        }
    }

    private String sipID = "";
    public String getSipID() {
        if (sipID != null && !sipID.isEmpty()){
            return sipID;
        }
        try {
            if (SipService.isThreadRegistered()) {
//            SipService.getInstance().registerThread();
                sipID = getInfo().getCallIdString();
                return sipID;
            }else {
                return getID() + " can't get sip ID";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.logToFile(getService(), e.getMessage());
            Utils.logToFile(e);
            return getID() + " can't get sip ID";
        }
    }

    @Override
    public void onCallTransferStatus(OnCallTransferStatusParam prm) {
        try {
            int state = getInfo().getState();
            if (prm.getStatusCode() == pjsip_status_code.PJSIP_SC_OK) {
               // Log.d(TAG, "Transfer State changed Call id " + getInfo().getId() + " transfer state " + prm.getStatusCode() + " hanging up");
                hangup(new CallOpParam());
            } else {
               // Log.d(TAG, "Transfer State changed Call id " + getInfo().getId() + " transfer state " + prm.getStatusCode());
            }
        } catch (Exception e) {
            Utils.logToFile(getService(), e.getMessage());
            Utils.logToFile(e);
            e.printStackTrace();
        }
        super.onCallTransferStatus(prm);
    }

    private void setMediaParams(CallOpParam param) {
        CallSetting callSetting = param.getOpt();
        callSetting.setAudioCount(1);
        callSetting.setVideoCount(0);
        callSetting.setFlag(pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA);
    }

    void makeCall(String dest) throws Exception{
        makeCall(dest, -3);
    }
    void makeCall(String dest, int connectionID) throws Exception {
        //Log.d("PJSIP_CALL_01", " make call " + dest);
        if (!dest.startsWith("sip:")) {
            dest = "sip:" + dest + "@" + SettingsHelper.getSipDomain();
        }
        setNameFromUri(dest);

        callStates.setEarly(true);
        Utils.updateLiveData(liveCallStates, callStates);

        AndroidAudioManager.getAudioManager().outgoingCallStarted();
        super.makeCall(dest, new CallOpParam());
        if (CallManager.getInstance() != null) {
            CallManager.getInstance().addCall(this);
            if (connectionID != -3){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && useConnectionService) {
                    setConnectionID(connectionID);
                    PJSIPConnection connection = PJSIPConnection.connections.get(connectionID);
                    if (!callStates.isHold()){
                        //android.util.Log.d("PJSIPConnectionService", "setting connection " + connectionID + " to active");
//                        connection.setActive();
                    }
                    connection.setCallID(getID());
//                    PJSIPConnection.connections.get(connectionID).setCallID(getId());
                }
            }
        }
//        getRemoteNumber().getName();
        account.getService().showActiveCallActivity(getID());

    }

    public void transfer(String dest) {
        if (!dest.startsWith("sip:")) {
            dest = "sip:" + dest + "@" + SettingsHelper.getSipDomain();
        }
        ///android.util.Log.d("PJSIP_TRANSFER", "Transferring call to " + dest);
        try {
            xfer(dest, new CallOpParam());
            //android.util.Log.d("PJSIP_TRANSFER", "Success Transferring call to " + dest);
        } catch (Exception e) {
            android.util.Log.d("PJSIP_TRANSFER", "Failed Transferring call to " + dest);
            Utils.logToFile(getService(), e.getMessage());
            Utils.logToFile(e);
            e.printStackTrace();
        }
    }

    public AudioMedia getRemoteStream() {
        return remoteStream;
    }

    public AudioMedia getLocalStream() {
        return localStream;
    }

    public boolean isOnHold() {
        return callStates.isHold();
    }

    public void mute(boolean on) {
        try {
            callStates.setMute(on);
            Utils.updateLiveData(liveCallStates, callStates);
            if (on) {
                localStream.stopTransmit(remoteStream);
            } else {
                localStream.startTransmit(remoteStream);
            }
        } catch (Exception e) {
            Utils.logToFile(getService(), e.getMessage());
            Utils.logToFile(e);
            e.printStackTrace();
        }
    }

    @Override
    public void addToConference() {
       // android.util.Log.d("Conf01", "Call adding to conference " + getId());
        getCallController().addToConference();
        CallManager.getInstance().addCallToConference(this);
    }

    @Override
    public int getID() {
        return getId();
    }

    @Override
    public int getId(){
        if (this.callID == NONEXSISTENT_PJSIP_ID){
            this.callID = super.getId();
        }
        return callID;
    }

    private MutableLiveData<CallStates> liveCallStates = new MutableLiveData<>();
    private CallStates callStates = new CallStates();

    @Override
    public MutableLiveData<CallStates> liveCallStates() {
        if (liveCallStates.getValue() == null) {
            Utils.updateLiveData(liveCallStates, new CallStates());
        }
        return liveCallStates;
    }

    private CallOperations opsSupported = new CallOperations(true, true, true, true);

    @Override
    public CallOperations opsSupported() {
        return opsSupported;
    }

    public void xferReplaces(TeleConsoleCall secondCall) {
        try {
            xferReplaces(secondCall, new CallOpParam());
        } catch (Exception e) {
            Utils.logToFile(getService(), e.getMessage());
            Utils.logToFile(e);
            e.printStackTrace();
        }
    }

    @Override
    public CallState getCallState() {
        if (callStates.isRinging() || isIncomingEarly()) {
            return CallState.RINGING;
        }
        if (callStates.isHold()) {
            return CallState.HOLD;
        }
        if (isActive) {
            return CallState.ACTIVE;
        }
        return CallState.CONNECTING;
    }

    @Override
    public SipService getService() {
        return account.getService();
    }

    public int getDuration() {
        if (!swigCMemOwn){
            return 0;
        }
        try {
            if (isActive) {
                return getInfo().getConnectDuration().getSec();
            } else {
                return getInfo().getTotalDuration().getSec();
            }
        } catch (Exception e) {
            Utils.logToFile(getService(), e.getMessage());
            Utils.logToFile(e);
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected void finalize() {
        super.finalize();
        ///android.util.Log.d("PJSIP_GC", "finalizing");
        Utils.logToFile("Garbage collection removing call? " + swigCMemOwn);
        if (swigCMemOwn) {
            CallManager.getInstance().removeCall(callID);
        }
//        getService().enqueueJob(this::delete, "Finalizing");
    }

    private String getRtcpString(){
        if (streamStat != null){
            RtcpStat rtcpStat = streamStat.getRtcp();
            RtcpStreamStat in = rtcpStat.getRxStat();
            RtcpStreamStat out = rtcpStat.getTxStat();
            String header = "\n---------------------------- RTCP STATS ---------------------------- \n";
            String rttString = "Rtt Stats: " + getMathStatString(rtcpStat.getRttUsec()) + "\n";
            String inString = "-------------- Incoming " + getRTCPStreamString(in) ;
            String outString = "-------------- Outgoing " + getRTCPStreamString(out);
            String jitterString = getJitterStatString(streamStat.getJbuf()) + " \n";
            return header + rttString + inString + outString;
        }
        return "Error Getting Rtcp Stats";
    }

    private String getRTCPStreamString(RtcpStreamStat stream){
        if(stream != null){
            String header = "Stream Stats: --------------\n";
            long pkt = stream.getPkt();
            long bytes = stream.getBytes();
            long discarded = stream.getDiscard();
            long loss = stream.getLoss();
            long reorder = stream.getReorder();
            String rawStats = "Raw Stats: packets = " + pkt + " total bytes = " + bytes + " Discarded = " + discarded + " loss = " + loss  +
                    " Total dropped packets = " + (loss + discarded) + " Reordered = " + reorder + "\n";
            String percentageStats = "Rate stats: Loss = " + ((loss * 1.0) / pkt) + " Discarded = " + ((discarded * 1.0) / pkt) + " Total Dropped = " + (((loss + discarded) * 1.0) / pkt) + "\n";
            String jitterString = "Jitter: " + getMathStatString(stream.getJitterUsec()) + "\n";
            return header + rawStats + percentageStats + jitterString;
        }
        return "Error Getting String ";
    }

    private String getMathStatString(MathStat mathStat){

        if (mathStat != null){
            return "Max: " + mathStat.getMax() + " Average: " + mathStat.getMean() + " Min: " + mathStat.getMin() + " Last: " + mathStat.getLast();
        }
        return "Error reading MathStat";
    }

    private String getJitterStatString(JbufState jitter){
        if (jitter != null){
            String delayStats = "Jitter Delay Stats: Max =" + jitter.getMaxDelayMsec() + " Average = " + jitter.getAvgDelayMsec() + " Min = " + jitter.getMinDelayMsec() + "\n";
            String frameStats = "Other Jitter Stats: Average Burst = " + jitter.getAvgBurst() + " Last Burst = " + jitter.getBurst() + " Frame Size = " + jitter.getFrameSize()
                    + " lost = " + jitter.getLost() + " discarded = " + jitter.getDiscard() ;
        }
        return "Error reading jitter";
    }

    private String serverID;

    @Nullable
    public String getServerID(){
        return serverID;
    }
    public String getServerID(String sipMessage){
        if (serverID == null){
            serverID = extractServerID(sipMessage);
        }
        return serverID;
    }

    private String extractServerID(String wholeMessage) {
        if (isNullOrEmpty(wholeMessage)){
            return "";
        }
        String headersPart = wholeMessage.split("\n\n")[0];
        String[] headers = headersPart.split("\n");
        HashMap<String, String> headersMap = new HashMap<>();
        for (String header : headers) {
            String[] parsed = header.split(":");
            if (parsed.length >= 2) {
                headersMap.put(parsed[0].trim(), parsed[1].trim());
            }
        }
        return headersMap.get("X-Enswitch-Uniqueid");
    }

    CallController callController;
    public CallController getCallController(){
        if (CallManager.getInstance().isInConference()){
            if (CallManager.getInstance().getConference() != null) {
                return CallManager.getInstance().getConference();
            }
        }
        if (callController == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && connectionID > 0 && useConnectionService) {
                callController = new ConnectionController(getConnectionID());
            } else {
                callController = new PJSIPCallController(getID());
            }
        }
        return callController;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
       // android.util.Log.d("PJSIPConnectionService", "connection id " + connectionID);
        if (connectionID >= 0 && useConnectionService){
            callController = new ConnectionController(connectionID);
        }
    }

    public void setIncoming(boolean isIncoming){
        this.isIncoming = isIncoming;
    }
    @Override
    public boolean isIncoming() {
        return isIncoming;
    }

    @Override
    public int getConnectionID() {
        return connectionID;
    }
    public static boolean useConnectionService = false;
}
