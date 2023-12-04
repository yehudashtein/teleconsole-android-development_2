package com.telebroad.teleconsole.controller.dashboard;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.databinding.ItemSearchCallHistoryBinding;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.SearchSms;
import com.telebroad.teleconsole.model.SearchSmsConversationsModel;
import com.telebroad.teleconsole.viewmodels.SearchSmsMessageViewModel;
import com.telebroad.teleconsole.viewmodels.SmsConversationsSearchViewModel;

import java.util.List;


public class SmsConversationsSearchAdapter<T extends SearchSms> extends ListAdapter<T, SmsConversationsSearchAdapter<T>.ViewModel> {
    private Context context;
    private String query;

    protected SmsConversationsSearchAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, Context context,String query) {
        super(diffCallback);
        this.context = context;
        this.query = query;
    }

    @Override
    public void submitList(@Nullable List<T> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public ViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //parent.getLayoutTransition().setAnimateParentHierarchy(false);
        return new ViewModel(ItemSearchCallHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewModel holder, int position) {
        List<T> jhhn = getCurrentList();
        if (getCurrentList().get(position) instanceof SmsConversationsSearchViewModel) {
            SmsConversationsSearchViewModel viewModel = (SmsConversationsSearchViewModel)getCurrentList().get(position);
            holder.binding.iconImageView.setBackgroundResource(R.drawable.bg_sms_icon);
            holder.binding.iconImageView.setImageResource(R.drawable.ic_baseline_message_24);
            holder.binding.infoTextView.setText(viewModel.getDataInfo(query));
            holder.binding.timeTextView.setText(viewModel.getFormattedTime(context, viewModel.getTime()));
            CharSequence nameInfo = viewModel.getNameInfo(query, context);
            if (nameInfo instanceof SpannableString) holder.binding.nameTextView.setText((SpannableString) nameInfo);
            else if (nameInfo instanceof String) holder.binding.nameTextView.setText(nameInfo);
            holder.binding.getRoot().setOnClickListener(v -> {
                SmsConversationActivity.show((Activity) context, viewModel.findMyNumber(), viewModel.findOtherNumber());
                //Toast.makeText(context, "1 "+String.valueOf(viewModel.getSender()), Toast.LENGTH_SHORT).show();
               // if (viewModel.isIn())
                   // SmsConversationActivity.show((Activity) context, String.valueOf(viewModel.getSender()));
              //  else
                    //SmsConversationActivity.show((Activity) context, String.valueOf(viewModel.getReceiver()));
            });
        }else if (getCurrentList().get(position) instanceof SearchSmsMessageViewModel){
            SearchSmsMessageViewModel smsMessageViewModel =(SearchSmsMessageViewModel) getCurrentList().get(position);
            holder.binding.iconImageView.setBackgroundResource(R.drawable.bg_sms_icon);
            holder.binding.iconImageView.setImageResource(R.drawable.ic_baseline_message_24);
            holder.binding.infoTextView.setText(smsMessageViewModel.getDataInfo(query));
            holder.binding.timeTextView.setText(smsMessageViewModel.getFormattedTime(context, smsMessageViewModel.getTime()));
            CharSequence nameInfo = smsMessageViewModel.getNameInfo(query, context);
            if (nameInfo instanceof SpannableString) holder.binding.nameTextView.setText((SpannableString) nameInfo);
            else if (nameInfo instanceof String) holder.binding.nameTextView.setText(nameInfo);
            holder.binding.getRoot().setOnClickListener(v -> {
                SmsConversationActivity.show((Activity) context, smsMessageViewModel.findMyNumber(), smsMessageViewModel.findOtherNumber());
//                Toast.makeText(context, "2"+String.valueOf(smsMessageViewModel.getSender()), Toast.LENGTH_SHORT).show();
//                if (smsMessageViewModel.isIn()) SmsConversationActivity.show((Activity) context, String.valueOf(smsMessageViewModel.getSender()));
//                else SmsConversationActivity.show((Activity) context, String.valueOf(smsMessageViewModel.getReceiver()));
//                Toast.makeText(context, String.valueOf(smsMessageViewModel.getSender()), Toast.LENGTH_SHORT).show();
            });
        }
    }

    class ViewModel extends RecyclerView.ViewHolder{
        private final ItemSearchCallHistoryBinding binding;
        public ViewModel(@NonNull ItemSearchCallHistoryBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
    public static class SearchDiff<T extends SearchSms> extends DiffUtil.ItemCallback<T>{

        @Override
        public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            if (oldItem instanceof SmsConversationsSearchViewModel && newItem instanceof SmsConversationsSearchViewModel){
                SmsConversationsSearchViewModel old = (SmsConversationsSearchViewModel) oldItem;
                SmsConversationsSearchViewModel newI = (SmsConversationsSearchViewModel) newItem;
                return String.valueOf(old.getId()).equals(String.valueOf(newI.getId()));
            }
            else return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return areItemsTheSame(oldItem,newItem);
        }
    }
}
