package com.fengmap.gpscollect.gpsLocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class LocationUtil implements LocationListener {
    private final Context mContext;
    private final LocationManager mLocationManager;
    private final String gpsProvider;
    private UpdateLocationListener updateListener;
    public static boolean isNeedNetworkLocation = false;

    public LocationUtil(Context context, UpdateLocationListener locationListener) {
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
        Log.e("best provider", gpsProvider);
    }


    public void requestLocationUpdates() {
        //启动定位
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                Toast.makeText(mContext, "定位权限没有允许", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        if (isNeedNetworkLocation) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        }
    }

    public void removeLocationUpdates() {
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
