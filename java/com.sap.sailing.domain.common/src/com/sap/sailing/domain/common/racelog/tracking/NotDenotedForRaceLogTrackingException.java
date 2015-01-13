package com.sap.sailing.domain.common.racelog.tracking;

import java.io.Serializable;

public class NotDenotedForRaceLogTrackingException extends Exception implements Serializable {

    private static final long serialVersionUID = 6721038282862173471L;

    public NotDenotedForRaceLogTrackingException() {
        super();
    }

    public NotDenotedForRaceLogTrackingException(String msg) {
        super(msg);
    }
}
