package com.telebroad.teleconsole.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.SettingsHelper;

import java.util.ArrayList;
import java.util.List;


public class MentionAdapter extends ArrayAdapter<String> {
    String charAtZero;
    String filterPattern;
    Handler handler = new android.os.Handler(Looper.getMainLooper());
    private List<String> MentionItemsFull;
    public MentionAdapter(@NonNull Context context, List<String> MentionItemList) {
        super(context, 0, MentionItemList);
        MentionItemsFull = new ArrayList<>(MentionItemList);
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.auto_complete_row,parent,false);
            TextView textView = convertView.findViewById(R.id.country_name);
            ImageView imageView = convertView.findViewById(R.id.flag);
            String mentionItem = getItem(position);

            if (mentionItem != null){
                textView.setText(mentionItem);
                imageView.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(mentionItem));
            }
        }
        return convertView;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<String> suggestions = new ArrayList<>();
            if (constraint != null){
                filterPattern = constraint.toString().toLowerCase().trim();
            }
            if (filterPattern.contains(" ")) {
                //suggestions.clear();
            }else {


                for (String item : MentionItemsFull) {
                    suggestions.clear();
                    if (filterPattern.length() > 1) {
                        charAtZero = Character.toString(filterPattern.charAt(1));
                    }

//                    if (constraint != null|| constraint.length() > 0){
//                        suggestions.addAll(MentionItemsFull);
//                    }
                    if (charAtZero != null && item.toLowerCase().startsWith(charAtZero)) {
                        handler.post(() -> {
                            clear();
                            suggestions.add(item);
                        });

                    }
                }
                if (filterPattern.startsWith("@") && charAtZero == null) {
                    suggestions.addAll(MentionItemsFull);
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null){
                handler.post(() -> {
                    clear();
                    addAll((List)results.values);
                    notifyDataSetChanged();
                });

            }

        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
//            BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.YELLOW);
//            SpannableString spannableString = new SpannableString(((String)resultValue).getCountryName());
//            int i = ((CountryItem)resultValue).getCountryName().indexOf("");
//            spannableString.setSpan(backgroundColorSpan,0,i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //return "@"+((CountryItem)resultValue).getCountryName()+" ";
            return "@"+ resultValue;
        }
    };
}
