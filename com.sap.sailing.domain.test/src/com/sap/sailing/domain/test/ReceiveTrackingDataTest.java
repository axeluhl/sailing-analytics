package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.RawListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;

public class ReceiveTrackingDataTest extends AbstractTracTracLiveTest {
    final private Object semaphor = new Object();
    final private Competitor[] firstTracked = new Competitor[1];
    final private GPSFixMoving[] firstData = new GPSFixMoving[1];

    public ReceiveTrackingDataTest() throws URISyntaxException,
            MalformedURLException {
        super();
    }
    
    /**
     * Sets up a single listener so that the rather time-consuming race setup is received only once, and all
     * tests in this class share a single feed execution. The listener fills in the first event received
     * into {@link #firstTracked} and {@link #firstData}. All events are converted into {@link GPSFixMovingImpl}
     * objects and appended to the {@link DynamicTrackedRace}s.
     */
    @Before
    public void setupListener() {
        final DomainFactory domainFactory = DomainFactory.INSTANCE;
        final RawListener<Competitor, GPSFixMoving> positionListener = new RawListener<Competitor, GPSFixMoving>() {
            private boolean first = true;
            
            @Override
            public void gpsFixReceived(GPSFixMoving fix, Competitor competitor) {
                System.out.println("Received fix "+fix);
                synchronized (semaphor) {
                    if (first) {
                        firstTracked[0] = competitor;
                        firstData[0] = fix;
                        first = false;
                    }
                    semaphor.notifyAll();
                }
            }
        };
        List<TypeController> listeners = new ArrayList<TypeController>();
        Event event = domainFactory.createEvent(getEvent());
        DynamicTrackedEvent trackedEvent = domainFactory.trackEvent(event);
        trackedEvent.addRaceListener(new RaceListener() {
            @Override
            public void raceAdded(TrackedRace trackedRace) {
                System.out.println("Subscribing raw position listener for race "+trackedRace);
                ((DynamicTrackedRace) trackedRace).addListener(positionListener);
            }
        });
        for (TypeController raceListener : domainFactory.getUpdateReceivers(trackedEvent)) {
            listeners.add(raceListener);
        }
        addListenersForStoredDataAndStartController(listeners.toArray(new TypeController[0]));
    }

    @Ignore
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
    }

    @Override
    public void liveDataConnectError(String arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void storedDataError(String arg0) {
        // TODO Auto-generated method stub
        
    }

}
