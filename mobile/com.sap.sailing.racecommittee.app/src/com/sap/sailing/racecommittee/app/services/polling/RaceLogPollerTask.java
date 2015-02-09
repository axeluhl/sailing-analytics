package com.sap.sailing.racecommittee.app.services.polling;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sse.common.Util;

public class RaceLogPollerTask extends AsyncTask<Util.Pair<Serializable, URL>, PollingResult, Void> {
    
    public interface PollingResultListener {
        public void onPollingResult(PollingResult result);
        public void onPollingFinished();
    }
    
    private final PollingResultListener listener;
    private final Context context;

    public RaceLogPollerTask(PollingResultListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground(Util.Pair<Serializable, URL>... queries) {
        for (Util.Pair<Serializable, URL> query : queries) {
            if (isCancelled()) {
                return null;
            }
            
            HttpRequest request = new HttpJsonPostRequest(query.getB(), context);
            InputStream responseStream = null;
            try {
                responseStream = request.execute();
                publishProgress(new PollingResult(true, 
                        new Util.Pair<Serializable, InputStream>(query.getA(), responseStream)));
            } catch (IOException e) {
                // don't need to close responseStream as it still must
                // be null because the only call that may throw an
                // IOException is the execute() call that would have
                // initialized responseStream
                publishProgress(new PollingResult(false, null));
            }
        }
        return null;
    }
    
    @Override
    protected void onProgressUpdate(PollingResult... values) {
        listener.onPollingResult(values[0]);
    }
    
    @Override
    protected void onPostExecute(Void result) {
        listener.onPollingFinished();
    }
}
