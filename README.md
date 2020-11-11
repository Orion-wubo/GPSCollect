# GPSCollect
后台定位采集
尝试使用 locationManager  但是不同设备差异比较大
尝试使用 google定位 但是华为的设备不可以
最终使用高德定位统一方式

# Android 8.0权限说明
最后更新时间: 2018年02月02日
从Android 8.0开始系统为实现降低功耗，对后台应用获取用户位置信息频率进行了限制，每小时只允许更新几次位置信息，详细信息请参考官方说明。按照官方指引，如果要提高位置更新频率，需要后台应用提供一个前台服务通知告知。如果您需要自己参考官方指引来完成设置，可以参考之前我们提供的github示例。

从定位SDK v3.8.0版本开始，我们将这一操作封装到了定位SDK中，您在使用过程中只需要调用一个接口就可以为您的应用创建一个前台服务通知，当您的应用切换到后台后仍然有一个前台服务通知存在，以此规避Android 8.0对后台定位的限制。这部分内容将对这一功能进行介绍。



第一步，创建一个通知栏
您需要创建一个通知栏，下面的代码是一个简单示例，具体请您根据自己的业务进行相关修改。

Java
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
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		String channelId = getPackageName();
		if(!isCreateChannel) {
			NotificationChannel notificationChannel = new NotificationChannel(channelId,
					NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
			notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
			notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
			notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
			notificationManager.createNotificationChannel(notificationChannel);
			isCreateChannel = true;
		}
		builder = new Notification.Builder(getApplicationContext(), channelId);
	} else {
		builder = new Notification.Builder(getApplicationContext());
	}
	builder.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(Utils.getAppName(this))
			.setContentText("正在后台运行")
			.setWhen(System.currentTimeMillis());

	if (android.os.Build.VERSION.SDK_INT >= 16) {
		notification = builder.build();
	} else {
		return builder.getNotification();
	}
	return notification;
}


第二步，设置当调用后台定位接口时，显示通知栏
在您的应用切到后台或者您需要显示前台通知的时候调用后台定位接口，显示前台服务通知栏。

Java
//启动后台定位，第一个参数为通知栏ID，建议整个APP使用一个
locationClient.enableBackgroundLocation(2001, buildNotification());


第三步，关闭后台定位以及通知栏
当您不再需要通知栏时，请调用关闭后台定位接口

Java
//关闭后台定位，参数为true时会移除通知栏，为false时不会移除通知栏，但是可以手动移除


# android 9.0上使用前台服务，需要添加权限

	
	<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

# Android 10.0 开发须知

1.在Android10.0系统上，当应用退到后台后为了保留对设备位置信息的访问权，需要在清单中声明服务类型为:android:foregroundServiceType="location"的前台服务或者添加后台定位权限android.permission.ACCESS_BACKGROUND_LOCATION。

注意：个别厂商会对定制版本的机型做特别适配，例如华为手机会严格限制后台定位导致无法使用后台定位功能，开发者需要自行和厂商联系。

2.如果应用以 Android 10 或更高版本为目标平台，则它必须具有 ACCESS_FINE_LOCATION 权限才能使用 WLAN、WLAN 感知或蓝牙 API 中的一些方法。

注意：如果您的应用在 Android 10 或更高版本平台上运行，但其目标平台是 Android 9（API 级别 28）或更低版本，则只要您的应用已声明 ACCESS_COARSE_LOCATION 或 ACCESS_FINE_LOCATION 权限，您就可以使用受影响的 API（WifiP2pManager API 除外）。
locationClient.disableBackgroundLocation(true);
注意事项
1、如果您的应用在切到后台时已经存在前台服务通知了，则不需再调用这个接口；

2、建议您在整个应用中只有一个AMapLocationClient调用enableBackgroundLocation和disableBackgroundLocation接口即可，但存在多个AMapLocationClient且都调用了enableBackgroundLocation接口则需要所有的AMapLocationClient都调用了disableBackgroundLocation(true)接口通知栏才会移除；

3、开启关闭后台定位接口只是提供一个前台服务通知栏并不具备开始、停止定位的功能，开启、停止定位请调用AMapLocationCLient的startLocation()和stopLocation()接口


# Android 11.0 开发须知

1.新增单次访问权限
在 Android 11 中，每当应用请求访问前台位置信息时，系统权限对话框都包含一个名为【仅限这一次】的选项，通过这一新选项，用户可以更好地控制应用何时有权访问位置信息。具体含义如下:

如果用户在对话框中选择【仅限这一次】选项，系统会向应用授予临时的单次授权。然后，应用可以在一段时间内访问相关数据，具体时间取决于应用的行为和用户的操作：

a.当应用的 Activity 可见时，应用可以访问相关数据。

b.如果用户将应用转为后台运行，应用可以在短时间内继续访问相关数据。

c.如果您在 Activity 可见时启动了一项前台服务，并且用户随后将您的应用转到后台，那么您的应用可以继续访问相关数据，直到该前台服务停止。

d.如果用户撤消单次授权（例如在系统设置中撤消），无论您是否启动了前台服务，应用都无法访问相关数据。与任何权限一样，如果用户撤消了应用的单次授权，应用进程就会终止。

当用户下次打开应用并且应用中的某项功能请求访问位置信息、麦克风或摄像头时，系统会再次提示用户授予权限。

2.后台位置信息访问权限
在搭载 Android 11 的设备上，如需启用后台位置信息访问权限，用户必须在设置页面上针对应用的位置权限设置【始终允许】选项。


3.访问设备的位置和摄像头
如果应用程序中的前台服务需要访问设备的位置和摄像头，请声明对应的服务，如下：

	<manifest>
	    <service ... android:foregroundServiceType="location|camera"/>
	</manifest>
