package com.fengmap.gpscollect;

import android.content.Context;
import android.os.PowerManager;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.POWER_SERVICE;

public class TimerUtil {

    private final Context context;
    private SimulateTask mTask;
    private Timer mTimer;

    public TimerUtil(Context context) {
        this.context = context;
    }

    public void start() {
        mTask = new SimulateTask();
        mTimer = new Timer();
        mTimer.schedule(mTask, 0, 10*60*1000);
    }

    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    public class SimulateTask extends TimerTask {
        @Override
        public void run() {
            screenOn();
        }
    }

    public void screenOn() {
        // turn on screen
        PowerManager mPowerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock mWakeLock =
                mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "tag");
        mWakeLock.acquire();
        mWakeLock.release();
    }

}
