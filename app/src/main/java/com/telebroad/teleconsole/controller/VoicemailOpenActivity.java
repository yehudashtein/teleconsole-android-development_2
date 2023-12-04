package com.telebroad.teleconsole.controller;

import android.annotation.SuppressLint;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.notification.VoicemailPlayingService;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.Voicemail;
import com.telebroad.teleconsole.model.repositories.VoicemailRepository;
import com.telebroad.teleconsole.viewmodels.VoicemailViewModel;
import java.util.List;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.USE_SIP;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.media.session.PlaybackState.STATE_PAUSED;
import static android.media.session.PlaybackState.STATE_PLAYING;
import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.telebroad.teleconsole.helpers.IntentHelper.MESSAGE_ID;
import static com.telebroad.teleconsole.helpers.IntentHelper.MESSAGE_TIME;


public class VoicemailOpenActivity extends AppCompatActivity {
    private VoicemailViewModel voicemailViewModel;
    private TextView mailboxView, nameView, dateView, durationView, transcriptionTextView;
    private SeekBar voicemailProgress;
    private ProgressBar loadingProgress;
    private CardView transcriptionCard;
    private FloatingActionButton playButton;
    private ImageView speakerButton;
    private int duration;
    private MediaBrowserCompat mediaBrowser;
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
           // android.util.Log.d("mode", "Playback state changed " + am.getMode() + " state " + state.getState());
            if (state.getState() != PlaybackStateCompat.STATE_BUFFERING){
                playButton.setEnabled(true);
                canSeek = true;
                loadingProgress.setVisibility(View.INVISIBLE);
            }
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING){
                voicemailViewModel.startPlaying();
            }else{
                voicemailViewModel.pausePlaying();
            }
            handlePBstate(state);
        }

        @Override
        public void onSessionDestroyed() {
            mediaBrowser.disconnect();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            handleMetaData(metadata);
        }
    };

    private void handlePBstate(PlaybackStateCompat state) {
        if (playButton != null)
        playButton.setImageResource(state.getState() == PlaybackStateCompat.STATE_PLAYING ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void handleMetaData(MediaMetadataCompat metadata) {
        duration = (int) metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        //android.util.Log.d("Dur", "Duration is " + duration);
        if (duration >  0) voicemailProgress.setMax(duration);
    }

    int contactCount = 0;
   private List<? extends Contact> matchedContacts;
    // A flag to keep track of weather the speakerphone was on when we started, so that we can restore it when we are done
    private final VoicemailRepository repository = new VoicemailRepository(AppController.getInstance());
    private boolean canSeek = false;
    private static final int EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 0;
    private static final int PHONE_PERMISSIONS_REQUEST = 1;
    private AudioManager am;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicemail_open);
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        voicemailViewModel = new VoicemailViewModel(); //ViewModelProviders.of(this).get(VoicemailViewModel.class);
        if (getIntent() == null || getIntent().getExtras() == null){
            return;
        }
        String id = getIntent().getExtras().getString(MESSAGE_ID, "");
        long time = getIntent().getExtras().getLong(MESSAGE_TIME, 0);
        if (id.isEmpty() && time == 0) {
            // We have no unique way to find the voicemail as id is empty and time is 0
            return;
        }
        mailboxView = findViewById(R.id.mailboxView);
        nameView = findViewById(R.id.nameView);
        dateView = findViewById(R.id.timeView);
        voicemailProgress = findViewById(R.id.voicemailProgress);
        durationView = findViewById(R.id.durationView);
        playButton = findViewById(R.id.playButton);
        loadingProgress = findViewById(R.id.loadingProgress);
        transcriptionCard = findViewById(R.id.transcriptionCardView);
        transcriptionTextView = findViewById(R.id.transcriptionTextView);
        playButton.setEnabled(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.white)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_round_arrow_back);
        }
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        speakerButton = findViewById(R.id.speakerButton);
        speakerButton.setOnClickListener((v) -> {
            AudioManager am = (AudioManager) this.getSystemService(AUDIO_SERVICE);
            if (am == null) {
                return;
            }
            //android.util.Log.d("mode", am.getMode() + "");
            am.setMode(AudioManager.MODE_IN_COMMUNICATION);
            //android.util.Log.d("mode", am.getMode() + "");
            boolean turnOn = v.isSelected();
            v.setSelected(!turnOn);
            speakerButton.setImageResource(turnOn ? R.drawable.ic_speaker_off : R.drawable.ic_voicemail_speaker);
            am.setSpeakerphoneOn(turnOn);
        });
        if (id.isEmpty()) {
            repository.getVoicemailByTime(time).observe(this, this::displayVoicemail);
        } else {
            repository.getVoicemail(id).observe(this, (voicemail) -> {
               // android.util.Log.d("Voicemail02", " get voicemail changed");
                this.displayVoicemail(voicemail);
            });
        }
        voicemailProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                   // android.util.Log.d("SeekBar", "progress " + progress + " can seek? " + canSeek);
                    if (canSeek) {
                        MediaControllerCompat.getMediaController(VoicemailOpenActivity.this).getTransportControls().seekTo(progress);
//                        voicemailPlayer.seekTo((int) Math.round(progress / 100.0 * voicemailPlayer.getDuration()));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
//
    @Override
    protected void onResume() {
        super.onResume();
        downloadVoicemail();
    }

    private void displayVoicemail(Voicemail voicemail) {
        if (voicemail != null) {
            if (voicemail.isNeedsNotification()) {
                AsyncTask.execute(() -> repository.setAsNotified(voicemail.getTimestamp()));
            }
            voicemailViewModel.setItem(voicemail);
            downloadVoicemail();
            voicemailViewModel.getOtherNumber().getMatchedContacts().observe(this, contacts -> {
                contactCount = contacts == null ? 0 : contacts.size();
                matchedContacts = contacts;
                invalidateOptionsMenu();
            });
           // android.util.Log.d("Voicemail02", " download voicemail");
            voicemailViewModel.getOtherName().observe(this, (name) -> {
                if (name == null || name.isEmpty() || name.equals(voicemailViewModel.getOtherNumber().formatted())) {
                    nameView.setText(voicemailViewModel.getOtherNumber().formatted());
                } else {
                    nameView.setText(name + " - " + voicemailViewModel.getOtherNumber().formatted());
                }
            });
            mailboxView.setText("Mailbox " + voicemailViewModel.getMailbox());
            dateView.setText(voicemailViewModel.getFullDate());
            durationView.setText(voicemailViewModel.getFormattedDuration());
            playButton.setOnClickListener(fab -> {
                changePlayback();
            });
            if (voicemail.getTranscription() != null && !voicemail.getTranscription().isEmpty()){
                transcriptionCard.setVisibility(View.VISIBLE);
                transcriptionTextView.setText(voicemail.getTranscription());
            }else{
                transcriptionCard.setVisibility(View.GONE);
            }
            initMediaPlayer();
           // android.util.Log.d("Voicemail02", " voicemail displayed "/*+ voicemail.toString()*/);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Display the initial state
        // Register a Callback to stay in sync
        MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                MediaControllerCompat mediaController = new MediaControllerCompat(VoicemailOpenActivity.this, token);
                MediaControllerCompat.setMediaController(VoicemailOpenActivity.this, mediaController);
                downloadVoicemail();
                // Display the initial state
                MediaMetadataCompat metadata = mediaController.getMetadata();
                PlaybackStateCompat pbState = mediaController.getPlaybackState();
               // Log.d("InitalPB ", pbState.toString());
                // Register a Callback to stay in sync
                mediaController.registerCallback(controllerCallback);
                handleMetaData(metadata);
                handlePBstate(pbState);
            }
        };
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, VoicemailPlayingService.class), connectionCallback, null);
        mediaBrowser.connect();
        downloadVoicemail();
    }
    private void downloadVoicemail(){
        if (MediaControllerCompat.getMediaController(this) != null && voicemailViewModel.getItem() != null){
            voicemailViewModel.downloadVoicemail(MediaControllerCompat.getMediaController(this), (error) -> {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.INVISIBLE);
                    if (error != null) {
                        Toast.makeText(VoicemailOpenActivity.this, error.getFullErrorMessage(), Toast.LENGTH_LONG).show();
                        Utils.logToFile("Voicemail download error, " + error);
                    } else {
                        playButton.setEnabled(true);
                        canSeek = true;
                    }
                });
            });
           // android.util.Log.d("MediaBrowserService", "We can add");
        }else{
           // android.util.Log.d("MediaBrowserService", "not ready to add, MC: " + MediaControllerCompat.getMediaController(this) + " VM " + voicemailViewModel.getItem());
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller != null) {
            if (controller.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING){
                voicemailViewModel.removeQueueItem(controller);
            }
            controller.unregisterCallback(controllerCallback);
        }
        voicemailViewModel.pausePlaying();
        mediaBrowser.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_voicemail_open, menu);
        MenuItem contactMenuItem1 = menu.findItem(R.id.viewContact);
        MenuItem contactMenuItem = menu.findItem(R.id.viewContact);
        if (contactCount > 0){
            contactMenuItem.setTitle(R.string.view_contact);
            contactMenuItem.setIcon(R.drawable.ic_person_white);
        }else{
            contactMenuItem.setTitle(R.string.add_contact);
            contactMenuItem.setIcon(R.drawable.ic_person_add);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.download) {
//            saveVoicemail(true);
//        } else
            if (item.getItemId() == R.id.delete) {
                AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.delete_voicemail).setMessage(R.string.delete_voicemail_confirmation)
            .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
                if (voicemailViewModel.getItem() == null){
                    Toast.makeText(this, "Unable to delete voicemail as it is not initialized yet", Toast.LENGTH_LONG).show();
                    return;
                }
                voicemailViewModel.deleteItem();
//                repository.deleteVoicemail(voicemailViewModel.getItem());
                finish();
            }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
                alert.setOnShowListener(dialog -> {
                    Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                    negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                });alert.show();
            return true;
        } else if (item.getItemId() == R.id.call) {
            if (AppController.getInstance().hasPermissions(RECORD_AUDIO, USE_SIP)) {
                call();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, USE_SIP}, PHONE_PERMISSIONS_REQUEST);
            }
            return true;
        }else if (item.getItemId() == R.id.text){
            if (voicemailViewModel.getItem() == null){
                Toast.makeText(this, "Unable to send text as voicemail is not yet initialized", Toast.LENGTH_LONG).show();
            }else {
                SmsConversationActivity.show(this, voicemailViewModel.getOtherNumber());
            }
            return true;
        }else if (item.getItemId() == R.id.viewContact){
            Utils.viewContact(matchedContacts, voicemailViewModel.getOtherNumber().fixed(), this);
            return true;
        }else if (item.getItemId() == R.id.share){
            if (voicemailViewModel.isDownloaded()){
                try {
                    Uri shareUri = FileProvider.getUriForFile(AppController.getInstance(), "com.telebroad.teleconsole.fileprovider", voicemailViewModel.getFile());
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    share.setDataAndType(shareUri, "audio/wav");
                    share.putExtra(Intent.EXTRA_STREAM, shareUri);
                    if (share.resolveActivity(getPackageManager()) == null) {
                        Toast.makeText(this, "You don't have any apps installed that can share audio files. Please install one from your app store", Toast.LENGTH_LONG).show();
                    } else {
                        startActivity(share);
                    }
                }catch (IllegalArgumentException iae){
                    Toast.makeText(this, "Error sharing file, please try again", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(this, "The voicemail has not been downloaded yet. Wait for the download to complete.", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void call() {
        PhoneNumber toCall = voicemailViewModel.getOtherNumber();
        if (toCall == null){
            Toast.makeText(this, "Unable to find number", Toast.LENGTH_LONG).show();
            return;
        }
        AudioManager am = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        am.setSpeakerphoneOn(false);
        SipManager.getInstance(this).call(voicemailViewModel.getOtherNumber().fixed(), this);
    }

    private void saveVoicemail(boolean shouldRequest) {
        if (AppController.getInstance().hasExternalStoragePermissions()) {
            voicemailViewModel.saveVoicemail();
        } else if (shouldRequest) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PERMISSION_GRANTED) {
                // TODO alert user of permission error
                return;
            }
        }
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSIONS_REQUEST:
                saveVoicemail(false);
                break;
            case PHONE_PERMISSIONS_REQUEST:
                call();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void initMediaPlayer() {
       // android.util.Log.d("VoicemailOpenActivity", " VoicemailPlayer initialized ");
//        voicemailPlayer.setOnPreparedListener(mp -> {
//            wabled(true);
//            loadingProgress.setVisibility(View.INVISIBLE);
//            durationView.setText(Utils.formatMilliSeconds(voicemailPlayer.getDuration()));
//            canSeek = true;
//        });
//        voicemailPlayer.setOnCompletionListener((mp) -> {
//            pausePlaying();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                voicemailProgress.setProgress(0, true);
//            } else {
//                voicemailProgress.setProgress(0);
//            }
//        });
//        voicemailPlayer.setOnTimedTextListener((mp, text) -> android.util.Log.d("TIMED", "Timed text: " + text));
        voicemailViewModel.getPlayerProgress.observe(this, progress -> {
//            android.util.Log.d("Running", "Running");
            long longProgress = MediaControllerCompat.getMediaController(this).getPlaybackState().getPosition();
           // Log.v("longProgress",String.valueOf(longProgress));
            if (progress == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                voicemailProgress.setProgress((int) longProgress , true);
            } else {
                voicemailProgress.setProgress((int) longProgress);
            }
            String curProgress = Utils.formatLongMilliSeconds(longProgress);
            int pbState = MediaControllerCompat.getMediaController(VoicemailOpenActivity.this).getPlaybackState().getState();
            if (pbState == STATE_PLAYING || pbState == STATE_PAUSED) {
                durationView.setText(curProgress + (duration > 0 ? "/" + Utils.formatLongMilliSeconds(duration) : ""));
            }else{
                durationView.setText(voicemailViewModel.getFormattedDuration());
            }
        });
    }

    void changePlayback() {
        int pbState = MediaControllerCompat.getMediaController(VoicemailOpenActivity.this).getPlaybackState().getState();
        if (pbState == PlaybackStateCompat.STATE_PLAYING){
            MediaControllerCompat.getMediaController(this).getTransportControls().pause();
            playButton.setImageResource(R.drawable.ic_play);
        }else{
            MediaControllerCompat.getMediaController(this).getTransportControls().play();
            playButton.setImageResource(R.drawable.ic_pause);
        }
//        if (voicemailPlayer.isPlaying()) {
//            voicemailPlayer.pause();
//            pausePlaying();
//        } else {
////            voicemailPlayer.start();
//            startPlaying();
//        }
    }

//    void startPlaying() {
//        voicemailViewModel.startPlaying();
//        playButton.setImageResource(R.drawable.ic_pause);
//    }
//
//    void pausePlaying() {
//        voicemailViewModel.pausePlaying();
//        playButton.setImageResource(R.drawable.ic_play);
//    }

}
