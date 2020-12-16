package com.fengmap.gpscollect.amapLocation;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.fengmap.gpscollect.FileUtil;
import com.fengmap.gpscollect.LocalBroadcastManager;
import com.fengmap.gpscollect.R;
import com.fengmap.gpscollect.ShowSettingUtil;
import com.fengmap.gpscollect.TimerUtil;
import com.fengmap.gpscollect.Utils;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;


public class AMapLocationActivity extends AppCompatActivity {

    private TextView tv_info;
    private TextView tv_status;
    private Switch mSwitch;
    private Button white_list, back_permission;
    private ShowSettingUtil showSettingUtil;
    private FileUtil fileUtil;
    private LinearLayout pb;
    private TimerUtil timerUtil;
    private LocationAMPService mService;
    private boolean mBound;
    private MyReceiver myReceiver;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_amap);

        showSettingUtil = new ShowSettingUtil(this);
        fileUtil = new FileUtil();
        timerUtil = new TimerUtil(this);

        checkPermission(false);

        tv_status = findViewById(R.id.tv_status);
        tv_info = findViewById(R.id.tv_info);
        mSwitch = findViewById(R.id.open);
        white_list = findViewById(R.id.white_list);
        back_permission = findViewById(R.id.back_permission);
        pb = findViewById(R.id.pb);

        // 添加监听
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkPermission(true);
                } else {
                    close();
                }
            }
        });

        white_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showSettingUtil.isIgnoringBatteryOptimizations()) {
                    showSettingUtil.requestIgnoreBatteryOptimizations();
                } else {
                    Toast.makeText(AMapLocationActivity.this, "已经设置好", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingUtil.showSetting();
            }
        });
    }

    private void checkPermission(final boolean start) {
        XXPermissions.with(this)
                .permission(Permission.ACCESS_BACKGROUND_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        if (all) {
//                            Toast.makeText(MainActivity.this, "已经设置好", Toast.LENGTH_SHORT).show();
                            if (start) {
                                open();
                                tv_status.setText("正在定位...");
                            }
                        } else {
                            Toast.makeText(AMapLocationActivity.this, "获取权限成功，部分权限未正常授予", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean never) {
                        if (never) {
                            Toast.makeText(AMapLocationActivity.this, "被永久拒绝授权，请手动授予位置和储存权限", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AMapLocationActivity.this, "获取位置和储存权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationAMPService.LocalBinder binder = (LocationAMPService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, LocationAMPService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationAMPService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }
    /**
     * Receiver for broadcasts sent by {@link LocationAMPService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationAMPService.EXTRA_LOCATION);
            if (location != null) {
                Log.e("location", location.toString());
                tv_info.setText(Utils.getLocationText((AMapLocation) location));
//                Toast.makeText(MainActivity.this, Utils.getLocationText(location), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void close() {
        String s = "------------本次定位结束--------------";
//        GpsInfo info = new GpsInfo();
//        info.setInfo(s);
//        info.save();
        fileUtil.write(s+ "\r\n");
        mService.removeLocationUpdates();
        timerUtil.stop();
        tv_status.setText("停止定位");
        tv_info.setText("");
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        pb.setVisibility(View.VISIBLE);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                fileUtil.getGpsInfo();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        pb.setVisibility(View.GONE);
//                        Toast.makeText(AMapLocationActivity.this, "获取完成，前往文件管理查找gpsCollection.txt文件查看",
//                                Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//        }).start();
    }

    private void open() {
        String s = "------------本次定位开始--------------";
//        GpsInfo info = new GpsInfo();
//        info.setInfo(s);
//        info.save();
        fileUtil.write(s+ "\r\n");
        mService.requestLocationUpdates();
        timerUtil.start();
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //TODO something
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
