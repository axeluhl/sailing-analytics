package com.sap.sailing.udpconnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public abstract class UDPReceiver<MessageType extends UDPMessage, ListenerType extends UDPMessageListener<MessageType>> implements Runnable {
    private boolean stopped = false;

    private final int listeningOnPort;

    private final DatagramSocket udpSocket;

    /**
     * For each listener remembers if the listener is only interested in valid messages.
     */
    private final Map<ListenerType, Boolean> listeners;

    /**
     * Maximum IP packet length
     */
    private static final int MAX_PACKET_SIZE = 65536;
    
    /**
     * You need call {@link #run} to actually start receiving events. To do this asynchronously,
     * start this object in a new thread.
     */
    public UDPReceiver(int listeningOnPort) throws SocketException {
        udpSocket = new DatagramSocket(listeningOnPort);
        listeners = new HashMap<ListenerType, Boolean>();
        this.listeningOnPort = listeningOnPort;
    }
    
    public void stop() throws SocketException, IOException {
        stopped = true;
        byte[] buf = new byte[0];
        DatagramPacket stopPacket = new DatagramPacket(buf, 0, InetAddress.getLocalHost(), listeningOnPort);
        new DatagramSocket().send(stopPacket);
        if (!udpSocket.isConnected()) {
            udpSocket.close();
        }
    }
    
    /**
     * If there are currently no listeners subscribed (see {@link #addListener(ListenerType, boolean)} and
     * {@link #removeListener(ListenerType)}), this receiver is {@link #stop() stopped} and <code>true</code>
     * is returned; otherwise, <code>false</code> is returned.
     */
    public synchronized boolean stopIfNoListeners() throws SocketException, IOException {
        boolean result = listeners.isEmpty();
        if (result) {
            stop();
        }
        return result;
    }
    
    public boolean isStopped() {
        return stopped;
    }

    public void run() {
        byte[] buf = new byte[MAX_PACKET_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        while (!stopped) {
            try {
                udpSocket.receive(p);
                String packetAsString = new String(p.getData(), p.getOffset(), p.getLength()).trim();
                if (packetAsString.length() > 0) {
                    MessageType msg = getParser().parse(p);
                    if (msg != null) {
                        for (ListenerType listener : listeners.keySet()) {
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
        if (udpSocket.isConnected()) {
            udpSocket.disconnect();
        }
    }

    public synchronized void addListener(ListenerType listener, boolean validMessagesOnly) {
        listeners.put(listener, validMessagesOnly);
    }

    public synchronized void removeListener(ListenerType listener) {
        listeners.remove(listener);
    }

    public int getPort() {
        return listeningOnPort;
    }

    protected abstract UDPMessageParser<MessageType> getParser();
}
