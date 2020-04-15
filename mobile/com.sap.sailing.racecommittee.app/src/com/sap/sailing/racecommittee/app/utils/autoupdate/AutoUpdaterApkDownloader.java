package com.sap.sailing.racecommittee.app.utils.autoupdate;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import com.sap.sailing.android.shared.data.http.FileBasedHttpGetRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest.HttpRequestProgressListener;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdaterChecker.AutoUpdaterState;

import android.content.Context;

public class AutoUpdaterApkDownloader extends AutoUpdaterDownloader<File> implements HttpRequestProgressListener {

    private static final String TAG = AutoUpdaterApkDownloader.class.getName();

    private final File targetFile;

    public AutoUpdaterApkDownloader(AutoUpdaterState state, File targetFile, Context context) {
        super(state, context);
        this.targetFile = targetFile;
    }

    @Override
    protected File downloadInBackground(final URL url) {
        try {
            HttpRequest request = new FileBasedHttpGetRequest(url, this, targetFile, context);
            InputStream result = null;
            try {
                result = request.execute();
                if (result != null) {
                    return targetFile;
                }
            } finally {
                if (result != null) {
                    result.close();
                }
            }
        } catch (Exception e) {
            ExLog.ex(context, TAG, e);
        }
        return null;
    }

    /*
     * On background thread!
     */
    @Override
    public void onHttpProgress(float progress) {
        publishProgress(progress);
    }

    @Override
    protected void onError() {
        state.onError();
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        if (values.length == 1) {
            state.onApkDownloadProgress(values[0]);
        }
    }

    @Override
    protected void onSuccess(File result) {
        state.onApkDownloadFinished(result);
    }

}
