package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.Util;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.MarkPassingsData;

public class ReceiveMarkPassingDataTest extends AbstractTracTracLiveTest {
    final private Object semaphor = new Object();
    final private MarkPassingsData[] firstData = new MarkPassingsData[1];
    private RaceDefinition raceDefinition;
    
    public ReceiveMarkPassingDataTest() throws URISyntaxException,
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
        Race race = getEvent().getRaceList().iterator().next();
        TypeController markPassingsListener = MarkPassingsData.subscribe(race,
                new ICallbackData<RaceCompetitor, MarkPassingsData>() {
                    private boolean first = true;
                    
                    @Override
                    public void gotData(RaceCompetitor route,
                            MarkPassingsData record) {
                        if (first) {
                            synchronized (semaphor) {
                                firstData[0] = record;
                                semaphor.notifyAll();
                            }
                            first = false;
                        }
                    }
                });
        List<TypeController> listeners = new ArrayList<TypeController>();
        listeners.add(markPassingsListener);
        for (TypeController tc : DomainFactory.INSTANCE.getUpdateReceivers(
                DomainFactory.INSTANCE.trackEvent(
                        DomainFactory.INSTANCE.createEvent(getEvent())))) {
            listeners.add(tc);
        }
        addListenersForStoredDataAndStartController(markPassingsListener);
        raceDefinition = DomainFactory.INSTANCE.getRaceDefinition(race);
        synchronized (semaphor) {
            while (firstData[0] == null) {
                try {
                    semaphor.wait();
                } catch (InterruptedException e) {
                    // print, ignore, wait on
                    e.printStackTrace();
                }
            }
        }
    }

    @Ignore("Currently no live data seems to be received")
    @Test
    public void testReceiveCompetitorPosition() {
        synchronized (semaphor) {
            while (firstData[0] == null) {
                try {
                    semaphor.wait();
                } catch (InterruptedException e) {
                    // print, ignore, wait on
                    e.printStackTrace();
                }
            }
        }
        assertNotNull(firstData[0]);
        assertTrue(firstData[0].getPassings().size() > 0);
        MarkPassingsData.Entry entry = firstData[0].getPassings().iterator().next();
        assertNotNull(entry);
        Waypoint waypoint = DomainFactory.INSTANCE.getWaypoint(entry.getControlPoint());
        assertNotNull(waypoint);
        assertTrue(Util.contains(raceDefinition.getCourse().getWaypoints(), waypoint));
    }

}
