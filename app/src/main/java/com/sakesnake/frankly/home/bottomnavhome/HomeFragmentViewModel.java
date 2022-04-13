package com.sakesnake.frankly.home.bottomnavhome;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parse.ParseObject;

import java.util.List;

public class HomeFragmentViewModel extends ViewModel {

    private final MutableLiveData<List<ParseObject>> imagesOrMemesData = new MutableLiveData<>(null);

    private final MutableLiveData<List<ParseObject>> blogOrNewsData = new MutableLiveData<>(null);

    private final MutableLiveData<List<ParseObject>> dreamsOrIdeasData = new MutableLiveData<>(null);

    private final MutableLiveData<List<ParseObject>> quotesOrQuestionData = new MutableLiveData<>(null);

    private final MutableLiveData<List<ParseObject>> storyOrPoemData = new MutableLiveData<>(null);

    private final MutableLiveData<Integer> refreshData = new MutableLiveData<>(101);

    public static final int REFRESH_LOCAL_AND_NETWORK_DATA = 100;

    public static final int REFRESH_NETWORK_DATA = 101;

    public static final int NO_REFRESHING = 0;


    // Setting Refresh Data
    // How does the refresh data works
    // 100 :- Retrieve data from from both localDataStore And from Network. App is just started
    // 101 :- Just refresh the data only using network mode. User has pulled Swipe To Refresh Layout
    // 0 :- Do noting nothing
    public void setRefreshData(int howToRefresh){
        this.refreshData.setValue(howToRefresh);
    }
    // Getting How To Refresh Data
    public LiveData<Integer> getRefreshType(){
        return this.refreshData;
    }

    // Setting ImageOrMeme Data (Here full fields will be retrieved)
    public void setImagesOrMemesData(List<ParseObject> imagesOrMemesData){
        this.imagesOrMemesData.setValue(imagesOrMemesData);
    }
    // Getting ImagesOrMeme Data
    public LiveData<List<ParseObject>> getImageOrMemeData(){
        return this.imagesOrMemesData;
    }

    // Setting BlogOrNews Data (Only selected fields will be retrieved)
    public void setBlogOrNewsData(List<ParseObject> blogOrNewsData){
        this.blogOrNewsData.setValue(blogOrNewsData);
    }
    // Getting BlogOrNews Data
    public LiveData<List<ParseObject>> getBlogOrNewsData(){
        return this.blogOrNewsData;
    }

    // Setting DreamOrIdeas Data (Only selected fields will be retrieved)
    public void setDreamsOrIdeasData(List<ParseObject> dreamsOrIdeasData){
        this.dreamsOrIdeasData.setValue(dreamsOrIdeasData);
    }
    // Getting DreamsOrIdeas Data
    public LiveData<List<ParseObject>> getDreamsOrIdeasData(){
        return this.dreamsOrIdeasData;
    }

    // Setting QuotesOrQuestion Data (Here full fields will be retrieved)
    public void setQuotesOrQuestionData(List<ParseObject> quotesOrQuestionData){
        this.quotesOrQuestionData.setValue(quotesOrQuestionData);
    }
    // Getting QuotesOrQuestion Data()
    public LiveData<List<ParseObject>> getQuotesOrQuestionData(){
        return this.quotesOrQuestionData;
    }

    // Setting StoryOrPoem Data (Only selected fields will be retrieved)
    public void setStoryOrPoemData(List<ParseObject> storyOrPoemData){
        this.storyOrPoemData.setValue(storyOrPoemData);
    }
    // Getting StoryOrPoem Data
    public LiveData<List<ParseObject>> getStoryOrPoemData(){
        return this.storyOrPoemData;
    }

}
