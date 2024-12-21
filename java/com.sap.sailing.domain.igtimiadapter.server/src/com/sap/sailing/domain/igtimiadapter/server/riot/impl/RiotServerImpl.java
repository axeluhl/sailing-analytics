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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiDevice.DeviceManagement;
import com.igtimi.IgtimiDevice.DeviceManagementResponse;
import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.DataPointTimePointExtractor;
import com.sap.sailing.domain.igtimiadapter.DataPointVisitor;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.igtimiadapter.server.Activator;
import com.sap.sailing.domain.igtimiadapter.server.RiotWebsocketHandler;
import com.sap.sailing.domain.igtimiadapter.server.replication.ReplicableRiotServer;
import com.sap.sailing.domain.igtimiadapter.server.replication.RiotReplicationOperation;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotConnection;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotMessageListener;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.replication.interfaces.impl.AbstractReplicableWithObjectInputStream;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.shared.util.Wait;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public class RiotServerImpl extends AbstractReplicableWithObjectInputStream<ReplicableRiotServer, RiotReplicationOperation<?>> implements RiotServer, ReplicableRiotServer, Runnable {
    private static final Logger logger = Logger.getLogger(RiotServerImpl.class.getName());

    private final ConcurrentMap<Long, Resource> resources;
    private final ConcurrentMap<Long, DataAccessWindow> dataAccessWindows;
    private final ConcurrentMap<Long, Device> devices;
    private final ConcurrentMap<String, Device> devicesBySerialNumber;
    private final Set<RiotMessageListener> listeners;
    private final Selector socketSelector;
    private final ServerSocketChannel serverSocketChannel;
    private final Thread communicatorThread;
    private boolean running;
    private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    private final Set<RiotWebsocketHandler> liveWebSocketConnections;
    
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
        this.devicesBySerialNumber = new ConcurrentHashMap<>();
        this.connections = new ConcurrentHashMap<>();
        this.domainObjectFactory = domainObjectFactory;
        this.mongoObjectFactory = mongoObjectFactory;
        this.liveWebSocketConnections = ConcurrentHashMap.newKeySet();
        for (final Device device : domainObjectFactory.getDevices()) {
            devices.put(device.getId(), device);
            devicesBySerialNumber.put(device.getSerialNumber(), device);
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
                            connections.put(deviceChannel, new RiotConnectionImpl(deviceChannel, this, mongoObjectFactory));
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
    public void addListener(RiotMessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RiotMessageListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Forwards the {@code message} to all {@link #addListener(RiotMessageListener) registered} listeners and sends it
     * to all {@link #liveWebSocketConnections} that are authorized to {@link DefaultActions#READ READ} from the
     * {@link Device} from which the message originated and for which a READable {@link DataAccessWindow} exists for
     * the web socket connection's authenticated user for the time points of the data points in the message.
     * 
     * @param message
     *            the message received from the device
     * @param deviceSerialNumber
     *            the serial number of the device from which the message originated
     */
    void notifyListeners(Msg message, String deviceSerialNumber) {
        if (deviceSerialNumber != null) {
            forwardMessageToEligibleWebSocketClients(message, deviceSerialNumber);
        }
        for (final RiotMessageListener listener : listeners) {
            listener.onMessage(message);
        }
    }

    /**
     * @param deviceSerialNumber must not be {@code null}
     */
    private void forwardMessageToEligibleWebSocketClients(Msg message, String deviceSerialNumber) {
        final Device device = getDeviceBySerialNumber(deviceSerialNumber);
        if (device == null) {
            logger.info("Received message from unknown devices "+deviceSerialNumber);
            // TODO should we use this as the trigger to *create* the device?
        } else {
            final byte[] messageAsBytes = message.toByteArray();
            final SecurityService securityService = Activator.getInstance().getSecurityService();
            final TimeRange messageDataTimeRange = getTimeRange(message);
            final Iterable<DataAccessWindow> daws = getDataAccessWindows(Collections.singleton(deviceSerialNumber), messageDataTimeRange);
            for (final RiotWebsocketHandler webSocketClient : liveWebSocketConnections) {
                final User user = webSocketClient.getAuthenticatedUser();
                final OwnershipAnnotation deviceOwnership = securityService.getOwnership(device.getIdentifier());
                final AccessControlListAnnotation deviceAccessControlList = securityService.getAccessControlList(device.getIdentifier());
                if (!Util.isEmpty(Util.filter(daws, daw->{
                    final OwnershipAnnotation dawOownership = securityService.getOwnership(daw.getIdentifier());
                    final AccessControlListAnnotation dawAccessControlList = securityService.getAccessControlList(daw.getIdentifier());
                    return PermissionChecker.isPermitted(
                            daw.getIdentifier().getPermission(DefaultActions.READ),
                            user, securityService.getAllUser(),
                            dawOownership==null?null:dawOownership.getAnnotation(),
                            dawAccessControlList==null?null:dawAccessControlList.getAnnotation());
                }))
                && PermissionChecker.isPermitted(
                        device.getIdentifier().getPermission(DefaultActions.READ),
                        user, securityService.getAllUser(),
                        deviceOwnership==null?null:deviceOwnership.getAnnotation(),
                        deviceAccessControlList==null?null:deviceAccessControlList.getAnnotation()) ) {
                    webSocketClient.sendBytesByFuture(ByteBuffer.wrap(messageAsBytes));
                }
            }
        }
    }

    /**
     * Returns a time range spanning the time point of the first and the last of the {@link DataPoint}s inside
     * 
     * @return {@code null} in case there is nothing inside the message that specifies a time point, otherwise a time
     *         range that includes the time points of all elements from the message that have a time stamp and that
     *         cannot be made any smaller while still containing all those elements.
     */
    private TimeRange getTimeRange(Msg message) {
        TimePoint from = null;
        TimePoint to = null;
        if (message.hasData()) {
            for (final DataMsg data : message.getData().getDataList()) {
                for (final DataPoint dataPoint : data.getDataList()) {
                    final TimePoint dataPointTimePoint = DataPointVisitor.accept(dataPoint, new DataPointTimePointExtractor());
                    if (from==null || dataPointTimePoint.before(from)) {
                        from = dataPointTimePoint;
                    }
                    if (to==null || dataPointTimePoint.after(to)) {
                        to = dataPointTimePoint;
                    }
                }
            }
        } else if (message.hasDeviceManagement()) {
            final DeviceManagement deviceManagement = message.getDeviceManagement();
            if (deviceManagement.hasResponse()) {
                final DeviceManagementResponse response = deviceManagement.getResponse();
                final TimePoint timePoint = TimePoint.of(response.getTimestamp());
                if (from==null || timePoint.before(from)) {
                    from = timePoint;
                }
                if (to==null || timePoint.after(to)) {
                    to = timePoint;
                }
            }
        }
        return TimeRange.create(from, to==null?null:to.plus(Duration.ONE_MILLISECOND));
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
    public Device getDeviceBySerialNumber(String deviceSerialNumber) {
        return devicesBySerialNumber.get(deviceSerialNumber);
    }

    @Override
    public void addDevice(Device device) {
        apply(s->s.internalAddDevice(device));
    }
    
    @Override
    public Void internalAddDevice(Device device) {
        devices.put(device.getId(), device);
        devicesBySerialNumber.put(device.getSerialNumber(), device);
        mongoObjectFactory.storeDevice(device);
        return null;
    }

    @Override
    public void removeDevice(long deviceId) {
        apply(s->s.internalRemoveDevice(deviceId));
    }
    
    @Override
    public Void internalRemoveDevice(long deviceId) {
        final Device device = devices.remove(deviceId);
        devicesBySerialNumber.remove(device.getSerialNumber());
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
    public Iterable<DataAccessWindow> getDataAccessWindows(Iterable<String> deviceSerialNumbers, TimeRange timeRange) {
        final Set<DataAccessWindow> result = new HashSet<>();
        final Set<String> deviceSerialNumbersAsSet = new HashSet<>();
        Util.addAll(deviceSerialNumbers, deviceSerialNumbersAsSet);
        // TODO provide a more efficient implementation if this turns out to become a performance bottleneck, e.g., by keeping the DataAccessWindows in a time-sorted TreeSet
        for (final DataAccessWindow daw : getDataAccessWindows()) {
            if (deviceSerialNumbersAsSet.contains(daw.getDeviceSerialNumber()) && timeRange.intersects(daw.getTimeRange())) {
                result.add(daw);
            }
        }
        return result;
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
        devicesBySerialNumber.clear();
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

    @SuppressWarnings("unchecked")
    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        devices.putAll((Map<Long, Device>) is.readObject());
        for (final Device device : devices.values()) {
            devicesBySerialNumber.put(device.getSerialNumber(), device);
        }
        resources.putAll((Map<Long, Resource>) is.readObject());
        dataAccessWindows.putAll((Map<Long, DataAccessWindow>) is.readObject());
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(new HashMap<>(devices));
        objectOutputStream.writeObject(new HashMap<>(resources));
        objectOutputStream.writeObject(new HashMap<>(dataAccessWindows));
    }

    @Override
    public Iterable<Msg> getMessages(String deviceSerialNumber, TimeRange timeRange) {
        return domainObjectFactory.getMessages(deviceSerialNumber, timeRange);
    }

    @Override
    public void addWebSocketClient(RiotWebsocketHandler riotWebsocketHandler) {
        liveWebSocketConnections.add(riotWebsocketHandler);
    }
    
    @Override
    public void removeWebSocketClient(RiotWebsocketHandler riotWebsocketHandler) {
        liveWebSocketConnections.remove(riotWebsocketHandler);
    }
}
