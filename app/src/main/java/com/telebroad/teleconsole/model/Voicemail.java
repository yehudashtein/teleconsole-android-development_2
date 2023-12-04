package com.telebroad.teleconsole.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import android.os.AsyncTask;
import android.util.Base64;

import com.android.volley.Request;
import com.telebroad.teleconsole.BuildConfig;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.repositories.VoicemailRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.GET;
import static com.google.common.base.Strings.isNullOrEmpty;

@Entity(tableName = "voicemail", primaryKeys = "id")
public class Voicemail extends Message {
    private int duration;
    private String mailbox;
    private String caller;
    private String callerid;
    private String callerid_ext;
    private String name;
    private String read_by;
    private String base64encoded;
    private String transcription;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallerid() {
        return callerid;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }

    public String getCallerid_ext() {
        return callerid_ext;
    }

    public void setCallerid_ext(String callerid_ext) {
        this.callerid_ext = callerid_ext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRead_by() {
        return read_by;
    }

    public void setRead_by(String read_by) {
        this.read_by = read_by;
    }

    public String getBase64encoded() {
        return base64encoded;
    }

    public void setBase64encoded(String base64encoded) {
        this.base64encoded = base64encoded;
    }


    @Override
    public String toString() {
        return "Voicemail{" +
                "duration=" + duration +
                "\n mailbox=" + mailbox +
                "\n caller=" + caller +
                "\n callerid=" + callerid +
                "\n callerid_ext=" + callerid_ext +
                "\n name=" + name +
                "\n read_by=" + read_by +
                '}';
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.VOICEMAIL;
    }

    public void  downloadVoicemail(com.telebroad.teleconsole.helpers.Consumer<String> completionHandler, Consumer<TeleConsoleError> errorHandler){
        if (!isNullOrEmpty(base64encoded)){
            completionHandler.accept(base64encoded);
            return;
        }
        Map<String, String> params = getParams();
        long time = System.currentTimeMillis();
        URLHelper.request(GET, URLHelper.GET_VOICEMAILFILE_URL, params, (result) -> {
            base64encoded = result.getAsJsonObject().get(URLHelper.KEY_DATA).getAsString();
            String transcription = result.getAsJsonObject().get("transcription").getAsString();
            VoicemailRepository.getInstance().setTranscription(transcription, this);
//            for (Map.Entry<String, JsonElement> entry : result.getAsJsonObject().entrySet()){
//                android.util.Log.d("VMTest", "entry key " + entry.getKey());
//                if (entry.getKey().equals("data")){
//                    continue;
//                }
//                android.util.Log.d("VMTest", "entry = " + entry.getKey() + ": " + entry.getValue());
//             }
            AsyncTask.execute(() -> {
                saveToStorage();
                //android.util.Log.d("Voicemail01", "success after " + (System.currentTimeMillis() - time) + " on " + getName() );
                completionHandler.accept(base64encoded);
            });

        }, (error) -> {
            errorHandler.accept(error);
           // android.util.Log.e("Voicemail01", "server error " + error.getFullErrorMessage() + " after " + (System.currentTimeMillis() - time));
        });
    }

    public void delete(){
        Map<String, String> params = getParams();
        //android.util.Log.d("VoicemailDelete", " params = " + getParams());
        URLHelper.request(Request.Method.DELETE, URLHelper.GET_VOICEMAILFILE_URL, params, (result) -> android.util.Log.d("VoicemailDelete", "Result " + result), (error) -> android.util.Log.d("VoicemailDelete", "error " + error.getFullErrorMessage())) ;

    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_MAILBOX, getMailbox());
        params.put(URLHelper.KEY_FILE, getName());
        params.put(URLHelper.KEY_DIR, getDirection() == Message.Direction.IN ? "INBOX" : "Old");
        return params;
    }

    @Ignore
    private int tries = 0;
    public void saveVoicemail(){
        AsyncTask.execute( () -> {
            if (tries >= 3){
                return;
            }
            if (base64encoded == null){
                downloadVoicemail((base64encoded) -> {
                    tries++;
                    saveVoicemail();
                }, teleConsoleError -> {

                });
            }else {
                saveToStorage();
            }
        });
    }

    public void saveToStorage() {
        String root = Utils.getRootFolder() + File.separator + "TeleConsole" + File.separator + "Voicemails";
        File filedirs = new File(root);
        final File filepath = new File(filedirs.getAbsoluteFile() + File.separator + getName() + ".wav");
        filedirs.mkdirs();

        try {
            FileOutputStream fos = new FileOutputStream(filepath, true);
            fos.write(Base64.decode(base64encoded, 0));
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }
}
