package com.telebroad.teleconsole.controller.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.MMSImageViewActivity;
import com.telebroad.teleconsole.helpers.SettingsHelper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ChatImgListAdapter extends ArrayAdapter {
ArrayList<String> arrayList;
    private String auth = "token", apikey = "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo", secret = SettingsHelper.getString(SettingsHelper.CHAT_TOKEN), domain = "https://apiconnact.telebroad.com";

    public ChatImgListAdapter(@NonNull Context context, int resource, ArrayList<String> arrayList) {
        super(context, resource);
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_images,parent,false);
        }
        try {
            ImageView imageView = convertView.findViewById(R.id.displayImages);
            String imgUrl = domain + arrayList.get(position) + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
            Glide.with(getContext()).load(imgUrl).into(imageView);
            imageView.setTransitionName(imgUrl);
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MMSImageViewActivity.class);
                intent.putExtra(MMSImageViewActivity.MMS_IMAGE_URL, imageView.getTransitionName());
                getContext().startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(), imageView, imageView.getTransitionName()).toBundle());
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return arrayList.get(position);
    }
}

