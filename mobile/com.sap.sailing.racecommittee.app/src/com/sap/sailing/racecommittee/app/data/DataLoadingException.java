package com.sap.sailing.racecommittee.app.data;

public class DataLoadingException extends Exception {
    private static final long serialVersionUID = 4787160523984332591L;

    public DataLoadingException() {
    }

    public DataLoadingException(String detailMessage) {
        super(detailMessage);
    }

    public DataLoadingException(Throwable throwable) {
        super(throwable);
    }

    public DataLoadingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
