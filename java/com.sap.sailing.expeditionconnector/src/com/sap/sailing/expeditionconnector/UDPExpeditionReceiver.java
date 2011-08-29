package com.sap.sailing.expeditionconnector;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.expeditionconnector.impl.ExpeditionMessageParser;
import com.sap.sailing.udpconnector.UDPReceiver;

/**
 * When run, starts receiving UDP packets expected to be in the format Expedition writes and notifies registered
 * listeners about all contents received. To stop receiving, call {@link #stop}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class UDPExpeditionReceiver extends UDPReceiver<ExpeditionMessage, ExpeditionListener> {
    /**
     * Remembers, per boat ID, the milliseconds difference between the time the message was received
     * and the GPS time stamp provided by the message.
     */
    private final Map<Integer, Long> timeStampOfLastMessageReceived;
    
    private final ExpeditionMessageParser parser;

    /**
     * Launches a listener and dumps messages received to the console
     * @param args 0: port to listen on
     *  
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        UDPExpeditionReceiver receiver = new UDPExpeditionReceiver(Integer.valueOf(args[0]));
        receiver.addListener(new ExpeditionListener() {
            @Override
            public void received(ExpeditionMessage message) {
                System.out.println(message);
            }
        }, /* validMessagesOnly */ false);
        receiver.run();
    }

    /**
     * You need call {@link #run} to actually start receiving events. To do this asynchronously,
     * start this object in a new thread.
     */
    public UDPExpeditionReceiver(int listeningOnPort) throws SocketException {
        super(listeningOnPort);
        this.timeStampOfLastMessageReceived = new HashMap<Integer, Long>();
        parser = new ExpeditionMessageParser(this);
    }
    
    public Map<Integer, Long> getTimeStampOfLastMessageReceived() {
        return timeStampOfLastMessageReceived;
    }
    
    protected ExpeditionMessageParser getParser() {
        return parser;
    }

}
