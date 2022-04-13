package com.sakesnake.frankly.home.bottomnavsearch.searchprofiles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parse.ParseUser;

import java.util.List;

// This view model is for search profile fragment
public class SearchProfilesViewModel extends ViewModel {

    private MutableLiveData<List<ParseUser>> parseUsers = new MutableLiveData<>();

    private MutableLiveData<List<ParseUser>> savedSearchedProfile = new MutableLiveData<>();

    // Setting parse users that are retrieved from network
    public void setParseUsers(List<ParseUser> users){
        parseUsers.setValue(users);
    }
    // Getting parse users that are retrieved from network
    public LiveData<List<ParseUser>> getParseUsersFromNetwork(){
        return parseUsers;
    }


    // Setting saved parse users that are retrieved from SQL
    /*
     * This methods are not use
     * Use these methods if you want to store SQL data (Saved Parse Profile)
     *
        public void setSavedSearchedProfile(List<ParseUser> savedSearchedProfile){
            this.savedSearchedProfile.postValue(savedSearchedProfile);
        }
        // Getting saved parse users that are retrieved from SQL
        public LiveData<List<ParseUser>> getSavedProfile(){
            return savedSearchedProfile;
        }
    */

}
