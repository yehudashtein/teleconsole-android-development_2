package com.telebroad.teleconsole.controller;

import android.annotation.SuppressLint;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.pjsip.SipManager;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.PhoneNumber;
import com.telebroad.teleconsole.model.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.telebroad.teleconsole.model.PhoneNumber.PhoneType.OTHER;

public class PhoneActionBottomSheetDialog extends ListBottomSheetDialog<PhoneNumber> {
    public static PhoneActionBottomSheetDialog getInstance(Contact contact) {
        return getInstance(contact.getAllLines());
    }

    public static PhoneActionBottomSheetDialog getInstance(PhoneNumber... numbers){
        return getInstance(Arrays.asList(numbers));
    }

    public static PhoneActionBottomSheetDialog getInstance(List<PhoneNumber> numbers){
        PhoneActionBottomSheetDialog phoneActionBottomSheetDialog = new PhoneActionBottomSheetDialog();
        phoneActionBottomSheetDialog.items = numbers;
        return phoneActionBottomSheetDialog;
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return new PhoneActionBottomSheetDialog.PhoneNumberAdapter(items);
    }

    class PhoneNumberAdapter extends RecyclerView.Adapter<PhoneNumberAdapter.PhoneNumberViewHolder> {

        List<PhoneNumber> numbers;
        List<PhoneAction> phoneActions = new ArrayList<>();

        PhoneNumberAdapter(@NonNull List<PhoneNumber> numbers) {
            this.numbers = numbers;
           // android.util.Log.d("BottomSheet", "creating adapter phoneLineSize " + numbers.size());
            for (PhoneNumber number : numbers) {
                if (number.getPhoneType() == null) {
                    number.setPhoneType(OTHER);
                }
                switch (number.getPhoneType()) {
                    case FAX:
                        phoneActions.add(new PhoneAction(number, Action.FAX));
                        break;
                    case MOBILE:
                        phoneActions.add(new PhoneAction(number, Action.CALL));
                        phoneActions.add(new PhoneAction(number, Action.SMS));
                        break;
                    case HOME:
                    case WORK:
                    case MAIN:
                    case OTHER:
                        phoneActions.add(new PhoneAction(number, Action.CALL));
                        if (number.fixed().length() >= 11){
                            phoneActions.add(new PhoneAction(number, Action.SMS));
                            phoneActions.add(new PhoneAction(number, Action.FAX));
                        }
                        break;
                    case EXTENSION:
                        phoneActions.add(new PhoneAction(number, Action.CALL));
                        break;
                }
            }
           // android.util.Log.d("BottomSheet", "finished adding actions " + phoneActions.size());
        }

        @NonNull
        @Override
        public PhoneNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phone_action, parent, false);

            return new PhoneNumberViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull PhoneNumberViewHolder holder, int position) {
            //android.util.Log.d("BottomSheet", "binding");
            PhoneAction action = phoneActions.get(position);
            holder.textView.setText(action.action.actionString + " " + action.phoneNumber.formatted());
            holder.icon.setImageResource(action.action.icon);
            holder.action = action;
        }

        @Override
        public int getItemCount() {
            //android.util.Log.d("BottomSheet", "get item count = " + phoneActions.size());
            return phoneActions.size();
        }

        class PhoneAction {
            PhoneNumber phoneNumber;
            Action action;

            PhoneAction(PhoneNumber phoneNumber, Action action) {
                this.phoneNumber = phoneNumber;
                this.action = action;
            }

            PhoneAction(String phoneNumber, Action action) {
                this(PhoneNumber.getPhoneNumber(phoneNumber), action);
            }

            @Override
            public String toString() {
                return "PhoneAction{" +
                        "phoneNumber=" + phoneNumber +
                        "\n action=" + action +
                        '}';
            }
        }

        class PhoneNumberViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView icon;
            PhoneAction action;
            public PhoneNumberViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.text1);
                icon = itemView.findViewById(R.id.icon);
                itemView.setOnClickListener(e -> {
                    if (action == null) {
                        return;
                    }
                    switch (action.action) {
                        case CALL:
                            SipManager.getInstance().call(action.phoneNumber.fixed());
                            break;
                        case FAX:
                            NewFaxActivity.showNewFaxActivity(getActivity(), action.phoneNumber.fixed());
                            break;
                        case SMS:
                            if (Settings.getInstance() == null || isNullOrEmpty(Settings.getInstance().getDefaultSMSLine())) {
                                break;
                            }
                            SmsConversationActivity.show(getActivity(), PhoneNumber.getPhoneNumber(Settings.getInstance().getDefaultSMSLine()), action.phoneNumber);
                            break;
                    }
                    dismiss();
                    //android.util.Log.d("Clicked", "In PhoneNumberViewHolder " + action.toString());
                });
            }
        }
    }

    enum Action {
        CALL("Call", R.drawable.ic_call),
        FAX("Send Fax to", R.drawable.ic_fax),
        SMS("Send SMS to", R.drawable.ic_message);
        String actionString;
        @DrawableRes int icon;
        Action(String actionString, int icon) {
            this.actionString = actionString;
            this.icon = icon;
        }
    }
}
