package com.telebroad.teleconsole.viewmodels;

import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

public class ConversationListViewModelFactory implements ViewModelProvider.Factory {

    String myNumber;
    String otherNumber;
    public ConversationListViewModelFactory(String myNumber, String otherNumber){
        this.myNumber = myNumber;
        this.otherNumber = otherNumber;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public ConversationListViewModel create(@NonNull Class modelClass){
        return new ConversationListViewModel(myNumber, otherNumber);
    }


}
