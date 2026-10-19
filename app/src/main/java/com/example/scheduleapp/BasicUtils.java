package com.example.scheduleapp;

public class BasicUtils {
    public void simpleNewAndUIThreads(MainActivity mainActivity, Runnable thread, Runnable runnableUI){
        new Thread(()->{
            thread.run();
            mainActivity.runOnUiThread(runnableUI);
        }).start();
    }
}
