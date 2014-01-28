package com.sap.sailing.racecommittee.app.services.polling;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import android.os.AsyncTask;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.racecommittee.app.data.http.HttpJsonPostRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;

public class RaceLogPollerTask extends AsyncTask<Pair<Serializable, URL>, PollingResult, Void> {
    
    public interface PollingResultListener {
        public void onPollingResult(PollingResult result);
        public void onPollingFinished();
    }
    
    private final PollingResultListener listener;

    public RaceLogPollerTask(PollingResultListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Pair<Serializable, URL>... queries) {
        for (Pair<Serializable, URL> query : queries) {
            if (isCancelled()) {
                return null;
            }
            
            HttpRequest request = new HttpJsonPostRequest(query.getB());
            InputStream responseStream = null;
            try {
                responseStream = request.execute();
                publishProgress(new PollingResult(true, 
                        new Pair<Serializable, InputStream>(query.getA(), responseStream)));
            } catch (IOException e) {
                if (responseStream != null) {
                    try {
                        responseStream.close();
                    } catch (IOException ie) {
                    }
                }
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
