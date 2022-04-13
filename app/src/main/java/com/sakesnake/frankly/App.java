package com.sakesnake.frankly;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.parse.Parse;
import com.parse.ParseInstallation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// This class will be loaded first when the application started
public class App extends Application
{
    private static ExecutorService EXECUTORS_SERVICE;

    private static Handler MAIN_THREAD_HANDLER;

    private static ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .enableLocalDataStore()
                .build());

        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("GCMSenderId",getString(R.string.sender_id));
        parseInstallation.saveInBackground();

        EXECUTORS_SERVICE = Executors.newCachedThreadPool();
        SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
        MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());
    }

    // get scheduled thread executor used for repeating tasks
    public static ScheduledExecutorService getScheduledExecutorService(){
        return SCHEDULED_EXECUTOR_SERVICE;
    }

    // get cached thread executor service for background work
    public static ExecutorService getCachedExecutorService(){
        return EXECUTORS_SERVICE;
    }

    // get main thread executor service
    public static Handler getMainThread(){
        return MAIN_THREAD_HANDLER;
    }
}
