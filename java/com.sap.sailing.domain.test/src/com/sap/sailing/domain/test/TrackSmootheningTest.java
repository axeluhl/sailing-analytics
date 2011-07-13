package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;

/**
 * Receives GPS tracks from a race. One test (if not ignored) stores these tracks in the resources/
 * folder for later (fast, off-line and reproducible) use by other tests. The other tests apply
 * smoothening and ensure that smoothening filters out serious outliers but doesn't alter good
 * tracks that don't have outliers.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TrackSmootheningTest extends AbstractTracTracLiveTest {
    final private Object semaphor = new Object();
    private final Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks;
    private boolean tracksLoaded;
    private DomainFactory domainFactory;

    public TrackSmootheningTest() throws URISyntaxException, MalformedURLException {
        super(new URL("http://germanmaster.traclive.dk/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race=357c700a-9d9a-11e0-85be-406186cbf87c"),
            tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":1520") : new URI("tcp://germanmaster.traclive.dk:1520"),
                    tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":1521") : new URI("tcp://germanmaster.traclive.dk:1521"));
        tracks = new HashMap<Competitor, DynamicTrack<Competitor,GPSFixMoving>>();
    }
    
    /**
     * Sets up a single listener so that the rather time-consuming race setup is received only once, and all
     * tests in this class share a single feed execution. The listener fills in the first event received
     * into {@link #firstTracked} and {@link #firstData}. All events are converted into {@link GPSFixMovingImpl}
     * objects and appended to the {@link DynamicTrackedRace}s.
     */
    @Before
    public void setupListener() throws InterruptedException {
        domainFactory = new DomainFactoryImpl();
        List<TypeController> listeners = new ArrayList<TypeController>();
        Event event = domainFactory.createEvent(getEvent());
        DynamicTrackedEvent trackedEvent = domainFactory.getOrCreateTrackedEvent(event);
        trackedEvent.addRaceListener(new RaceListener() {
            @Override
            public void raceAdded(TrackedRace trackedRace) {
                try {
                    loadTracks();
                    tracksLoaded = true;
                    synchronized (semaphor) {
                        semaphor.notifyAll();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public void raceRemoved(TrackedRace trackedRace) {
            }
        });
        for (Receiver receiver : domainFactory.getUpdateReceivers(trackedEvent, getEvent(), EmptyWindStore.INSTANCE, this)) {
            for (TypeController raceListener : receiver.getTypeControllers()) {
                listeners.add(raceListener);
            }
        }
        Iterable<Receiver> updateReceivers = domainFactory.getUpdateReceivers(trackedEvent, getEvent(),
                EmptyWindStore.INSTANCE, this, ReceiverType.RACECOURSE, ReceiverType.RACESTARTFINISH);
        addListenersForStoredDataAndStartController(updateReceivers);
        synchronized (semaphor) {
            while (!tracksLoaded) {
                semaphor.wait();
            }
        }
    }

    private void loadTracks() throws FileNotFoundException, IOException {
        for (com.tractrac.clientmodule.Competitor c : getEvent().getCompetitorList()) {
            Competitor competitor = domainFactory.getCompetitor(c);
            DynamicTrack<Competitor, GPSFixMoving> track = readTrack(competitor);
            if (track != null) {
                tracks.put(competitor, track);
            }
        }
    }

    protected String getExpectedEventName() {
        return "Kieler Woche";
    }

    private DynamicTrack<Competitor, GPSFixMoving> getTrackByCompetitorName(String name) {
        for (Map.Entry<Competitor, DynamicTrack<Competitor, GPSFixMoving>> e : tracks.entrySet()) {
            if (e.getKey().getName().equals(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    @Test
    public void checkLoadedTracksCount() throws InterruptedException, FileNotFoundException, IOException {
        assertEquals(36, tracks.size());
    }
    
    @Test
    public void assertBirknersEquatorJump() {
        DynamicTrack<Competitor, GPSFixMoving> track = getTrackByCompetitorName("Birkner");
        assertNotNull(track);
        assertOutlierInTrack(track);
    }

    @Test
    public void assertPlattnersKielerFoerdeJump() {
        DynamicTrack<Competitor, GPSFixMoving> track = getTrackByCompetitorName("Dr.Plattner");
        assertNotNull(track);
        assertOutlierInTrack(track);
    }

    protected void assertOutlierInTrack(DynamicTrack<Competitor, GPSFixMoving> track) {
        TimePoint lastTimePoint = null;
        GPSFixMoving lastFix = null;
        GPSFixMoving outlier = null;
        for (GPSFixMoving fix : track.getFixes()) {
            if (lastTimePoint != null) {
                TimePoint thisTimePoint = fix.getTimePoint();
                long intervalInMillis = thisTimePoint.asMillis()-lastTimePoint.asMillis();
                Distance distanceFromLast = lastFix.getPosition().getDistance(fix.getPosition());
                Speed speedBetweenFixes = distanceFromLast.inTime(intervalInMillis);
                if (speedBetweenFixes.getKnots() > 50) {
                    // then it's not an olympic-class sports boat but a GPS jump
                    outlier = fix;
                }
            }
            lastTimePoint = fix.getTimePoint();
            lastFix = fix;
        }
        assertNotNull(outlier); // assert that we found an outlier
    }

}
