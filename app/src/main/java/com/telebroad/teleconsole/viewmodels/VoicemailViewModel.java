package com.telebroad.teleconsole.viewmodels;

import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.Voicemail;
import com.telebroad.teleconsole.model.repositories.VoicemailRepository;
import java.io.File;
import java.io.IOException;
import static com.google.common.base.Strings.nullToEmpty;

public class VoicemailViewModel extends MessageViewModel<Voicemail>  {
    boolean hasEnd;
    private MediaDescriptionCompat mediaDescriptionCompat;
    //private VoicemailPlayingService voicemailPlayingService;

    @Override
    public PhoneNumber findOtherNumber() {
        if (getItem() == null) {
            return null;
        }
        PhoneNumber otherNumber = PhoneNumber.getPhoneNumber(getItem().getCallerid_ext());
        otherNumber.setDefaultName(getItem().getCaller() == null || getItem().getCaller().isEmpty() || otherNumber.phoneNumberEquals(getItem().getCaller()) ? null : getItem().getCaller());
        return otherNumber;
    }

    //    public MediaPlayer voicemailPlayer = new MediaPlayer();
    public MutableLiveData<Integer> getPlayerProgress = new MutableLiveData<>();
    private Handler progressHandler = new Handler();

    private boolean wasSpeakerphone;
    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            Utils.updateLiveData(getPlayerProgress, 0);
            progressHandler.postDelayed(this, 200);
        }
    };

    @Override
    protected void finalize() {
        progressHandler.removeCallbacks(updateProgress);
        super.finalize();
    }


    //    @Override
//    protected void onCleared() {
//        android.util.Log.d("Voicemail01", " clearing voicemail");
//        super.onCleared();
//        progressHandler.removeCallbacks(updateProgress);
//        try {
//            voicemailPlayer.stop();
//        }catch (IllegalStateException illegalState){
//            android.util.Log.i("VoicemailPlayingService", "was already stopped");
//        }
//        voicemailPlayer.release();
//        AudioManager am = (AudioManager) AppController.getInstance().getSystemService(Context.AUDIO_SERVICE);
//        if (am == null){
//            return;
//        }
//        voicemailPlayer = new MediaPlayer();
//        am.setSpeakerphoneOn(wasSpeakerphone);
//        am.setMode(AudioManager.MODE_NORMAL);
//    }
    public void downloadVoicemail(MediaControllerCompat mediaController, Consumer<TeleConsoleError> errorHandler) {
        final File filepath = getFile();
        Uri fileUri = getFileUri(filepath);
        if (filepath.exists() && fileUri != null) {
            Utils.asyncTask(() -> {
                addURItoQueue(mediaController, fileUri);
                errorHandler.accept(null);
            });
        } else {
            getItem().downloadVoicemail((base64encoded) -> {
               // android.util.Log.d("Voicemail01", "Base64 encoded? " + (base64encoded != null) + " " + (base64encoded != null && base64encoded.isEmpty()));
                if (base64encoded != null && !base64encoded.isEmpty()) {
                    if (filepath.exists()) {
                        addURItoQueue(mediaController, fileUri);
//                        initVoicemailPlayer(mediaController);
                        errorHandler.accept(null);
                    }else{
                        errorHandler.accept(new TeleConsoleError.CustomError(400, "Unable to download voicemail"));
                    }

                }
            }, errorHandler);
        }
    }

    private Uri getFileUri(File filepath) {
        return FileProvider.getUriForFile(AppController.getInstance(), "com.telebroad.teleconsole.fileprovider", filepath);
    }

//    protected void addItemToQueue(MediaControllerCompat mediaController, File filepath) {
//        addURItoQueue(mediaController, fileUri);
//    }

    @WorkerThread
    private void addURItoQueue(MediaControllerCompat mediaController, Uri fileUri) {
        mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                .setMediaUri(fileUri)
                .setDescription("Voicemail")
                .setTitle("Voicemail")
                .setSubtitle(getOtherNumber().getNameBackground())
                .build();
        mediaController.addQueueItem(mediaDescriptionCompat);
        startPlaying();
    }
    @WorkerThread
    public void addURItoQueueChat(MediaControllerCompat mediaController, Uri fileUri, String name, Bitmap bitmap) {
       // if (mediaDescriptionCompat == null || !mediaDescriptionCompat.getMediaUri().equals(fileUri)) {
        //MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
            mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                    .setMediaUri(fileUri)
                    //.setm
                    .setDescription("Chat Voice note")
                    .setTitle("Chat Voice note")
                    .setSubtitle(name)
                    .setIconBitmap(bitmap)
                    .build();
            mediaController.addQueueItem(mediaDescriptionCompat);
        //}
    }
    @WorkerThread
    public void addURItoQueueChat(MediaControllerCompat mediaController, Uri fileUri) {
        mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                .setMediaUri(fileUri)
                .setDescription("Chat Voice note")
                .setTitle("Chat Voice note")
                .build();
        mediaController.addQueueItem(mediaDescriptionCompat);
    }
    @WorkerThread
    public void addURItoQueueSms(MediaControllerCompat mediaController, Uri fileUri,String num) {
        mediaDescriptionCompat = new MediaDescriptionCompat.Builder()
                .setMediaUri(fileUri)
                .setDescription(num)
                .setSubtitle(num)
                .setTitle("SMS Voice note")
                .build();
        mediaController.addQueueItem(mediaDescriptionCompat);
        //}
    }

    public File getFile() {
        String root = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Voicemails";
        File filedirs = new File(root);
        return new File(filedirs.getAbsoluteFile() + File.separator + getItem().getName() + ".wav");
    }

    public boolean isDownloaded() {
        return getFile().exists();
    }

    public void startPlaying() {
        progressHandler.post(updateProgress);
    }

    public void pausePlaying() {
        progressHandler.removeCallbacks(updateProgress);
    }

    public void saveVoicemail() {
        getItem().saveVoicemail();
    }

    private void initVoicemailPlayer(MediaControllerCompat mediaController) throws IOException {
//        voicemailPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build());
//        try {
//            voicemailPlayer.prepare();
//        }catch (IllegalStateException ise){
//            downloadVoicemail(mediaController);
//        }
//        catch (IOException ioe){
//
//        }
//        AudioManager am = (AudioManager) AppController.getInstance().getSystemService(Context.AUDIO_SERVICE);
//        if(am == null){
//            return;
//        }
//        wasSpeakerphone = am.isSpeakerphoneOn();
//        am.setSpeakerphoneOn(true);
    }


    @Override
    public PhoneNumber findMyNumber() {
        return PhoneNumber.getPhoneNumber(getItem().getMailbox());
    }

    @Override
    public boolean isNew() {
        return nullToEmpty(getItem().getRead_by()).isEmpty();
    }

    @Override
    public int getIconResource() {
        return R.drawable.ic_voicemail;
    }

    @Override
    public String getInfo() {
        return "(" + getFormattedDuration() + ")";
    }

    @Override
    public void deleteItem() {
        File voicemailFile = getFile();
        if (voicemailFile.exists()){
            voicemailFile.delete();
        }
        new VoicemailRepository(AppController.getInstance()).deleteVoicemail(getItem());
    }

    @Override
    public int getIconBackgroundResource() {
        return R.drawable.bg_voicemail_icon;
    }

    //    public String getFileName(){
//        return getItem().getName();
//    }
//    public String getDir(){
//        return getItem().getDir() == Message.Direction.IN ? "INBOX" : "SENT";
//    }
//    public String getFormattedPosition(){
//        return Utils.formatMilliSeconds(voicemailPlayer.getCurrentPosition());
//    }
    public String getFormattedDuration() {
        return Utils.formatSeconds(getItem().getDuration());
//        SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss", Locale.getDefault());
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//        Date date = new Date(getItem().getDuration() * 1000);
//        return dateFormat.format(date);
    }

    public String getMailbox() {
        return PhoneNumber.getPhoneNumber(getItem().getMailbox()).formatted();
    }

    public void checkIfNeedToLoadMore() {
        VoicemailRepository.getInstance().checkIfNeedToLoadMore(getItem().getTimestamp());
    }

    public void removeQueueItem(MediaControllerCompat controller) {
        if (mediaDescriptionCompat != null){
            controller.removeQueueItem(mediaDescriptionCompat);
        }
    }
}
