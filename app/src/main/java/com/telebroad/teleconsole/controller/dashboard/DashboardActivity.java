package com.telebroad.teleconsole.controller.dashboard;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.telebroad.teleconsole.BuildConfig;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.GetMessage;
import com.telebroad.teleconsole.chat.client.JoinTopicModel;
import com.telebroad.teleconsole.chat.client.setMassage;
import com.telebroad.teleconsole.chat.viewModels.ChatViewModel;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.EditProfileActivity;
import com.telebroad.teleconsole.controller.FoldersActivity;
import com.telebroad.teleconsole.controller.NewFaxActivity;
import com.telebroad.teleconsole.controller.NewTextActivity;
import com.telebroad.teleconsole.controller.login.SignInActivity;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.db.models.Replies;
import com.telebroad.teleconsole.helpers.IntentHelper;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PubnubInfo;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.Settings;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import com.telebroad.teleconsole.model.db.TeleConsoleDatabase;
import com.telebroad.teleconsole.notification.HistoryGroupNotification;
import com.telebroad.teleconsole.notification.NotificationBuilder;
import com.telebroad.teleconsole.pjsip.SipService;
import com.telebroad.teleconsole.viewmodels.ContactViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.app.NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED;
import static android.content.Intent.ACTION_SEND;
import static com.android.volley.Request.Method.PUT;
import static com.telebroad.teleconsole.helpers.IntentHelper.NUMBER_TO_CALL;
import static com.telebroad.teleconsole.helpers.IntentHelper.TAB_TO_OPEN;
import static com.telebroad.teleconsole.helpers.SettingsHelper.DIRECT_CALLS_ONLY;
import static com.telebroad.teleconsole.helpers.SettingsHelper.DO_NOT_DISTURB;
import static com.telebroad.teleconsole.helpers.URLHelper.READ_URL;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private ViewPager fragmentContainer;
    private ContactViewModel contacts;
    private TextView nameInfo, phoneInfo, statusInfo;
    private SwitchCompat doNotDisturbSwitch;
    private SwitchCompat directCallSwitch;
    private TextView doNotDisturbBanner;
    private NavigationView navigationView;
    private ImageView profileImageView;
    private int customTabToOpen = -1;
    public int currentTab;
    private final Gson gson = new Gson();
    private final BroadcastReceiver dndListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDND_UI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //android.util.Log.d("Layout", "Creating Dashboard Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        contacts = new ViewModelProvider.AndroidViewModelFactory(AppController.getInstance()).create(ContactViewModel.class);
        contacts.getActiveContacts((LifecycleOwner & ViewModelStoreOwner) this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PubnubInfo.fetchPubnubInfo();
        customTabToOpen = getIntent().getIntExtra(TAB_TO_OPEN, -1);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        setupViewPager();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        AppBarLayout appBar = findViewById(R.id.dashboard_appbar);
        doNotDisturbBanner = findViewById(R.id.dnd_banner);
        doNotDisturbBanner.setOnClickListener(v -> {
            boolean isPhoneDnd = Utils.isPhoneDND();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setTitle("Turn off Do not Disturb")
                    .setMessage(isPhoneDnd ? "Your phone is in Do not Disturb mode, do you want to turn it off? " : "Do you want to turn off Do Not Disturb?")
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());

            AlertDialog alert;
            if (SettingsHelper.getBoolean(DO_NOT_DISTURB, false)) {
                alert = builder.setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    SettingsHelper.putBoolean(DO_NOT_DISTURB, false);
                    updateDND_UI();
                })).create();
            } else if (isPhoneDnd) {
                alert = builder.setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    startActivity(new Intent(android.provider.Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS));
                })).create();
            } else {
                alert = builder.create();
            }
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
//            AlertDialog alert = new MaterialAlertDialogBuilder(this).
//                    setTitle("Turn off Do not Disturb").
//                    setMessage( isPhoneDnd ? "Your phone is in Do not Disturb mode, do you want to turn it off? " : "Do you want to turn off Do Not Disturb?").
////                    setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> {
////                        if (isPhoneDnd){
////                        }else {
////                            SettingsHelper.putBoolean(DO_NOT_DISTURB, false);
////                            updateDND_UI();
////                        }
////                    }).
//                    setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
//            if (SettingsHelper.getBoolean(DO_NOT_DISTURB, false)){
//                alert.setPositiveButton(android.R.string.ok, ((dialog, which) -> {
//                    SettingsHelper.putBoolean(DO_NOT_DISTURB, false);
//                    updateDND_UI();
//                }));
//            }
//            if (isPhoneDnd){
//                alert.setPositiveButton(android.R.string.ok, ((dialog, which) -> {
//                    startActivity(new Intent(android.provider.Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS));
//                }));
//            }
//            alert.create();
        });
        setupNavHeader();
        bottomNavigationView = findViewById(R.id.navigation);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager.isPowerSaveMode()) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.WHITE);
            bottomNavigationView.setItemIconTintList(colorStateList);
            bottomNavigationView.setItemTextColor(colorStateList);
        }else {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.BLACK);
            bottomNavigationView.setItemIconTintList(colorStateList);
            bottomNavigationView.setItemTextColor(colorStateList);
        }
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.WHITE);
            bottomNavigationView.setItemIconTintList(colorStateList);
            bottomNavigationView.setItemTextColor(colorStateList);
            bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.barColor,null));
        }else {
            int colorInt = Color.parseColor("#263238");
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked},
                            new int[]{android.R.attr.state_checked}
                    },
                    new int[]{Color.parseColor("#68615F"), colorInt}
            );
            bottomNavigationView.setItemIconTintList(colorStateList);
        }
        //bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_messages:
                    fragmentContainer.setCurrentItem(0);
                    break;
                case R.id.navigation_callhistory:
                    fragmentContainer.setCurrentItem(1);
                    break;
                case R.id.navigation_contacts:
                    fragmentContainer.setCurrentItem(2);
                    break;
                case R.id.navigation_dialpad:
                    fragmentContainer.setCurrentItem(3);
                    break;
                case R.id.navigation_settings:
                    fragmentContainer.setCurrentItem(4);
                    break;
                default:
                    fragmentContainer.setCurrentItem(3);
                    break;
            }
            return false;
        });
    }

    private void handleCallBackIntent(Intent intent) {
        String phoneNumber = intent.getStringExtra(NUMBER_TO_CALL);
        if (phoneNumber != null && !phoneNumber.isEmpty()){
            SipManager.getInstance(getApplicationContext()).call(phoneNumber, this);
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            int notificationID = intent.getIntExtra(IntentHelper.NOTIFICATION_ID, -1);
            if (notificationID != -1){
                AsyncTask.execute(() -> TeleConsoleDatabase.getInstance(DashboardActivity.this).callHistoryDao().setAsNotified(phoneNumber));
                manager.cancel(notificationID);
            }
            intent.putExtra(NUMBER_TO_CALL, (String) null);
        }
    }

    private void updateSoftInputMode(int position) {
        if (getWindow() != null){
            getWindow().setSoftInputMode(position == 3 ? WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING : WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null){
            return;
        }
        Activity activity = this;
        if (Utils.checkLoggedOut(intent, activity)) return;
        // If it is a share action, and it is an image
        if (intent.getAction() == Intent.ACTION_SEND && intent.getType() != null && intent.getType().contains("image")){
            Bundle bundle = intent.getExtras();
            AlertDialog alert = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.share_dialog_title)
                    .setItems(new String[]{ "MMS message", "Fax","Chat"}, (dialog, which) -> {
                        switch (which){
                            case 1:
                                //Log.d("FAX02", "Fax clicked data is " + intent.getData());
                                NewFaxActivity.showNewFaxActivity(this, (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
                                break;
                            case 0:
                                Intent newTextIntent = new Intent(this, NewTextActivity.class);
                                Utils.copyShareIntent(intent, newTextIntent);
                                startActivity(newTextIntent);
                                break;
                            case 2:
                                if (ChatWebSocket.isConnected) {
                                    ListenableFuture<List<ChannelDB>> allChannels = ChatDatabase.getInstance().channelDao().getAllChannels();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        Futures.addCallback(allChannels, new FutureCallback<List<ChannelDB>>() {
                                            @Override
                                            public void onSuccess(List<ChannelDB> channelDBS) {
                                                if (channelDBS != null){
                                                    String list = gson.toJson(channelDBS);
                                                    Intent newChatIntent = new Intent(DashboardActivity.this,JoinTopicActivity.class);
                                                    newChatIntent.putExtra(Intent.EXTRA_STREAM, (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
                                                    newChatIntent.putExtra("shareList",list);
                                                    startActivity(newChatIntent);
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {}
                                        },this.getMainExecutor());
                                    }
                                }else {
                                    ChatWebSocket.getInstance().connect();
                                    new CountDownTimer(300, 100) {
                                        public void onTick(long millisUntilFinished) {}
                                        public void onFinish() {
                                            ListenableFuture<List<ChannelDB>> allChannels = ChatDatabase.getInstance().channelDao().getAllChannels();
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                Futures.addCallback(allChannels, new FutureCallback<List<ChannelDB>>() {
                                                    @Override
                                                    public void onSuccess(List<ChannelDB> channelDBS) {
                                                        if (channelDBS != null){
                                                            String list = gson.toJson(channelDBS);
                                                            Intent newChatIntent = new Intent(DashboardActivity.this,JoinTopicActivity.class);
                                                            newChatIntent.putExtra(Intent.EXTRA_STREAM, (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
                                                            newChatIntent.putExtra("shareList",list);
                                                           // Log.d("ActivityStart", "Starting activity from [specific location in your code]");
                                                            startActivity(newChatIntent);
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable t) {}
                                                },DashboardActivity.this.getMainExecutor());
                                            }
                                        }
                                    }.start();
                                }
                                break;
                            default:
                                break;
                        }
                    })
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                    .create();
            alert.setOnShowListener(dialog -> {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();
            //NewFaxActivity.showNewFaxActivity(this, (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
            intent.setType(null);
        }
        if (intent.getData() != null && intent.getData().getScheme().equals("tel")){
            handleCallIntent(intent);
        }
        handleCallBackIntent(intent);
    }

    private void setupNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        doNotDisturbSwitch = navigationView.getMenu().findItem(R.id.nav_DND).getActionView().findViewById(R.id.menuSwitch);
        directCallSwitch = navigationView.getMenu().findItem(R.id.nav_direct_calls).getActionView().findViewById(R.id.menuSwitch);
//        navigationView.getMenu().findItem(R.id.nav_documents).setVisible(Build.VERSION.SDK_INT < Build.VERSION_CODES.R);
       // android.util.Log.d("NullCheck0006", doNotDisturbSwitch + "");
        doNotDisturbSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.putBoolean(DO_NOT_DISTURB, isChecked);
            updateDND_UI();
        });
        updateDND_UI();
//        doNotDisturbSwitch.setChecked(SettingsHelper.getBoolean(DO_NOT_DISTURB, false));
        directCallSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.putBoolean(DIRECT_CALLS_ONLY, isChecked);
        });
        directCallSwitch.setChecked(SettingsHelper.getBoolean(DIRECT_CALLS_ONLY, false));
        nameInfo = headerView.findViewById(R.id.name_info);
        phoneInfo = headerView.findViewById(R.id.line_info);
        statusInfo = headerView.findViewById(R.id.status_info);
        profileImageView = headerView.findViewById(R.id.profileImageView);
        View logout = navigationView.findViewById(R.id.nav_logout);
        //logout.setText(getString(R.string.logout_with_param, SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME, "")));
        //setObservations();
        statusInfo.setOnClickListener(click -> {
            if (SipManager.isRegistered.getValue() != null && SipManager.isRegistered.getValue()) {
                SipService service = SipService.getInstance();
                if (service != null){
                    service.stopSelf();
                }
                //SipManager.getInstance().deregister();
            } else {
                SipManager.getInstance().updateUser(true);
            }
        });
        //Utils.updateLiveData(SipManager.isRegistered, null);
    }

    private void setObservations() {
        TeleConsoleProfile.getLiveInstance().observe(this, profile -> {
            if (profile == null){
                //android.util.Log.d("ProfileError", "profile is null");
                //android.util.Log.d("ProfileError", "Get Profile Instance " + TeleConsoleProfile.getInstance());
                return;
            }
            nameInfo.setText(profile.getFullName());
            if (profile.getPhoto() != null && !profile.getPhoto().isEmpty()){
                Glide.with(this).load(profile.getPhoto()).circleCrop().into(profileImageView);
            }
        });
        Settings.getLiveInstance().observe(this, settings -> {
            if (settings == null){
                return;
            }
            String extension = settings.getPhoneLine() == null || settings.getPhoneLine().getFcode() == null || settings.getPhoneLine().getFcode().equalsIgnoreCase("null") ? "" : getString(R.string.ext) + " " + settings.getPhoneLine().getCallerIDint() + " ";
            String phoneLine = settings.getPhoneLine() == null || settings.getPhoneLine().getName() == null ? "" : getString(R.string.sip) + " " +  settings.getPhoneLine().getName();
            String phoneInfoText = extension + phoneLine;
            phoneInfo.setText(phoneInfoText);
        });
        SipManager.isRegistered.observe(this, registered -> {
            if (registered == null){
                registered = false;
            }
            if (registered){
                statusInfo.setTextColor(0x8000FF00);
                statusInfo.setText(R.string.registered);
            }else{
                statusInfo.setTextColor(0xA0FF0000);
                statusInfo.setText(R.string.not_registered);
            }
        });
    }

    private void setupViewPager(){
        fragmentContainer = findViewById(R.id.fragment_container);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(MessagesFragment.newInstance());
        adapter.addFragment(CallHistoryFragment.newInstance());
        adapter.addFragment(ContactFragment.newInstance());
        if (BuildConfig.DEBUG){
            adapter.addFragment(new ChatFragment());
        }else {
            if (!DialPadFragment.getInstance().isAdded()){
               adapter.addFragment(DialPadFragment.getInstance());
            }
        }
        adapter.addFragment(SettingsFragment.newInstance());
        fragmentContainer.setAdapter(adapter);
        fragmentContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               // android.util.Log.d("SoftInput", "page selected");
                // Closes keyboard if it's open
                updateSoftInputMode(position);
                // Checks for the old tab
                int oldTab = SettingsHelper.getInt(SettingsHelper.LAST_SELECTED_TAB, 0);
                // Saves the current tab as the last tab
                SettingsHelper.putInt(SettingsHelper.LAST_SELECTED_TAB, position);
                // If the old tab is a valid tab
                if (oldTab >= 0 && oldTab <= 5){
                    // Tell the bottom navigation view that the old tab is no longer selected
                    bottomNavigationView.getMenu().getItem(oldTab).setChecked(false);
                }
                // Check the new tab
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                Fragment frag = adapter.getItem(position);
                if (frag instanceof ScrollableFragment){
                    ScrollableFragment scrollableFragment = (ScrollableFragment) frag;
                    scrollableFragment.scrollToTop();
                }
                currentTab = position;
                // If we open the callHistory tab
                if (position == 1){
                    AsyncTask.execute(() -> {
                        // Mark all as read, and remove missed call notifications
                        List<HistoryGroupNotification> groups = TeleConsoleDatabase.getInstance(DashboardActivity.this).callHistoryDao().getAllNeedingNotification();
                        //android.util.Log.d("HistoryGroupNotif", "Number of groups " + groups);
                        for (HistoryGroupNotification group : groups) {
                            NotificationBuilder.getInstance().dismissNotification(group.snumber.hashCode());
                            TeleConsoleDatabase.getInstance(DashboardActivity.this).callHistoryDao().setAllAsNotified();
                        }
                    });
                }
                if(getSupportActionBar() != null){
                    getSupportActionBar().setTitle(getResources().getStringArray(R.array.fragment_titles)[position]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDND_UI();
        int tabToOpen = customTabToOpen > 0 ? customTabToOpen : SettingsHelper.getInt(SettingsHelper.LAST_SELECTED_TAB, 3);
        setObservations();
        handleIntent(getIntent());
        FirebaseCrashlytics.getInstance().setCustomKey("username", SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME));
        fragmentContainer.setCurrentItem(tabToOpen);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(getResources().getStringArray(R.array.fragment_titles)[tabToOpen]);
        }
        IntentFilter dndFilter = new IntentFilter();
        dndFilter.addAction(ACTION_INTERRUPTION_FILTER_CHANGED);
        registerReceiver(this.dndListener, dndFilter);
//        try{
//            startService(new Intent(Intent.ACTION_MAIN).setClass(getApplicationContext(), ShutdownService.class));}
//        catch (IllegalStateException isEx){
//            Bugfender.e("ServiceError", "App in background in on resume");
//        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(dndListener);
        super.onPause();
    }

    private void handleCallIntent(Intent intent) {
        SipManager.getInstance().call(intent.getData().toString(), this);
        //android.util.Log.d("ACTION01", "data string " + intent.getDataString());
        intent.setData(null);
        //finish();
    }

    @Override
    public void onBackPressed() {
       // android.util.Log.d("Dashboard", "Back pressed");
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        PagerAdapter adapter = fragmentContainer.getAdapter();
//        Fragment currentFrag = null;
//        if (adapter instanceof ViewPagerAdapter){
//            currentFrag = ((ViewPagerAdapter)adapter).getItem(fragmentContainer.getCurrentItem());
//        }
//        if (currentFrag != null){
//            currentFrag.onActivityResult(requestCode, resultCode, data);
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void logout(View view) {
        TeleConsoleProfile.logout(getApplicationContext());
        Intent reloginIntent = new Intent(getApplicationContext(), SignInActivity.class);
        reloginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(reloginIntent);
    }


    private void updateDND_UI(){
        boolean newDNDState = SettingsHelper.getBoolean(DO_NOT_DISTURB, false);
        doNotDisturbSwitch.setChecked(newDNDState);
        boolean phoneDND = Utils.isPhoneDND();
        if (phoneDND){
            doNotDisturbBanner.setText(R.string.do_not_disturb_phone_banner);
        }
        if (newDNDState){
            doNotDisturbBanner.setText(R.string.do_not_disturb_teleconsole_banner);
        }
        doNotDisturbBanner.setVisibility(newDNDState || Utils.isPhoneDND() ? View.VISIBLE : View.GONE);
//        doNotDisturbBanner.animate().translationY(newDNDState ? 0 : doNotDisturbBanner.getHeight()).alpha(newDNDState ? 1f : 0f).start();
//        doNotDisturbBanner.animate().alpha(newDNDState ? 1.0f : 0f).setListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                doNotDisturbBanner.setVisibility(newDNDState ? View.VISIBLE : View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//                doNotDisturbBanner.setVisibility(newDNDState ? View.VISIBLE : View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//            }
//        }).start();
//        doNotDisturbBanner.setVisibility(newDNDState ? View.VISIBLE : View.GONE);
//        doNotDisturbBanner.setAlpha(newDNDState ? 1.0f : 0f);
//        doNotDisturbBanner.requestLayout();
//        doNotDisturbBanner.forceLayout();
//        doNotDisturbBanner.invalidate();
//        doNotDisturbBanner.postInvalidate();
//        doNotDisturbBanner.requestLayout();
//        doNotDisturbBanner.invalidate();
//        appBar.requestLayout();
//        appBar.invalidate();
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (item.getItemId()){
            case R.id.nav_edit_profile:
                startActivity(new Intent(this, EditProfileActivity.class));
                break;
            case R.id.nav_DND:
                boolean newDNDState = !doNotDisturbSwitch.isChecked();
                SettingsHelper.putBoolean(DO_NOT_DISTURB, newDNDState);
                runOnUiThread(() -> updateDND_UI());
                return true;
            case R.id.nav_direct_calls:
                boolean newDirectCallsState = !directCallSwitch.isChecked();
                SettingsHelper.putBoolean(DIRECT_CALLS_ONLY, newDirectCallsState);
                directCallSwitch.setChecked(newDirectCallsState);
                return true;
            case R.id.nav_documents:
                File teleconsoleFile = new File(Utils.getRootFolder().getAbsolutePath(),"TeleConsole");
                Uri fileURI = Uri.parse(teleconsoleFile.getAbsolutePath());
                Intent intent = new Intent();
                intent.setDataAndType(fileURI, "resource/folder");
                Intent i = new Intent(this, FoldersActivity.class);
                startActivity(i);
//                if (intent.resolveActivity(getPackageManager()) != null){
//                    startActivity(intent);
//                }else{
//                    Intent playStoreIntent = new Intent(ACTION_VIEW);
//                    playStoreIntent.setData(Uri.parse("https://play.google.com/store/search?q=file explorer" ));
//                    playStoreIntent.setPackage("com.android.vending");
//                    if (intent.resolveActivity(getPackageManager()) != null){
//                        startActivity(playStoreIntent);
//                    }else{
//                        Toast.makeText(this, "You don't have a file explorer installed, please install one", Toast.LENGTH_LONG).show();
//                    }
//                }
                break;
            case R.id.nav_read:
                URLHelper.request(PUT, READ_URL, new HashMap<>(), result -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.mark_all_read_toast, Toast.LENGTH_SHORT).show();
                    });

                    }, error -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.mark_all_read_error_toast, Toast.LENGTH_SHORT).show();
                    });
                });
                break;
            case R.id.uploadLogs:
                URLHelper.uploadLogs(Utils.getLogFile(getApplicationContext()), "User_Uploaded_");
                Toast.makeText(this, R.string.logs_uploaded, Toast.LENGTH_SHORT).show();
//                throw new RuntimeException("Testing upload with crashlyitcs");
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                runOnUiThread(() -> updateDND_UI());
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.asyncTask(() -> ChatDatabase.getInstance().reactionsDao().deleteAll());
        Utils.asyncTask(() -> ChatDatabase.getInstance().chatMessageDao().deleteAll());
        Utils.asyncTask(() -> ChatDatabase.getInstance().repliesDao().deleteAll());
    }
}
