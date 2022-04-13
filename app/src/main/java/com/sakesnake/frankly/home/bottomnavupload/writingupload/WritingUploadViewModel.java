package com.sakesnake.frankly.home.bottomnavupload.writingupload;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WritingUploadViewModel extends ViewModel
{
    MutableLiveData<Integer> setSelectedPosition;

    public WritingUploadViewModel()
    {
        setSelectedPosition = new MutableLiveData<>();
    }

    public void setPosition(int position)
    {
        setSelectedPosition.setValue(position);
    }

    public LiveData<Integer> getSelectedPosition()
    {
        return setSelectedPosition;
    }
}
