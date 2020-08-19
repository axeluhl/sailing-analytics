package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sse.util.HttpUrlConnectionHelper;

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
public class SailMasterConnectorForUrlDownload extends AbstractSailMasterConnector {
    private static final Logger logger = Logger.getLogger(SailMasterConnectorForUrlDownload.class.getName());
    
    private final URL raceDataUrl;
    
    /**
     * If the {@link #socket} is not being used, a {@link HttpURLConnection} is expected to provide the
     * {@link InputStream} from which this connector reads.
     */
    private URLConnection urlConnection;
    
    public SailMasterConnectorForUrlDownload(String raceId, URL raceDataUrl, String raceName, String raceDescription, BoatClass boatClass, SwissTimingRaceTrackerImpl swissTimingRaceTracker) throws InterruptedException, ParseException {
        super(raceId, raceName, raceDescription, boatClass, swissTimingRaceTracker);
        this.raceDataUrl = raceDataUrl;
        startReceiverThread();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return null; // we don't write to the URL connection's output stream
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return urlConnection.getInputStream();
    }

    @Override
    protected void connect() throws IOException {
        logger.info("Connecting to "+raceDataUrl);
        urlConnection = HttpUrlConnectionHelper.redirectConnection(raceDataUrl);
    }

    @Override
    protected boolean isConnected() throws IOException {
        return urlConnection != null && getInputStream() != null;
    }

    @Override
    protected void disconnect() throws IOException {
        if (urlConnection != null) {
            if (urlConnection.getInputStream() != null) {
                urlConnection.getInputStream().close();
            }
            urlConnection = null;
            stop(); // disconnecting from the URL also means to stop the connector
        }
    }
    
    @Override
    protected TrackedRaceStatusEnum getStatusAfterLoadingIsComplete()  {
        return TrackedRaceStatusEnum.FINISHED; // nothing more to come
    }
}
