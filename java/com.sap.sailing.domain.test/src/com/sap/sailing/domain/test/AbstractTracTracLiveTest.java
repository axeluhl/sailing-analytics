package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.tractrac.clientmodule.util.Base64;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.DataController.Listener;
import com.tractrac.clientmodule.setup.KeyValue;

/**
 * Subclassing tests have to call {@link #addListenersForStoredDataAndStartController(Iterable)} to kick off
 * the data receiving process. If subclasses use this class's default constructor, a connection to the
 * STG default account with a few test races is established. Subclasses may also choose to configure other
 * races / events using the {@link #AbstractTracTracLiveTest(URL, URI, URI)} constructor variant.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractTracTracLiveTest extends StoredTrackBasedTest implements Listener {
    protected static final boolean tractracTunnel = Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"));
    protected static final String tractracTunnelHost = System.getProperty("tractrac.tunnel.host", "localhost");
    private static final String START_SIMULATOR_URL = "http://sapsimulation.tracdev.dk/start.php";
    private static final String KILL_URL = "http://sapsimulation.tracdev.dk/kill.php";
    private Event event;
    private final Collection<Receiver> receivers;
    
    private Thread ioThread;
    private DataController controller;

    protected AbstractTracTracLiveTest() throws URISyntaxException, MalformedURLException {
        receivers = new HashSet<Receiver>();
    }

    /**
     * Default set-up for an STG training session in Weymouth, 2011
     */
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        setUp(new URL("http://germanmaster.traclive.dk/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=bd8c778e-7c65-11e0-8236-406186cbf87c"),
            tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4412") : new URI("tcp://germanmaster.traclive.dk:4400"),
                    tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4413") : new URI("tcp://germanmaster.traclive.dk:4401"));
    }
    
    protected void setUp(URL paramUrl, URI liveUri, URI storedUri) throws FileNotFoundException, MalformedURLException {
        // Read event data from configuration file
        event = KeyValue.setup(paramUrl);
        assertNotNull(event);
        // Initialize data controller using live and stored data sources
        controller = new DataController(liveUri, storedUri, this);
        // Start live and stored data streams
        ioThread = new Thread(controller, "io");
        // test cases need to start the thread calling startController
        // after adding their listeners
    }

    protected String getExpectedEventName() {
        return "Sailing Team Germany";
    }
    
    protected void addListenersForStoredDataAndStartController(Iterable<Receiver> receivers) {
        for (Receiver receiver : receivers) {
            this.receivers.add(receiver);
            for (TypeController typeController : receiver.getTypeControllersAndStart()) {
                getController().add(typeController);
            }
        }
        startController();
    }
    
    /**
     * Called when the {@link #storedDataEnd()} event was received. Adds the listeners
     * returned to the {@link #getController() controller}, presumably for live data.
     * This default implementation returns an empty iterable. Subclasses may override
     * to return more.
     */
    protected Iterable<TypeController> getListenersForLiveData() {
        return Collections.emptySet();
    }

    protected void startController() {
        ioThread.start();
    }
    
    @After
    public void tearDown() throws MalformedURLException, IOException {
        killAllRunningSimulations();
        controller.stop(/* abortStored */ true);
        try {
            ioThread.join();
        } catch (InterruptedException ex) {
            Assert.fail(ex.getMessage());
        }
        for (Receiver receiver : receivers) {
            receiver.stopPreemptively();
        }
    }

    
    protected void startRaceSimulation(int speedMultiplier, int raceNumber)
            throws MalformedURLException, IOException, InterruptedException {
        URL url = new URL(
                START_SIMULATOR_URL+"?racenumber="+raceNumber+"&speed="+
                speedMultiplier+"&replaytime=sample");
        URLConnection conn = url.openConnection();
        authorize(conn);
        conn.getContent(new Class[] { String.class });
        Thread.sleep(2000); // wait 2s to ensure server has cleaned up properly
    }

    private void killAllRunningSimulations() throws IOException,
            MalformedURLException {
        URL url = new URL(KILL_URL);
        URLConnection conn = url.openConnection();
        authorize(conn);
        conn.getContent(new Class[] { String.class });
    }

    private void authorize(URLConnection conn) {
        conn.setRequestProperty("Authorization", "Basic "+
                Base64.encode("SAP:ext2Boat".getBytes()));
    }

    protected Event getEvent() {
        return event;
    }

    protected DataController getController() {
        return controller;
    }

    @Override
    public void liveDataConnected() {
        System.out.println("Live data connected");
    }

    @Override
    public void liveDataDisconnected() {
        System.out.println("Live data disconnected");
    }

    @Override
    public void stopped() {
        System.out.println("stopped");
    }

    @Override
    public void storedDataBegin() {
        System.out.println("Stored data begin");
    }

    @Override
    public void storedDataEnd() {
        System.out.println("Stored data end");
    }

    @Override
    public void storedDataProgress(float progress) {
        System.out.println("Stored data progress: "+progress);
        
    }

    @Override
    public void storedDataError(String arg0) {
        System.err.println("Error with stored data "+arg0);
    }

    @Override
    public void liveDataConnectError(String arg0) {
        System.err.println("Error with live data "+arg0);
    }

}
