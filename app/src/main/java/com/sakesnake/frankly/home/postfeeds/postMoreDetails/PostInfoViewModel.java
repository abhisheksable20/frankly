package com.sakesnake.frankly.home.postfeeds.postMoreDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parse.ParseUser;

// View model for PostMoreInfoFragment
@Deprecated
public class PostInfoViewModel extends ViewModel {

    private MutableLiveData<ParseUser> parseUserMutableLiveData = new MutableLiveData<>(null);

    // Setting the parse user that has benn tapped by the current user and also including current user
    public void setParseUser(final ParseUser parseUser){
        parseUserMutableLiveData.setValue(parseUser);
    }

    // Getting the setted parse user including current parse user
    public LiveData<ParseUser> getParseUser(){
        return parseUserMutableLiveData;
    }

}
