package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.sap.sailing.domain.tractracadapter.impl.ControlPointAdapter;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.route.IControl;

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
    private static final Logger logger = Logger.getLogger(AbstractTracTracLiveTest.class.getName());
    protected static final boolean tractracTunnel = Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"));
    protected static final String tractracTunnelHost = System.getProperty("tractrac.tunnel.host", "localhost");
    private IEvent event;
    private final Collection<Receiver> receivers;
    
    private Thread ioThread;
    private DataController controller;
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = new Timeout(2 * 60 * 1000);

    protected AbstractTracTracLiveTest() throws URISyntaxException, MalformedURLException {
        receivers = new HashSet<Receiver>();
    }

    /**
     * Default set-up for an STG training session in Weymouth, 2011
     */
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        final String eventID = "event_20110505_SailingTea";
        final String raceID = "bd8c778e-7c65-11e0-8236-406186cbf87c";
        setUp(getParamURL(eventID, raceID), getLiveURI(), getStoredURI());
    }

    public static URI getStoredURI() throws URISyntaxException {
        return tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":"+TracTracConnectionConstants.PORT_TUNNEL_STORED) : new URI("tcp://" + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_STORED);
    }

    public static URI getLiveURI() throws URISyntaxException {
        return tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":"+TracTracConnectionConstants.PORT_TUNNEL_LIVE) : new URI("tcp://" + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_LIVE);
    }

    public static URL getParamURL(final String eventID, final String raceID) throws MalformedURLException {
        return new URL("http://" + TracTracConnectionConstants.HOST_NAME + "/events/event_20110505_SailingTea/clientparams.php?event="+eventID+
                "&race="+raceID);
    }
    
    protected void setUp(URL paramUrl, URI liveUri, URI storedUri) throws FileNotFoundException, MalformedURLException {
        // Read event data from configuration file
        event = KeyValue.setup(paramUrl);
        assertNotNull(event);
        // Initialize data controller using live and stored data sources
        if (storedUri.toString().startsWith("file:")) {
            try {
                URI oldStoredUri = storedUri;
                storedUri = new URI(storedUri.toString().replaceFirst("file:/([^/])", "file:////$1"));
                logger.info("Replaced storedUri "+oldStoredUri+" by "+storedUri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        controller = new DataController(liveUri, storedUri, this);
        // Start live and stored data streams
        ioThread = new Thread(controller, "I/O for event "+event.getName()+", paramURL "+paramUrl);
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
    public void tearDown() throws MalformedURLException, IOException, InterruptedException {
        logger.info("entering "+getClass().getName()+".tearDown()");
        Thread.sleep(500); // wait a bit before stopping the controller; in earlier versions we did a web request to stop the
        // simulator here; then, the ioThread joined flawlessly; aggressively stopping the controller doesn't let the ioThread join
        if (getController() != null) // the controller (and the ioThread see below) are null if the data is being read from a local file.
            controller.stop(/* abortStored */ true);
    logger.info("successfully stopped controller");
        try {
            if (ioThread != null) {
                ioThread.join(3000); // just wait a little bit, then give up
            }
        } catch (InterruptedException ex) {
            Assert.fail(ex.getMessage());
        }
        logger.info("successfully joined ioThread");
        for (Receiver receiver : receivers) {
            receiver.stopPreemptively();
            logger.info("successfully stopped receiver "+receiver);
        }
        logger.info("leaving "+getClass().getName()+".tearDown()");
    }

    
    protected IEvent getTracTracEvent() {
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

    public static Iterable<Pair<TracTracControlPoint, PassingInstruction>> getTracTracControlPointsWithPassingInstructions(Iterable<IControl> controlPoints) {
        List<Pair<TracTracControlPoint, PassingInstruction>> ttControlPoints = new ArrayList<Pair<TracTracControlPoint, PassingInstruction>>();
        for (IControl cp : controlPoints) {
            ttControlPoints.add(new Pair<TracTracControlPoint, PassingInstruction>(new ControlPointAdapter(cp), null));
        }
        return ttControlPoints;
    }
    
    public static Iterable<TracTracControlPoint> getTracTracControlPoints(Iterable<IControl> controlPoints) {
        List<TracTracControlPoint> ttControlPoints = new ArrayList<>();
        for (IControl cp : controlPoints) {
            ttControlPoints.add(new ControlPointAdapter(cp));
        }
        return ttControlPoints;
    }

    public static URI getCourseDesignUpdateURI() throws URISyntaxException {
        return new URI("http://tracms.traclive.dk/update_course");
    }

    public static String getTracTracUsername() {
        return "tracTest";
    }

    public static String getTracTracPassword() {
        return "tracTest";
    }
}
