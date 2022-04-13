package com.sakesnake.frankly.home.bottomnavupload.connectedusers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ConnectedUsersViewModel extends ViewModel {

    private MutableLiveData<List<ParseUser>> parseConnectedUsers = new MutableLiveData<>(new ArrayList<>());

    public void connectUser(ParseUser userObject) {
        parseConnectedUsers.getValue().add(userObject);
        parseConnectedUsers.setValue(parseConnectedUsers.getValue());
    }

    public void removeUser(int position) {
        parseConnectedUsers.getValue().remove(position);
    }

    public List<ParseUser> getConnectedUser() {
        return parseConnectedUsers.getValue();
    }

    public LiveData<List<ParseUser>> getConnectedUsers() {
        return parseConnectedUsers;
    }
}
