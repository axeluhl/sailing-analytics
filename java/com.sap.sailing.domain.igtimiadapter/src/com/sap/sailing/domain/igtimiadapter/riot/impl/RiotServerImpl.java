package com.sap.sailing.domain.igtimiadapter.riot.impl;

import java.io.IOException;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.riot.RiotConnection;
import com.sap.sailing.domain.igtimiadapter.riot.RiotServer;

public class RiotServerImpl implements RiotServer, Runnable {
    private static final Logger logger = Logger.getLogger(RiotServerImpl.class.getName());

    private final Set<BulkFixReceiver> listeners;
    private final Selector socketSelector;
    private final ServerSocketChannel serverSocketChannel;
    private final Thread communicatorThread;
    private boolean running;
    
    /**
     * The active connections managed by this server. Heartbeats will be sent into the channels found in the key set on
     * a regular basis. The {@link SocketChannel} keys are registered with the {@link #socketSelector}. Data sent by the
     * devices is read by the {@link #communicatorThread} and {@link RiotConnection#dataReceived(java.nio.ByteBuffer)
     * forwarded} to the {@link RiotConnection} object associated with the key socket channel. It is the connection's
     * responsibility to respond to messages that require a response, such as an authentication request.
     */
    private final Map<SocketChannel, RiotConnection> connections;

    public RiotServerImpl(int port) throws IOException, InterruptedException {
        this.listeners = ConcurrentHashMap.newKeySet();
        this.connections = new ConcurrentHashMap<>();
        this.socketSelector = Selector.open();
        this.serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        this.running = true;
        this.communicatorThread = new Thread(this, "Riot thread listening on port "+port);
        communicatorThread.setDaemon(true);
        communicatorThread.start();
        while (communicatorThread.getState() == State.NEW) {
            Thread.sleep(1);
        }
        logger.info("Riot thread is running and listening for new connections on port "+port);
    }
    
    @Override
    public void stop() throws IOException {
        logger.info("Stopping Riot server listening on address "+serverSocketChannel.getLocalAddress());
        running = false; // cause the thread to exit latest 1s after this call
        serverSocketChannel.keyFor(socketSelector).cancel();
        serverSocketChannel.close();
        for (final SocketChannel deviceChannel : connections.keySet()) {
            logger.info("Closing active device connection "+connections.get(deviceChannel)+" from "+deviceChannel.getRemoteAddress());
            deviceChannel.keyFor(socketSelector).cancel();
        }
        connections.clear();
    }

    @Override
    public void run() {
        while (running) {
            try {
                final int numberOfKeys = socketSelector.select(1000 /* ms timeout; e.g., in order to stop 1s after stop() was called */);
                if (numberOfKeys > 0) {
                    for (final SelectionKey selectedKey : socketSelector.selectedKeys()) {
                        if (selectedKey.isAcceptable()) {
                            final SocketChannel deviceChannel = serverSocketChannel.accept();
                            deviceChannel.configureBlocking(false);
                            deviceChannel.register(socketSelector, SelectionKey.OP_READ);
                            logger.info("New device connection from "+deviceChannel.getRemoteAddress());
                        } else if (selectedKey.isReadable()) {
                            final SocketChannel deviceChannel = (SocketChannel) selectedKey.channel();
                            final RiotConnection connection = connections.get(deviceChannel);
                            final ByteBuffer buffer = ByteBuffer.allocate(4096);
                            final int numberOfBytesRead = deviceChannel.read(buffer);
                            if (numberOfBytesRead > 0 && connection != null) {
                                connection.dataReceived(buffer);
                            } else {
                                logger.warning("Data received from a socket with address "+deviceChannel.getRemoteAddress()
                                              +" that we don't have a managed connection for");
                            }
                            if (numberOfBytesRead == -1) { // EOF
                                logger.info("Device channel from "+deviceChannel.getRemoteAddress()+" closed");
                                if (connection != null) {
                                    logger.info("The device was handled by connection "+connection);
                                    connections.remove(deviceChannel);
                                }
                                deviceChannel.close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception trying to read or accept new connection. Continuing...", e);
            }
        }
    }

    @Override
    public void addListener(BulkFixReceiver listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(BulkFixReceiver listener) {
        listeners.remove(listener);
    }
    
    void notifyListeners(Iterable<Fix> fixes) {
        for (final BulkFixReceiver listener : listeners) {
            listener.received(fixes);
        }
    }
}
