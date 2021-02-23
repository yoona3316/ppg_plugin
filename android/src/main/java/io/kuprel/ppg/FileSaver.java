package io.kuprel.ppg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;

public class FileSaver {
    public synchronized void saveFile(Context context, Queue<double[]> queue) {
        if (queue.size()<500) return;
        Long dateTimeKey = System.currentTimeMillis();
        @SuppressLint("DefaultLocale") String filename = String.format("%d", dateTimeKey);
        File file = new File(context.getFilesDir(), filename);
        Log.d("filesaver", queue.size()+"");
        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bufwr = new BufferedWriter(fw);
            int cnt = 500;
            while (cnt>0&&!queue.isEmpty()) {
                bufwr.write(Arrays.toString(queue.poll()));
                bufwr.newLine();
                cnt--;
            }
            bufwr.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
