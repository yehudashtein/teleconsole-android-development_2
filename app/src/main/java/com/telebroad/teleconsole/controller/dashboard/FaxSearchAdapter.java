package com.telebroad.teleconsole.controller.dashboard;
import static com.google.common.base.Strings.isNullOrEmpty;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.FaxOpenActivity;
import com.telebroad.teleconsole.databinding.ItemSearchCallHistoryBinding;
import com.telebroad.teleconsole.helpers.IntentHelper;
import com.telebroad.teleconsole.viewmodels.FaxSearchViewModel;

import java.util.Locale;

public class FaxSearchAdapter extends ListAdapter<FaxSearchViewModel, FaxSearchAdapter.ViewModel> {
    private Context context;
    String query;

    protected FaxSearchAdapter(@NonNull DiffUtil.ItemCallback<FaxSearchViewModel> diffCallback, Context context,String query) {
        super(diffCallback);
        this.context = context;
        this.query=query;
    }

    @NonNull
    @Override
    public FaxSearchAdapter.ViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        parent.getLayoutTransition().setAnimateParentHierarchy(false);
        return new FaxSearchAdapter.ViewModel(ItemSearchCallHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewModel holder, int position) {
        FaxSearchViewModel viewModel = getCurrentList().get(position);
        holder.binding.iconImageView.setBackgroundResource(R.drawable.bg_fax_icon);
        holder.binding.iconImageView.setImageResource(R.drawable.ic_fax);
        holder.binding.infoTextView.setTextColor(context.getResources().getColor(viewModel.getStatusColor(),null));
        holder.binding.infoTextView.setText(viewModel.getInfo());
        holder.binding.timeTextView.setText(viewModel.getFormattedTime(context,Long.parseLong(viewModel.getTime())));
        viewModel.findOtherNumber().getName(null).observe((LifecycleOwner) context, s -> {
            if (!isNullOrEmpty(s)) {
                if (query != null) {
                    SpannableString spannableString = new SpannableString(s);
                    int startIdx = s.toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                    int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
                    if (startIdx != -1) {
                        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    holder.binding.nameTextView.setText(spannableString);
                }
            }
        });
        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(context, FaxOpenActivity.class);
            intent.putExtra(IntentHelper.MESSAGE_ID, viewModel.getId());
            Bundle transitionBundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
            context.startActivity(intent, transitionBundle);
        });

    }

    class ViewModel extends RecyclerView.ViewHolder{
        private ItemSearchCallHistoryBinding binding;
        public ViewModel(@NonNull ItemSearchCallHistoryBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
    public static class SearchDiff extends DiffUtil.ItemCallback<FaxSearchViewModel>{

        @Override
        public boolean areItemsTheSame(@NonNull FaxSearchViewModel oldItem, @NonNull FaxSearchViewModel newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull FaxSearchViewModel oldItem, @NonNull FaxSearchViewModel newItem) {
            return oldItem.equals(newItem);
        }
    }
}
