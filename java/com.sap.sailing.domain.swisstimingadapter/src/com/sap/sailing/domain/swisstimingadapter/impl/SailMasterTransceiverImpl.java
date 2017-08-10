package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;

/**
 * A sender / receiver for a SwissTiming Sail Master system.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SailMasterTransceiverImpl implements SailMasterTransceiver {
    private static final Logger logger = Logger.getLogger(SailMasterTransceiverImpl.class.getName());
    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;

    /**
     * Sends the string message to the output stream <code>os</code>, using the cp1252 character set
     * for encoding and framing the message with the <code>STX</code> and <code>ETX</code> bytes.
     */
    @Override
    public synchronized void sendMessage(String message, OutputStream os) throws IOException {
        os.write(STX);
        os.write(message.getBytes(Charset.forName("cp1252")));
        os.write(ETX);
    }
    
    @Override
    public synchronized void sendMessage(SailMasterMessage message, OutputStream os) throws IOException {
        if (message.getSequenceNumber() == null) {
            sendMessage(message.getMessage(), os);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(message.getSequenceNumber());
            sb.append('|');
            sb.append(message.getMessage());
            sendMessage(sb.toString(), os);
        }
        os.flush();
    }

    /**
     * Receives a single message from the <code>inputStream</code> specified. The surrounding <code>STX/ETX</code>
     * marker bytes are removed from the message. The message is decoded into a string using the cp1252 character
     * encoding.
     * 
     * @return <code>null</code> if the end of the stream has been reached without finding a message start byte; the
     *         message otherwise.
     */
    public String receiveMessage(InputStream inputStream) throws IOException {
        // read until an STX byte comes along
        int read;
        while ((read = inputStream.read()) != -1 && read != STX);
        // now either we've read an STX byte or reached EOF
        String message = null;
        if (read == STX) {
            message = readMessage(inputStream);
        } else {
            // EOF; indicate by returning null
            logger.info("Received EOF in SailMasterTransceiver. Returning null as message.");
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
