package com.telebroad.teleconsole.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.helpers.Consumer;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.repositories.SMSRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

@Entity(tableName = "sms", primaryKeys = {"id", "timestamp"})

public class SMS extends Message {
    private int idx;
    @SerializedName("new")
    private int isNew;
    private int read;
    private String sender;
    private String receiver;
    private String msgdata;
    private String sent_by;
    private String read_by;
    private String seen;
    private String dlr_status;
    private String dlr_error;
    private long dlr_time;
    private String lid = "x";
    private boolean sending = false;
    private boolean blocked;
    @SerializedName("media")
    @TypeConverters(MediaConverter.class)
    private ArrayList<String> media;
//    TODO commented out as not priority
//    private boolean failed = false;
//    boolean sending = false;
//    @Ignore
//    MutableLiveData<Boolean> finishedSending = new MutableLiveData<>();
    public SMS(long timestamp, String receiver, String sender, String msgdata, Direction direction) {
        this.setTimestamp(timestamp);
        this.setId(String.valueOf(timestamp));
        this.setReceiver(PhoneNumber.fix(receiver));
        this.setSender(PhoneNumber.fix(sender));
        TeleConsoleProfile.getInstance();
        this.setSent_by(nullToEmpty(TeleConsoleProfile.getInstance().getPbxUid()));
        this.setMsgdata(msgdata);
        this.setDirection(direction);
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsgdata() {
        return msgdata;
    }

    public void setMsgdata(String msgdata) {
        this.msgdata = msgdata;
    }

    public String getSent_by() {
        return sent_by;
    }

    public void setSent_by(String sent_by) {
        this.sent_by = sent_by;
    }

    public String getRead_by() {
        return read_by;
    }

    public void setRead_by(String read_by) {
        this.read_by = read_by;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = nullToEmpty(lid);
    }

    public boolean isSending() {
        return sending;
    }

    public void setSending(boolean sending) {
        this.sending = sending;
    }

    @Override
    public String toString() {
        return "SMS{" +
                "idx=" + idx +
                "\n isNew=" + isNew +
                "\n sender=" + sender +
                "\n receiver=" + receiver +
                "\n msgdata=" + msgdata +
                "\n sent_by=" + sent_by +
                "\n read_by=" + read_by +
                "\n seen=" + seen +
                "\n lid=" + lid +
                '}';
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.SMS;
    }

    public void send(Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
         send(null, resultHandler, completionHandler);
    }

    public void send(List<MMSMedia> media, Consumer<JsonElement> resultHandler, Consumer<TeleConsoleError> completionHandler) {
        Map<String, String> params = new HashMap<>();
        params.put(URLHelper.KEY_SMSLINE, PhoneNumber.fix(getSender()));
        params.put(URLHelper.KEY_RECEIVER, PhoneNumber.fix(getReceiver()));
        params.put(URLHelper.KEY_MSGDATA, msgdata);
        if (media != null && !media.isEmpty()){
            params.put("media", new Gson().toJson(media));
        }
        setLid("sent");
        SMSRepository.getInstance().addSMSToConversation(this);
        // If the sender is not included in the list of active sms channels, make it active.
        if (Settings.getInstance() == null){
            completionHandler.accept(new TeleConsoleError.CustomError( 703, "Settings is null"));
            return;
        }
        if (!Settings.getInstance().getSmsLines().contains(new Line(sender))){
          //  android.util.Log.d("CONVO1", "Sender is not here");
            List<Line> smsLines = Settings.getInstance().getSmsLines();
            Line senderLine = PubnubInfo.getInstance().getLine(sender);
            smsLines.add(senderLine);
            Settings.getInstance().setSmsLines(smsLines);
            PubnubInfo.getInstance().subscribeToChannels(senderLine);
        }
        // TODO commented out as is not priority
        sending = true;
        //android.util.Log.d("Send09", "msgdata " + msgdata + " sending " + sending + " " + hashCode());
       // android.util.Log.d("Send09", params.toString());
        URLHelper.request(Request.Method.POST, URLHelper.SEND_SMS_URL, params, result -> {
            SMSRepository.getInstance().loadConversationFromServer(getSender(), getReceiver());
            resultHandler.accept(result);
        }, error -> {
            if (error != null) {
                //     failed = true;
            }
            //Utils.updateLiveData(finishedSending, true);
            completionHandler.accept(error);
        });
    }

    @TypeConverters(MediaConverter.class)
    public ArrayList<String> getMedia() {
        return media;
    }

    @TypeConverters(MediaConverter.class)
    public void setMedia(ArrayList<String> media) {
        this.media = media;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public static class MediaConverter {
        @TypeConverter
        public String fromMedia(ArrayList<String> media) {
            if (media == null || media.isEmpty()) {
                return "";
            }
            return new Gson().toJson(media);
        }

        @TypeConverter
        public ArrayList<String> toMedia(String media) {
            if (isNullOrEmpty(media)) {
                return null;
            }
            return new Gson().fromJson(media, new TypeToken<List<String>>() {
            }.getType());
        }
    }

    public static class MMSMedia {
        final static int KILO_BYTES_IN_BYTES = 1024;
        final static int MAX_MMS_IMAGE_SIZE = 850 * KILO_BYTES_IN_BYTES;
        private String name;
        private String mimetype;
        private String value;
        public MMSMedia(String name, String mimetype, String value) {
            this.name = name;
            this.mimetype = mimetype;
            this.value = value;
        }


        public static SMS.MMSMedia compressImage(String imageFilepath, @NonNull Consumer<String> errorHandler, @NonNull Consumer<Bitmap> resultHandler) throws FileNotFoundException {
            if (isNullOrEmpty(imageFilepath)) {
                errorHandler.accept("empty file name");
                return null;
            }
            File file = new File(imageFilepath);
            long length = file.length();
         //   android.util.Log.d("PIX01", "b " + length);
            String extension = MimeTypeMap.getFileExtensionFromUrl(imageFilepath);
            // If the image is less then 850 kb, and it is a file type supported by MMS we don't need compression
            if (length < MAX_MMS_IMAGE_SIZE && isMMSSupported(imageFilepath)) {
                errorHandler.accept("");
                return new SMS.MMSMedia(file.getName(), MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension), base64encode(new FileInputStream(file)));
            } else {
                // If the mime type is a gif we need to do gif compression
                if (extension.equals("gif")) {
                    // TODO handle gif compression
                    errorHandler.accept("Wrong file type");
                    return null;
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                if (bm == null){
                    errorHandler.accept("Unable to get image");
                    return null;
                }
               // android.util.Log.d("PIX02", "Before " + bm.getByteCount() + " bytes");
                // We need to compress the image anyway, we might as well compress it with the best format for MMS - JPEG and in the process convert the file to a .jpg
                // We only need to do this if the image is not yet a .jpg so we check if it is a jpg
                if (!extension.equals("jpg") && !extension.equals("jpeg")) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bm = BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size(), options);
                    // After converting to JPG check if it is small enough to send
                    if (bm.getByteCount() < MAX_MMS_IMAGE_SIZE) {
                        errorHandler.accept("");
                        resultHandler.accept(bm);
                        return new MMSMedia(file.getName(), "image/jpeg", Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
                    }
                }
                // At this point we should only have jpg files that are larger then 850kb, We need to compress it down to 850kb.
                // We will not touch the compression ratio, rather we resize the JPEG to the optimal size for MMS, 1080w x 1920h
                // All images at this point will be larger then 1080x1920, as JPEGs at that size even at 100% quality are about 420kb
                int width = bm.getWidth();
                int height = bm.getHeight();
                float aspectRatio = (float) height / width;
                // Aspect ratio of 1080 by 1920 is 1.78, if the actual aspect ratio is less then this, scale by width
                float scale = aspectRatio < 1.78f ? 1080.0f / width : 1920.0f / height;
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
//                    bm.setWidth(newWidth);
//                    bm.setHeight(newHeight);
                bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                String filename = file.getName();
                filename = filename.substring(0, filename.lastIndexOf(".")).concat(".jpg");
               // android.util.Log.d("PIX02", "After " + bm.getByteCount() + " bytes filename " + filename);
                errorHandler.accept("");
                resultHandler.accept(bm);
                return new MMSMedia(filename, "image/jpeg", Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            }
        }


        private static boolean isMMSSupported(String fileName) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
            return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("gif");
        }

        public static String base64encode(InputStream inputStream) {
            byte[] bytes;
            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {

            }
            bytes = outputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMimetype() {
            return mimetype;
        }

        public void setMimetype(String mimetype) {
            this.mimetype = mimetype;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "MMSMedia{" +
                    "\n name=" + name +
                    "\n mimetype=" + mimetype +
                    "\n value=" + value +
                    '}';
        }
    }

    public String getDlr_status() {
        return dlr_status;
    }

    public void setDlr_status(String dlr_status) {
        this.dlr_status = dlr_status;
    }

    public String getDlr_error() {
        return dlr_error;
    }

    public void setDlr_error(String dlr_error) {
        this.dlr_error = dlr_error;
    }

    public long getDlr_time() {
        return dlr_time;
    }

    public void setDlr_time(long dlr_time) {
        this.dlr_time = dlr_time;
    }
// TODO commented out as is not priority
//    public boolean isFailed() {
//        return failed;
//    }
//
//    public void setFailed(boolean failed) {
//        this.failed = failed;
//    }
}
