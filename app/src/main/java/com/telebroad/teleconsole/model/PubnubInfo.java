package com.telebroad.teleconsole.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Tasks;
//import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.push.PNPushAddChannelResult;
import com.pubnub.api.models.consumer.push.PNPushListProvisionsResult;
import com.pubnub.api.models.consumer.push.PNPushRemoveAllChannelsResult;
import com.pubnub.api.models.consumer.push.PNPushRemoveChannelResult;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.telebroad.teleconsole.helpers.URLHelper.LOGIN_URL;
import static com.telebroad.teleconsole.helpers.URLHelper.request;

import android.content.ContextWrapper;
import android.os.Build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PubnubInfo {
    private String uuid;

    private static PubnubInfo INSTANCE;
    static MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
    // These בהמות misspelled subscribe, wasted 3 hours of my time
    @SerializedName("subcribe_key")
    String subscribe_key;
    String publish_key;
    String auth;
    String chat;
    List<Line> channels;
    Map<String, Set<Consumer<JsonObject>>> listeners = new HashMap<>();

    public static MutableLiveData<String> getMutableLiveData() {
        return mutableLiveData;
    }

    private static int retries = 0;

    public static PubnubInfo getInstance() {
        if (INSTANCE == null) {
            String pnInfo = SettingsHelper.getString(SettingsHelper.PUBNUB_INFO);
            if (pnInfo != null) {
                return new Gson().fromJson(pnInfo, PubnubInfo.class);
            }
        }
        return INSTANCE;
    }

    private static PubNub pubNub;

    private static String getUUID() {
        final String androidId;
        try {
            androidId = Tasks.await(FirebaseMessaging.getInstance().getToken());
            //android.util.Log.d("pnInfo", "android id " + androidId);
            return UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString();
        } catch (ExecutionException | InterruptedException | IOException | RuntimeExecutionException e) {
            e.printStackTrace();
            //Utils.logToFile( "Unable to get id " + e.getMessage());

        }
        // Use the Android ID unless it's broken, in which case fallback on deviceId,
        // unless it's not available, then fallback on a random number which we store
        // to a prefs file
        return UUID.randomUUID().toString();
    }

    public static void fetchPubnubInfo() {
        //android.util.Log.d("Pubnub", "fetching info");
        Map<String, String> params = new HashMap<>();
        Utils.asyncTask(() -> {
            String uuid = getUUID();
            android.util.Log.d("Pubnub", uuid);
            params.put("uuid", uuid);
            RequestQueue requestQueue = Volley.newRequestQueue(AppController.getInstance());
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST, LOGIN_URL, response -> {
                Utils.updateLiveData(mutableLiveData, response.toString());
                Gson gson = new Gson();
                JsonElement jsonElement = gson.fromJson(response, JsonElement.class);
                handleResult(jsonElement);
            }, error -> {
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("uuid", getUUID());
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", URLHelper.getJWTAuthorization());
                    headers.put("X-App-Key", "tb.app");
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);

            android.util.Log.d("pnInfo", "uuid sent " + uuid);
//            Utils.getSingleThreadExecutor().execute(() -> request(Request.Method.POST, LOGIN_URL, params, result -> {
//                Utils.updateLiveData(mutableLiveData, result.toString());
//                android.util.Log.d("Pubnub1", result.toString());
//                handleResult(result);
//            }, error -> {
//                // android.util.Log.d("Pubnub", "error = " + (error != null ? error.getFullErrorMessage() : "none"));
//                // TODO display a warning to the user
//                android.util.Log.d("Pubnub", error.toString());
//            }));

        });
    }

    //    private static void handleResult(JsonElement result) {
//        if (result instanceof JsonObject) {
//            PubnubInfo pnInfo = new Gson().fromJson(result, PubnubInfo.class);
//            SettingsHelper.putString(SettingsHelper.PUBNUB_INFO, result.toString());
//            INSTANCE = pnInfo;
//            PNConfiguration pnConfiguration = getPnConfiguration(pnInfo);
//            pubNub = new PubNub(pnConfiguration);
//            setupPushNotification(pnInfo);
//            setupSubscriptionListener(pubNub);
//        }
//    }
    private static void handleResult(JsonElement result) {
        if (result instanceof JsonObject) {
            JsonElement nestedResult = ((JsonObject) result).get("result");
            if (nestedResult instanceof JsonObject) {
                PubnubInfo pnInfo = new Gson().fromJson(nestedResult, PubnubInfo.class);
                SettingsHelper.putString(SettingsHelper.PUBNUB_INFO, nestedResult.toString());
                INSTANCE = pnInfo;
                PNConfiguration pnConfiguration = getPnConfiguration(pnInfo);
                pubNub = new PubNub(pnConfiguration);
                setupPushNotification(pnInfo);
                setupSubscriptionListener(pubNub);
            }
        }
    }


    private static void setupSubscriptionListener(PubNub pubnub) {
        pubnub.addListener(new SubscribeCallback.BaseSubscribeCallback() {
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                if (pnMessageResult.getMessage().isJsonObject()) {
                    JsonObject jsonObject = pnMessageResult.getMessage().getAsJsonObject();
                    if (jsonObject.get("dir") != null && jsonObject.get("dir").isJsonPrimitive() && jsonObject.get("dir").getAsJsonPrimitive().isString() && "DLR".equals(jsonObject.get("dir").getAsString())) {
                        Set<Consumer<JsonObject>> listeners = getInstance().listeners.get(pnMessageResult.getChannel());
                        if (listeners != null) {
                            for (Consumer<JsonObject> listener : listeners) {
                                listener.accept(jsonObject);
                            }
                        }
                    }
                }
                // android.util.Log.d("DLR_PN", pnMessageResult.getMessage().toString());
            }
        });
    }

    public void addListener(String channel, Consumer<JsonObject> consumer) {
        Set<Consumer<JsonObject>> listeners = getInstance().listeners.get(channel);
        if (listeners == null) {
            listeners = new HashSet<>();
            getInstance().listeners.put(channel, listeners);
        }
        listeners.add(consumer);

    }

    private static void setupPushNotification(PubnubInfo pnInfo) {
        List<String> channels = new ArrayList<>();
        List<Line> subscribedLines = new ArrayList<>();
        if (Settings.getInstance() == null) {
            return;
        }
        Line.PhoneLine phoneLine = Settings.getInstance().getPhoneLine();
        // android.util.Log.d("Pubnub", phoneLine.getPubnub_channel() + "");
        if (phoneLine.getPubnub_channel() == null || phoneLine.getPubnub_channel().isEmpty()) {
            int phoneChannelIndex = pnInfo.channels.indexOf(phoneLine);
            if (phoneChannelIndex >= 0) {
                Settings.getInstance().getPhoneLine().setPubnub_channel(pnInfo.channels.get(phoneChannelIndex).getPubnub_channel());
            }
        }
        channels.add(Settings.getInstance().getPhoneLine().getPubnub_channel());
        subscribedLines.addAll(Settings.getInstance().getSmsLines());
        subscribedLines.addAll(Settings.getInstance().getFaxLines());
        subscribedLines.addAll(Settings.getInstance().getVoicemails());
        for (Line line : subscribedLines) {
            if (line != null) {
                // android.util.Log.d("Pubnub", "line " + line + " channel " + line.getPubnub_channel());
                String pubnubChannel = line.getPubnub_channel();
                if (pubnubChannel == null) {
                    Line found = PubnubInfo.getInstance().findChannelByName(line.getName());
                    if (found != null) {
                        pubnubChannel = found.getPubnub_channel();
                    }
                }
                if (pubnubChannel != null) {
                    channels.add(pubnubChannel);
                }
            }
        }
        channels.add(pnInfo.chat);
//        channels.add(pnInfo.chat + "-apn");
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(
                    task -> {
                        try {
                            //android.util.Log.d("MPUSH-PUBNUB", "token is " + task.getResult());
                            pubNub.addPushNotificationsOnChannels()
                                    .pushType(PNPushType.FCM)
                                    .channels(channels)
                                    .deviceId(task.getResult())
                                    .async(PubnubInfo::handlePubnubResponse);
                            subscribe(channels);
                        } catch (RuntimeExecutionException exception) {
                            exception.getMessage();
                        }
                    }
            );
        } catch (RuntimeExecutionException exception) {
            Utils.logToFile("unable to add push notifications, service not available " + exception);
        }
    }

    public String getChat() {
        return chat;
    }

    private static void subscribe(List<String> channels) {
        if (pubNub != null) {
            pubNub.subscribe().channels(channels).execute();
        }
    }

    private static PNConfiguration getPnConfiguration(PubnubInfo pnInfo) {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setAuthKey(pnInfo.auth);
        pnConfiguration.setSubscribeKey(pnInfo.subscribe_key);
        pnConfiguration.setPublishKey(pnInfo.publish_key);
        pnConfiguration.setSecure(false);
        pnConfiguration.setUuid(pnInfo.uuid);
        pnConfiguration.setSecretKey(AppController.getInstance().getString(R.string.pubnub_secret));
        pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);
        return pnConfiguration;
    }

    public void subscribeToChannels(List<? extends Line> lines) {
        if (pubNub != null) {
            try {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    try {
                        List<String> channels = getChannels(lines);
                        pubNub.addPushNotificationsOnChannels()
                                .pushType(PNPushType.FCM)
                                .channels(channels)
                                .deviceId(task.getResult())
                                .async(PubnubInfo::handlePubnubResponse);
                        subscribe(channels);
                    } catch (RuntimeExecutionException ree) {
                        Utils.logToFile("Unable to subscribe, service not available");
                    }
                });
            } catch (RuntimeExecutionException ree) {
                Utils.logToFile("Unable to subscribe, service not available");
            }
        }
    }

    public static void listChannels() {
        if (pubNub != null) {
            try {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    try {
                        pubNub.auditPushChannelProvisions().pushType(PNPushType.FCM)
                                .deviceId(task.getResult())
                                .async((result, status) -> {
                                    if (result != null) {
                                        // android.util.Log.d("Pubnub", "List is " + result.getChannels().toString());
                                    } else {
                                        Utils.logToFile("unable to list channels " + status.toString());
                                    }
                                });
                    } catch (RuntimeExecutionException ree) {
                        Utils.logToFile("Unable to audit, service not available");
                    }
                });
            } catch (RuntimeExecutionException ree) {
                Utils.logToFile("Unable to audit, service not available");
            }
        }
    }

    public void subscribeToChannelsByName(List<String> names) {
        subscribeToChannels(getLines(names));
    }

    public void unSubscribeToChannelsByName(List<String> names) {
        unSubscribeToChannels(getLines(names));
    }

    public List<Line> getLines(Collection<String> names) {
        List<Line> lines = new ArrayList<>();
        for (String name : names) {
            Line toAdd = findChannelByName(name);
            if (toAdd != null) {
                lines.add(toAdd);
            }
        }
        return lines;
    }

    public Line getLine(String name) {
        return findChannelByName(name);
    }

    @NonNull
    private List<String> getChannels(List<? extends Line> lines) {
        List<String> channels = new ArrayList<>();
        for (Line line : lines) {
            if (line == null) {
                continue;
            }
            String channel = line.getPubnub_channel();
            if (channel == null || channel.isEmpty()) {
                Line foundLine = findChannelByName(line.getName());
                if (foundLine != null) {
                    channel = foundLine.getPubnub_channel();
                }
            }
            if (channel != null) {
                channels.add(channel);
            }
        }
        return channels;
    }

    private Line findChannelByName(String name) {
        if (channels != null) {
            int index = channels.indexOf(new Line(name));
            if (index < 0 || index >= channels.size()) {
                return null;
            }
            Line line = channels.get(index);
            line.setName(PhoneNumber.getPhoneNumber(line.getName()).fixed());
            return channels.get(index);
        }
        return null;
    }

    public void logout() {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                try {
                    pubNub.removeAllPushNotificationsFromDeviceWithPushToken().pushType(PNPushType.FCM).deviceId(
                            task.getResult()).async(new PNCallback<PNPushRemoveAllChannelsResult>() {
                        @Override
                        public void onResponse(PNPushRemoveAllChannelsResult result, PNStatus status) {
                            handlePubnubResponse(result, status);
                        }
                    });
                    pubNub.unsubscribeAll();
                } catch (RuntimeExecutionException ree) {
                    Utils.logToFile("Unable to logout, service not available");
                }
            });
        } catch (RuntimeExecutionException ree) {
            Utils.logToFile("Unable to logout, service not available");
        }
    }

    private static void handlePubnubResponse(Object result, PNStatus status) {
        if (status.getCategory() == PNStatusCategory.PNAcknowledgmentCategory) {
            // android.util.Log.d("Pubnub", "result " + result + "status " + status.toString());
            listChannels();
            retries = 0;
        }
        if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory && retries++ < 3) {
            new android.os.Handler().postDelayed(() -> {
                //  android.util.Log.d("Pubnub", "result " + result + " status " + status.toString());
                Utils.logToFile(AppController.getInstance(), "Pubnub result " + result + " status " + status);
                status.retry();
            }, 3000);
        }
    }


    public void subscribeToChannels(Line... lines) {
        subscribeToChannels(Arrays.asList(lines));
    }

    public void unSubscribeToChannels(Line... lines) {
        unSubscribeToChannels(Arrays.asList(lines));
    }

    public void unSubscribeToChannels(List<? extends Line> lines) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                try {
                    List<String> channels = getChannels(lines);
                    pubNub.removePushNotificationsFromChannels()
                            .pushType(PNPushType.FCM)
                            .channels(channels)
                            .deviceId(task.getResult())
                            .async(PubnubInfo::handlePubnubResponse);
                    pubNub.unsubscribe().channels(channels);
                } catch (RuntimeExecutionException ree) {
                    Utils.logToFile("Unable to logout, service not available");
                }
            });
        } catch (RuntimeExecutionException ree) {
            Utils.logToFile("Unable to unsubscribe, service not available");
        }
    }

    @Override
    public String toString() {
        return "PubnubInfo{" +
                " uuid=" + uuid +
                "\n subscribe_key=" + subscribe_key +
                "\n publish_key=" + publish_key +
                "\n auth=" + auth +
                "\n chat=" + chat +
                "\n channels=" + channels +
                '}';
    }
}
