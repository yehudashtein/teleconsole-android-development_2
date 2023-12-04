package com.telebroad.teleconsole.chat.client;

import android.os.Build;
import android.widget.VideoView;

import com.google.firebase.installations.FirebaseInstallations;
import com.telebroad.teleconsole.BuildConfig;
import com.telebroad.teleconsole.chat.ChatWebSocket;

public class HiMessage {
    String id = ChatWebSocket.HI_ID;
    String ver = "0.19.3";
    String ua = "teams (Teleconsole/"+ BuildConfig.VERSION_CODE + "; Android v" + Build.VERSION.SDK_INT + "); tinodejs/0.18.1 ";
    String lang = "en-US";
    String platf = "android";
    String dev;
}

//{"hi":{"id":"78796","ver":"0.18.1","ua":"teams (Teleconsole/0.5; Win32); tinodejs/0.18.1","lang":"en-US","platf":"android"}}