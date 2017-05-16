package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;

public class SailMasterMessageImpl implements SailMasterMessage {
    private static Pattern leadingSequenceNumber = Pattern.compile("^([0-9][0-9]*)\\|(.*)$");
    private final String message;
    private final Long sequenceNumber;
    
    private String[] sections;

    /**
     * Produces a message from a message string; if the message string carries a leading sequence number (instead of a
     * leading {@link MessageType} name), the {@link #sequenceNumber} field will be filled accordingly or remain
     * <code>null</code> otherwise.
     * 
     * @param message
     *            If the message starts with the pattern "[0-9][0-9]*|" then the number preceding the pipe symbol is
     *            taken to be the {@link #sequenceNumber} and everything after the pipe symbol is assigned to
     *            {@link #message}. Otherwise, the {@link #sequenceNumber} field is initialized to <code>null</code>,
     *            and the <code>message</code> parameter's value is assigned to the {@link #message} field unchanged.
     */
    public SailMasterMessageImpl(String message) {
        super();
        Matcher m = leadingSequenceNumber.matcher(message);
        if (m.matches()) {
            this.sequenceNumber = Long.valueOf(m.group(1));
            this.message = m.group(2);
        } else {
            this.sequenceNumber = null;
            this.message = message;
        }
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
        return (getSequenceNumber() == null ? "" : ""+getSequenceNumber()+": ")+getMessage();
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
        try {
            return MessageType.valueOf(typeAsString);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
