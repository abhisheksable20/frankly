package com.sakesnake.frankly.home.postfeeds.postMoreDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class PostDetailsViewModel extends ViewModel {

    private MutableLiveData<ParseUser> parseUserMutableLiveData = new MutableLiveData<>(null);

    private MutableLiveData<Integer> processViewModel = new MutableLiveData<>(0);

    private MutableLiveData<ParseObject> parseObj = new MutableLiveData<>(null);

    public static final int PROCESS_POST_DELETED = 1;

    public static final int PROCESS_REPORT_POST = 2;

    public void setParseUserData(final ParseUser parseUser){
        parseUserMutableLiveData.setValue(parseUser);
    }

    public LiveData<ParseUser> getParseUserData(){
        return parseUserMutableLiveData;
    }

    public void setProcessData(final int workToDo){
        processViewModel.setValue(workToDo);
    }

    public LiveData<Integer> getProcessToDo(){
        return processViewModel;
    }

    // Setting parse object
    public void setParseObj(final ParseObject object){
        parseObj.setValue(object);
    }

    // Getting parse object
    public ParseObject getParseObj(){
        return parseObj.getValue();
    }

}
