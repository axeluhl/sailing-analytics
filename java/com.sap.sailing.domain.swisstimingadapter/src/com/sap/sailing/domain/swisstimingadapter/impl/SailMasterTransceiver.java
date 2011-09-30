package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SailMasterTransceiver {
    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;

    public void sendMessage(String message, OutputStream os) throws IOException {
        os.write(STX);
        os.write(message.getBytes()); // TODO clarify which character set / encoding to use
        os.write(ETX);
    }

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
     * Read bytes until EOF or ETX is reached and turn those bytes into a string.
     */
    private String readMessage(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        int read = is.read();
        while (read != -1 && read != ETX) {
            bos.write(read);
            read = is.read();
        }
        String message = new String(bos.toByteArray()); // TODO clarify character set / encoding with Kai
        return message;
    }

}
