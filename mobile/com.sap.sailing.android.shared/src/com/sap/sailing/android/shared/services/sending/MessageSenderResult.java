package com.sap.sailing.android.shared.services.sending;

import java.io.InputStream;

import android.content.Intent;

public class MessageSenderResult {

    private final Intent mIntent;
    private final InputStream mInputStream;
    private final Exception mException;

    public MessageSenderResult() {
        this(null, null, null);
    }

    public MessageSenderResult(Intent intent) {
        this(intent, null, null);
    }

    public MessageSenderResult(Intent intent, InputStream inputStream) {
        this(intent, inputStream, null);
    }

    public MessageSenderResult(Intent intent, Exception exception) {
        this(intent, null, exception);
    }

    public MessageSenderResult(Intent intent, InputStream inputStream, Exception exception) {
        mIntent = intent;
        mInputStream = inputStream;
        mException = exception;
    }

    public boolean isSuccessful() {
        return mException == null;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public Exception getException() {
        return mException;
    }
}
