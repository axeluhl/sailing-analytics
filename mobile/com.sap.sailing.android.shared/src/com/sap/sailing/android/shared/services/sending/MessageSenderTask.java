package com.sap.sailing.android.shared.services.sending;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sse.common.Util;

public class MessageSenderTask extends AsyncTask<Intent, Void, Util.Triple<Intent, Boolean, InputStream>> {
    
    private static String TAG = MessageSenderTask.class.getName();

    public interface MessageSendingListener {
        public void onMessageSent(Intent intent, boolean success, InputStream inputStream);
    }

    private MessageSendingListener listener;
    private Context context;

    public MessageSenderTask(MessageSendingListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @SuppressWarnings("unused")
    @Override
    protected Util.Triple<Intent, Boolean, InputStream> doInBackground(Intent... params) {
        Util.Triple<Intent, Boolean, InputStream> result;
        Intent intent = params[0];
        if (intent == null) {
            return new Util.Triple<Intent, Boolean, InputStream>(intent, false, null);
        }
        Bundle extras = intent.getExtras();
        String payload = extras.getString(MessageSendingService.PAYLOAD);
        String url = extras.getString(MessageSendingService.URL);
        if (payload == null || url == null) {
            return new Util.Triple<Intent, Boolean, InputStream>(intent, false, null);
        }
        InputStream responseStream = null;
        try {
            ExLog.i(context, TAG, "Posting message: " + payload);
            HttpRequest post = new HttpJsonPostRequest(new URL(url), payload, context);
            responseStream = post.execute();
            ExLog.i(context, TAG, "Post successful for the following message: " + payload);
            result = new Util.Triple<Intent, Boolean, InputStream>(intent, true, responseStream);
        } catch (IOException e) {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException ie) { }
            }
            ExLog.e(context, TAG, String.format("Post not successful, exception occured: %s", e.toString()));
            result = new Util.Triple<Intent, Boolean, InputStream>(intent, false, null);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Util.Triple<Intent, Boolean, InputStream> resultTriple) {
        super.onPostExecute(resultTriple);
        listener.onMessageSent(resultTriple.getA(), resultTriple.getB(), resultTriple.getC());
    }

}
