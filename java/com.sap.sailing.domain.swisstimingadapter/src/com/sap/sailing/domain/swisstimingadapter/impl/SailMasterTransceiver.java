package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A sender / receiver for a SwissTiming Sail Master system.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SailMasterTransceiver {
    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;

    /**
     * Sends the string message to the output stream <code>os</code>, using the cp1252 character set
     * for encoding and framing the message with the <code>STX</code> and <code>ETX</code> bytes.
     */
    public synchronized void sendMessage(String message, OutputStream os) throws IOException {
        os.write(STX);
        os.write(message.getBytes(Charset.forName("cp1252")));
        os.write(ETX);
    }

    /**
     * Receives a single message from the <code>inputStream</code> specified. The surrounding
     * <code>STX/ETX</code> marker bytes are removed from the message. The message is decoded
     * into a string using the cp1252 character encoding.
     */
    public String receiveMessage(InputStream inputStream) throws IOException {
        // read until an STX byte comes along
        int read = inputStream.read();
        while (read != -1 && read != STX) {
            read = inputStream.read();
        }
        String message = null;
        if (read == STX) {
            message = readMessage(inputStream);
        }
        return message;
    }

    /**
     * Read bytes until <code>EOF</code> or <code>ETX</code> is reached and turn those bytes into a string
     * using the cp1252 character encoding.
     */
    private String readMessage(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        int read = is.read();
        while (read != -1 && read != ETX) {
            bos.write(read);
            read = is.read();
        }
        String message = new String(bos.toByteArray(), Charset.forName("cp1252"));
        return message;
    }

}
