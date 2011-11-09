package com.sap.sailing.domain.swisstimingadapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sap.sailing.util.Util.Pair;

public interface SailMasterTransceiver {
    void sendMessage(String message, OutputStream os) throws IOException;

    /**
     * Receives a single message from the <code>inputStream</code> specified. The surrounding <code>STX/ETX</code>
     * marker bytes are removed from the message. The message is decoded into a string using the cp1252 character
     * encoding.
     * 
     * @return the message read, or <code>null</code> if the end of the stream has been reached without finding a
     *         message start byte.
     */
    Pair<String, Integer> receiveMessage(InputStream inputStream) throws IOException;

}
