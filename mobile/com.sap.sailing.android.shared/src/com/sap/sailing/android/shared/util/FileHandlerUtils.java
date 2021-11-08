package com.sap.sailing.android.shared.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.logging.ExLog;

import android.content.Context;
import android.os.Environment;

public class FileHandlerUtils {

    private final static String TAG = FileHandlerUtils.class.getName();

    public static String convertStreamToString(InputStream inputStream, Context context) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine() method. We iterate until the
         * BufferedReader return null which means there's no more data to read. Each line will appended to a
         * StringBuilder and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            ExLog.e(context, TAG,
                    "In Method convertStreamToString while converting stream to string: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                ExLog.e(context, TAG,
                        "In Method convertStreamToString while closing the input stream: " + e.getMessage());
            }
        }
        return stringBuilder.toString();
    }

    public static File getExternalApplicationFolder(Context context) {
        String appFolder = context.getResources().getString(R.string.app_folder);
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + appFolder);
        dir.mkdirs();
        return dir;
    }

    public static File getExternalFileFolder(Context context) {
        return new File(context.getExternalFilesDir(null).getPath() + "/");
    }

    public static File getExternalCacheFolder(Context context) {
        return new File(context.getExternalCacheDir().getPath() + "/");
    }

}
