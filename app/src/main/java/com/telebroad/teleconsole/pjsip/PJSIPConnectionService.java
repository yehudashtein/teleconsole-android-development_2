package com.telebroad.teleconsole.pjsip;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;

import androidx.annotation.RequiresApi;

import com.telebroad.teleconsole.controller.AppController;

import java.lang.ref.WeakReference;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PJSIPConnectionService extends ConnectionService {


    private static WeakReference<PJSIPConnectionService> instance;

    public static PJSIPConnectionService getInstance() {
        return instance.get();
    }

    @Override
    public void onCreate() {
      //  android.util.Log.d("PJSIPConnectionService", "starting with permission " + AppController.getInstance().hasPermissions(Manifest.permission.READ_CALL_LOG));
        instance = new WeakReference<>(this);
        super.onCreate();
    }


    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {

       // android.util.Log.d("PJSIPConnectionService", "Incoming call"  );
        PJSIPConnection connection = new PJSIPConnection(Uri.fromParts("tel","611", null));
        int pjsipID = request.getExtras().getInt(SipService.EXTRA_PJSIP_ID);
       // android.util.Log.d("PJSIPConnectionService", "Incoming call with id " + pjsipID );
        connection.setCallID(pjsipID);
        CallManager.getInstance().getCall(pjsipID).setConnectionID(connection.getConnectionID());
        return connection;

    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
//        android.util.Log.d("PJSIPConnectionService", "Incoming call from " + request.getAddress().getSchemeSpecificPart() + " failed "  );

        int pjsipID = request.getExtras().getInt(SipService.EXTRA_PJSIP_ID);
        PJSIPCallController pjsipCallController = new PJSIPCallController(pjsipID);
        pjsipCallController.decline();
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
    }
    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
       // android.util.Log.d("PJSIP_CONNECTION_FAILED", "Outgoing call to " + request.getAddress().getSchemeSpecificPart() + " "  );

        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
    }
    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        //android.util.Log.d("PJSIP_CONNECTION", "Outgoing call to " + request.getAddress().getSchemeSpecificPart() + " handle ID "  + connectionManagerPhoneAccount.getId() + " existing connections " + getAllConnections().size());


        PJSIPConnection connection = new PJSIPConnection(request.getAddress());
       // android.util.Log.d("Conf02", request.getExtras().toString());
        if (request.getExtras().getBoolean(SipService.EXTRA_IS_CONFERENCE)){
           // android.util.Log.d("Conf01", "extra is conference true");
            PJSIPManager.getInstance().sendConference(request.getExtras().getInt(SipService.EXTRA_PJSIP_ID),connection.getConnectionID(), request.getAddress().getSchemeSpecificPart());
        }else {
            PJSIPManager.getInstance().sendCall(request.getAddress().getSchemeSpecificPart(), true, connection.getConnectionID());
        }
        connection.setActive();
        return connection;
    }

    public PJSIPConnectionService() {
        android.util.Log.d("PJSIPConnectionService", "constructor");
    }

    @Override
    public boolean onUnbind(Intent intent) {
       // android.util.Log.d("PJSIPConnectionService", "unbinding");
        return super.onUnbind(intent);
    }

    @Override
    public void onConnectionServiceFocusLost() {

        //android.util.Log.d("PJSIPConnectionService", "connection service lost");
        super.onConnectionServiceFocusLost();
    }

    @Override
    public void onConnectionServiceFocusGained() {
        //android.util.Log.d("PJSIPConnectionService", "connection service gained");
        super.onConnectionServiceFocusGained();
    }

    @Override
    public void onConference(Connection connection1, Connection connection2) {
        super.onConference(connection1, connection2);
    }

    @Override
    public void onDestroy() {

    }
}
