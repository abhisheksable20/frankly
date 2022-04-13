package com.sakesnake.frankly.permissionmessage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StoragePermissionViewModel extends ViewModel
{
    private MutableLiveData<Boolean> storagePermission;

    public StoragePermissionViewModel()
    {
        if (storagePermission == null)
        {
            storagePermission = new MutableLiveData<>();
        }
        storagePermission.setValue(false);
    }

    public void setStoragePermission(boolean permission)
    {
        storagePermission.setValue(permission);
    }

    public LiveData<Boolean> getStoragePermission()
    {
        return storagePermission;
    }
}
