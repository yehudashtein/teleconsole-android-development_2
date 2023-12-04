package com.telebroad.teleconsole.viewmodels;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
//import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.repositories.ContactRepository;
import com.telebroad.teleconsole.model.repositories.SMSRepository;

import java.security.MessageDigest;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

public class ConversationViewModel extends SMSViewModel {
    private ContactRepository repository = ContactRepository.getInstance();
    private LiveData<String> name;
    private MediatorLiveData<String> nameTime;

    @BindingAdapter({"mms_image"})
    public static void loadImage(ImageView view, String imageUrl) {
        if (isNullOrEmpty(imageUrl)) {
            return;
        }
        String mimeType =  MimeTypeMap.getFileExtensionFromUrl(imageUrl);
        if (mimeType.equals("jpg") || mimeType.equals("png")||mimeType.equals("gif") || mimeType.equals("webp")
                || mimeType.equals("tiff") || mimeType.equals("raw") || mimeType.equals("heif") || mimeType.equals("jpeg2000")|| mimeType.equals("jpeg")) {
            Glide.with(view.getContext()).applyDefaultRequestOptions(new RequestOptions().transform(new BitmapTransformation() {
                @Override
                protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                   // android.util.Log.d("Glide01", "density " + toTransform.getDensity());
                    toTransform.setDensity(160);
                    return toTransform;
                }

                @Override
                public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

                }
            })).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(view);
        }
    }
    @BindingAdapter({"mms_image_recycler"})
    public static void loadImageForRecyclerView(ImageView view, String imageUrl) {
        if (isNullOrEmpty(imageUrl)) {
            return;
        }
        String mimeType =  MimeTypeMap.getFileExtensionFromUrl(imageUrl);
        if (mimeType.equals("jpg") || mimeType.equals("png")||mimeType.equals("gif") || mimeType.equals("webp")
                || mimeType.equals("tiff") || mimeType.equals("raw") || mimeType.equals("heif") || mimeType.equals("jpeg2000")|| mimeType.equals("jpeg")) {
            Glide.with(view.getContext()).applyDefaultRequestOptions(new RequestOptions().transform(new BitmapTransformation() {
                @Override
                protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                   // android.util.Log.d("Glide01", "density " + toTransform.getDensity());
                    toTransform.setDensity(160);
                    return toTransform;
                }

                @Override
                public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

                }
            })).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(view);
        }
    }


    public CharSequence getMsgData() {
//        String media = "";
//        if (isNotImage() && getItem().getMedia() != null && !getItem().getMedia().isEmpty() && !isWav()) {
//            media = getItem().getMedia().get(0) +"\n";
//        }
//        String msgData = nullToEmpty(getItem().getMsgdata());
//        if ((media + msgData).isEmpty() && isNotImage() && !isWav()){
//            return "(1/1)";
//        }
//        return media + msgData;
        return nullToEmpty(getItem().getMsgdata());
    }

    public boolean isCopyable(){
        return !isNullOrEmpty(getItem().getMsgdata());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ConversationViewModel){
            return getID().equals(((ConversationViewModel)obj).getID());
        }
        return false;
    }

    @Override
    public MutableLiveData<String> getTime() {
        if (getItem().getDirection() == Message.Direction.IN) {
            return super.getTime();
        }

        if (nameTime == null) {
            nameTime = new MediatorLiveData<>();
            nameTime.addSource(super.getTime(), time -> {
//                android.util.Log.d("NameTime", " time " + time + " name ");

                String separator = isNullOrEmpty(getName().getValue()) ? "" : AppController.getInstance().getString(com.telebroad.teleconsole.R.string.separator);
                Utils.updateLiveData(nameTime, AppController.getInstance().getString(com.telebroad.teleconsole.R.string.name_time, nullToEmpty(getName().getValue()), separator, getTimeSegment(time)));
            });
            nameTime.addSource(getName(), nameString -> {
                String separator = isNullOrEmpty(getName().getValue()) ? "" : AppController.getInstance().getString(com.telebroad.teleconsole.R.string.separator);
                Utils.updateLiveData(nameTime, AppController.getInstance().getString(com.telebroad.teleconsole.R.string.name_time, nullToEmpty(nameString), separator, getTimeSegment(super.getTime().getValue())));
            });

        }
        return nameTime;
    }

    public String getTimeSegment(String time){
      //  Log.d("Sending", "msgdata " + getItem().getMsgdata() + " sending " + getItem().isSending() + " " + getID() + " time " + time + " dlr_status " + getItem().getDlr_status() + " dlr_error " + getItem().getDlr_error());
        if (getItem().isSending()){
            return "Sending...";
        }else{
            return nullToEmpty(time);
        }
    }


    @Override
    public String getID() {
        return getItem().getId();
    }
    public int msgVisibility(){
        return msgDataVisibility() == GONE && imagviewVisibility() == GONE && audioVisibility()==GONE? GONE : VISIBLE;
    }

    public int msgDataVisibility() {
        return isNullOrEmpty(getMsgData().toString()) ? GONE : VISIBLE;
    }

    public LiveData<Integer> dlrErrorVisibility(){
       // android.util.Log.d("DLR_VIS", "getting dlr visibility " + isDLRError() + " msgdata " + getItem().getMsgdata());
        if (liveDLRVisibility.getValue() == null){
            updateDLRVisibility();
        }
        return liveDLRVisibility;
    }

    public int imagviewVisibility() {
        return getItem().getMedia() == null || getItem().getMedia().isEmpty() || areAllNotImages(getItem().getMedia()) ? GONE : VISIBLE;
    }
    public boolean isNotImage() {
        String gg = getMMSImage();
        if (!isNullOrEmpty(gg)){
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(getMMSImage()));
            if (!isNullOrEmpty(type)) {
                if (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/jpg") || type.equals("image/gif") || type.equals("image/webp") || type.equals("image/tiff") || type.equals("image/raw") || type.equals("image/bmp") || type.equals("image/heif") || type.equals("image/jpeg2000") || type.equals("image/jfif") || type.equals("image/.jfif") || type.equals("Image")) {
                     return false;
                }else {
                    return true;
                }
            }
        }
        return true;
        //return isNullOrEmpty(getMMSImage());
    }

    public boolean areAllNotImages(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return true;
        }
        for (String url : urls) {
            if (!isNullOrEmpty(url)) {
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
                if (!isNullOrEmpty(type) && Utils.isImageType(type)) {
                    return false;  // Found an image URL in the list
                }
            }
        }
        return true;  // No image URLs found in the list
    }

    public int audioVisibility() {
        return getItem().getMedia() == null || getItem().getMedia().isEmpty() || !isWav() ? GONE : VISIBLE;
    }
    public boolean isWav() {
        String gg = getMMSImage();
        if (!isNullOrEmpty(gg)){
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(getMMSImage()));
            if (!isNullOrEmpty(mimeType)) {
                if (mimeType.equals("audio/x-wav") || mimeType.equals("audio/mpeg")) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
        //return isNullOrEmpty(getMMSImage());
    }

//    private boolean isNotImage() {
//        return isNullOrEmpty(getMMSImage());
//    }

    public LiveData<String> getName() {
        if (name == null) {
            name = repository.findContactByID(getItem().getSent_by());
        }
        return name;
    }

    public void setSent(){
        SMSRepository.getInstance().setSMSasSent(getItem().getTimestamp());
        getItem().setSending(false);
        updateTime();
    }
    public String getMMSImage() {
        List<String> images = getItem().getMedia();
        if (images == null || images.isEmpty()) {
            return "";
        }
        if (images.size() == 1) {
            String imageURL = images.get(0);
//        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(imageURL));
//        if (nullToEmpty(mimetype).startsWith("image")) {
//            return imageURL;
//        }
//        return "";
            return imageURL;
        }
        return images.toString();
    }
    public void checkIfNeedToLoadMore(){

    }
}
