package com.telebroad.teleconsole.notification;
import static android.content.Context.JOB_SCHEDULER_SERVICE;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.ChatActivity;
import com.telebroad.teleconsole.controller.dashboard.ChatReplyActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatNotifications {

    public final static String KEY_REPLY = "key_reply";
   private String frm;
   private String seq;
   private String head;
   private String time;
   private String teams;
   private String frmFn;
   private String topic;
   private String message;
   private static ChatNotifications instance;
   private static Context context;
    static boolean isSenderTheSame = true;
    static boolean isSenderTheSame1 = true;
    static int id = 0;
    private static String topic1;
   private static ArrayList<ChatNotifications> chatNotificationsArrayList = new ArrayList<>();

    public ChatNotifications(String frm, String seq, String head, String time, String teams, String frmFn, String topic, String message) {
        this.frm = frm;
        this.seq = seq;
        this.head = head;
        this.time = time;
        this.teams = teams;
        this.frmFn = frmFn;
        this.topic = topic;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrm() {
        return frm;
    }

    public void setFrm(String frm) {
        this.frm = frm;
    }

    public static void setChatNotificationsArrayList(ArrayList<ChatNotifications> chatNotificationsArrayList) {
        ChatNotifications.chatNotificationsArrayList = chatNotificationsArrayList;
    }

    public static ArrayList<ChatNotifications> getChatNotificationsArrayList() {
        return chatNotificationsArrayList;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getTeams() {
        return teams;
    }

    public void setTeams(String teams) {
        this.teams = teams;
    }

    public String getFrmFn() {
        return frmFn;
    }

    public void setFrmFn(String frmFn) {
        this.frmFn = frmFn;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

   public class Head{
       private String mime;
       List<Attachments> attachments;

        public Head(String mime) {
            this.mime = mime;
        }

        public Head(String mime, List<Attachments> attachments) {
            this.mime = mime;
            this.attachments = attachments;
        }

        private class Attachments{
          private String path;
          private String size;
          private String icon;
          private String name;
          private String type;

           public String getPath() {
               return path;
           }

           public void setPath(String path) {
               this.path = path;
           }

           public String getSize() {
               return size;
           }

           public void setSize(String size) {
               this.size = size;
           }

           public String getIcon() {
               return icon;
           }

           public void setIcon(String icon) {
               this.icon = icon;
           }

           public String getName() {
               return name;
           }

           public void setName(String name) {
               this.name = name;
           }

           public String getType() {
               return type;
           }

           public void setType(String type) {
               this.type = type;
           }
       }
   }
    class HeadDeserializer implements JsonDeserializer<Head> {
        @Override
        public Head deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String headJson = jsonObject.get("head").getAsString();
            String unescapedHeadJson = headJson.replace("\\", "");
            jsonObject.remove("head");
            jsonObject.add("head",new JsonParser().parse(unescapedHeadJson));
            return new Gson().fromJson(jsonObject, typeOfT);
        }
    }

    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Head.class, new HeadDeserializer())
                .create();
    }
    public static ChatNotifications getInstance(Context context){
        if (instance == null){
            instance = new ChatNotifications(context);
        }
        return instance;
    }

    private ChatNotifications(Context context) {
        ChatNotifications.context = context;
//        if (context == null) {
//            ChatNotifications.context = AppController.getInstance();
//        }
    }
     void  showChatNotification(ChatNotifications smsNotification) {
        // Log.d("chat_notification_data","hi5");
//        if ( AppController.getInstance().getActiveActivity() != null &&AppController.getInstance().getActiveActivity() instanceof ChatActivity ||AppController.getInstance().getActiveActivity() instanceof ChatReplyActivity && !AppController.getInstance().isActiveActivityPaused() && ((ChatActivity) AppController.getInstance().getActiveActivity()).getName1() != null
//                        && ((ChatActivity) AppController.getInstance().getActiveActivity()).getName1().equals(smsNotification.getFrmFn())) {
//                    return;
//                }
         Activity activeActivity = AppController.getInstance().getActiveActivity();

         if (activeActivity != null && !AppController.getInstance().isActiveActivityPaused()) {
             if (activeActivity instanceof ChatActivity) {
                 ChatActivity chatActivity = (ChatActivity) activeActivity;
                 if (chatActivity.getName1() != null && chatActivity.getName1().equals(smsNotification.getFrmFn())) {
                     return;
                 }else {
                     DisplayNotification(smsNotification);
                 }
             } else if (activeActivity instanceof ChatReplyActivity) {
                 ChatReplyActivity chatReplyActivity = (ChatReplyActivity) activeActivity;
                 if (chatReplyActivity.getName1() != null && chatReplyActivity.getName1().equals(smsNotification.getFrmFn())) {
                     return;
                 }else {
                     DisplayNotification(smsNotification);
                 }
             }else {
                 DisplayNotification(smsNotification);
             }
         } else {
             DisplayNotification(smsNotification);
         }
    }


    public static void setTopic1(String topic1) {
        ChatNotifications.topic1 = topic1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public  void DisplayNotification(ChatNotifications chatNotifications){
        //Log.d("chat_notification_data","hi6");//ChatWebSocket.getInstance().subscribe(chatNotifications.getTopic());
        topic1 = chatNotifications.getTopic();
        Person me = new Person.Builder().setName("You").build();
        Person otherPerson = new Person.Builder().setName(chatNotifications.getFrmFn()).build();
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY).setLabel(context.getString(R.string.say_something)).build();
        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_send, context.getString(R.string.notif_reply), getReplyIntent(context,chatNotifications,remoteInput))
                .addRemoteInput(remoteInput)
                .build();
        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(me);
        if (chatNotifications.getTopic() != null &&chatNotifications.getTopic().contains("grp") ){
            messagingStyle.setConversationTitle("Group Chat");
        }
        chatNotificationsArrayList.add(chatNotifications);
        for (int position = 0;position<chatNotificationsArrayList.size();position++) {
            if (position != 0) {
//                ChannelDB channelDB = ChatViewModel.getInstance().getChannelsByTopic().get(chatNotificationsArrayList.get(position).getTopic());
//                ChannelDB channelDB1 = ChatViewModel.getInstance().getChannelsByTopic().get(chatNotificationsArrayList.get(position).getTopic());
//                isSenderTheSame = chatNotificationsArrayList.get(position - 1).getFrmFn() != null && chatNotificationsArrayList.get(position).getFrmFn() != null
//                        && channelDB1.getName().equals(channelDB.getName());
                isSenderTheSame = chatNotificationsArrayList.get(position - 1).getFrmFn() != null && chatNotificationsArrayList.get(position).getFrmFn() != null
                        && chatNotificationsArrayList.get(position).getFrmFn().equals(chatNotificationsArrayList.get(position - 1).getFrmFn());
                if (isSenderTheSame) {
                    messagingStyle.addMessage(chatNotificationsArrayList.get(position).getMessage(), Long.parseLong(chatNotificationsArrayList.get(position).getTime()), otherPerson);
                    isSenderTheSame = true;
                    String hh = chatNotificationsArrayList.get(position).getFrmFn();
                } else if (chatNotificationsArrayList.get(position).getFrmFn() != null && !isSenderTheSame) {
                    chatNotificationsArrayList.clear();
                    chatNotificationsArrayList.add(chatNotifications);
                    id++;
                    isSenderTheSame = true;
                    for (int position1 = 0; position1 < chatNotificationsArrayList.size(); position1++) {
                        if (position1 != 0) {
//                        isSenderTheSame1 = chatNotificationsArrayList.get(position1).getFrmFn().equals(chatNotificationsArrayList.get(position1 - 1).getFrmFn()) &&
//                                chatNotificationsArrayList.get(position1 - 1).getFrmFn() != null;
                            if (!isSenderTheSame1) {
                            }
                        }
                        messagingStyle = null;
                         messagingStyle = new NotificationCompat.MessagingStyle(me);
                        messagingStyle.addMessage(chatNotificationsArrayList.get(position1).getMessage(), Long.parseLong(chatNotificationsArrayList.get(position1).getTime()), otherPerson);
                    }
                } else if (messagingStyle!= null){
                    messagingStyle.addMessage(chatNotificationsArrayList.get(position).getMessage(), Long.parseLong(chatNotificationsArrayList.get(position).getTime()), me);
                }
            }else {
                //chatNotificationsArrayList.clear();
                //chatNotificationsArrayList.add(chatNotifications);
                messagingStyle.addMessage(chatNotificationsArrayList.get(0).getMessage(), Long.parseLong(chatNotificationsArrayList.get(0).getTime()), otherPerson);
            }
        }
            Intent intent = new Intent(context, ChatActivity.class);
            intent.setAction("SubscribeToTopic");
            intent.putExtra(ChatActivity.CURRENT_CHAT_EXTRA,chatNotifications.getTopic());
        PendingIntent actionIntent = PendingIntent.getActivity(context,chatNotifications.getTopic().hashCode(),intent,PendingIntent.FLAG_IMMUTABLE);
            Intent deleteIntent = new Intent(context,ChatNotificationJobService.class);
        //Intent subscribeIntent = new Intent(context, NotificationBroadcastReceiver.class);
       // subscribeIntent.setAction("SUBSCRIBE_ACTION");
       // subscribeIntent.putExtra("TOPIC_EXTRA", chatNotifications.getTopic());
        //PendingIntent subscribePendingIntent = PendingIntent.getBroadcast(context, 0, subscribeIntent, PendingIntent.FLAG_IMMUTABLE);
            deleteIntent.setAction("DeleteIntent");
        @SuppressLint("WrongConstant") PendingIntent pendingIntent = PendingIntent.getService(context, 0, deleteIntent, PendingIntent.FLAG_MUTABLE);

//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addNextIntentWithParentStack(intent);
//        PendingIntent actionIntent =  stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,AppController.CHAT_CHANNEL)
                    .setContentIntent(actionIntent)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("chat")
                    .setContentText(chatNotifications.getMessage())
                    .setStyle(messagingStyle)
                    .setDeleteIntent(pendingIntent)
                    .setAutoCancel(true)
                    .addAction(replyAction)
                    //.addAction(R.drawable.profile_icon, "Subscribe", subscribePendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(id,builder.build());
    }

    private static PendingIntent getReplyIntent(Context context,ChatNotifications chatNotifications,RemoteInput remoteInput) {
        Intent intent = new Intent(context, ChatNotificationJobService.class);
        @SuppressLint("WrongConstant") PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        Bundle bundle = new Bundle();
        bundle.putParcelable("my_pending_intent_key", pendingIntent);
        PersistableBundle persistableBundle = toPersistableBundle(bundle);
        persistableBundle.putString("message",chatNotifications.getMessage());
        persistableBundle.putString("topic",chatNotifications.getTopic());
        persistableBundle.putString("frm",chatNotifications.getFrm());
        persistableBundle.putString("seq",chatNotifications.getSeq());
        persistableBundle.putString("head",chatNotifications.getHead());
        persistableBundle.putString("time",chatNotifications.getTime());
        persistableBundle.putString("teams",chatNotifications.getTeams());
        persistableBundle.putString("frmFn",chatNotifications.getFrmFn());
        ComponentName componentName = new ComponentName(context,ChatNotificationJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(123,componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setExtras(persistableBundle)
                .build();
        JobScheduler scheduler=(JobScheduler) context. getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS){
        }else {}
        return pendingIntent;
    }
    public static boolean isPersistableBundleType(Object value) {
        return ((value instanceof PersistableBundle) ||
                (value instanceof Integer) || (value instanceof int[]) ||
                (value instanceof Long) || (value instanceof long[]) ||
                (value instanceof Double) || (value instanceof double[]) ||
                (value instanceof String) || (value instanceof String[]) ||
                (value instanceof Boolean) || (value instanceof boolean[])
        );
    }
    public static void putIntoBundle(BaseBundle baseBundle, String key, Object value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Unable to determine type of null values");
        } else if (value instanceof Integer) {
            baseBundle.putInt(key, (int) value);
        } else if (value instanceof int[]) {
            baseBundle.putIntArray(key, (int[]) value);
        } else if (value instanceof Long) {
            baseBundle.putLong(key, (long) value);
        } else if (value instanceof long[]) {
            baseBundle.putLongArray(key, (long[]) value);
        } else if (value instanceof Double) {
            baseBundle.putDouble(key, (double) value);
        } else if (value instanceof double[]) {
            baseBundle.putDoubleArray(key, (double[]) value);
        } else if (value instanceof String) {
            baseBundle.putString(key, (String) value);
        } else if (value instanceof String[]) {
            baseBundle.putStringArray(key, (String[]) value);
        } else if (value instanceof Boolean) {
            baseBundle.putBoolean(key, (boolean) value);
        } else if (value instanceof boolean[]) {
            baseBundle.putBooleanArray(key, (boolean[]) value);
        } else if (value instanceof PersistableBundle) {
            if (baseBundle instanceof PersistableBundle)
                ((PersistableBundle) baseBundle).putPersistableBundle(key, (PersistableBundle)value);
            else if (baseBundle instanceof Bundle)
                ((Bundle) baseBundle).putBundle(key, toBundle((PersistableBundle) value));
        } else {
            throw new IllegalArgumentException("Objects of type " + value.getClass().getSimpleName()
                    + " can not be put into a " + BaseBundle.class.getSimpleName());
        }
    }
    public static Bundle toBundle(PersistableBundle persistableBundle) {
        if (persistableBundle == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putAll(persistableBundle);
        return bundle;
    }
    public static PersistableBundle toPersistableBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        PersistableBundle persistableBundle = new PersistableBundle();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (isPersistableBundleType(value)) {
                putIntoBundle(persistableBundle, key, value);
            }
        }
        return persistableBundle;
    }
}
