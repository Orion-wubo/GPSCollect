package com.fengmap.gpscollect.gpsLocation;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationUtil implements LocationListener {
    private final Context mContext;
    private final LocationManager mLocationManager;
    private final String gpsProvider;
    private UpdateLocationListener updateListener;

    public LocationUtil(Context context,UpdateLocationListener locationListener) {
        this.mContext = context;
        this.updateListener = locationListener;
        //Get location service
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        //创建一个Criteria对象
        Criteria mCriteria = new Criteria();
        //设置粗略精确度
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否需要返回海拔信息
        mCriteria.setAltitudeRequired(true);
        //设置是否需要返回方位信息
        mCriteria.setBearingRequired(true);
        //设置是否允许付费服务
        mCriteria.setCostAllowed(true);
        //设置电量消耗等级
        mCriteria.setPowerRequirement(Criteria.POWER_HIGH);
        //设置是否需要返回速度信息
        mCriteria.setSpeedRequired(true);

        gpsProvider = mLocationManager.getBestProvider(mCriteria, true); //获取GPS信息
    }


    public void requestLocationUpdates() {
        //启动定位
        mLocationManager.requestLocationUpdates(gpsProvider, 1000, 0, this);
    }

    public void removeLocationUpdates(){
       mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateListener.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    interface UpdateLocationListener {
        void onLocationChanged(Location location);
    }
}
