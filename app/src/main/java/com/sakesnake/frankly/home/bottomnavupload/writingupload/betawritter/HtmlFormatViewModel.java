package com.sakesnake.frankly.home.bottomnavupload.writingupload.betawritter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HtmlFormatViewModel extends ViewModel {

    private final MutableLiveData<String> htmlString = new MutableLiveData<>();

    private final MutableLiveData<String> htmlDescriptionString = new MutableLiveData<>();

    public void setHtmlString(String html){
        htmlString.setValue(html);
    }
    public LiveData<String> getHtmlFormat(){
        return htmlString;
    }
    public String getHardValue(){
        if (htmlString.getValue() == null)
            return "";
        else
            return htmlString.getValue();
    }


    public void setHtmlDescriptionString(String description){
        htmlDescriptionString.setValue(description);
    }
    public LiveData<String> getHtmlDescriptionString(){
        return htmlDescriptionString;
    }
    public String getDescriptionHardValue(){
        if (htmlDescriptionString.getValue() == null)
            return "";
        else
            return htmlDescriptionString.getValue();
    }
}
