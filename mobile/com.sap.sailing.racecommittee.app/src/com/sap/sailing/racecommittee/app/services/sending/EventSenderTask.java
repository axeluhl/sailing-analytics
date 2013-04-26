package com.sap.sailing.racecommittee.app.services.sending;

import java.io.Serializable;
import java.net.URI;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.http.HttpJsonPostRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class EventSenderTask extends AsyncTask<Intent, Void, Pair<Intent, Integer>> {

    public interface EventSendingListener {
        public void onResult(Intent intent, boolean success);
    }

    private EventSendingListener listener;

    public EventSenderTask(EventSendingListener listener) {
        this.listener = listener;
    }

    @Override
    protected Pair<Intent, Integer> doInBackground(Intent... params) {

        Intent intent = params[0];
        if (intent == null) {
            return Pair.create(intent, -1);
        }

        Bundle extras = intent.getExtras();
        Serializable serializedEvent = extras.getSerializable(AppConstants.EXTRAS_JSON_KEY);
        String url = extras.getString(AppConstants.EXTRAS_URL);
        if (serializedEvent == null || url == null) {
            return Pair.create(intent, -1);
        }

        try {
            HttpRequest post = new HttpJsonPostRequest(URI.create(url), serializedEvent.toString());
            try {
                post.execute().close();
            } finally {
                post.disconnect();
            }
            ExLog.i(getClass().getName(), "Post successful for the following event: " + serializedEvent);
        } catch (Exception e) {
            ExLog.e(getClass().getName(), e.toString());
            return Pair.create(intent, -1);
        }
        return Pair.create(intent, 0);
    }

    @Override
    protected void onPostExecute(Pair<Intent, Integer> resultPair) {
        super.onPostExecute(resultPair);
        listener.onResult(resultPair.first, resultPair.second == 0);
    }

}
