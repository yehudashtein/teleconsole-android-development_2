package com.telebroad.teleconsole.controller.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.MMSImageViewActivity;
import com.telebroad.teleconsole.helpers.SettingsHelper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ChatBaseImgAdapter extends BaseAdapter {
    ArrayList<String> arrayList;
    Context context;
    private String auth = "token", apikey = "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo", secret = SettingsHelper.getString(SettingsHelper.CHAT_TOKEN), domain = "https://apiconnact.telebroad.com";

    public ChatBaseImgAdapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_images,parent,false);
        }
        ImageView imageView = convertView.findViewById(R.id.displayImages);
        String imgUrl = null;
        try {
            imgUrl = domain + arrayList.get(position) + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Glide.with(context).load(imgUrl).into(imageView);
        imageView.setTransitionName(imgUrl);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MMSImageViewActivity.class);
            intent.putExtra(MMSImageViewActivity.MMS_IMAGE_URL, imageView.getTransitionName());
            context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, imageView, imageView.getTransitionName()).toBundle());
        });
        return convertView;
    }
}
