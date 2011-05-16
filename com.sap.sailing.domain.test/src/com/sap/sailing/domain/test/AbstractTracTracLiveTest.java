package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.maptrack.client.io.TypeController;
import com.sap.tractrac.clientmodule.util.Base64;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.DataController.Listener;
import com.tractrac.clientmodule.setup.KeyValue;

public abstract class AbstractTracTracLiveTest implements Listener {
    private final URL paramUrl;
    private final URI liveUri;
    private final URI storedUri;
    private Event event;
    
    private Thread ioThread;
    private DataController controller;

    protected AbstractTracTracLiveTest() throws URISyntaxException, MalformedURLException {
        paramUrl = new URL("http://www.traclive.dk/simulateconf/j80race12.txt");
        liveUri = new URI("tcp://localhost:1621");
        storedUri = new URI("tcp://localhost:1620");
    }

    @Before
    public void setUp() throws MalformedURLException, IOException {
        killAllRunningSimulations();
        startRaceSimulation(3, 7);
        // Read event data from configuration file
        event = KeyValue.setup(paramUrl);
        assertNotNull(event);
        assertEquals("J80 Worlds", event.getName());
        // Initialize data controller using live and stored data sources
        controller = new DataController(liveUri, storedUri, this);
        // Start live and stored data streams
        ioThread = new Thread(controller, "io");
        // test cases need to start the thread calling startController
        // after adding their listeners
    }
    
    protected void addListenersAndStartController(TypeController... listeners) {
        for (TypeController listener : listeners) {
            getController().add(listener);
        }
        startController();
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
    }

    private void startRaceSimulation(int speedMultiplier, int raceNumber)
            throws MalformedURLException, IOException {
        URL url = new URL(
                "http://www.traclive.dk/simulate/start.php?racenumber="+raceNumber+"&speed="+
                speedMultiplier+"&replaytime=sample");
        URLConnection conn = url.openConnection();
        authorize(conn);
        conn.getContent(new Class[] { String.class });
    }

    private void killAllRunningSimulations() throws IOException,
            MalformedURLException {
        URL url = new URL("http://www.traclive.dk/simulate/kill.php");
        URLConnection conn = url.openConnection();
        authorize(conn);
        conn.getContent(new Class[] { String.class });
    }

    private void authorize(URLConnection conn) {
        conn.setRequestProperty("Authorization", "Basic "+
                Base64.encode("tracsim:simming10".getBytes()));
    }

    protected Event getEvent() {
        return event;
    }

    protected DataController getController() {
        return controller;
    }

    @Override
    public void liveDataConnected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void liveDataDisconnected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopped() {
        // TODO Auto-generated method stub

    }

    @Override
    public void storedDataBegin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void storedDataEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void storedDataProgress(float arg0) {
        // TODO Auto-generated method stub

    }

}
