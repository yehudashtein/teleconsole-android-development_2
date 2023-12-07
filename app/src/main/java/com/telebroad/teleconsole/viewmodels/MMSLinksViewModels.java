package com.telebroad.teleconsole.viewmodels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Strings.isNullOrEmpty;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.telebroad.teleconsole.controller.SmsTumbnailHashMapWrapper;
import com.telebroad.teleconsole.helpers.Utils;

import java.security.MessageDigest;
import java.util.HashMap;


public class MMSLinksViewModels {

    private static HashMap<String, Bitmap> thumbnailCache;// = new HashMap<>();

    private String url;
    private static final String TAG = "yehuda-MMSLinksViewModels";

    public String url() {
        return url;
    }
    public int isVideo(){
        if (!isNullOrEmpty(url) && Utils.isVidoeType(MimeTypeMap.getFileExtensionFromUrl(url))) return VISIBLE;
        return GONE;
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
        //view.setDrawingCacheEnabled(true);
        Log.d(TAG, "setCustomAttribute: called");
        if (isNullOrEmpty(imageUrl)) {
            return;
        }
        String mimeType =  MimeTypeMap.getFileExtensionFromUrl(imageUrl);
        if(Utils.isImageType(mimeType)) {
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

        else if (Utils.isVidoeType(mimeType)) {
            if (thumbnailCache == null){
                thumbnailCache = new SmsTumbnailHashMapWrapper(view.getContext().getCacheDir().getPath()).hashMap;
            }

            Bitmap cachedBitmap = thumbnailCache.get(imageUrl);
            if (cachedBitmap == null) {
                //if (view.get)

                RetrieveVideoFrameTask rf = new RetrieveVideoFrameTask(view, imageUrl);
                rf.execute(imageUrl);
            }
            else {
                Glide.with(view.getContext()).load(cachedBitmap).into(view);
            }


        }

    }


    public static class RetrieveVideoFrameTask extends AsyncTask<String, Void, Bitmap> {

        private MediaMetadataRetriever retriever;

        public ImageView view;
        public String url;

        public RetrieveVideoFrameTask(ImageView view, String url){
            this.view = view;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            retriever = new MediaMetadataRetriever();
            try {
                // Use one of these methods
                retriever.setDataSource(params[0], new HashMap<String, String>());
                // or retriever.setDataSource(params[0]);

                // Extract a frame
                Bitmap bitmap = retriever.getFrameAtTime();
                retriever.release();
                return bitmap;


            } catch (Exception ex) {
                // Handle exceptions
            } finally {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //view.setImageBitmap(bitmap);
//            Glide.with(view.getContext()).applyDefaultRequestOptions(new RequestOptions().transform(new BitmapTransformation() {
//                @Override
//                protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
//                    // android.util.Log.d("Glide01", "density " + toTransform.getDensity());
//                    toTransform.setDensity(80);
//                    return toTransform;
//                }
//
//                @Override
//                public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
//
//                }
//            })).load(bitmap).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(view);
//            view.setImageBitmap(bitmap);
            if (bitmap != null) {
                thumbnailCache.put(url, bitmap); // Cache the thumbnail
                Glide.with(view.getContext()).load(bitmap).into(view);
            }
        }
    }




}
