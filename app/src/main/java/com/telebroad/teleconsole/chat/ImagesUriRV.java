package com.telebroad.teleconsole.chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.telebroad.teleconsole.chat.server.CtrlMessage;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.ChatActivity;
import com.telebroad.teleconsole.controller.dashboard.ChatReplyActivity;
import com.telebroad.teleconsole.databinding.ChatImagesItemToSendBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import java.io.UnsupportedEncodingException;

public class ImagesUriRV extends ListAdapter<CtrlMessage,ImagesUriRV.ViewHolder> {
    private final Context context;
    private final String secret = SettingsHelper.getString(SettingsHelper.CHAT_TOKEN);

    public ImagesUriRV(@NonNull DiffUtil.ItemCallback<CtrlMessage> diffCallback, Context context) {
        super(diffCallback);
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ChatImagesItemToSendBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            String auth = "token";
            String apikey = "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo";
            String domain = "https://apiconnact.telebroad.com";
            String newURL = domain + getCurrentList().get(position).getParams().getUrl() + "?apikey=" + apikey + "&auth=" + auth + "&secret=" + SettingsHelper.encodeValue(secret);
            Glide.with(context).asBitmap().load(newURL).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.binding.imgPdf);
        } catch (UnsupportedEncodingException e) {e.printStackTrace();}
        holder.binding.imgCancel.setOnClickListener(v -> {
            Activity activeActivity = AppController.getInstance().getActiveActivity();
            if (activeActivity != null && !AppController.getInstance().isActiveActivityPaused()) {
                if (activeActivity instanceof ChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) activeActivity;
                    chatActivity.getCtrls().remove(position);
                    notifyItemRemoved(position);
                   if (getItemCount() == 0) chatActivity.ClearViews();
                } else if (activeActivity instanceof ChatReplyActivity) {
                    ChatReplyActivity chatReplyActivity = (ChatReplyActivity) activeActivity;
                    chatReplyActivity.getCtrls().remove(position);
                    notifyItemRemoved(position);
                    if (getItemCount() == 0) chatReplyActivity.ClearViews();
                }
            }
           // ChatActivity chatActivity= (ChatActivity)context;
//            chatActivity.getCtrls().remove(position);
//            notifyItemRemoved(position);
//            if (getItemCount() == 0) chatActivity.ClearViews();
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ChatImagesItemToSendBinding binding;
        public ViewHolder(@NonNull ChatImagesItemToSendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class DIFF_CALLBACK extends DiffUtil.ItemCallback<CtrlMessage>{
        @Override
        public boolean areItemsTheSame(@NonNull CtrlMessage oldItem, @NonNull CtrlMessage newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CtrlMessage oldItem, @NonNull CtrlMessage newItem) {
            return oldItem.getParams().getUrl().equals(newItem.getParams().getUrl());
        }
    }
}
