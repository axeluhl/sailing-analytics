package com.sap.sailing.domain.racelog.tracking;

public class NotDenotableForTrackingException extends Exception {

    private static final long serialVersionUID = 6721038282862173471L;

    public NotDenotableForTrackingException() {
        super();
    }

    public NotDenotableForTrackingException(String msg) {
        super(msg);
    }
}
