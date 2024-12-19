package com.sap.sailing.domain.igtimiadapter.server.riot.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Thread.State;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
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
import com.sap.sailing.domain.igtimiadapter.server.replication.ReplicableRiotServer;
import com.sap.sailing.domain.igtimiadapter.server.replication.RiotReplicationOperation;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotConnection;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.Duration;
import com.sap.sse.replication.interfaces.impl.AbstractReplicableWithObjectInputStream;
import com.sap.sse.shared.util.Wait;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public class RiotServerImpl extends AbstractReplicableWithObjectInputStream<ReplicableRiotServer, RiotReplicationOperation<?>> implements RiotServer, ReplicableRiotServer, Runnable {
    private static final Logger logger = Logger.getLogger(RiotServerImpl.class.getName());

    private final ConcurrentMap<Long, Resource> resources;
    private final ConcurrentMap<Long, DataAccessWindow> dataAccessWindows;
    private final ConcurrentMap<Long, Device> devices;
    private final Set<BulkFixReceiver> listeners;
    private final Selector socketSelector;
    private final ServerSocketChannel serverSocketChannel;
    private final Thread communicatorThread;
    private boolean running;
    private final MongoObjectFactory mongoObjectFactory;
    
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
        this.mongoObjectFactory = mongoObjectFactory;
        for (final Device device : domainObjectFactory.getDevices()) {
            devices.put(device.getId(), device);
        }
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
    public Iterable<Device> getDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }
    
    @Override
    public Device getDeviceById(long id) {
        return devices.get(id);
    }

    @Override
    public void addDevice(Device device) {
        apply(s->s.internalAddDevice(device));
    }
    
    @Override
    public Void internalAddDevice(Device device) {
        devices.put(device.getId(), device);
        mongoObjectFactory.storeDevice(device);
        return null;
    }

    @Override
    public void removeDevice(long deviceId) {
        apply(s->s.internalRemoveDevice(deviceId));
    }
    
    @Override
    public Void internalRemoveDevice(long deviceId) {
        devices.remove(deviceId);
        mongoObjectFactory.removeDevice(deviceId);
        return null;
    }

    @Override
    public Iterable<Resource> getResources() {
        return Collections.unmodifiableCollection(resources.values());
    }
    
    @Override
    public Resource getResourceById(long id) {
        return resources.get(id);
    }
    
    @Override
    public void addResource(Resource resource) {
        apply(s->s.internalAddResource(resource));
    }
    
    @Override
    public Void internalAddResource(Resource resource) {
        resources.put(resource.getId(), resource);
        mongoObjectFactory.storeResource(resource);
        return null;
    }

    @Override
    public void removeResource(long resourceId) {
        apply(s->s.internalRemoveResource(resourceId));
    }
    
    @Override
    public Void internalRemoveResource(long resourceId) {
        resources.remove(resourceId);
        mongoObjectFactory.removeResource(resourceId);
        return null;
    }

    @Override
    public Iterable<DataAccessWindow> getDataAccessWindows() {
        return Collections.unmodifiableCollection(dataAccessWindows.values());
    }
    
    @Override
    public DataAccessWindow getDataAccessWindowById(long id) {
        return dataAccessWindows.get(id);
    }

    @Override
    public void addDataAccessWindow(DataAccessWindow daw) {
        apply(s->s.internalAddDataAccessWindow(daw));
    }

    @Override
    public Void internalAddDataAccessWindow(DataAccessWindow daw) {
        dataAccessWindows.put(daw.getId(), daw);
        mongoObjectFactory.storeDataAccessWindow(daw);
        return null;
    }
    
    @Override
    public void removeDataAccessWindow(long dawId) {
        apply(s->s.internalRemoveDataAccessWindow(dawId));
    }

    @Override
    public Void internalRemoveDataAccessWindow(long dawId) {
        dataAccessWindows.remove(dawId);
        mongoObjectFactory.removeDataAccessWindow(dawId);
        return null;
    }

    /**
     * Use this method only for testing/debugging. It clears the transient state of this service,in particular
     * all resources and data access windows.
     */
    public void clear() {
        devices.clear();
        resources.clear();
        dataAccessWindows.clear();
        connections.clear();
        listeners.clear();
    }

    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        clear();
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is,
            Map<String, Class<?>> classLoaderCache) throws IOException {
        return new ObjectInputStreamResolvingAgainstCache<Object>(is, /* dummy cache */ new Object(), /* resolve listener */ null, classLoaderCache) {
        }; // use anonymous inner class in this class loader to see all that this class sees
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        // TODO Auto-generated method stub
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(devices);
        objectOutputStream.writeObject(resources);
        objectOutputStream.writeObject(dataAccessWindows);
    }
}
