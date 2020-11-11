package com.fengmap.gpscollect;

/**
 * Created by bai on 2018/4/8.
 */

public interface LocationCallBack {
    void gpsStart();

    void gpsSuccess(String location);

    void gpsStop();

    boolean reStart();
}
