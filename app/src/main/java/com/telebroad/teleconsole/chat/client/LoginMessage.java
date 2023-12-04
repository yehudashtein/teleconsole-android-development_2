package com.telebroad.teleconsole.chat.client;

import android.util.Base64;

import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.helpers.SettingsHelper;

import java.nio.charset.StandardCharsets;

public class LoginMessage {

    String id = ChatWebSocket.LOGIN_ID;
    String scheme = "enswitchJWT";
    String secret;

    public LoginMessage(){

    }
    public LoginMessage(String token){
        String secret = "tb.app:" + token;
        byte[] data = secret.getBytes(StandardCharsets.US_ASCII);
        this.secret = Base64.encodeToString(data, Base64.NO_WRAP);
        android.util.Log.d("CWS", "Secret now is " + this.secret);
    }
//    String request = "{\n" +
//            "  \"login\": {\n" +
//            "    \"id\": \"108296\",\n" +
//            "    \"scheme\": \"enswitchJWT\",\n" +
//            "    \"secret\": \"" + base64 + "\"\n" +
//            "  }\n" +
//            "}";
}
