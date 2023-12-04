package com.telebroad.teleconsole;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.pjsip.SipManager;

import androidx.annotation.NonNull;

public class NetworkChangeCallBack extends ConnectivityManager.NetworkCallback {

    private static NetworkChangeCallBack instance;

    private final ConnectivityManager connectivityManager;

    private NetworkChangeCallBack() {
        connectivityManager = (ConnectivityManager) AppController.getInstance().
                getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void register() {
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    //.addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
//                    .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                    //.addTransportType(NetworkCapabilities.TRANSPORT_LOWPAN)
//                    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                    .build(), this);
        }
    }

    public void deregister(){
        connectivityManager.unregisterNetworkCallback(this);
    }
    @NonNull
    public static NetworkChangeCallBack getInstance() {
        if (instance == null) {
            instance = new NetworkChangeCallBack();
        }
        return instance;
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        SipManager.getInstance().updateNetworkReachability();
        Utils.logToFile("Network Changed: capabilities " + networkCapabilities);
    }

    @Override
    public void onLinkPropertiesChanged(@NonNull Network network, LinkProperties linkProperties) {
        SipManager.getInstance().updateNetworkReachability();
        Utils.logToFile("Network Changed: link properties " + linkProperties.toString());
       // android.util.Log.d("Network01", "link properties " + linkProperties.toString());
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
//        SipManager.getInstance().updateNetworkReachability();
       // android.util.Log.d("Network01", "Losing ");
    }

    @Override
    public void onLost(@NonNull Network network) {
        SipManager.getInstance().updateNetworkReachability();
       // android.util.Log.d("Network01", "Lost " );
    }

    @Override
    public void onUnavailable() {
       // android.util.Log.d("Network01", "Unavailable ");
//        SipManager.getInstance().updateNetworkReachability();
    }


}
