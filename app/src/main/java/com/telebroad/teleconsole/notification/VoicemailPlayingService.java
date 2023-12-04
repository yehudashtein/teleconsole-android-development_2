package com.telebroad.teleconsole.notification;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.ChatActivity;
import com.telebroad.teleconsole.helpers.Utils;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;

import org.checkerframework.checker.units.qual.Speed;

public  class VoicemailPlayingService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener{
    private MediaSessionCompat mediaSession;
    private ExoPlayer player;
    public AudioManager am ;//= (AudioManager) getSystemService(AUDIO_SERVICE);
    private static final MutableLiveData<Integer> liveState = new MutableLiveData<>();

    public static LiveData<Integer> getLiveState(){
        return liveState;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "VMP");
//        Intent sessionActivityIntent = new Intent(this, ChatActivity.class);
//        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionActivityIntent, FLAG_IMMUTABLE);
//        mediaSession.setSessionActivity(sessionActivityPendingIntent);
       //  = new PlaybackStateCompat.Builder().setActions(ACTION_PLAY | ACTION_PLAY_PAUSE | ACTION_SEEK_TO);
        MediaSessionConnector mediaConnector = new MediaSessionConnector(mediaSession);
        mediaConnector.setQueueEditor(getQueueEditor());
//        mediaConnector.setControlDispatcher(new DefaultControlDispatcher() {
//            @Override
//            public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) {
//                 player.seekTo(positionMs);
//                 return true;
//            }
//        });
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
       // android.util.Log.d("mode", "Before builder " + am.getMode());
        player = new ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT,true)
                .setHandleAudioBecomingNoisy(true).setWakeMode(C.WAKE_MODE_LOCAL)
//                .setAudioAttributes(getAudioAttributes(), false)
                .build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
               // android.util.Log.d("PlaybackNotification", " playback changed " + (state == PlaybackStateCompat.STATE_PLAYING));
                Utils.updateLiveData(liveState, state);
                if (state == ExoPlayer.STATE_ENDED){
                    stopForeground(true);
                    stopSelf();
                    //hasEnd();
                }else if (state == ExoPlayer.STATE_READY){
                    startService(new Intent(VoicemailPlayingService.this, VoicemailPlayingService.class));
                    getNotification();
//                    startForeground(9685, getNotification());
                }
            }
        });
       // android.util.Log.d("mode", "before setting session token " + am.getMode());
        setSessionToken(mediaSession.getSessionToken());
       // android.util.Log.d("mode", "Before connection " + am.getMode());
        mediaConnector.setPlayer(player);
      //  android.util.Log.d("mode", "Before finishing " + am.getMode());
    }
//    private AudioAttributes getAudioAttributes() {
//        return new AudioAttributes.Builder()
//                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
//                .setUsage(C.USAGE_VOICE_COMMUNICATION)
//                .build();
//    }
    private MediaDescriptionCompat mediaDescription;
    private MediaSessionConnector.QueueEditor getQueueEditor() {
        return new MediaSessionConnector.QueueEditor() {
            @Override
            public boolean onCommand(Player player, String command, @Nullable Bundle extras, @Nullable ResultReceiver cb) {
                float speed = extras.getFloat("SpeedProgress");
                PlaybackParameters param = new PlaybackParameters(speed);
                player.setPlaybackParameters(param);
                player.setPlaybackSpeed(speed);
                return false;
            }

            @Override
            public void onAddQueueItem(Player player, MediaDescriptionCompat description) {
                //android.util.Log.d("Queue01", description.getMediaUri().toString());
               // android.util.Log.d("mode", "Before checking description " + am.getMode());
//                am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                if (mediaDescription != null){
                    player.clearMediaItems();
                }
             //   android.util.Log.d("Queue01", "setting item");
                mediaDescription = description;
                //android.util.Log.d("mode", "Before setting meta " + am.getMode());
                MediaItem mediaItem = new MediaItem.Builder().setMediaMetadata( new MediaMetadata.Builder().setTitle(description.getTitle().toString()).build())
                        .setUri(description.getMediaUri()).build();
                if (mediaItem.equals(player.getCurrentMediaItem())){
                    return;
                }
//                VoicemailPlayingService.this.getMainExecutor().execute(() -> {
//                    android.util.Log.d("mode", "Before setting media item " + am.getMode() + " player " + VoicemailPlayingService.this.player.getAudioStreamType());
                    player.setMediaItem(mediaItem);
                player.seekTo( C.TIME_UNSET);
//                    android.util.Log.d("mode", "Before preparing " + am.getMode() + " player " + VoicemailPlayingService.this.player.getAudioStreamType());
                    player.prepare();
                    if (player instanceof ExoPlayer){
                        //((SimpleExoPlayer) player).setAudioAttributes(getAudioAttributes());
                    }
//                    android.util.Log.d("mode", " player type " + player + " player " + VoicemailPlayingService.this.player.getAudioStreamType());
                   // android.util.Log.d("mode", "after preparing " + am.getMode());
//                });
            }

            @Override
            public void onAddQueueItem(Player player, MediaDescriptionCompat description, int index) {
            }

            @Override
            public void onRemoveQueueItem(Player player, MediaDescriptionCompat description) {
                //android.util.Log.d("mode", "before stopping " + am.getMode());
                player.stop();
                //android.util.Log.d("mode", "after stopping " + am.getMode());
                mediaDescription = null;
//                player.removeMediaItem(0);
//                mediaSession.setActive(false);
            }
        };
    }

    @Override
    public void onDestroy() {
        player.release();
        mediaSession.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("no root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(new ArrayList<>());
    }
    private Notification getNotification(){

        PlayerNotificationManager.NotificationListener notificationListener = new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                if (dismissedByUser) {
                    player.stop();
                }
                stopForeground(true);
            }

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                startForeground(notificationId, notification);
            }
        };
        PlayerNotificationManager manager = new PlayerNotificationManager.Builder(this, 578, AppController.VOICEMAIL_PLAYING_CHANNEL)
                .setChannelNameResourceId(R.string.exo_controls_show)
                .setChannelDescriptionResourceId(R.string.exo_controls_cc_disabled_description)
                .setMediaDescriptionAdapter(new MediaAdapter(mediaDescription))
                .setNotificationListener(notificationListener)
                .build();
//        manager = PlayerNotificationManager
//                .createWithNotificationChannel(VoicemailPlayingService.this, AppController.VOICEMAIL_PLAYING_CHANNEL, R.string.exo_controls_show, R.string.exo_controls_cc_disabled_description, 578, new MediaAdapter(mediaDescription), notificationListener);
        manager.setMediaSessionToken(mediaSession.getSessionToken());
        manager.setUsePlayPauseActions(true);
//        manager.setUseNavigationActions(false);
//        manager.setUseNavigationActionsInCompactView(false);
        manager.setPlayer(player);
        MediaControllerCompat mediaController = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = mediaController.getMetadata();
        MediaDescriptionCompat mediaDescription = this.mediaDescription != null ? this.mediaDescription : mediaMetadata.getDescription() ;
       // android.util.Log.d("VMService", "get Notification description " + mediaDescription.toString());
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_telebroad_logo_only);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_telebroad_logo_only);
        Bitmap bitmap = drawableToBitmap(drawable);
        bitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, false);
        return new NotificationCompat.Builder(this, AppController.ACTIVE_CALL_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(mediaDescription.getIconBitmap() !=null? mediaDescription.getIconBitmap():bitmap)
                .setContentTitle(mediaDescription.getTitle())
                .setContentText(mediaDescription.getSubtitle())
                .setSubText(mediaDescription.getDescription())
                .setContentIntent(mediaController.getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(getPlayPauseAction())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)
                       //.setShowCancelButton(true)
//                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, ACTION_STOP)))
                ).build();
    }

    private NotificationCompat.Action getPlayPauseAction() {
        if (mediaSession == null || mediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) {
            return new NotificationCompat.Action(R.drawable.exo_icon_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, ACTION_PLAY));
        }else{
            return new NotificationCompat.Action(R.drawable.exo_icon_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE));
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS:
                player.pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                player.prepare();
                player.play();
                break;
        }
    }
//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        if (intent == null || intent.getAction() == null){
//            return;
//        }
//    }

    private class MediaAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {
        private final MediaDescriptionCompat description;

        private MediaAdapter(MediaDescriptionCompat description) {
            this.description = description;
        }

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return description.getTitle();
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return mediaSession.getController().getSessionActivity();
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return description.getSubtitle();
        }

        @Nullable
        @Override
        public CharSequence getCurrentSubText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return description.getIconBitmap();
        }
    }
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
