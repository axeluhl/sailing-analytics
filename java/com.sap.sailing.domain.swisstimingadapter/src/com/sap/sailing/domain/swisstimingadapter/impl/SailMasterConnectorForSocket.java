package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.swisstimingadapter.MessageType;

/**
 * Implements the connector to the SwissTiming Sail Master system. It uses a host name and port number to establish the
 * connection via TCP. The connector offers a number of explicit service request methods. Additionally, the connector
 * can receive "spontaneous" events sent by the sail master system. Clients can register for those spontaneous events
 * (see {@link #addSailMasterListener}).
 * <p>
 * 
 * When the connector is used with SailMaster instances hidden behind a "bridge" / firewall, no explicit requests are
 * possible, and the connector has to rely solely on the events it receives. It may, though, load recorded race-specific
 * messages through a {@link RaceSpecificMessageLoader} object. If a non-<code>null</code> {@link RaceSpecificMessageLoader}
 * is provided to the constructor, the connector will fetch the {@link #getRace() race} from that loader.
 * Additionally, the connector will use the loader upon each {@link #trackRace(String)} to load all messages recorded
 * by the loader for the race requested so far.
 * <p>
 * 
 * Generally, the connector needs to be instructed for which races it shall handle events using calls to the
 * {@link #trackRace} and {@link #stopTrackingRace} operations. {@link MessageType#isRaceSpecific() Race-specific
 * messages} for other races are ignored and not forwarded to any listener.<p>
 * 
 * Clients that want to wait until the connector changes to {@link #isStopped()} can {@link Object#wait()} on this
 * object because it notifies all waiters when changing from !{@link #isStopped()} to {@link #isStopped()}. 
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class SailMasterConnectorForSocket extends AbstractSailMasterConnector {
    private static final Logger logger = Logger.getLogger(SailMasterConnectorForSocket.class.getName());
    
    private final String host;
    private final int port;
    
    /**
     * Will have a socket connecting to {@link #host}:{@link #port}
     */
    private Socket socket;
    
    public SailMasterConnectorForSocket(String host, int port, String raceId, String raceName, String raceDescription, BoatClass boatClass, SwissTimingRaceTrackerImpl swissTimingRaceTracker) throws InterruptedException, ParseException {
        super(raceId, raceName, raceDescription, boatClass, swissTimingRaceTracker);
        this.host = host;
        this.port = port;
        startReceiverThread();
    }

    @Override
    protected void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.INFO, "Exception trying to close socket. Maybe already closed. Continuing", e);
            }
            socket = null;
        }
    }
    
    @Override
    protected OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
    
    @Override
    protected InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }
    
    @Override
    protected void connect() throws IOException {
        logger.info("Opening socket to " + host + ":" + port + " and sending " + MessageType.OPN.name()
                + " message...");
        socket = new Socket(host, port);
    }

    @Override
    protected boolean isConnected() {
        return socket != null && socket.isConnected();
    }

}
