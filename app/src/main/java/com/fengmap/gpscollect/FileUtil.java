package com.fengmap.gpscollect;

import android.os.Environment;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            write(file, info);
        }
        LitePal.deleteAll(GpsInfo.class);
    }

    private void write(File file, String content) {
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
}
