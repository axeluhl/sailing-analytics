package com.sap.sailing.domain.swisstimingadapter;

public interface SailMasterMessage {

    /**
     * @return the entire message with STX and ETX stripped
     */
    String getMessage();

    /**
     * @return the {@link #getMessage()} result split along occurrences of the "|" character ("pipe")
     */
    String[] getSections();
    
    MessageType getType();
    
    boolean isRequest();
    
    boolean isResponse();
    
    boolean isEvent();

    Integer getSequenceNumber();
}
