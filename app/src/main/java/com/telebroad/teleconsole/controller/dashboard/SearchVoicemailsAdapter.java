package com.telebroad.teleconsole.controller.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.VoicemailOpenActivity;
import com.telebroad.teleconsole.databinding.ItemSearchCallHistoryBinding;
import com.telebroad.teleconsole.helpers.IntentHelper;
import com.telebroad.teleconsole.viewmodels.SearchVoicemailViewModel;

public class SearchVoicemailsAdapter extends ListAdapter<SearchVoicemailViewModel,SearchVoicemailsAdapter.ViewModel> {
    private final Context context;
    private String query;

    protected SearchVoicemailsAdapter(@NonNull DiffUtil.ItemCallback<SearchVoicemailViewModel> diffCallback, Context context,String query) {
        super(diffCallback);
        this.context = context;
        this.query = query;
    }

    @NonNull
    @Override
    public SearchVoicemailsAdapter.ViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        parent.getLayoutTransition().setAnimateParentHierarchy(false);
        return new SearchVoicemailsAdapter.ViewModel(ItemSearchCallHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewModel holder, int position) {
        SearchVoicemailViewModel viewModel = getCurrentList().get(position);
        holder.binding.iconImageView.setBackgroundResource(R.drawable.bg_voicemail_icon);
        holder.binding.iconImageView.setImageResource(R.drawable.ic_voicemail);
        //holder.binding.nameTextView.setText(viewModel.getUserInfo(query));
        CharSequence nameInfo = viewModel.getUserInfo(query);
        if (nameInfo instanceof SpannableString) holder.binding.nameTextView.setText((SpannableString) nameInfo);
        else if (nameInfo instanceof String) holder.binding.nameTextView.setText(nameInfo);
        holder.binding.infoTextView.setText(viewModel.getInfo());
        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(context, VoicemailOpenActivity.class);
            intent.putExtra(IntentHelper.MESSAGE_ID, viewModel.getTime());
            Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
            context.startActivity(intent, transitionBundle);
        });

    }


    class ViewModel extends RecyclerView.ViewHolder{
        private final ItemSearchCallHistoryBinding binding;
        public ViewModel(@NonNull ItemSearchCallHistoryBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
    public static class SearchDiff extends DiffUtil.ItemCallback<SearchVoicemailViewModel>{

        @Override
        public boolean areItemsTheSame(@NonNull SearchVoicemailViewModel oldItem, @NonNull SearchVoicemailViewModel newItem) {
            return oldItem.getTime().equals(newItem.getTime());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull SearchVoicemailViewModel oldItem, @NonNull SearchVoicemailViewModel newItem) {
            return oldItem.equals(newItem);
        }
    }
}
