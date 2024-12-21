package com.sap.sailing.domain.igtimiadapter.server.riot;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.igtimiadapter.server.RiotWebsocketHandler;
import com.sap.sailing.domain.igtimiadapter.server.replication.ReplicableRiotServer;
import com.sap.sailing.domain.igtimiadapter.server.replication.RiotReplicationOperation;
import com.sap.sailing.domain.igtimiadapter.server.riot.impl.RiotServerImpl;
import com.sap.sailing.domain.igtimiadapter.shared.IgtimiWindReceiver;
import com.sap.sse.common.TimeRange;
import com.sap.sse.replication.Replicable;

/**
 * A server implementation according to the protocol specification found
 * <a href="https://support.yacht-bot.com/YachtBot%20Products/Riot%20Protocol/">here</a>.
 * An instance can be created using the {@link #create(int)} method, returning a
 * server listening for incoming connections on the TCP port specified.<p>
 * 
 * Connections will be tracked and managed. When a device has connected, heartbeats
 * are sent to the device approximately every 15s until the device disconnects
 * or, due to missing heartbeat messages sent by the device, the connection is deemed
 * dead.<p>
 * 
 * {@link DataMsg} messages {@link DataMsg#getDataList() containing} {@link DataPoint}s are
 * converted to lists of {@link Fix}es which are forwarded to {@link BulkFixReceiver}s that
 * can be registered with this server using the {@link #addListener(BulkFixReceiver)} method.
 * <p>
 * 
 * When combined with {@link IgtimiWindReceiver} (which is such a {@link BulkFixReceiver},
 * {@link IgtimiWindListener}s can be registered on the wind receiver, just as they can
 * for a websocket connection.
 * 
 * The server uses <tt>java.nio</tt> and {@link ServerSocketChannel}s, avoiding the creation
 * of a thread per connection.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RiotServer extends Replicable<ReplicableRiotServer, RiotReplicationOperation<?>> {
    /**
     * Created a {@link RiotServer} listening on the {@code port} specified. If the port is not
     * available, an {@link IOException} will be thrown.
     */
    static RiotServer create(int port, DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) throws Exception {
        return new RiotServerImpl(port, domainObjectFactory, mongoObjectFactory);
    }
    
    /**
     * Creates a {@link RiotServer} listening on an available local TCP port selected automatically.
     */
    static RiotServer create(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) throws Exception {
        return new RiotServerImpl(domainObjectFactory, mongoObjectFactory);
    }
    
    void addListener(RiotMessageListener listener);
    
    void removeListener(RiotMessageListener listener);

    /**
     * Stops this server and frees its socket resources. If the server is not running, e.g., because it was already
     * stopped by an earlier call to this method, calling this method has no effect.
     */
    void stop() throws IOException;

    /**
     * @return the IP port this server is listening on for new connections
     */
    int getPort() throws IOException;

    Iterable<Resource> getResources();
    
    Resource getResourceById(long id);
    
    void addResource(Resource resource);
    
    void removeResource(long resourceId);

    Iterable<Device> getDevices();
    
    Device getDeviceById(long id);
    
    void addDevice(Device device);
    
    void removeDevice(long deviceId);
    
    Iterable<DataAccessWindow> getDataAccessWindows();
    
    DataAccessWindow getDataAccessWindowById(long id);
    
    Iterable<DataAccessWindow> getDataAccessWindows(Iterable<String> serialNumbers, TimeRange timeRange);
    
    void addDataAccessWindow(DataAccessWindow daw);
    
    void removeDataAccessWindow(long dawId);

    // TODO clarify what this means in the presence of replication, when run on a Replica
    Iterable<Msg> getMessages(String deviceSerialNumber, TimeRange timeRange);

    void addWebSocketClient(RiotWebsocketHandler riotWebsocketHandler);

    void removeWebSocketClient(RiotWebsocketHandler riotWebsocketHandler);
}
