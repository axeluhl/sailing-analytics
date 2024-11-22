package com.sap.sailing.domain.igtimiadapter.riot;

import java.nio.channels.ServerSocketChannel;

import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataPoint;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.riot.impl.RiotServerImpl;
import com.sap.sailing.domain.igtimiadapter.shared.IgtimiWindReceiver;

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
public interface RiotServer {
    static RiotServer create(int port) {
        return new RiotServerImpl(port);
    }
    
    void addListener(BulkFixReceiver listener);
    
    void removeListener(BulkFixReceiver listener);
}
