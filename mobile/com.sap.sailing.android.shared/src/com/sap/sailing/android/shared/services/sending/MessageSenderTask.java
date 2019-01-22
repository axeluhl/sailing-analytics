package com.sap.sailing.android.shared.services.sending;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.logging.ExLog;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class MessageSenderTask extends AsyncTask<Intent, Void, MessageSenderResult> {

    private static String TAG = MessageSenderTask.class.getName();
    private MessageSendingListener listener;
    private Context context;

    public MessageSenderTask(MessageSendingListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected MessageSenderResult doInBackground(Intent... params) {
        MessageSenderResult result;
        Intent intent = params[0];
        if (intent == null) {
            return new MessageSenderResult();
        }
        Bundle extras = intent.getExtras();
        String payload = extras.getString(MessageSendingService.PAYLOAD);
        final boolean isResend = extras.getBoolean(MessageSendingService.RESEND);
        String url = extras.getString(MessageSendingService.URL);
        if (payload == null || url == null) {
            return new MessageSenderResult(intent);
        }
        InputStream responseStream = null;
        try {
            ExLog.i(context, TAG, "Posting message" + (isResend ? " (resend)" : "") + ": " + payload);
            HttpRequest post = new HttpJsonPostRequest(context, new URL(url), payload);
            responseStream = post.execute();
            ExLog.i(context, TAG,
                    "Post successful for the following" + (isResend ? " (resend)" : "") + " message: " + payload);
            result = new MessageSenderResult(intent, responseStream);
        } catch (IOException e) {
            ExLog.e(context, TAG, String.format("Post not successful, exception occured: %s", e.toString()));
            result = new MessageSenderResult(intent, e);
        } finally {
            try {
                if (responseStream != null) {
                    responseStream.close();
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(MessageSenderResult result) {
        super.onPostExecute(result);
        listener.onMessageSent(result);
    }

    public interface MessageSendingListener {
        void onMessageSent(MessageSenderResult result);
    }
}
