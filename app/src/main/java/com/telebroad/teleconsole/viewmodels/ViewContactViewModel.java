package com.telebroad.teleconsole.viewmodels;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.style.TtsSpan;

import androidx.core.app.ActivityCompat;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.ViewModel;
import androidx.databinding.Observable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.model.Contact;
import com.telebroad.teleconsole.model.TeleConsoleContact;

public class ViewContactViewModel extends ViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private Contact item;
    public Contact getItem() {
        return item;
    }

    public void setItem(Contact item) {
        this.item = item;
        notifyChange();
    }

    public String getName(){
        if (getItem() == null){
            return "";
        }
        return getItem().getWholeName();
    }

    public Drawable getInitialDrawable(){
//        android.util.Log.d("ViewContact01", "contact " + (getName().isEmpty() ? "empty" : getName()));
        String initial = "?";
        if (!getName().isEmpty()){
            initial = String.valueOf(getName().trim().charAt(0)).toUpperCase();
        }
        if(initial.matches("[(0-9]")){
            initial = "#";
        }
        if (getItem() == null){
            return TextDrawable.builder().buildRound("?", ActivityCompat.getColor(AppController.getInstance(), android.R.color.holo_red_light));
        }
        int color;
        switch (getItem().getType()){
            case "corporate":
                color = R.color.corporateContact;
                break;
            case "personal":
                color = R.color.personalContact;
                break;
            case "mobile":
                color = R.color.phoneContact;
                break;
            default:
                color = R.color.colorPrimary;
                break;
        }
//        android.util.Log.d("ViewContact01", "initial is " + initial);
        return TextDrawable.builder().buildRound(initial, ActivityCompat.getColor(AppController.getInstance(), color));
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }
    void notifyChange() {
        callbacks.notifyCallbacks(this, 0, null);
    }
    void notifyPropertyChanged(int fieldId){
        callbacks.notifyCallbacks(this, fieldId, null);
    }
}
