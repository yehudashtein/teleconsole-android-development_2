package com.telebroad.teleconsole.viewmodels;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.View;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.Message;
import com.telebroad.teleconsole.model.PhoneNumber;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static com.google.common.base.Strings.nullToEmpty;

public abstract class MessageViewModel<Item extends Message>  implements Observable {

    private Item item;
    private final PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    private PhoneNumber otherNumber;
    private PhoneNumber myNumber;
    private Handler timeHandler;
    private Runnable timeRunnable;
    private LiveData<String> updatableName;
    private MediatorLiveData<String> name;

    public final PhoneNumber getOtherNumber() {
        if (otherNumber == null) {
            otherNumber = findOtherNumber();
        }
        return otherNumber;
    }

    public final PhoneNumber getMyNumber() {
        if (myNumber == null) {
            myNumber = findMyNumber();
        }
        return myNumber;
    }

    protected abstract PhoneNumber findOtherNumber();

    protected abstract PhoneNumber findMyNumber();

    public abstract boolean isNew();

    public String type() {
        return getItem().getMessageType().name();
    }

    @Bindable
    public LiveData<String> getOtherName() {
        if (name == null) {
            name = new MediatorLiveData<>();
            LiveData<String> currentName = getOtherNumber().getName(null);
            name.addSource(currentName, name::setValue);
        }
        return getOtherNumber().getName(null);
    }

    private MutableLiveData<String> liveTime;

    public LiveData<List<? extends Contact>> getMatchedContacts() {
        if (matchedContacts == null) {
            matchedContacts = getOtherNumber().getMatchedContacts();
        }
        return matchedContacts;
    }

    private LiveData<List<? extends Contact>> matchedContacts;

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public int getNotificationID() {
        return getMyNumber().fixed().hashCode() + getOtherNumber().fixed().hashCode();
    }

    public String getID() {
        return getItem().getId();
    }

    public boolean isNotList() {
        //TODO make abstract and override
//        return this instanceof CallHistoryViewModel || this instanceof ConversationViewModel;
        return true;
    }

    @Bindable
    public MutableLiveData<String> getTime() {
        if (liveTime == null) {
            liveTime = new MutableLiveData<>();
//            long now = System.currentTimeMillis();
//            long time = getItem().getTimeStamp() * 1000;
//            Utils.updateLiveData(liveTime,DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString());
//            //liveTime.setValue(DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString());
//            long updateTime;
//            long elapsedTime = now - time;
//            if (elapsedTime < MINUTE_IN_MILLIS){
//                updateTime = SECOND_IN_MILLIS;
//            }else if (elapsedTime < HOUR_IN_MILLIS){
//                updateTime = MINUTE_IN_MILLIS;
//            }else if (elapsedTime < DAY_IN_MILLIS){
//                updateTime = HOUR_IN_MILLIS;
//            }else{
//                updateTime = DAY_IN_MILLIS;
//            }
//            timeHandler = new Handler();
//            timeRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    liveTime.postValue(DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString());
//                    timeHandler.postDelayed(this, updateTime);
//                }
//            };
//            timeHandler.postDelayed(timeRunnable, updateTime - (elapsedTime % updateTime));
        }
        updateTime();
        return liveTime;
    }

    public int getStatusColor(){
        return R.color.colorPrimary;
    }
    protected void updateTime() {
        long seconds = getItem().getTimeStamp();
        long millis = seconds * 1000;
        if (System.currentTimeMillis() - millis < (60 * 1000)) {
            Utils.updateLiveData(liveTime, AppController.getInstance().getString(R.string.just_now));
        } else {
            String day = "";
            boolean isToday = DateUtils.isToday(millis);
            if (DateUtils.isToday(millis + DAY_IN_MILLIS)) {
                if (!isNotList()) {
                    Utils.updateLiveData(liveTime, AppController.getInstance().getString(R.string.yesterday));
                    return;
                }
                isToday = true;
                day = AppController.getInstance().getString(R.string.yesterday) + ", ";
            }
            String d = day + DateUtils.formatDateTime(AppController.getInstance(), millis, ((isNotList() || isToday) ? FORMAT_SHOW_TIME : 0) | (isToday ? 0 : FORMAT_SHOW_DATE) | FORMAT_ABBREV_MONTH);
            Utils.updateLiveData(liveTime, day + DateUtils.formatDateTime(AppController.getInstance(), millis, ((isNotList() || isToday) ? FORMAT_SHOW_TIME : 0) | (isToday ? 0 : FORMAT_SHOW_DATE) | FORMAT_ABBREV_MONTH));
        }
    }

    @Override
    protected void finalize() {
        if (timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    public String getFullDate() {
        return DateUtils.formatDateTime(AppController.getInstance(), getItem().getTimeStamp() * 1000, FORMAT_SHOW_TIME | FORMAT_SHOW_DATE | FORMAT_SHOW_WEEKDAY);
    }

    public Drawable getIcon() {
        return getDrawable(getIconResource());
    }

    public Drawable getDrawable(@DrawableRes int resource) {
        return ContextCompat.getDrawable(AppController.getInstance(), resource);
//        return AppController.getInstance().getDrawable(resource);
    }

    public abstract int getIconResource();

    public abstract String getInfo();

    public abstract void deleteItem();

    // Will be overridden in some classes
    public boolean isBlocked(){
        return false;
    }

    public int getBlockedVisibility(){
        return isBlocked() ? View.VISIBLE : View.GONE;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof MessageViewModel) {
            return getID().equals(((MessageViewModel) obj).getID());
        }
        return false;
    }

    public abstract int getIconBackgroundResource();
//    public void prepare(){
//        AsyncTask.execute(() -> {
//            getOtherName();
//        });
//    }

    public boolean matches(String query) {
        long start = System.currentTimeMillis();
        //android.util.Log.i("Match01", "starting at " + start);
        if (getOtherNumber().fixed() != null && getOtherNumber().fixed().contains(PhoneNumber.fix(query))) {
           // android.util.Log.i("Match010", "true after " + (System.currentTimeMillis() - start));
            return true;
        } else if (nullToEmpty(getOtherName().getValue()).toLowerCase().contains(query.toLowerCase())) {
          //  android.util.Log.i("Match011", "true after " + (System.currentTimeMillis() - start));
            return true;
        } else if (getMatchedContacts().getValue() != null) {
            for (Contact contact : getMatchedContacts().getValue()) {
                if (contact.getWholeName() != null && contact.getWholeName().toLowerCase().contains(query.toLowerCase())) {
                    return true;
                }
            }
        }
        //android.util.Log.i("Match013", "false after " + (System.currentTimeMillis() - start));
        return false;
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    public void deleteFromList() {
        deleteItem();
    }
    //    public MediatorLiveData<Integer> getGroup3Icon(){
//        if (group3Icon == null){
//            group3Icon = new MediatorLiveData<>();
//            group3Icon.addSource(getMatchedContacts(), contacts -> {
//                if (contacts.size() == 0){
//                    group3Icon.setValue(R.drawable.ic_person_add);
//                }else{
//                    group3Icon.setValue(R.drawable.ic_person_black);
//                }
//            });
//        }
//        return group3Icon;
//    }
//
//    public void setGroup3Icon(MediatorLiveData<Integer> group3Icon){
//        this.group3Icon = group3Icon;
//    }
    void notifyChange() {
        callbacks.notifyCallbacks(this, 0, null);
    }

    void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }


    public abstract void checkIfNeedToLoadMore();
}
