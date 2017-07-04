package com.sap.sailing.udpconnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UDPReceiver<MessageType extends UDPMessage, ListenerType extends UDPMessageListener<MessageType>> implements Runnable {
    private static final Logger logger = Logger.getLogger(UDPReceiver.class.getName());
    
    private boolean stopped = false;

    private final int listeningOnPort;

    private final DatagramSocket udpSocket;
    
    private final Map<ListenerType, ToListenerDispatcher> listenerThreads;

    /**
     * For each listener remembers if the listener is only interested in valid messages.
     */
    private final Map<ListenerType, Boolean> listeners;

    /**
     * Maximum IP packet length
     */
    private static final int MAX_PACKET_SIZE = 65536;
    
    private class ToListenerDispatcher extends Thread {
        private final LinkedBlockingDeque<MessageType> queue;
        private final ListenerType listener;
        private boolean stopped;
        
        public ToListenerDispatcher(ListenerType listener) {
            super("UDPReceiver ToListenerDispatcher for listener "+listener);
            queue = new LinkedBlockingDeque<MessageType>(/* capacity */ 10000);
            this.listener = listener;
            stopped = false;
        }
        
        /**
         * Offers the message to the queue used by this dispatcher. If the queue is full, the message is dropped without notice.
         */
        public void dispatch(MessageType message) {
            queue.offer(message);
        }
        
        public void halt() {
            stopped = true;
        }
        
        public void run() {
            MessageType message;
            try {
                message = queue.poll(/* timeout */ 10000, TimeUnit.MILLISECONDS);
                while (!stopped) {
                    if (message != null) {
                        listener.received(message);
                    }
                    message = queue.poll(/* timeout */ 10000, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                // when interrupted, this means we'll terminate
            }
        }
    }
    
    /**
     * You need call {@link #run} to actually start receiving events. To do this asynchronously,
     * start this object in a new thread.
     */
    public UDPReceiver(int listeningOnPort) throws SocketException {
        udpSocket = new DatagramSocket(listeningOnPort);
        listeners = new HashMap<ListenerType, Boolean>();
        this.listeningOnPort = listeningOnPort;
        listenerThreads = new HashMap<ListenerType, ToListenerDispatcher>();
    }
    
    public void stop() throws SocketException, IOException {
        stopped = true;
        byte[] buf = new byte[0];
        DatagramPacket stopPacket = new DatagramPacket(buf, 0, InetAddress.getLocalHost(), listeningOnPort);
        
        DatagramSocket stopper = new DatagramSocket();
        stopper.send(stopPacket);
        stopper.close();
        
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
                                try {
                                    ToListenerDispatcher dispatcher = listenerThreads.get(listener);
                                    dispatcher.dispatch(msg);
                                } catch (Exception e) {
                                    logger.info("Exception while dispatching UDP packet received to "+listener+": "+e.getMessage());
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception while receiving UDP packet", e);
            }
        }
        logger.info("Closing UDP socket on port "+udpSocket.getPort());
        udpSocket.close();
        if (udpSocket.isConnected()) {
            udpSocket.disconnect();
        }
    }

    public synchronized void addListener(ListenerType listener, boolean validMessagesOnly) {
        final ToListenerDispatcher listenerThread = new ToListenerDispatcher(listener);
        listenerThread.setDaemon(true);
        listenerThread.start();
        listenerThreads.put(listener, listenerThread);
        listeners.put(listener, validMessagesOnly);
    }

    public synchronized void removeListener(ListenerType listener) {
        listeners.remove(listener);
        listenerThreads.remove(listener).halt();
    }

    public int getPort() {
        return listeningOnPort;
    }

    protected abstract UDPMessageParser<MessageType> getParser();
}
