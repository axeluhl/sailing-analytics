package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.CompetitorPositionRawData;
import com.tractrac.clientmodule.data.ICallbackData;

public class ReceiveTrackingDataTest extends AbstractTracTracLiveTest {
    final private Object semaphor = new Object();
    final private RaceCompetitor[] firstTracked = new RaceCompetitor[1];
    final private CompetitorPositionRawData[] firstData = new CompetitorPositionRawData[1];
    final private Map<Race, DynamicTrackedRace> trackedRaces = new HashMap<Race, DynamicTrackedRace>();

    public ReceiveTrackingDataTest() throws URISyntaxException,
            MalformedURLException {
        super();
    }
    
    /**
     * Sets up a single listener so that the rather time-consuming race setup is received only once, and all
     * tests in this class share a single feed execution. The listener fills in the first event received
     * into {@link #firstTracked} and {@link #firstData}. All events are converted into {@link GPSFixMovingImpl}
     * objects and appended, synchronized by {@link #semaphor}, to 
     */
    @Before
    public void setupListener() {
        final DomainFactory domainFactory = DomainFactory.INSTANCE;
        ICallbackData<RaceCompetitor, CompetitorPositionRawData> positionListener = new ICallbackData<RaceCompetitor, CompetitorPositionRawData>() {
            private boolean first = true;
            
            public void gotData(RaceCompetitor tracked,
                    CompetitorPositionRawData record) {
                System.out.println("Received event "+record);
                synchronized (semaphor) {
                    if (first) {
                        firstTracked[0] = tracked;
                        firstData[0] = record;
                        first = false;
                    }
                    DynamicTrackedRace trackedRace = trackedRaces.get(tracked.getRace());
                    GPSFixMoving fix = domainFactory.createGPSFixMoving(record);
                    Competitor competitor = domainFactory.getCompetitor(tracked.getCompetitor());
                    trackedRace.recordFix(competitor, fix);
                    semaphor.notifyAll();
                }
            }
        };
        List<TypeController> listeners = new ArrayList<TypeController>();
        for (Race race : getEvent().getRaceList()) {
            System.out.println("Subscribing raw position listener for race "+race);
            TypeController listener = CompetitorPositionRawData.subscribe(race,
                positionListener, /* fromTime */0 /* means ALL */);
            listeners.add(listener);
            trackedRaces.put(race, new DynamicTrackedRaceImpl(domainFactory.createRaceDefinition(race)));
        }
        Event event = domainFactory.createEvent(getEvent());
        for (TypeController raceListener : DomainFactory.INSTANCE.getRaceCourseReceiver(event, getEvent()).getRouteListeners()) {
            listeners.add(raceListener);
        }
        addListenersAndStartController(listeners.toArray(new TypeController[0]));
    }

    @Test
    public void testReceiveCompetitorPosition() {
        synchronized (semaphor) {
            while (firstTracked[0] == null) {
                try {
                    semaphor.wait();
                } catch (InterruptedException e) {
                    // print, ignore, wait on
                    e.printStackTrace();
                }
            }
        }
        assertNotNull(firstTracked[0]);
        assertNotNull(firstData[0]);
        Position pos = DomainFactory.INSTANCE.createPosition(firstData[0]);
        assertNotNull(pos);
        assertEquals(firstData[0].getLatitude(), pos.getLatDeg(), /* epsilon */ 0.00000001);
        assertEquals(firstData[0].getLongitude(), pos.getLngDeg(), /* epsilon */ 0.00000001);
    }

}
