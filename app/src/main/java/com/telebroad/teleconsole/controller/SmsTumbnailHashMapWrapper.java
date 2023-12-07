package com.telebroad.teleconsole.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class SmsTumbnailHashMapWrapper {
    String cachedir;
    public HashMap<String, Bitmap> hashMap;
    static final String fileName = "/videoTumbnails";
    File file;

    public SmsTumbnailHashMapWrapper(String cachedir){
        cachedir = cachedir;
        file = new File(cachedir + fileName);
        if (file.exists())
            hashMap = loadHashMapObject(file);
        else hashMap = new HashMap<String, Bitmap>();
    }



    private void saveHashMapObject() {
        try {
            if (hashMap == null) return;

            HashMap<String, byte[]> toSave = new HashMap<String, byte[]>();
            hashMap.forEach((key, value) -> {
                toSave.put(key, bitmapToByteArray(value));
            });

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(toSave);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static HashMap<String, Bitmap> loadHashMapObject(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String, byte[]> fromFile = (HashMap<String, byte[]>) ois.readObject();

            HashMap<String, Bitmap> tumbnailHashMap = new HashMap<String, Bitmap>();
            fromFile.forEach((key, value) -> {
                tumbnailHashMap.put(key, byteArrayToBitmap(value));
            });
            ois.close();
            fis.close();
            return tumbnailHashMap;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        saveHashMapObject();
    }

    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    private static Bitmap byteArrayToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



}
