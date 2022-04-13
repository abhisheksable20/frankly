package com.sakesnake.frankly.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parse.ParseUser;

public class ParseViewModel extends ViewModel {
    private final MutableLiveData<ParseUser> currentUser;

    public ParseViewModel() {
        this.currentUser = new MutableLiveData<>(null);
    }

    public void setCurrentUser(ParseUser user){
        this.currentUser.setValue(user);
    }

    public LiveData<ParseUser> getCurrentUser(){
        return currentUser;
    }

    public ParseUser getLatestUserData(){
        if (currentUser.getValue() == null)
            return null;
        else
            return currentUser.getValue();
    }
}
