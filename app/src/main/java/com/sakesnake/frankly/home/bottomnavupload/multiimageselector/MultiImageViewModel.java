package com.sakesnake.frankly.home.bottomnavupload.multiimageselector;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MultiImageViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<Uri>> imageUri = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Uri>> preSelectedImages = new MutableLiveData<>(new ArrayList<>());

    private MutableLiveData<Integer> modifyIntFlag;

    /*
    If true then select multiple image selection
    If false then select single image selection
    */
    private final MutableLiveData<Boolean> multiOrSingleImageSelection = new MutableLiveData<>(false);


    @Deprecated
    public void setSelectedImages(ArrayList<Uri> imagesUri) {
        imageUri.setValue(imagesUri);
    }



    public void setPreSelectedImages(List<Uri> images){
        preSelectedImages.setValue(images);
    }

    public List<Uri> getPreSelectedImages(){
        return preSelectedImages.getValue();
    }

    public LiveData<List<Uri>> observePreSelectedImages(){
        return preSelectedImages;
    }

    // Clearing pre selected images
    public void clearPreSelectedImages(){
        if (preSelectedImages.getValue() != null)
            preSelectedImages.getValue().clear();
    }

    // Setting modify int flag (100:- Modify banner 101:- Modify profile pic)
    public void setModifyIntFlag(final int intFlag){
        if (modifyIntFlag == null)
            modifyIntFlag = new MutableLiveData<>();

        modifyIntFlag.setValue(intFlag);
    }

    // Get modify int flag (value)
    public int getModifyFlag(){
        if (modifyIntFlag == null || modifyIntFlag.getValue() == null)
            return 0;

        return modifyIntFlag.getValue();
    }


    public void setMultiOrSingleImageSelection(boolean multiOrSingle) {
        multiOrSingleImageSelection.setValue(multiOrSingle);
    }

    public LiveData<Boolean> getMultiOrSingleImageSelection() {
        return multiOrSingleImageSelection;
    }

    public boolean getImageSelectionType(){
        return multiOrSingleImageSelection.getValue();
    }

    @Deprecated
    public LiveData<ArrayList<Uri>> getImagesUri() {
        return imageUri;
    }

    @Deprecated
    public ArrayList<Uri> getImagesUris() {
        if (imageUri != null) {
            if (imageUri.getValue().size() > 0) {
                return imageUri.getValue();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
