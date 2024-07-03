package me.bruno.rafael.requests.scheduler;

import java.util.Timer;
import java.util.TimerTask;

public class RequestsTimerScheduler extends TimerTask {

    private final Runnable runnable;

    public RequestsTimerScheduler(long delayInMillis, Runnable runnable) {
        this.runnable = runnable;

        new Timer().schedule(this, delayInMillis, delayInMillis);
    }

    @Override
    public void run() {
        runnable.run();
    }
}
