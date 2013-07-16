package com.sap.sailing.racecommittee.app.services.sending;

import java.io.InputStream;
import java.net.URI;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.http.HttpJsonPostRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class EventSenderTask extends AsyncTask<Intent, Void, Pair<Intent, Boolean>> {
    
    private static String TAG = EventSenderTask.class.getName();

    public interface EventSendingListener {
        public void onResult(Intent intent, boolean success);
    }

    private EventSendingListener listener;

    public EventSenderTask(EventSendingListener listener) {
        this.listener = listener;
    }

    @Override
    protected Pair<Intent, Boolean> doInBackground(Intent... params) {
        Pair<Intent, Boolean> result;
        Intent intent = params[0];
        if (intent == null) {
            return Pair.create(intent, false);
        }
        Bundle extras = intent.getExtras();
        String serializedEventAsJson = extras.getString(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT);
        String url = extras.getString(AppConstants.EXTRAS_URL);
        if (serializedEventAsJson == null || url == null) {
            return Pair.create(intent, false);
        }
        try {
            ExLog.i(TAG, "Posting event: " + serializedEventAsJson);
            HttpRequest post = new HttpJsonPostRequest(URI.create(url), serializedEventAsJson);
            try {
                // TODO read JSON-serialized race log events that need to be merged into the local race log because they were added on the server in the interim
                final InputStream inputStream = post.execute();
                inputStream.close();
            } finally {
                post.disconnect();
            }
            ExLog.i(TAG, "Post successful for the following event: " + serializedEventAsJson);
            result = Pair.create(intent, true);
        } catch (Exception e) {
            ExLog.e(TAG, String.format("Post not successful, exception occured: %s", e.toString()));
            result = Pair.create(intent, false);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Pair<Intent, Boolean> resultPair) {
        super.onPostExecute(resultPair);
        listener.onResult(resultPair.first, resultPair.second);
    }

}
