package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;

public class SailMasterMessageImpl implements SailMasterMessage {
    private final String message;
    private final Long sequenceNumber;
    
    private String[] sections;

    public SailMasterMessageImpl(String message, Long sequenceNumber) {
        super();
        this.message = message;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public Long getSequenceNumber() {
        return sequenceNumber;
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
        return ""+getSequenceNumber()+": "+getMessage();
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

    /**
     * For race-specific messages, the race ID is always found in section #1
     */
    @Override
    public String getRaceID() {
        String result;
        if (getType().isRaceSpecific()) {
            result = getSections()[1];
        } else {
            result = null;
        }
        return result;
    }
}
