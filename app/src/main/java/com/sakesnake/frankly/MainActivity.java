package com.sakesnake.frankly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.parse.ParseUser;

/**
 * This application is single activity only and the rest is all fragments.
 * Application starts from App.class (com.frankly.sakesnake) package
 */
public class MainActivity extends AppCompatActivity {

    private SessionErrorViewModel sessionErrorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionErrorViewModel = new ViewModelProvider(this).get(SessionErrorViewModel.class);

        sessionErrorViewModel.getIsSessionExpired().observe(this, expired->{
            if (expired){
                sessionErrorViewModel.isSessionExpired(false);
                if (ParseUser.getCurrentUser() != null){
                    ParseUser.logOutInBackground(e -> {
                        restartActivity();
                    });
                    return;
                }

                restartActivity();
            }
        });
    }

    private void restartActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}