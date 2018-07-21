package com.sap.sailing.domain.swisstimingadapter;

public interface SailMasterMessage {

    /**
     * @return the entire message with STX and ETX stripped
     */
    String getMessage();

    /**
     * @return the {@link #getMessage()} result split along occurrences of the "|" character ("pipe"). The element with index 0
     * is usually the message type field.
     */
    String[] getSections();
    
    MessageType getType();
    
    boolean isRequest();
    
    boolean isResponse();
    
    boolean isEvent();
    
    /**
     * Most messages emitted by a SailMaster are specific to a single race. If this is such a message,
     * the race ID is returned. Otherwise, (e.g., if the message is a {@link MessageType#RAC RAC} message),
     * <code>null</code> is returned.
     */
    String getRaceID();

    /**
     * The sequence number as added to each message as a prefix by
     * the SwissTiming SAP Gateway. This sequence number can be used
     * in a <code>LSN</code> message to query race messages starting
     * from a specific message onwards.
     */
    Long getSequenceNumber();
}
