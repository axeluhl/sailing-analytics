package com.sap.sailing.domain.common.racelog.tracking;

public class RaceNotCreatedException extends RuntimeException {

    private static final long serialVersionUID = 6721038282862173471L;

    public RaceNotCreatedException() {
        super();
    }

    public RaceNotCreatedException(String msg) {
        super(msg);
    }
}
