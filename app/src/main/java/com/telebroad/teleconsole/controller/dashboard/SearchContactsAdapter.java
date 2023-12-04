package com.telebroad.teleconsole.controller.dashboard;

import static com.google.common.base.Strings.isNullOrEmpty;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.ViewContactActivity;
import com.telebroad.teleconsole.databinding.ItemSearchContactBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneContact;
import com.telebroad.teleconsole.model.SearchContactsModel;
import com.telebroad.teleconsole.model.TeleConsoleContact;

import java.util.List;
import java.util.Locale;

public class  SearchContactsAdapter<T extends Contact> extends ListAdapter<T , SearchContactsAdapter<T>.ViewModel> {
    private final Context context;
    private String query;

    protected SearchContactsAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, Context context,String query) {
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
        return new ViewModel(ItemSearchContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewModel holder, int position) {
        List<T> hgh = getCurrentList();
        holder.binding.getRoot().setVisibility(View.VISIBLE);
        if (getCurrentList().get(position) instanceof SearchContactsModel) {
            holder.binding.getRoot().setVisibility(View.VISIBLE);
            holder.binding.contactName2.setVisibility(View.VISIBLE);
            SearchContactsModel contactsModel = (SearchContactsModel) getCurrentList().get(position);
            if (!isNullOrEmpty(contactsModel.getPhoto()))
                Glide.with(context).load(contactsModel.getPhoto()).circleCrop().into(holder.binding.contactInitial);
            else if (!isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getPhoto()))
                holder.binding.contactInitial.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(contactsModel.getFname()));
            else if (isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getPhoto()) && !isNullOrEmpty(contactsModel.getUsername()))
                holder.binding.contactInitial.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(contactsModel.getUsername()));
            if (!isNullOrEmpty(contactsModel.getFname()) && !isNullOrEmpty(contactsModel.getLname()) && query != null) {
                holder.binding.contactName2.setVisibility(View.VISIBLE);
                SpannableString spannableString = new SpannableString(contactsModel.getFname());
                int startIdx = contactsModel.getFname().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
                if (startIdx != -1) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                holder.binding.contactName.setText(spannableString);
                SpannableString spannableString1 = new SpannableString(contactsModel.getLname());
                int startIdx1 = contactsModel.getLname().indexOf(query.toLowerCase(Locale.ROOT));
                int endIdx1 = startIdx1 + query.toLowerCase(Locale.ROOT).length();
                if (startIdx1 != -1) {
                    spannableString1.setSpan(new StyleSpan(Typeface.BOLD), startIdx1, endIdx1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                holder.binding.contactName2.setText(spannableString1);
            } else if (isNullOrEmpty(contactsModel.getFname()) && !isNullOrEmpty(contactsModel.getLname()) && query != null) {
                holder.binding.contactName2.setVisibility(View.GONE);
                SpannableString spannableString1 = new SpannableString(contactsModel.getLname());
                int startIdx1 = contactsModel.getLname().indexOf(query.toLowerCase(Locale.ROOT));
                int endIdx1 = startIdx1 + query.toLowerCase(Locale.ROOT).length();
                if (startIdx1 != -1) {
                    spannableString1.setSpan(new StyleSpan(Typeface.BOLD), startIdx1, endIdx1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                holder.binding.contactName.setText(spannableString1);
            } else if (!isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getLname())) {
                holder.binding.contactName2.setVisibility(View.GONE);
                if (query != null){
                SpannableString spannableString1 = new SpannableString(contactsModel.getFname());
                int startIdx1 = contactsModel.getFname().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                int endIdx1 = startIdx1 + query.toLowerCase(Locale.ROOT).length();
                if (startIdx1 != -1) {
                    spannableString1.setSpan(new StyleSpan(Typeface.BOLD), startIdx1, endIdx1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                holder.binding.contactName.setText(spannableString1);
                }
            } else if (!isNullOrEmpty(contactsModel.getWork()) && isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getLname())) {
                holder.binding.contactName.setText(contactsModel.formatPhoneNumber(contactsModel.getWork()));
                holder.binding.contactName2.setVisibility(View.GONE);
            }
            else if (isNullOrEmpty(contactsModel.getWork()) && !isNullOrEmpty(contactsModel.getHome()) && isNullOrEmpty(contactsModel.getFname())
                    && isNullOrEmpty(contactsModel.getLname())) {
                holder.binding.contactName2.setVisibility(View.GONE);
            }else {
                holder.binding.contactName2.setVisibility(View.GONE);
                holder.binding.contactName.setText(contactsModel.getExtension());
            }
            if (!isNullOrEmpty(contactsModel.getWork())) {
                holder.binding.contactNumber.setText(contactsModel.formatPhoneNumber(contactsModel.getWork()));
            }
            else if (!isNullOrEmpty(contactsModel.getHome()) && isNullOrEmpty(contactsModel.getWork())){
                holder.binding.contactNumber.setText(contactsModel.formatPhoneNumber(contactsModel.getHome()));
            }
            holder.binding.contactType.setText(contactsModel.setType(contactsModel.getContactType(), context));
            holder.binding.getRoot().setOnClickListener(v -> {
                try {
                    Intent viewContactIntent = new Intent(context, ViewContactActivity.class);
                    Bundle transistionBundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
                    viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_ID, String.valueOf(contactsModel.getId()));
                    viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_TYPE, contactsModel.getContactType());
                    context.startActivity(viewContactIntent, transistionBundle);
                } catch (Exception e) {e.printStackTrace();}

            });
        }else if (getCurrentList().get(position) instanceof PhoneContact) {
                PhoneContact phoneContact1 = (PhoneContact) getCurrentList().get(position);
                        holder.binding.getRoot().setVisibility(View.VISIBLE);
                        if (!isNullOrEmpty(phoneContact1.getPhotoURI()))
                            Glide.with(context).load(phoneContact1.getPhotoURI()).circleCrop().into(holder.binding.contactInitial);
                        else if (!isNullOrEmpty(phoneContact1.getWholeName())) {
                            holder.binding.contactInitial.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(phoneContact1.getWholeName()));
//                            holder.binding.contactName.setText(phoneContact1.getWholeName());
                            SpannableString spannableString = new SpannableString(phoneContact1.getWholeName());
                            int startIdx = phoneContact1.getWholeName().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                            int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
                            if (startIdx != -1) {
                                spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            holder.binding.contactName.setText(spannableString);
                            holder.binding.contactName2.setVisibility(View.GONE);
                        }
                        holder.binding.contactNumber.setText(phoneContact1.getTelephoneLines().get(0).formatted());
                        holder.binding.contactType.setText(phoneContact1.getType());
                        holder.binding.getRoot().setOnClickListener(v -> {
                            Intent viewContactIntent = new Intent(context, ViewContactActivity.class);
                            Bundle transistionBundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
                            viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_ID, String.valueOf(phoneContact1.getID()));
                            viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_TYPE, phoneContact1.getType());
                            context.startActivity(viewContactIntent, transistionBundle);
                        });
        }else if (getCurrentList().get(position) instanceof TeleConsoleContact){
            holder.binding.getRoot().setVisibility(View.VISIBLE);
            holder.binding.contactName2.setVisibility(View.VISIBLE);
            TeleConsoleContact contactsModel = (TeleConsoleContact) getCurrentList().get(position);
            if (!isNullOrEmpty(contactsModel.getPhoto()))
                Glide.with(context).load(contactsModel.getPhoto()).circleCrop().into(holder.binding.contactInitial);
            else if (!isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getPhoto()))
                holder.binding.contactInitial.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(contactsModel.getFname()));
            else if (isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getPhoto()) && !isNullOrEmpty(contactsModel.getUsername()))
                holder.binding.contactInitial.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(contactsModel.getUsername()));
            if (!isNullOrEmpty(contactsModel.getFname()) && !isNullOrEmpty(contactsModel.getLname()) && query != null) {
                holder.binding.contactName2.setVisibility(View.VISIBLE);
                SpannableString spannableString = new SpannableString(contactsModel.getFname());
                int startIdx = contactsModel.getFname().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                int endIdx = startIdx + query.toLowerCase(Locale.ROOT).length();
                if (startIdx != -1) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                holder.binding.contactName.setText(spannableString);
                SpannableString spannableString1 = new SpannableString(contactsModel.getLname());
                int startIdx1 = contactsModel.getLname().indexOf(query.toLowerCase(Locale.ROOT));
                int endIdx1 = startIdx1 + query.toLowerCase(Locale.ROOT).length();
                if (startIdx1 != -1) {
                    spannableString1.setSpan(new StyleSpan(Typeface.BOLD), startIdx1, endIdx1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                holder.binding.contactName2.setText(spannableString1);
            } else if (isNullOrEmpty(contactsModel.getFname()) && !isNullOrEmpty(contactsModel.getLname()) && query != null) {
                holder.binding.contactName2.setVisibility(View.GONE);
                SpannableString spannableString1 = new SpannableString(contactsModel.getLname());
                int startIdx1 = contactsModel.getLname().indexOf(query.toLowerCase(Locale.ROOT));
                int endIdx1 = startIdx1 + query.toLowerCase(Locale.ROOT).length();
                if (startIdx1 != -1) {
                    spannableString1.setSpan(new StyleSpan(Typeface.BOLD), startIdx1, endIdx1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                holder.binding.contactName.setText(spannableString1);
            } else if (!isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getLname())) {
                holder.binding.contactName2.setVisibility(View.GONE);
                if (query != null){
                    SpannableString spannableString1 = new SpannableString(contactsModel.getFname());
                    int startIdx1 = contactsModel.getFname().toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT));
                    int endIdx1 = startIdx1 + query.toLowerCase(Locale.ROOT).length();
                    if (startIdx1 != -1) {
                        spannableString1.setSpan(new StyleSpan(Typeface.BOLD), startIdx1, endIdx1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    holder.binding.contactName.setText(spannableString1);
                }
            } else if (!isNullOrEmpty(contactsModel.getWork()) && isNullOrEmpty(contactsModel.getFname()) && isNullOrEmpty(contactsModel.getLname())) {
                holder.binding.contactName.setText(contactsModel.getWork());
                holder.binding.contactName2.setVisibility(View.GONE);
            }
            else if (isNullOrEmpty(contactsModel.getWork()) && !isNullOrEmpty(contactsModel.getHome()) && isNullOrEmpty(contactsModel.getFname())
                    && isNullOrEmpty(contactsModel.getLname())) {
                holder.binding.contactName2.setVisibility(View.GONE);
            }else {
                holder.binding.contactName2.setVisibility(View.GONE);
                holder.binding.contactName.setText(contactsModel.getExtension());
            }
            if (!isNullOrEmpty(contactsModel.getWork())) {
                holder.binding.contactNumber.setText(contactsModel.getWork());
                // Log.d("letsSee1",contactsModel.getWork());
            }
            else if (!isNullOrEmpty(contactsModel.getHome()) && isNullOrEmpty(contactsModel.getWork())){
                holder.binding.contactNumber.setText(contactsModel.getHome());
                //  Log.d("letsSee12",contactsModel.getHome());
            }
            holder.binding.contactType.setText(contactsModel.getContactType());
            holder.binding.getRoot().setOnClickListener(v -> {
                try {
                    Intent viewContactIntent = new Intent(context, ViewContactActivity.class);
                    Bundle transistionBundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_bottom, R.anim.slide_out_top).toBundle();
                    viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_ID, String.valueOf(contactsModel.getId()));
                    viewContactIntent.putExtra(ViewContactActivity.EXTRA_VIEW_CONTACT_TYPE, contactsModel.getContactType());
                    context.startActivity(viewContactIntent, transistionBundle);
                } catch (Exception e) {e.printStackTrace();}

            });
        }
    }

    class ViewModel extends RecyclerView.ViewHolder{
        private final ItemSearchContactBinding binding;
        public ViewModel(@NonNull ItemSearchContactBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }
    }
    public static class SearchDiff<T extends Contact> extends DiffUtil.ItemCallback<T>{

        @Override
        public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            if (oldItem instanceof SearchContactsModel && newItem instanceof SearchContactsModel){
                SearchContactsModel old = (SearchContactsModel) oldItem;
                SearchContactsModel newI = (SearchContactsModel) newItem;
                return String.valueOf(old.getId()).equals(String.valueOf(newI.getId()));
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            if (oldItem instanceof SearchContactsModel && newItem instanceof SearchContactsModel){
                SearchContactsModel old = (SearchContactsModel) oldItem;
                SearchContactsModel newI = (SearchContactsModel) newItem;
                return old.getPbx_line().equals(newI.getPbx_line());
            }

            String jbj = "";
            return false;
        }
    }
}
