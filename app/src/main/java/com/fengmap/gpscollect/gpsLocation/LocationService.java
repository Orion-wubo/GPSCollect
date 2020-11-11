package com.fengmap.gpscollect.gpsLocation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.fengmap.gpscollect.Config;
import com.fengmap.gpscollect.GpsInfo;
import com.fengmap.gpscollect.LocationCallBack;
import com.fengmap.gpscollect.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by bai on 2018/4/8.
 */

public class LocationService extends Service {
    private LocationManager mLocationManager;
    private String mProvider;
    private LocationListener mGpsLocationListener;
    private String TAG = "Location";
    private LocationCallBack callBack;
    private MyBinder myBinder;
    private boolean isFirst = true;
    private String content = "UNKNOW";

    private static final String CHANNEL_ID = "channel_01";
    private static final String PACKAGE_NAME =
            "com.fengmap.gpscollect";

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";
    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";

    private static final int NOTIFICATION_ID = 12345678;
    private NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }


    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法。该方法只被调用一次
     */
    @Override
    public void onCreate() {
        myBinder = new MyBinder();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        //Get location service
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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

        mProvider = mLocationManager.getBestProvider(mCriteria, true); //获取GPS信息

        mGpsLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (location == null) {
                    return;
                }

                float accuracy = location.getAccuracy();
                double altitude = location.getAltitude();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                float speed = location.getSpeed();
                float bearing = location.getBearing();
                long time = location.getTime();

                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy年MM月dd日");
                Date curDate = new Date(time);
                String curTime = formatter.format(curDate);
                String curTime2 = formatter2.format(curDate);


                // gps location
                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    if (isFirst) {
                        GpsInfo gpsInfo = new GpsInfo();
                        gpsInfo.setInfo("------本次采集开始(GPS)------"+curTime2);
                        gpsInfo.save();
                        isFirst = false;
                    }

                    content = "GPS--时间：" + curTime +
                            "；经度：" + longitude +
                            "；纬度：" + latitude +
                            "；精度：" + accuracy +
                            "；海拔：" + altitude +
                            "；方位：" + bearing +
                            "；速度：" + speed;
                    callBack.gpsSuccess(content);
                    GpsInfo gpsInfo = new GpsInfo();
                    gpsInfo.setInfo(content);
                    gpsInfo.save();
                    // network location
                } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                    if (isFirst) {
                        GpsInfo gpsInfo = new GpsInfo();
                        gpsInfo.setInfo("------本次采集开始(NETWORK)------"+curTime2);
                        gpsInfo.save();
                        isFirst = false;
                    }

                    content = "NETWORK--时间：" + curTime +
                            "；经度：" + longitude +
                            "；纬度：" + latitude +
                            "；精度：" + accuracy +
                            "；海拔：" + altitude +
                            "；方位：" + bearing +
                            "；速度：" + speed;
                    callBack.gpsSuccess(content);
                    GpsInfo gpsInfo = new GpsInfo();
                    gpsInfo.setInfo(content);
                    gpsInfo.save();
                }
                startForeground(NOTIFICATION_ID, getNotification());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                switch (status) {
                    //GPS状态为可见时
                    case LocationProvider.AVAILABLE:
                        break;
                    //GPS状态为服务区外时
                    case LocationProvider.OUT_OF_SERVICE:
                        break;
                    //GPS状态为暂停服务时
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        break;
                }
            }

            /**
             * 方法描述：GPS开启时触发
             * @param provider
             */
            @Override
            public void onProviderEnabled(String provider) {
                callBack.gpsStart();
            }

            /**
             * 方法描述： GPS禁用时触发
             * @param provider
             */
            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //状态监听
        GpsStatus.Listener listener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    //第一次定位
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        Log.i(TAG, "第一次定位");
                        break;
                    //卫星状态改变
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        Log.i(TAG, "卫星状态改变");
                        //获取当前状态
                        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                        //获取卫星颗数的默认最大值
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        //创建一个迭代器保存所有卫星
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                        int count = 0;
                        while (iters.hasNext() && count <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            count++;
                        }
                        System.out.println("搜索到：" + count + "颗卫星");
                        break;
                    //定位启动
                    case GpsStatus.GPS_EVENT_STARTED:
                        callBack.gpsStart();
                        Log.i(TAG, "定位启动");
                        break;
                    //定位结束
                    case GpsStatus.GPS_EVENT_STOPPED:
                        callBack.gpsStop();
                        Log.i(TAG, "定位结束");
                        break;
                }
            }
        };
        //绑定监听状态
        mLocationManager.addGpsStatusListener(listener);
        super.onCreate();
    }

    /**
     * 每次通过startService()方法启动Service时都会被回调。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocation();
        return super.onStartCommand(intent, flags, startId);

    }

    private void startLocation() {
        // best location
        mLocationManager.requestLocationUpdates(mProvider, 1000, 0, mGpsLocationListener);
//         gps location
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,mGpsLocationListener);
//        // 基站 location
//        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,mGpsLocationListener);
    }

    public void setCallBack(LocationCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        // if locationService is dead why is not by youselfe then restart
        if (Config.gpsSwitch) {
            Log.e("重启","restart");
            GpsInfo gpsInfo = new GpsInfo();
            gpsInfo.setInfo("重启");
            gpsInfo.save();
            callBack.reStart();
        } else {
            close();
            Log.e("关闭","close");
            GpsInfo gpsInfo = new GpsInfo();
            gpsInfo.setInfo("关闭");
            gpsInfo.save();
        }
    }

    private boolean close() {
        stopForeground(true);//取消最高级进程
        callBack.gpsStop();
        mLocationManager.removeUpdates(mGpsLocationListener);
        return false;
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationService.class);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.mipmap.ic_launcher, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.mipmap.ic_launcher, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(content)
                .setContentTitle("GPS采集正在定位")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
//                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }


}
