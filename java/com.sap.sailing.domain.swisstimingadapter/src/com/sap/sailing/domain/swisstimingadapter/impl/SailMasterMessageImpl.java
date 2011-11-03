package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;

public class SailMasterMessageImpl implements SailMasterMessage {
    private final String message;
    
    private String[] sections;

    public SailMasterMessageImpl(String message) {
        super();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public synchronized String[] getSections() {
        if (sections == null) {
            sections = getMessage().split("\\|");
        }
        return sections;
    }
    
    @Override
    public String toString() {
        return getMessage();
    }

    @Override
    public MessageType getType() {
        String typeAsStringWithOptionalSuffix = getSections()[0];
        String typeAsString;
        if (isEvent()) {
            typeAsString = typeAsStringWithOptionalSuffix;
        } else {
            typeAsString = typeAsStringWithOptionalSuffix.substring(0, typeAsStringWithOptionalSuffix.length()-1);
        }
        return MessageType.valueOf(typeAsString);
    }

    @Override
    public boolean isRequest() {
        return getSections()[0].charAt(getSections()[0].length()-1) == '?';
    }

    @Override
    public boolean isResponse() {
        return getSections()[0].charAt(getSections()[0].length()-1) == '!';
    }

    @Override
    public boolean isEvent() {
        return !isRequest() && !isResponse();
    }
}
