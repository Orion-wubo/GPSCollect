package com.fengmap.gpscollect;

import android.content.Context;
import android.os.Environment;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class FileUtil {

    private final File file;

    public FileUtil() {
        // write location to local
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gpsCollection.txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getGpsInfo() {
        List<GpsInfo> all = LitePal.findAll(GpsInfo.class);
        for (int i = 0; i < all.size(); i++) {
            String info = all.get(i).getInfo();
            info = info + "\r\n";
            write(info);
        }
        LitePal.deleteAll(GpsInfo.class);
    }

    public void write(String content) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(file, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readTextFromSDcard(Context context, String fileName) {
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(context.getAssets().open(fileName), "UTF-8");
            if (inputStreamReader == null) {
                return null;
            }
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStreamReader.close();
            bufferedReader.close();
            return stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
