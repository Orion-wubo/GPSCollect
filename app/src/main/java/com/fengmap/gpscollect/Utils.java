/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fengmap.gpscollect;


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import com.amap.api.location.AMapLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATES,
                false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES,
                requestingLocationUpdates).apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    public static String getLocationText(Location location) {
        if (location == null) {
            return "Unknown location";
        } else {
            float accuracy = location.getAccuracy();
            double altitude = location.getAltitude();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float speed = location.getSpeed();
            float bearing = location.getBearing();
            String curTime = getCurTime();
            JSONObject obj = new JSONObject();
            try {
                obj.put("time", curTime);
                obj.put("longitude", longitude);
                obj.put("latitude", latitude);
                obj.put("type", location.getProvider());
                obj.put("accuracy", accuracy);
                obj.put("altitude", altitude);
                obj.put("bearing", bearing);
                obj.put("speed", speed);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj.toString() + "\r\n";
        }
    }

    public static String getLocationText(AMapLocation location) {
        if (location == null) {
            return "Unknown location";
        } else {
            float accuracy = location.getAccuracy();
            double altitude = location.getAltitude();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float speed = location.getSpeed();
            float bearing = location.getBearing();
            String curTime = getCurTime();
            JSONObject obj = new JSONObject();
            try {
                obj.put("time", curTime);
                obj.put("longitude", longitude);
                obj.put("latitude", latitude);
                obj.put("type", getLocationType(location.getLocationType()));
                obj.put("accuracy", accuracy);
                obj.put("altitude", altitude);
                obj.put("bearing", bearing);
                obj.put("speed", speed);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj.toString() + "\r\n";
        }
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated, DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static String getLocationType(int code) {
        String result = "";
        switch (code) {
            case 1:
                result = "GPS定位结果";
                break;
            case 2:
                result = "前次定位结果";
                break;
            case 4:
                result = "缓存定位结果";
                break;
            case 5:
                result = "wifi定位结果";
                break;
            case 6:
                result = "基站定位结果";
                break;
            case 8:
                result = "离线定位结果";
                break;
            case 9:
                result = "最后位置缓存";
                break;
            default:
                result = "其他定位结果";
                break;
        }
        return result;
    }

    public static String getCurTime() {
        long cur = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date(cur);
        String curTime = formatter.format(curDate);
        return curTime;
    }

    public static String getErrorLocation(AMapLocation location) {
        String result;
        result =
                "时间: " + getCurTime() + ", ErrCode:" + location.getErrorCode() + ", errInfo:" + location.getErrorInfo();
        return result + "\r\n";
    }
}
