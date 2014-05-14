package com.sap.sailing.domain.common.racelog.tracking;

public class NotDenotableForRaceLogTrackingException extends Exception {

    private static final long serialVersionUID = 6721038282862173471L;

    public NotDenotableForRaceLogTrackingException() {
        super();
    }

    public NotDenotableForRaceLogTrackingException(String msg) {
        super(msg);
    }
}
