package com.sakesnake.frankly.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SessionErrorViewModel extends ViewModel {

    private final MutableLiveData<Boolean> sessionExpiredMLData = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> hideBottomNavView = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> loggedOutFromSession = new MutableLiveData<>(false);


    // Setting is session expired
    public void isSessionExpired(boolean expired){
        sessionExpiredMLData.setValue(expired);
    }
    public LiveData<Boolean> getIsSessionExpired(){
        return sessionExpiredMLData;
    }

    // Setting is to hide bottom nav view
    public void setIsToHideBottomNavView(boolean hide){
        hideBottomNavView.setValue(hide);
    }
    public LiveData<Boolean> getIsToHideBottomNavView(){
        return hideBottomNavView;
    }

    // Setting is logged out from session
    public void isLoggedOut(boolean loggedOut){
        loggedOutFromSession.setValue(loggedOut);
    }

    // Getting is logged from session
    public LiveData<Boolean> isLoggedFromSession(){
        return loggedOutFromSession;
    }
}
