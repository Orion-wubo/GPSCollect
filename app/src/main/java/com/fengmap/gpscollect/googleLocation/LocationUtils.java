package com.fengmap.gpscollect.googleLocation;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 *
 * Created by bai on 2018/8/13.
 */

public class LocationUtils {
    // GPS定位
    private final static String GPS_LOCATION = LocationManager.GPS_PROVIDER;
    // 网络定位
    private final static String NETWORK_LOCATION = LocationManager.NETWORK_PROVIDER;
    // 解码经纬度最大结果数目
    private final static int MAX_RESULTS = 1;
    // 时间更新间隔，单位：ms
    private final static long MIN_TIME = 1000;
    // 位置刷新距离，单位：m
    private final static float MIN_DISTANCE = (float) 0.01;
    // singleton
    private static LocationUtils instance;
    // 定位回调
    private LocationCallBack mLocationCallBack;
    // 定位管理实例
    LocationManager mLocationManager;
    // 上下文
    private Context mContext;

    /**
     * 构造函数
     */
    private LocationUtils(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * singleton
     */
    public static LocationUtils getInstance(Context mContext) {
        if (instance == null) {
            instance = new LocationUtils(mContext);
        }
        return instance;
    }

    /**
     * 获取定位
     */
    @SuppressWarnings("MissingPermission")
    public void requestLocationUpdates(LocationCallBack mLocationCallBack) {
        this.mLocationCallBack = mLocationCallBack;
        if (mLocationCallBack == null)
            return;
        // 定位管理初始化
        mLocationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS定位，较精确，也比较耗电
        LocationProvider gpsProvider =
                mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
        // 通过网络定位，对定位精度度不高或省点情况可考虑使用
        LocationProvider netProvider =
                mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);
        // 优先考虑GPS定位，其次网络定位。
        if (gpsProvider != null){
            gpsLocation();
        }else if(netProvider != null){
            netWorkLocation();
        }else {
            mLocationCallBack.setLocation(null);
        }
    }

    /**
     * GPS定位
     */
    @SuppressWarnings("MissingPermission")
    private void gpsLocation(){
        if (mLocationManager == null)
            mLocationManager = (LocationManager)
                    mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(
                GPS_LOCATION, MIN_TIME, MIN_DISTANCE,mLocationListener);
    }

    /**
     * 网络定位
     */
    @SuppressWarnings("MissingPermission")
    private void netWorkLocation(){
        if (mLocationManager == null)
            mLocationManager = (LocationManager)
                    mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(
                NETWORK_LOCATION, MIN_TIME, MIN_DISTANCE,mLocationListener);
    }

    private boolean isFirst = true;
    // 定位监听
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (mLocationCallBack != null){
                mLocationCallBack.setLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            isFirst = true;
            // 如果gps定位不可用,改用网络定位
            if (provider.equals(LocationManager.GPS_PROVIDER)){
                netWorkLocation();
            }
        }
    };

    /**
     * 根据经纬度获取地址
     */
    public void getAddress(double latitude, double longitude){
        // Address列表
        List<Address> locationList = null;
        // 经纬度解码实例
        Geocoder gc = new Geocoder(mContext, Locale.getDefault());
        try {
            // 获取Address列表
            locationList = gc.getFromLocation(latitude, longitude, MAX_RESULTS);
            // 获取Address实例
            Address address = locationList.get(0);
            if (mLocationCallBack != null)
                mLocationCallBack.setAddress(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取地址周边信息
     */
    public String getAddressLine(Address address){
        String result = "";
        for (int i = 0; address.getAddressLine(i) != null; i++) {
            String addressLine = address.getAddressLine(i);
            result = result + addressLine;
        }
        return result;
    }

    public void requestLocation() {

    }

    public void removeLocationUpdates() {
        mLocationManager.removeUpdates(mLocationListener);
    }

    /**
     * 定位回调
     */
    public interface LocationCallBack{
        void setLocation(Location location);
        void setAddress(Address address);
    }
}
