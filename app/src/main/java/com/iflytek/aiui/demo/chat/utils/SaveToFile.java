package com.iflytek.aiui.demo.chat.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * 保存信息到文件中
 */
public class SaveToFile extends Thread {
    private String info;

    private boolean isByte;

    private byte[] bytes;

    @Override
    public void run() {
        super.run();
        if (isByte) {
            saveToFile(bytes);
        } else {
            if (info != null && info.length() > 0) {
                saveToFile(info.getBytes());
            }
        }


    }


    public void saveInfo(String info) {
        this.info = info;
        start();
    }

    public void saveInfo(byte[] infos) {
        this.isByte = true;
        this.bytes = infos;
        start();
    }

    private void saveToFile(byte[] buffer) {
        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath() + File.separator + "info.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(buffer);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
