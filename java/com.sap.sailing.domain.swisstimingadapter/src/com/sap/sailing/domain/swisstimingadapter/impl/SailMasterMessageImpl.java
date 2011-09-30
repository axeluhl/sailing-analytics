package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;

public class SailMasterMessageImpl implements SailMasterMessage {
    private final String message;

    public SailMasterMessageImpl(String message) {
        super();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String[] getSections() {
        return getMessage().split("|");
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
