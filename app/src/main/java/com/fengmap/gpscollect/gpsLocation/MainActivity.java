package com.fengmap.gpscollect.gpsLocation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.fengmap.gpscollect.Config;
import com.fengmap.gpscollect.FileUtil;
import com.fengmap.gpscollect.LocationCallBack;
import com.fengmap.gpscollect.R;
import com.fengmap.gpscollect.ShowSettingUtil;
import com.fengmap.gpscollect.TimerUtil;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;


public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private TextView tv_info;
    private TextView tv_status;
    private Switch mSwitch;
    private Button white_list, back_permission, get_info;
    private LocationService.MyBinder myBinder;
    private ShowSettingUtil showSettingUtil;
    private FileUtil fileUtil;
    private LinearLayout pb;
    private TimerUtil timerUtil;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showSettingUtil = new ShowSettingUtil(this);
        fileUtil = new FileUtil();
        timerUtil = new TimerUtil(this);

        checkPermission(false);

        tv_status = findViewById(R.id.tv_status);
        tv_info = findViewById(R.id.tv_info);
        mSwitch = findViewById(R.id.open);
        white_list = findViewById(R.id.white_list);
        back_permission = findViewById(R.id.back_permission);
        get_info = findViewById(R.id.get_info);
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
                    Toast.makeText(MainActivity.this, "已经设置好", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingUtil.showSetting();
            }
        });

        get_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fileUtil.getGpsInfo();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "获取完成，前往文件管理查找gpsCollection.txt文件查看",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
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
                            Toast.makeText(MainActivity.this, "获取权限成功，部分权限未正常授予", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean never) {
                        if (never) {
                            Toast.makeText(MainActivity.this, "被永久拒绝授权，请手动授予位置和储存权限", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "获取位置和储存权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void close() {
        Config.gpsSwitch = false;
        Intent intent = new Intent(this, LocationService.class);
        unbindService(this);
        stopService(intent);
        timerUtil.stop();
    }

    private void open() {
        Config.gpsSwitch = true;
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
        timerUtil.start();
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

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        myBinder = (LocationService.MyBinder) iBinder;
        LocationService service = myBinder.getService();
        service.setCallBack(new LocationCallBack() {
            @Override
            public void gpsStart() {
                tv_status.setText("正在定位...");
                tv_info.setText("");
            }

            @Override
            public void gpsSuccess(String location) {
                tv_status.setText("定位成功");
                tv_info.setText(location);
            }

            @Override
            public void gpsStop() {
                tv_status.setText("定位结束");
                tv_info.setText("");
            }

            @Override
            public boolean reStart() {
                open();
                return true;
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        tv_status.setText("disconnected");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
