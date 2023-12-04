package com.telebroad.teleconsole.viewmodels;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.graphics.Bitmap;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;

import java.security.MessageDigest;

public class MMSLinksViewModels {
    private String url;

    public String url() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
//    public void createInstance(String url){
//        this.url = url;
//    }
    public static MMSLinksViewModels createInstance(String url) {
        MMSLinksViewModels instance = new MMSLinksViewModels();
        instance.setUrl(url);
        return instance;
    }
    @BindingAdapter("customAttribute")
    public static void setCustomAttribute(ImageView view, String imageUrl) {
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
}
