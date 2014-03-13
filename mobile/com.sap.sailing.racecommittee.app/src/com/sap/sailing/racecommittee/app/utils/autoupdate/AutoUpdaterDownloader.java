package com.sap.sailing.racecommittee.app.utils.autoupdate;

import java.net.URL;

import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdaterChecker.AutoUpdaterState;

import android.os.AsyncTask;

public abstract class AutoUpdaterDownloader<T> extends AsyncTask<URL, Float, T> {

    protected final AutoUpdaterState state;
    
    public AutoUpdaterDownloader(AutoUpdaterState state) {
        this.state = state;
    }

    @Override
    protected T doInBackground(URL... params) {
        if (params.length != 1) {
            throw new IllegalArgumentException("Expecting just one URL to download from");
        }
        return downloadInBackground(params[0]);
    }

    @Override
    protected void onPostExecute(T result) {
        if (isCancelled()) {
            return;
        }
        
        if (result == null) {
            onError();
        } else {
            onSuccess(result);
        }       
    }

    
    protected abstract T downloadInBackground(URL url);
    protected abstract void onError();
    protected abstract void onSuccess(T result);

}
