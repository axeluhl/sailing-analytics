package com.sap.sailing.android.shared.util;

public interface AbstractAsyncTaskListener<Result> {
    void onResultReceived(Result accessToken);

    void onException(Exception exception);
}