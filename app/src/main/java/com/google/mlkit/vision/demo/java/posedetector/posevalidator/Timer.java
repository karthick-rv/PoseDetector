package com.google.mlkit.vision.demo.java.posedetector.posevalidator;

import android.os.Handler;
import android.os.Looper;

public class Timer {

    final Handler handler;
    final Runnable runnable;

    boolean isRunning = false;



    public Timer(Runnable runnable){
        handler = new Handler(Looper.getMainLooper());
        this.runnable = runnable;
    }


    public void start(){
        handler.postDelayed(runnable, 1000);
        isRunning = true;
    }

    public void stop(){
        handler.removeCallbacks(runnable);
        isRunning = false;
    }

    public boolean isRunning(){
        return isRunning;
    }


}
