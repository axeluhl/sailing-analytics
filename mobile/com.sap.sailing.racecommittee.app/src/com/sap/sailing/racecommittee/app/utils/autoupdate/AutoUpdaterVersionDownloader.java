package com.sap.sailing.racecommittee.app.utils.autoupdate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import com.sap.sailing.android.shared.data.http.HttpGetRequest;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdaterChecker.AutoUpdaterState;
import com.sap.sse.common.Util;

import android.content.Context;
import android.util.Log;

public class AutoUpdaterVersionDownloader extends AutoUpdaterDownloader<Util.Pair<Integer, String>> {
    private final static String TAG = AutoUpdaterVersionDownloader.class.getName();

    public AutoUpdaterVersionDownloader(AutoUpdaterState state, Context context) {
        super(state, context);
    }

    @Override
    protected Util.Pair<Integer, String> downloadInBackground(URL url) {
        HttpGetRequest request = new HttpGetRequest(url, context);
        InputStream stream = null;
        try {
            stream = request.execute();
            if (stream != null) {
                String contents = readStream(stream);
                return parseVersionFile(contents);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while trying to read version file", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while trying to close version file stream", e);
                }
            }
        }

        return null;
    }

    private Util.Pair<Integer, String> parseVersionFile(String contents) {
        String[] map = contents.split("=");
        if (map.length == 2) {
            String apkFileName = map[0];
            String versionInfo = map[1];
            String versionCode;
            if (versionInfo.contains("-")) {
                String[] version = versionInfo.split("-");
                versionCode = version[0];
                String versionVariant = version[1];
            } else {
                versionCode = versionInfo;
            }
            try {
                Integer code = Integer.parseInt(versionCode);
                return new Util.Pair<Integer, String>(code, apkFileName);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Exception while trying to parse version from version file");
            }
        }
        return null;
    }

    @Override
    protected void onError() {
        state.onError();
    }

    @Override
    protected void onSuccess(Util.Pair<Integer, String> result) {
        state.updateToVersion(result.getA(), result.getB());
    }

    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return new String(baos.toByteArray(), Charset.defaultCharset());
    }

}
