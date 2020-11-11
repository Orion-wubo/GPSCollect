package com.fengmap.gpscollect;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

public class App extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
