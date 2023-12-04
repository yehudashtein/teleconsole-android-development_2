package com.telebroad.teleconsole.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ComponentActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.reflect.TypeToken;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.login.EmailResetActivity;
import com.telebroad.teleconsole.controller.login.SignInActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static android.Manifest.permission.CALL_PHONE;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.BLOCKED_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.LOGIN_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.MALFORMED_ERROR;
import org.json.JSONObject;
/**
 * Created by yser on 3/13/2018.
 */

public class URLHelper {
     public static Context context;
    private static final String BASE_URL = "https://webserv.telebroad.com/api/teleconsole/rest";
    public static final String GET_VOICEMAIL_URL = getBaseUrl() + "/voicemail";
    public static final String GET_MYPROFILE_URL = getBaseUrl() + "/myProfile";
    public static final String GET_MYCREDENTIALS_URL = getBaseUrl() + "/myCredentials";
    public static final String GET_MYPHOTO_URL = getBaseUrl() + "/myPhoto";
    public static final String GET_CONTACT_URL = getBaseUrl() + "/contact";
    public static final String GET_VOICEMAILFILE_URL = getBaseUrl() + "/voicemail/file";
    public static final String GET_SMS_CONVERSATIONS_URL = getBaseUrl() + "/sms/conversations";
    public static final String GET_CONVERSATION_URL = getBaseUrl() + "/sms/conversation";
    public static final String GET_CDRS_URL = getBaseUrl() + "/phone/cdrs";
    public static final String GET_PEOPLE_URL = getBaseUrl() + "/people";
    public static final String GET_NUMBERS_URL = getBaseUrl() + "/numbers";
    public static final String GET_USER_SETTING = getBaseUrl() + "/user/settings";
    public static final String GET_CALL_FORWARDING = getBaseUrl() + "/phone/forwarding";
    public static final String GET_CALL_DND = getBaseUrl() + "/phone/dnd";
    public static final String GET_PHONE = getBaseUrl() + "/phone";
    public static final String GET_FAX_HISTORY = getBaseUrl() + "/fax/messages";
    public static final String GET_FAX_FILE = getBaseUrl() + "/fax/message";
    public static final String DELETE_SMS = getBaseUrl() + "/sms/message";
    public static final String DELETE_CONVERSATION = getBaseUrl() + "/sms/conversation";
    public static final String DELETE_SMS_ALL = getBaseUrl() + "/sms/messages";
    public static final String DELETE_FAX = getBaseUrl() + "/fax/messages";
    public static final String GET_CALL_HISTORY = getBaseUrl() + "/call/history";
    public static final String POST_SEND_CALL = getBaseUrl() + "/send/call";
    public static final String POST_REDIRECT_CALL = getBaseUrl() + "/call/redirect";
    public static final String POST_FORGET_PASSWORD = getBaseUrl() + "/forget/credentials";
    public static final String POST_RESET_PASSWORD = getBaseUrl() + "/reset/password";
    public static final String LOGIN_URL = getBaseUrl() + "/push/login";
    public static final String SEND_SMS_URL = getBaseUrl() + "/send/sms";
    public static final String SEND_FAX_URL = getBaseUrl() + "/send/fax";
    public static final String READ_URL = getBaseUrl() + "/messages/read";
    public static final String RINGING_URL = getBaseUrl() + "/my/calls/ringing";
    public static final String LOG_URL = "https://push2.telebroad.com:8443/system/log/v2/";
    public static final String BLOCK_SMS_URL = getBaseUrl() + "/sms/blacklist";
    public static final String UNBLOCK_SMS_URL = getBaseUrl() + "/sms/blacklist/recipient";
    public static final String UNREAD_URL = getBaseUrl() + "/messages/unread";
    public static final String JWT_LOGIN_URL = getBaseUrl() + "/login";
    public static final String MICROSOFT_AUTH = getBaseUrl() + "/auth/microsoft";
    public static final String GOOGLE_AUTH = getBaseUrl() + "/auth/google";
    public static final String KEY_ERROR = "error";
    public static final String KEY_NULL = "null";
    public static final String KEY_RESULT = "result";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_SUBSCRIBEKEY = "subcribe_key";
    public static final String KEY_PUBLISHKEY = "publish_key";
    public static final String KEY_AUTH = "auth";
    public static final String KEY_CHAT = "chat";
    public static final String KEY_CHANNELS = "channels";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_LINE = "line";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_CHANNEL = "channel";
    public static final String KEY_CODE = "code";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_ID = "id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_FIRSTNAME = "firstname";
    public static final String KEY_LASTNAME = "lastname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_FALSE = "false";
    public static final String KEY_CALLERNAME = "callername";
    public static final String KEY_CALLERID = "callerid";
    public static final String KEY_TITLE = "title";
    public static final String KEY_PBX_LINE = "pbx_line";
    public static final String KEY_CHAT_CHANNEL = "chat_channel";
    public static final String KEY_EXTENSION = "extension";
    public static final String KEY_HOME = "home";
    public static final String KEY_WORK = "work";
    public static final String KEY_STATUS = "status";
    public static final String KEY_STATUS_MSG = "status_msg";
    public static final String KEY_CONTACT_TYPE = "contactType";
    public static final String KEY_FILE_EXTENSION = "file_extension";
    public static final String KEY_TIME = "time";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_MAILBOX = "mailbox";
    public static final String KEY_CALLER = "caller";
    public static final String KEY_CALLERID_EXT = "callerid_ext";
    public static final String KEY_DIR = "dir";
    public static final String KEY_FILE = "file";
    public static final String KEY_DATA = "data";
    public static final String KEY_FNAME = "fname";
    public static final String KEY_LNAME = "lname";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_PUBLIC = "public";
    public static final String KEY_OWNED = "owned";
    public static final String KEY_ORGANIZATION = "organization";
    public static final String KEY_FAX = "fax";
    public static final String KEY_WEBSITE = "website";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_PHONES = "phones";
    public static final String KEY_SMS = "sms";
    public static final String KEY_VOX = "vox";
    public static final String KEY_SMSLINE = "sms_line";
    public static final String KEY_CONVERSATION = "conversation";
    public static final String KEY_LIMIT_ = "_limit";
    public static final String KEY_LIMIT = "limit";
    public static final String KEY_OFFSET_ = "_offset";
    public static final String KEY_OFFSET = "offset";
    public static final String KEY_DESCENDING = "descending";
    public static final String KEY_END = "end";
    public static final String KEY_START = "start";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_IDX = "idx";
    public static final String KEY_NEW = "new";
    public static final String KEY_DIRECTION = "direction";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_MSGDATA = "msgdata";
    public static final String KEY_SENTBY = "sent_by";
    public static final String KEY_CANNEL = "channel";
    public static final String KEY_DELETED = "deleted";
    public static final String KEY_SMSDATA = "smsdata";
    public static final String KEY_VOICEDATA = "VOICEMAIL_NEW";
    public static final String KEY_FAXDATA = "FAX_NEW";
    public static final String KEY_ROWTYPE = "type";
    public static final String KEY_CHATNAME = "chatname";
    public static final String KEY_CHATTIME = "chattime";
    public static final String KEY_CHATMESSAGE = "chatmessage";
    public static final String KEY_CUSTOMER = "customer";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_INGROUP = "ingroup";
    public static final String KEY_CLASS = "class";
    public static final String KEY_STYPE = "stype";
    public static final String KEY_SNUMBER = "snumber";
    public static final String KEY_SNAME = "sname";
    public static final String KEY_SNAME_ACTION = "sname_action";
    public static final String KEY_DTYPE = "dtype";
    public static final String KEY_DNUMBER = "dnumber";
    public static final String KEY_ROUTING = "routing";
    public static final String KEY_UPDATE_ROUTING = "update_routing";
    public static final String KEY_SHORTCUT = "shortcut";
    public static final String KEY_ALIAS = "alias";
    public static final String KEY_RECORD_GRUOP = "recordgroup";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_PLAY_MESSAGE = "play_message";
    public static final String KEY_MUSIC = "music";
    public static final String KEY_MAXIMUM_SECONDS = "maximum_seconds";
    public static final String KEY_SHARED = "instance";
    public static final String KEY_DIRECTORY = "directory";
    public static final String KEY_EMERGENCY_REGISTER = "emergency_register";
    public static final String KEY_PORTED = "ported";
    public static final String KEY_SCREEN = "screen";
    public static final String KEY_DIRECT = "direct";
    public static final String KEY_FAX_DETECT = "fax_detect";
    public static final String KEY_FAX_DTYPE = "fax_dtype";
    public static final String KEY_FAX_DNUMBER = "fax_dnumber";
    public static final String KEY_MIMETYPE = "mimetype";
    public static final String KEY_PANEL = "panel";
    public static final String KEY_USERS = "users";
    public static final String KEY_SMS_CHAT_LINE = "sms_chat_line";
    public static final String KEY_TELECONSOLE = "teleconsole";
    public static final String KEY_DEFAULT_SMSLINE = "defaultSmsLine";
    public static final String KEY_LIVECALLS_HANDLER = "liveCallsHandler";
    public static final String KEY_ACTIVE_VOICEMAILS = "activeVoicemails";
    public static final String KEY_ACTIVE_SMSLINES = "activeSmsLines";
    public static final String KEY_ACTIVE_FAXES = "activeFaxes";
    public static final String KEY_ACTIVE_PHONELINE = "activePhoneLine";
    public static final String KEY_CALLERID_INT = "callerIDint";
    public static final String KEY_FCODE = "fcode";
    public static final String KEY_SECRET = "secret";
    public static final String KEY_CALLERID_EXTERNEL = "callerid_external";
    public static final String KEY_PUBNUB_CHANNEL = "pubnub_channel";
    public static final String KEY_USER_STATUSES = "userStatuses";
    public static final String KEY_CALLED = "called";
    public static final String KEY_PAGES = "pages";
    public static final String KEY_CNUMBER = "cnumber";
    public static final String KEY_CALLERIDD = "calleridd";
    public static final String KEY_ANSWER1 = "answer1";
    public static final String KEY_ANSWER2 = "answer2";
    public static final String KEY_UNIQUEID = "uniqueid";
    public static final String KEY_CTYPE = "ctype";
    public static final String KEY_CALL_ID = "callid";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_TIMEZONE = "timezone";
    public static final String KEY_NEW_PASSWORD = "new_password";
    public static final String KEY_NEW_USERNAME = "new_username";
    public static final String KEY_RECIPIENT = "recipient";
    public static final String KEY_REASON = "reason";
    public static final String LOGIN_CREDENTIALS_NULL = "No Authorization found";
    public static void request(int method, String url, Map<String, String> params, boolean isArray, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        request(method, url, params, null, true, isArray, resultHandler, completionHandler);
    }

    public static void request(int method, String url, Map<String, String> params, boolean isArray, String tag, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        request(method, url, params, tag, true, isArray, resultHandler, completionHandler);
    }

    public static void request(int method, String url, Map<String, String> params, boolean isArray, boolean requiresAuth, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        request(method, url, params, null, isArray, requiresAuth, resultHandler, completionHandler);
    }

    public static void request(int method, String url, Map<String, String> params, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        request(method, url, params, null, true, false, resultHandler, completionHandler);
    }

    public static void request(int method, String url, Map<String, String> params, String tag, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        request(method, url, params, tag, true, false, resultHandler, completionHandler);
    }

    public static boolean needsNewToken(String jwtToken){
        if (jwtToken == null || jwtToken.isEmpty()) {
            Utils.logToFile("token is empty");
            return true;
        }
        String[] split = jwtToken.split("\\.");
        if (split.length < 2){
            Utils.logToFile("token not valid");
            return true;
        }
        String bodyDecoded = new String (Base64.getDecoder().decode(split[1]));
        HashMap<String, String> parsed = new Gson().fromJson(bodyDecoded, new TypeToken<HashMap<String, String>>(){}.getType());
        String expStr = parsed.get("exp");
        if ( expStr == null || expStr.isEmpty() ){
            Utils.logToFile("exo is null or empty");
            return true;
        }
        try {
            long exp = Long.parseLong(expStr) * SECOND_IN_MILLIS;
            long now = System.currentTimeMillis();
            return exp < now;
        }catch (NumberFormatException numberFormatException){
            return true;
        }
    }

    public static void requestJWTAuth(Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        String jwtToken = SettingsHelper.getString(SettingsHelper.JWT_TOKEN);
        //android.util.Log.d("JWTDecode", "needs new token? ");
        if (needsNewToken(jwtToken)) {
            Utils.logToFile("token is empty");
            Map<String, String> params = new HashMap<>();
            final String appSecret = "1b8078b7962ecb467a278d2c6a9deba0";
            final String username = SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME);
            final String password = SettingsHelper.getString(SettingsHelper.TELEBROAD_PASSWORD);
            if (username == null || password == null) {
                Utils.logToFile("username or password is null");
                //android.util.Log.d("JWT_MSAL", "username and password are null " + username + password);
                completionHandler.accept(LOGIN_ERROR);
                return;
            }
            String passHash = appSecret + password;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashedBytes = md.digest(passHash.getBytes());
                StringBuilder output = new StringBuilder(hashedBytes.length);
                for (int i = 0; i < hashedBytes.length; i++) {
                    String hex = Integer.toHexString(0xFF & hashedBytes[i]);
                    if (hex.length() == 1)
                        hex = "0" + hex;
                    output.append(hex);
                }
                String passHashResult = output.toString();
                //android.util.Log.d("JWT", "pass hash result is " + passHashResult);
                params.put(KEY_USERNAME, username);
                params.put("passhash", password);
                //Log.v("passhash",String.valueOf(params));
                JWTRequest stringRequest = new JWTRequest(POST, JWT_LOGIN_URL, new JSONObject(params), getJsonResultListener(resultHandler, completionHandler), getErrorListener(completionHandler));
                stringRequest.addJWTHeaders(jwtToken);
                stringRequest.setRetryPolicy(getRetryPolicy());
                AppController.getInstance().addToRequestQueue(stringRequest);
            } catch (NoSuchAlgorithmException e) {
                completionHandler.accept(new TeleConsoleError.CustomError(401, "Unable to sha-256 password"));
            }
        }
        else {
            completionHandler.accept(null);
            JsonObject result = new JsonObject();
            result.addProperty("tempJwt", jwtToken);
            resultHandler.accept(result.get("tempJwt"));
        }
//        stringRequest.setParams(params);
    }

    static String getAuthorization() {
        final String username = SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME);
        final String password = SettingsHelper.getString(SettingsHelper.TELEBROAD_PASSWORD);
        // If the username or password is unavailable, just return a No Auth Reply
        if (username == null || password == null) {
            return LOGIN_CREDENTIALS_NULL;
        }
        return LOGIN_CREDENTIALS_NULL;
//        return "Basic " + Base64.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    public static String getJWTAuthorization() {
        String token = SettingsHelper.getString(SettingsHelper.JWT_TOKEN);
        if (token == null) {
            Utils.logToFile(context,"token is null");
            return null;
        } else {
            return "Bearer " + token;
        }
    }

    public static void request(int method, String url, Map<String, String> params, String tag, boolean requiresAuth, boolean isArray, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        // if the call requires authorization but doesn't have any return a LOGIN_ERROR immediately
        if (requiresAuth && getJWTAuthorization() == null && getAuthorization().equals(LOGIN_CREDENTIALS_NULL)) {
            //android.util.Log.d("MSAL", "request 1?");
            completionHandler.accept(LOGIN_ERROR);
            return;
        }
        switch (method) {
            case Request.Method.GET:
                getRequest(url, params, resultHandler, completionHandler);
                break;
            case PUT:
                putRequest(url, params, resultHandler, completionHandler);
                break;
            case POST:
                postRequest(url, params, resultHandler, completionHandler);
                break;
            case Request.Method.DELETE:
                deleteRequest(url, params, resultHandler, completionHandler);
                break;
        }
    }

    private static void deleteRequest(String url, Map<String, String> params, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder = builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
       // Log.d("URL Parts", "preapprove " + builder.build().toString());
        String requestURL = builder.build().toString();
        TCStringRequest stringRequest = new TCStringRequest(Request.Method.DELETE, requestURL, getResultListener(resultHandler, completionHandler), getErrorListener(completionHandler));
        stringRequest.addAuthorizationHeaders();
        stringRequest.setRetryPolicy(getRetryPolicy());
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private static void putRequest(String url, Map<String, String> params, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        //Log.d("Settings", "Putting Request " + url);
        TCStringRequest stringRequest = new TCStringRequest(PUT, url, getResultListener(resultHandler, completionHandler), getErrorListener(completionHandler));
        stringRequest.addAuthorizationHeaders();
        stringRequest.setRetryPolicy(getRetryPolicy());
        stringRequest.setParams(params);
//        try {
//            Log.d("Contact12", "Put body " + new String( stringRequest.getBody()));
//        } catch (AuthFailureError authFailureError) {
//            Log.e("Contcat12", "Put body read error");
//        }
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public static void uploadLogs(File file, String prefix) {
        uploadLogs(file, 0, prefix);
    }

    public static void uploadLogs(File file) {
        uploadLogs(file, 0, "");
    }

    private static String logFile = "";

    private static void uploadLogs(File file, int retry, String prefix) {
        if (logFile == null || file == null) {
            FirebaseCrashlytics.getInstance().setCustomKey("No Logfile", "" + (logFile == null) + (file == null));
            return;
        }
        if (logFile.equals(file.getAbsolutePath())) {
            return;
        }
        logFile = file.getAbsolutePath();
        int maxRetries = 3;
        //android.util.Log.d("UPLOAD", "Starting log upload");
        //android.util.Log.d("TestLOG", "uploading file " + file.getAbsolutePath());
        byte[] fileData = new byte[(int) file.length()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(fileData);
            in.close();
        } catch (IOException | OutOfMemoryError e) {
            retryLogUpload(file, retry, maxRetries, prefix);
            e.printStackTrace();
            return;
        }
        FirebaseInstallations.getInstance().getId().addOnCompleteListener(task -> {
            String url = LOG_URL + SettingsHelper.getString(SettingsHelper.TELEBROAD_USERNAME) + "/" + task.getResult() + "/" + prefix + file.getName();
            FirebaseCrashlytics.getInstance().setCustomKey("last_uploaded_log_url", url);
          //  android.util.Log.d("UPLOAD", "new url " + url);
            StringRequest request = new StringRequest(PUT, url,
                    response -> {
                        //android.util.Log.d("UploadLogs", "respoded");
                        if (prefix == null || prefix.isEmpty()) {
                            file.delete();
                        }
                    },
                    error -> {
                      //  android.util.Log.d("UploadLogs", "error");
                        retryLogUpload(file, retry, maxRetries, prefix);
                    })
            {
                @Override
                public String getBodyContentType() {
                    return "text/plain";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return fileData;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("X-APP-KEY", "tb.app2");
                    headers.put("Authorization", getJWTAuthorization());
                    //android.util.Log.d("UPLOAD", headers.toString());
                    return headers;
                }
            };
            request.setRetryPolicy(getRetryPolicy());
            AppController.getInstance().addToRequestQueue(request);
        });
    }

    private static void retryLogUpload(File file, int retry, int maxRetries, String prefix) {
        if (retry < maxRetries) {
            uploadLogs(file, retry + 1, prefix);
        } else {
            file.delete();
        }
    }

    private static void postRequest(String url, Map<String, String> params, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
       // Log.d("Pubnub", "Posting Request " + url);
        TCStringRequest stringRequest = new TCStringRequest(POST, url, getResultListener(resultHandler, completionHandler), getErrorListener(completionHandler));
        stringRequest.addAuthorizationHeaders();
        stringRequest.setRetryPolicy(getRetryPolicy());
        stringRequest.setParams(params);
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private static void getRequest(String url, Map<String, String> params, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        {
            Uri.Builder builder = Uri.parse(url).buildUpon();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder = builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
            // Log.d("URL Parts", "preapprove " + builder.build().toString());
            String requestURL = builder.build().toString();
            TCStringRequest stringRequest = new TCStringRequest(requestURL, getResultListener(resultHandler, completionHandler), getErrorListener(completionHandler));
            stringRequest.addAuthorizationHeaders();
            stringRequest.setRetryPolicy(getRetryPolicy());
            AppController.getInstance().addToRequestQueue(stringRequest);
        }
    }


    @NonNull
    private static DefaultRetryPolicy getRetryPolicy() {
        return new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    @NonNull
    private static Response.ErrorListener getErrorListener(Consumer<TeleConsoleError> completionHandler) {
        return error -> {
          //  Log.d("URL Error", "error " + error.getLocalizedMessage() + " cause " + error.getCause() + " message " + error.getMessage() + " error " + error, error.getCause());
            if (error instanceof TimeoutError) {
                completionHandler.accept(new TeleConsoleError.CustomError(504, "Server Timed Out"));
            }
        };
    }

    @NonNull
    private static Response.Listener<String> getResultListener(Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        return response -> {
            handleResult(resultHandler, completionHandler, response);
        };
    }

    private static void handleResult(Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler, String response) {
        //Utils.getSingleThreadExecutor().execute(() -> {
            try {
                Gson gson = new Gson();
                JsonElement jsonElement = gson.fromJson(response, JsonElement.class);
                if (jsonElement != null && jsonElement.isJsonObject()) {
                    JsonObject jsonObject = (JsonObject) jsonElement;
                    JsonElement errorElement = jsonObject.get(KEY_ERROR);
                    // If the error is null
                    if (errorElement.isJsonNull()) {
                        resultHandler.accept(jsonObject.get(KEY_RESULT));
                    } else {
                        TeleConsoleError.CustomError customError = gson.fromJson(errorElement, TeleConsoleError.CustomError.class);
                        if (customError.getCode() == 433) {
                           // android.util.Log.d("MSAL", jsonElement.toString());
                            completionHandler.accept(LOGIN_ERROR);
                        } else if (customError.getCode() == 439) {
                            completionHandler.accept(BLOCKED_ERROR);
                        } else {
                            completionHandler.accept(customError);
                        }
                    }
                } else {
                    completionHandler.accept(MALFORMED_ERROR);
                }
            } catch (ClassCastException cce) {
               // Log.e("Class cast", "There was a class cast exception");
                completionHandler.accept(TeleConsoleError.ServerError.MALFORMED_ERROR);
            }
        //});
    }

    @NonNull
    private static Response.Listener<JSONObject> getJsonResultListener(Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        return response -> {
            handleResult(resultHandler, completionHandler, response.toString());
        };
    }

    public static Consumer<TeleConsoleError> getDefaultErrorHandler(Consumer<TeleConsoleError> errorHandler) {
        final Activity[] activity = new Activity[1];
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                activity[0] = AppController.getInstance().getActiveActivity();
                // Update UI here.
            }
        });

        return error -> {
            if (error == LOGIN_ERROR && activity[0] != null) {
                //Activity activity = AppController.getInstance().getActiveActivity();
                activity[0].startActivity(new Intent(activity[0], SignInActivity.class));
            } else if (error == BLOCKED_ERROR && AppController.getInstance().getActiveActivity() != null) {
                //Activity activity = AppController.getInstance().getActiveActivity();
                showBlockedError(activity[0]);
            } else {
                errorHandler.accept(error);
            }
        };
    }

    public static void showBlockedError(Activity activity) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.account_blocked_title)
                .setMessage(R.string.account_blocked_message)
                .setPositiveButton("Reset Password", ((dialog, which) -> {
                    activity.startActivity(new Intent(activity, EmailResetActivity.class));
                    dialog.dismiss();
                }));
        if (AppController.getInstance().hasPermissions(CALL_PHONE)) {
            builder.setNegativeButton(R.string.contact,
                    (DialogInterface dialog, int which) -> {
                        callSupport(activity, dialog);
                    });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressLint("MissingPermission")
    private static void callSupport(Activity activity, DialogInterface dialog) {
        Utils.callSupport(activity);
        dialog.dismiss();
    }

    public static String getBaseUrl() {
        if (AppController.getInstance() != null) {
            return AppController.getAppString(R.string.base_url);
        }
        return BASE_URL;
    }
}

class JWTParams {
    String username;
    String passhash;

    JWTParams(String username, String passhash) {
        this.username = username;
        this.passhash = passhash;
    }
}

class JWTRequest extends JsonObjectRequest {
    private Map<String, String> params;
    private Map<String, String> headers = Collections.emptyMap();

    public JWTRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {return headers;}

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {return params;}

    void addJWTHeaders(String refreshToken) {
        try {
            if (getHeaders().equals(Collections.emptyMap())) {
                headers = new HashMap<>();
            }
        } catch (AuthFailureError authFailureError) {
            headers = new HashMap<>();
            authFailureError.printStackTrace();
        } finally {
            headers.put("Content-Type", "application/json");
            headers.put("X-App-Key", "tb.app");
            if (refreshToken != null && !refreshToken.isEmpty()) {
                headers.put("Authorization", refreshToken);
            }
        }
    }
}

class TCStringRequest extends StringRequest {
    private Map<String, String> headers = Collections.emptyMap();
    private Map<String, String> params;

    public TCStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    TCStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {return headers;}

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {return params;}

    void addAuthorizationHeaders() {
        String jwtAuth = URLHelper.getJWTAuthorization();
        if (jwtAuth == null) {
            addAuthHeader(URLHelper.getAuthorization(), false);
        } else {
            addAuthHeader(jwtAuth, true);
        }
    }

    private void addAuthHeader(String auth, boolean jwt) {
        try {
            if (getHeaders().equals(Collections.emptyMap())) {
                headers = new HashMap<>();
            }
        } catch (AuthFailureError authFailureError) {
            headers = new HashMap<>();
            authFailureError.printStackTrace();
        } finally {
            headers.put("Authorization", auth);
            headers.put("X-App-Key", "tb.app");
        }
    }
}

