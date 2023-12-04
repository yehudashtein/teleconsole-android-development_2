package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.telebroad.teleconsole.model.FullPhone;

public class FullPhoneViewModel extends ViewModel {
    public MutableLiveData<FullPhone> fullPhone = FullPhone.getLiveInstance();
}
