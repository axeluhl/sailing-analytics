package com.sap.sailing.expeditionconnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.expeditionconnector.impl.ExpeditionMessageImpl;

/**
 * When run, starts receiving UDP packets expected to be in the format Expedition writes and notifies registered
 * listeners about all contents received. To stop receiving, call {@link #stop}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class UDPExpeditionReceiver implements Runnable {
    private boolean stopped = false;

    private final int listeningOnPort;

    private final DatagramSocket udpSocket;

    /**
     * For each listener remembers if the listener is only interested in valid messages.
     */
    private final Map<ExpeditionListener, Boolean> listeners;

    /**
     * Maximum IP packet length
     */
    private static final int MAX_PACKET_SIZE = 65536;

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

    public UDPExpeditionReceiver(int listeningOnPort) throws SocketException {
        udpSocket = new DatagramSocket(listeningOnPort);
        listeners = new HashMap<ExpeditionListener, Boolean>();
        this.listeningOnPort = listeningOnPort;
    }

    public void stop() throws SocketException, IOException {
        stopped = true;
        byte[] buf = new byte[0];
        DatagramPacket stopPacket = new DatagramPacket(buf, 0, InetAddress.getLocalHost(), listeningOnPort);
        new DatagramSocket().send(stopPacket);
    }

    public void run() {
        byte[] buf = new byte[MAX_PACKET_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        while (!stopped) {
            try {
                udpSocket.receive(p);
                String packetAsString = new String(p.getData(), p.getOffset(), p.getLength()).trim();
                if (packetAsString.length() > 0) {
                    ExpeditionMessage msg = parse(packetAsString);
                    if (msg != null) {
                        for (ExpeditionListener listener : listeners.keySet()) {
                            if (!listeners.get(listener) || msg.isValid()) {
                                listener.received(msg);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        udpSocket.close();
    }

    private ExpeditionMessage parse(String packetAsString) {
        Pattern completeLinePattern = Pattern
                .compile("#([0-9]*)(( *,([0-9][0-9]*) *, *(-?[0-9]*(\\.[0-9]*)?))*)\\*([0-9a-fA-F][0-9a-fA-F]*)");
        Matcher m = completeLinePattern.matcher(packetAsString);
        boolean valid = m.matches();
        if (valid) {
            int boatID = Integer.valueOf(m.group(1));
            String variableValuePairs = m.group(2).trim().substring(",".length()); // skip the leading ","
            Map<Integer, Double> values = new HashMap<Integer, Double>();
            String[] variablesAndValuesInterleaved = variableValuePairs.split(",");
            for (int i = 0; i < variablesAndValuesInterleaved.length; i++) {
                int variableID = Integer.valueOf(variablesAndValuesInterleaved[i++]);
                double variableValue = Double.valueOf(variablesAndValuesInterleaved[i]);
                values.put(variableID, variableValue);
            }
            int checksum = Integer.valueOf(m.group(m.groupCount()), 16);
            valid = valid && checksumOk(checksum, packetAsString);
            return new ExpeditionMessageImpl(boatID, values, valid);
        } else {
            System.err.println("Unparsable expedition message: " + packetAsString);
            return null; // couldn't even parse
        }
    }

    private boolean checksumOk(int checksum, String packetAsString) {
        int b = 0;
        for (byte stringByte : packetAsString.substring(0, packetAsString.lastIndexOf('*')).getBytes()) {
            b ^= stringByte;
        }
        return b == checksum;
    }

    public void addListener(ExpeditionListener listener, boolean validMessagesOnly) {
        listeners.put(listener, validMessagesOnly);
    }

    public void removeListener(ExpeditionListener listener) {
        listeners.remove(listener);
    }

}
