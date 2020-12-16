package com.fengmap.gpscollect.amapLocation;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.fengmap.gpscollect.R;

public class AMapLocationUtil {
    private final AMapLocationClient mLocationClient;
    private final AMapLocationClientOption mLocationOption;
    private final Context mContext;

    public AMapLocationUtil(Context context,AMapLocationListener locationListener) {
        this.mContext = context;
        mLocationClient = new AMapLocationClient(context);

        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(1000);
        mLocationOption.setNeedAddress(false);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.setLocationListener(locationListener);

    }

    public void setOption(AMapLocationClientOption option) {
        mLocationClient.setLocationOption(option);
    }

    public void requestLocationUpdates() {
        //启动定位
        mLocationClient.startLocation();
    }

    public void removeLocationUpdates(){
        mLocationClient.stopLocation();
    }

    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;
    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = mContext.getPackageName();
            if(!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(mContext, channelId);
        } else {
            builder = new Notification.Builder(mContext);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GPS采集")
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    public void startBackLocation(){
        //启动后台定位，第一个参数为通知栏ID，建议整个APP使用一个
        mLocationClient.enableBackgroundLocation(2001, buildNotification());
    }

    public void stopBackLocation(){
        //关闭后台定位，参数为true时会移除通知栏，为false时不会移除通知栏，但是可以手动移除
        mLocationClient.disableBackgroundLocation(true);
    }

    public void onDestroy(AMapLocationListener locationListener) {
        mLocationClient.unRegisterLocationListener(locationListener);
        mLocationClient.onDestroy();
    }
}
