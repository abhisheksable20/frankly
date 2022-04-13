package com.sakesnake.frankly.backgroundTask;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Deprecated
public class BackgroundWork
{
    private static final Object LOCK = new Object();
    private static BackgroundWork backgroundWork;
    private final Executor diskIO;
    private final Executor mainThread;
    private final Executor networkIO;

    public BackgroundWork(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
        this.networkIO = networkIO;
    }

    public static BackgroundWork getInstance(){
        if (backgroundWork == null){
            synchronized (LOCK){
                backgroundWork = new BackgroundWork(Executors.newSingleThreadExecutor(),Executors.newFixedThreadPool(3),new MainThreadExecutor());
            }
        }
        return backgroundWork;
    }

    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    public Executor getNetworkIO() {
        return networkIO;
    }

    private static class MainThreadExecutor implements Executor{
        private Handler handler = new Handler(Looper.getMainLooper());
        @Override
        public void execute(Runnable runnable) {
            handler.post(runnable);
        }
    }
}
