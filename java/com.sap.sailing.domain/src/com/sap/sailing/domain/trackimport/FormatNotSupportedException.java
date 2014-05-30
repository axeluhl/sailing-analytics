package com.sap.sailing.domain.trackimport;

public class FormatNotSupportedException extends Exception {

    private static final long serialVersionUID = 9018109746837015286L;

    public FormatNotSupportedException(String msg) {
        super(msg);
    }

    public FormatNotSupportedException() {
        super();
    }
}
