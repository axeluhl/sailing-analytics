package com.sap.sailing.racecommittee.app.services.sending;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.http.HttpJsonPostRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class EventSenderTask extends AsyncTask<Intent, Void, Triple<Intent, Boolean, InputStream>> {
    
    private static String TAG = EventSenderTask.class.getName();

    public interface EventSendingListener {
        public void onEventSent(Intent intent, boolean success, InputStream inputStream);
    }

    private EventSendingListener listener;

    public EventSenderTask(EventSendingListener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unused")
    @Override
    protected Triple<Intent, Boolean, InputStream> doInBackground(Intent... params) {
        Triple<Intent, Boolean, InputStream> result;
        Intent intent = params[0];
        if (intent == null) {
            return new Triple<Intent, Boolean, InputStream>(intent, false, null);
        }
        Bundle extras = intent.getExtras();
        String serializedEventAsJson = extras.getString(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT);
        String url = extras.getString(AppConstants.EXTRAS_URL);
        if (serializedEventAsJson == null || url == null) {
            return new Triple<Intent, Boolean, InputStream>(intent, false, null);
        }
        InputStream responseStream = null;
        try {
            ExLog.i(TAG, "Posting event: " + serializedEventAsJson);
            HttpRequest post = new HttpJsonPostRequest(new URL(url), serializedEventAsJson);
            responseStream = post.execute();
            ExLog.i(TAG, "Post successful for the following event: " + serializedEventAsJson);
            result = new Triple<Intent, Boolean, InputStream>(intent, true, responseStream);
        } catch (IOException e) {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException ie) { }
            }
            ExLog.e(TAG, String.format("Post not successful, exception occured: %s", e.toString()));
            result = new Triple<Intent, Boolean, InputStream>(intent, false, null);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Triple<Intent, Boolean, InputStream> resultTriple) {
        super.onPostExecute(resultTriple);
        listener.onEventSent(resultTriple.getA(), resultTriple.getB(), resultTriple.getC());
    }

}
