package com.sap.sailing.racecommittee.app.services.sending;

import java.net.URI;
import java.net.URLDecoder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.http.HttpJsonPostRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class EventSenderTask extends AsyncTask<Intent, Void, Pair<Intent, Integer>> {
    
    private static String TAG = EventSenderTask.class.getName();

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
        String serializedEventAsJson = extras.getString(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT);
        String url = extras.getString(AppConstants.EXTRAS_URL);
        if (serializedEventAsJson == null || url == null) {
            return Pair.create(intent, -1);
        }
        try {
            ExLog.i(TAG, "Posting event: " + serializedEventAsJson);
            HttpRequest post = new HttpJsonPostRequest(URI.create(url), URLDecoder.decode(serializedEventAsJson));
            try {
                post.execute().close();
            } finally {
                post.disconnect();
            }
            ExLog.i(TAG, "Post successful for the following event: " + serializedEventAsJson);
        } catch (Exception e) {
            ExLog.e(TAG, String.format("Post not successful, exception occured: %s", e.toString()));
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
