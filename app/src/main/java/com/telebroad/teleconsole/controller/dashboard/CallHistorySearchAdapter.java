package com.telebroad.teleconsole.controller.dashboard;
import static com.google.common.base.Strings.isNullOrEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.telebroad.teleconsole.controller.SmsConversationActivity;
import com.telebroad.teleconsole.databinding.ItemSearchCallHistoryBinding;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.viewmodels.SearchCallHistoryViewModel;

public class CallHistorySearchAdapter extends ListAdapter<SearchCallHistoryViewModel, CallHistorySearchAdapter.ViewModel> {
    private Context context;
    private String query;

    protected CallHistorySearchAdapter(@NonNull DiffUtil.ItemCallback<SearchCallHistoryViewModel> diffCallback, Context context,String query) {
        super(diffCallback);
        this.context = context;
        this.query =query;
    }

    @NonNull
    @Override
    public CallHistorySearchAdapter.ViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CallHistorySearchAdapter.ViewModel(ItemSearchCallHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public int getItemCount() {
        return getCurrentList() == null ? 0 : getCurrentList().size();
    }


    @Override
    public void onBindViewHolder(@NonNull CallHistorySearchAdapter.ViewModel holder, int position) {
        SearchCallHistoryViewModel viewModel = getCurrentList().get(position);
        viewModel.getNameInfo(query, context, result -> {
            if (result instanceof SpannableString) holder.binding.nameTextView.setText(result);
            else if (result instanceof String) holder.binding.nameTextView.setText(result);
        });
        holder.binding.iconImageView.setBackgroundResource(viewModel.getIconBackgroundResource());
        holder.binding.iconImageView.setImageResource(viewModel.getIconResource());
        holder.binding.timeTextView.setText(viewModel.getFormattedTime(context,Long.parseLong(viewModel.getTime())));
        holder.binding.blockView.setVisibility(View.GONE);
        if (!isNullOrEmpty(viewModel.getSnumber()))
        holder.binding.infoTextView.setText(viewModel.formatPhoneNumber(viewModel.getSnumber()) +" "+ viewModel.formatSeconds(Integer.parseInt(viewModel.getDuration())));
        else if(isNullOrEmpty(viewModel.getSnumber()) && !isNullOrEmpty(viewModel.getDnumber())) holder.binding.infoTextView.setText( viewModel.getDnumber() +" "+ viewModel.formatSeconds(Integer.parseInt(viewModel.getDuration())));
        holder.binding.getRoot().setOnClickListener(v -> {
            if (holder.binding.group4.getVisibility() == View.VISIBLE){
                holder.binding.group4.setVisibility(View.GONE);
                holder.binding.group2.setVisibility(View.GONE);
            }else {
                holder.binding.group2.setVisibility(View.VISIBLE);
                holder.binding.group4.setVisibility(View.VISIBLE);
            }
        });
        holder.binding.group1.setOnClickListener(v -> {SipManager.getInstance().call(viewModel.getSnumber(),(Activity) context);});
        holder.binding.group2.setOnClickListener(v -> SmsConversationActivity.show((Activity) context, viewModel.getSnumber()));
        holder.binding.group3.setOnClickListener(v -> {
            FragmentActivity fragmentActivity = (FragmentActivity) context;
            ContactSaveLocationDialog dialog = new ContactSaveLocationDialog();
            dialog.setDefaultPhoneNumber(viewModel.getSnumber());
            dialog.setShowExisting(true);
            dialog.show(fragmentActivity.getSupportFragmentManager(), "callHistContacts");
        });
    }

    class ViewModel extends RecyclerView.ViewHolder{
        private ItemSearchCallHistoryBinding binding;
        public ViewModel(@NonNull ItemSearchCallHistoryBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
    public static class SearchDiff extends DiffUtil.ItemCallback<SearchCallHistoryViewModel>{

        @Override
        public boolean areItemsTheSame(@NonNull SearchCallHistoryViewModel oldItem, @NonNull SearchCallHistoryViewModel newItem) {
            return String.valueOf(oldItem.getId()).equals(String.valueOf(newItem.getId()));
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull SearchCallHistoryViewModel oldItem, @NonNull SearchCallHistoryViewModel newItem) {
            return oldItem.equals(newItem);
        }
    }
}

