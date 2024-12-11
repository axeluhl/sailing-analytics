package com.sap.sailing.domain.igtimiadapter.server.riot.impl;

import java.io.IOException;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotConnection;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.Duration;
import com.sap.sse.shared.util.Wait;

public class RiotServerImpl implements RiotServer, Runnable {
    private static final Logger logger = Logger.getLogger(RiotServerImpl.class.getName());

    private final ConcurrentMap<Long, Resource> resources;
    private final ConcurrentMap<Long, DataAccessWindow> dataAccessWindows;
    private final ConcurrentMap<String, Device> devices;
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

    /**
     * Like {@link #RiotServerImpl(int)}, only that an available port is selected automatically.
     * The port can be obtained by calling {@link #getPort}.
     */
    public RiotServerImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) throws Exception {
        this(null, domainObjectFactory, mongoObjectFactory);
    }
    
    public RiotServerImpl(int port, DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) throws Exception {
        this(new InetSocketAddress("localhost", port), domainObjectFactory, mongoObjectFactory);
    }
    
    /**
     * @param localAddress if {@code null}, select a local port to listen on automatically
     */
    private RiotServerImpl(SocketAddress localAddress, DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) throws Exception {
        this.listeners = ConcurrentHashMap.newKeySet();
        this.resources = new ConcurrentHashMap<>();
        this.dataAccessWindows = new ConcurrentHashMap<>();
        this.devices = new ConcurrentHashMap<>();
        this.connections = new ConcurrentHashMap<>();
        for (final Resource resource : domainObjectFactory.getResources()) {
            resources.put(resource.getId(), resource);
        }
        for (final DataAccessWindow daw : domainObjectFactory.getDataAccessWindows()) {
            dataAccessWindows.put(daw.getId(), daw);
        }
        this.socketSelector = Selector.open();
        this.serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(localAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        this.running = true;
        this.communicatorThread = new Thread(this, "Riot thread listening on port "+getPort());
        communicatorThread.setDaemon(true);
        communicatorThread.start();
        Wait.wait(()->communicatorThread.getState() != State.NEW, Optional.of(Duration.ONE_SECOND.times(5)), Duration.ONE_SECOND);
        logger.info("Riot thread is running and listening for new connections on port "+getPort());
    }
    
    @Override
    public int getPort() throws IOException {
        return ((InetSocketAddress) serverSocketChannel.getLocalAddress()).getPort();
    }
    
    @Override
    public void stop() throws IOException {
        if (running) {
            logger.info("Stopping Riot server listening on address "+serverSocketChannel.getLocalAddress());
            running = false; // cause the thread to exit latest 1s after this call
            serverSocketChannel.keyFor(socketSelector).cancel();
            serverSocketChannel.close();
            for (final SocketChannel deviceChannel : connections.keySet()) {
                logger.info("Closing active device connection "+connections.get(deviceChannel)+" from "+deviceChannel.getRemoteAddress());
                deviceChannel.keyFor(socketSelector).cancel();
            }
            connections.clear();
        } else {
            logger.info("Trying to stop a Riot server that is not running. Ignoring.");
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                socketSelector.select(1000 /* ms timeout; e.g., in order to stop 1s after stop() was called */);
                for (final SelectionKey selectedKey : socketSelector.selectedKeys()) {
                    if (selectedKey.isAcceptable()) {
                        final SocketChannel deviceChannel = serverSocketChannel.accept();
                        if (deviceChannel != null) { // may be null because the server socket is in non-blocking mode
                            deviceChannel.configureBlocking(false);
                            deviceChannel.register(socketSelector, SelectionKey.OP_READ);
                            connections.put(deviceChannel, new RiotConnectionImpl(deviceChannel, this));
                            logger.info("New device connection from "+deviceChannel.getRemoteAddress());
                        }
                    } else if (selectedKey.isReadable()) {
                        final SocketChannel deviceChannel = (SocketChannel) selectedKey.channel();
                        final ByteBuffer buffer = ByteBuffer.allocate(4096);
                        final int numberOfBytesRead = deviceChannel.read(buffer);
                        final RiotConnection connection = connections.get(deviceChannel);
                        if (numberOfBytesRead > 0) {
                            if (connection != null) {
                                connection.dataReceived(buffer);
                            } else {
                                logger.warning("Data received from a socket with address "+deviceChannel.getRemoteAddress()
                                              +" that we don't have a managed connection for");
                            }
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

    @Override
    public Iterable<Resource> getResources() {
        return Collections.unmodifiableCollection(resources.values());
    }

    @Override
    public Iterable<Device> getDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }

    @Override
    public Iterable<DataAccessWindow> getDataAccessWindows() {
        return Collections.unmodifiableCollection(dataAccessWindows.values());
    }

    /**
     * Use this method only for testing/debugging. It clears the transient state of this service,in particular
     * all resources and data access windows.
     */
    public void clear() {
        resources.clear();
        dataAccessWindows.clear();
    }
}
