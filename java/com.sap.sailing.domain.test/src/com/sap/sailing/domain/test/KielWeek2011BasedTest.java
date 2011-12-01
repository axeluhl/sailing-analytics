package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedEventImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.TrackedLegImpl;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.tractrac.clientmodule.Race;

/**
 * Connects to the Kieler Woche 2011 TracTrac data of the 505 Race 2. Subclasses should implement a @Before method which
 * calls {@link #setUp(String, String, ReceiverType[])} with a useful set of receiver types and the race they want to observe /
 * load, or they should call {@link #setUp(String, String, ReceiverType[])} at the beginning of each respective test in case
 * they want to select/load different races for different tests. When all stored data has been received, the
 * {@link #getSemaphor() semaphor} is notified. Therefore, a typical pattern for subclasses should be to invoke
 * {@link #setUp(ReceiverType[])}, then wait on the semaphor before starting with test processing.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class KielWeek2011BasedTest extends AbstractTracTracLiveTest {
    private DomainFactoryImpl domainFactory;
    private Event domainEvent;
    private DynamicTrackedEvent trackedEvent;
    private RaceDefinition race;
    private DynamicTrackedRace trackedRace;

    private final Object semaphor = new Object();
    
    /**
     * When the {@link #semaphor} is notified, this flag indicates whether {@link #storedDataEnd()} has already
     * been called.
     */
    private boolean storedDataLoaded;

    protected KielWeek2011BasedTest() throws MalformedURLException, URISyntaxException {
        super();
    }
    
    
    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        domainFactory = new DomainFactoryImpl(new com.sap.sailing.domain.base.impl.DomainFactoryImpl());
        // keep superclass implementation from automatically setting up for a Weymouth event and force subclasses
        // to select a race
    }

    protected void setUp(String eventName, String raceId, ReceiverType... receiverTypes) throws MalformedURLException,
            IOException, InterruptedException, URISyntaxException {
        setUpWithoutLaunchingController(eventName, raceId);
        assertEquals(getExpectedEventName(), getEvent().getName());
        completeSetupLaunchingControllerAndWaitForRaceDefinition(receiverTypes);
    }


    protected void completeSetupLaunchingControllerAndWaitForRaceDefinition(ReceiverType... receiverTypes)
            throws InterruptedException {
        setStoredDataLoaded(false);
        ArrayList<Receiver> receivers = new ArrayList<Receiver>();
        for (Receiver r : domainFactory.getUpdateReceivers(trackedEvent, getEvent(), EmptyWindStore.INSTANCE,
                new DynamicRaceDefinitionSet() {
                    @Override
                    public void addRaceDefinition(RaceDefinition race) {
                    }
                }, receiverTypes)) {
            receivers.add(r);
        }
        addListenersForStoredDataAndStartController(receivers);
        Race tractracRace = getEvent().getRaceList().iterator().next();
        // now we expect that there is no 
        assertNull(domainFactory.getExistingRaceDefinitionForRace(tractracRace));
        race = getDomainFactory().getAndWaitForRaceDefinition(tractracRace);
        assertNotNull(race);
        synchronized (getSemaphor()) {
            while (!isStoredDataLoaded()) {
                getSemaphor().wait();
            }
        }
        for (Receiver receiver : receivers) {
            receiver.stopAfterProcessingQueuedEvents();
            receiver.join();
        }
        trackedRace = getTrackedEvent().getTrackedRace(race);
    }


    private void setStoredDataLoaded(boolean storedDataLoaded) {
        this.storedDataLoaded = storedDataLoaded;
    }


    protected void setUpWithoutLaunchingController(String eventName, String raceId) throws FileNotFoundException, MalformedURLException,
            URISyntaxException {
        super.setUp(new URL("http://germanmaster.traclive.dk/events/"+eventName+"/clientparams.php?event="+eventName+"&race="+raceId),
                tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4412") : new URI("tcp://germanmaster.traclive.dk:4400"),
                        tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":4413") : new URI("tcp://germanmaster.traclive.dk:4401"));
        if (domainFactory == null) {
            domainFactory = new DomainFactoryImpl(new com.sap.sailing.domain.base.impl.DomainFactoryImpl());
        }
        domainEvent = domainFactory.getOrCreateEvent(getEvent());
        trackedEvent = new DynamicTrackedEventImpl(domainEvent);
    }
    
    protected Competitor getCompetitorByName(String nameRegexp) {
        Pattern p = Pattern.compile(nameRegexp);
        for (Competitor c : getTrackedRace().getRace().getCompetitors()) {
            if (p.matcher(c.getName()).matches()) {
                return c;
            }
        }
        return null;
    }

    /**
     * If a leg's type needs to be determined, some wind data is required to decide on upwind,
     * downwind or reaching leg. Wind information is queried by {@link TrackedLegImpl} based on
     * the marks' positions. Therefore, approximate mark positions are set here for all marks
     * of {@link #getTrackedRace()}'s courses for the time span starting at the epoch up to now.
     */
    public static void fixApproximateMarkPositionsForWindReadOut(DynamicTrackedRace the505Race2) {
        TimePoint epoch = new MillisecondsTimePoint(0l);
        TimePoint now = MillisecondsTimePoint.now();
        Map<String, Position> buoyPositions = new HashMap<String, Position>();
        buoyPositions.put("K Start (left)", new DegreePosition(54.497439439999994, 10.205943000000001));
        buoyPositions.put("K Start (right)", new DegreePosition(54.500209999999996, 10.20206472));
        buoyPositions.put("K Mark4 (right)", new DegreePosition(54.499422999999986, 10.200381692));
        buoyPositions.put("K Mark4 (left)", new DegreePosition(54.498954999999995, 10.200982));
        buoyPositions.put("K Mark1", new DegreePosition(54.489738990000006, 10.17079423000015));
        buoyPositions.put("K Finish (left)", new DegreePosition(54.48918199999999, 10.17003714));
        buoyPositions.put("K Finish (right)", new DegreePosition(54.48891756, 10.170632146666675));
        for (Waypoint w : the505Race2.getRace().getCourse().getWaypoints()) {
            for (Buoy buoy : w.getBuoys()) {
                the505Race2.getOrCreateTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), epoch));
                the505Race2.getOrCreateTrack(buoy).addGPSFix(new GPSFixImpl(buoyPositions.get(buoy.getName()), now));
            }
        }
    }

    protected RaceDefinition getRace() {
        return race;
    }

    protected DynamicTrackedRace getTrackedRace() {
        return trackedRace;
    }

    /**
     * When all stored data has been transmitted, notify the semaphor for tests to start processing
     */
    @Override
    public void storedDataEnd() {
        super.storedDataEnd();
        synchronized (semaphor) {
            storedDataLoaded = true;
            semaphor.notifyAll();
        }
    }

    protected String getExpectedEventName() {
        return "Kieler Woche";
    }

    protected DomainFactoryImpl getDomainFactory() {
        return domainFactory;
    }

    protected Event getDomainEvent() {
        return domainEvent;
    }

    protected DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    protected Object getSemaphor() {
        return semaphor;
    }

    protected boolean isStoredDataLoaded() {
        return storedDataLoaded;
    }
    
}
