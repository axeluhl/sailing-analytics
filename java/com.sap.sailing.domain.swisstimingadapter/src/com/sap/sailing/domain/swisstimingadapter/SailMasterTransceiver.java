package com.sap.sailing.domain.swisstimingadapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface SailMasterTransceiver {
    /**
     * Sends a message, prefixing it with a start byte and suffixing it with an end byte, as required by the
     * SwissTiming SailMaster protocol.
     */
    void sendMessage(String message, OutputStream os) throws IOException;
    
    /**
     * Sends the message, prefixing it with a start byte and suffixing it with an end byte, as required by the
     * SwissTiming SailMaster protocol. Before the start byte, the ASCII-encoded
     * {@link SailMasterMessage#getSequenceNumber() sequence number} is transmitted.
     */
    void sendMessage(SailMasterMessage message, OutputStream os) throws IOException;

    /**
     * Receives a single message from the <code>inputStream</code> specified. The surrounding <code>STX/ETX</code>
     * marker bytes are removed from the message. The message is decoded into a string using the cp1252 character
     * encoding.
     * 
     * @return the message read, or <code>null</code> if the end of the stream has been reached without finding a
     *         message start byte.
     */
    String receiveMessage(InputStream inputStream) throws IOException;

}
