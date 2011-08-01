package com.sap.sailing.domain.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

/**
 * Receives GPS tracks from a race. One test (if not ignored) stores these tracks in the resources/
 * folder for later (fast, off-line and reproducible) use by other tests. The other tests apply
 * smoothening and ensure that smoothening filters out serious outliers but doesn't alter good
 * tracks that don't have outliers.
 * 
 * @author Axel Uhl (D043530)
 *
 */
@Ignore("Un-ignore when you need to fetch new tracks")
public class FetchTracksAndStoreLocallyTest extends KielerWoche2011BasedTest {
    final private Object semaphor = new Object();
    private final Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks;
    private boolean trackComplete;

    public FetchTracksAndStoreLocallyTest() throws URISyntaxException, MalformedURLException {
        super();
        tracks = new HashMap<Competitor, DynamicTrack<Competitor,GPSFixMoving>>();
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
        final RaceChangeListener<Competitor> positionListener = new RaceChangeListener<Competitor>() {
            @Override
            public void gpsFixReceived(GPSFix fix, Competitor competitor) {
                DynamicTrack<Competitor, GPSFixMoving> track = tracks.get(competitor);
                if (track == null) {
                    track = new DynamicGPSFixMovingTrackImpl<Competitor>(competitor, /* millisecondsOverWhichToAverage */ 40000);
                    tracks.put(competitor, track);
                }
                track.addGPSFix((GPSFixMoving) fix);
            }
            @Override
            public void markPassingReceived(MarkPassing markPassing) {
            }
            @Override
            public void windDataReceived(Wind wind) {
            }
            @Override
            public void windDataRemoved(Wind wind) {
            }
            @Override
            public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            }
            @Override
            public void windAveragingChanged(long oldMillisecondsOverWhichToAverage,
                    long newMillisecondsOverWhichToAverage) {
            }
        };
        List<TypeController> listeners = new ArrayList<TypeController>();
        Event event = domainFactory.createEvent(getEvent());
        DynamicTrackedEvent trackedEvent = domainFactory.getOrCreateTrackedEvent(event);
        trackedEvent.addRaceListener(new RaceListener() {
            @Override
            public void raceAdded(TrackedRace trackedRace) {
                System.out.println("Subscribing raw position listener for race "+trackedRace);
                ((DynamicTrackedRace) trackedRace).addListener(positionListener);
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
                EmptyWindStore.INSTANCE, this, ReceiverType.RACECOURSE, ReceiverType.RACESTARTFINISH, ReceiverType.RAWPOSITIONS);
        addListenersForStoredDataAndStartController(updateReceivers);
    }

    @Override
    public void storedDataEnd() {
        super.storedDataEnd();
        trackComplete = true;
        synchronized (semaphor) {
            semaphor.notifyAll();
        }
    }

    @Test
    public void storeReceivedTracks() throws InterruptedException, FileNotFoundException, IOException {
        synchronized (semaphor) {
            while (!trackComplete) {
                semaphor.wait();
            }
        }
        storeTracks();
    }
    
    private void storeTracks() throws FileNotFoundException, IOException {
        for (Map.Entry<Competitor, DynamicTrack<Competitor, GPSFixMoving>> competitorAndTrack : tracks.entrySet()) {
            Competitor competitor = competitorAndTrack.getKey();
            DynamicTrack<Competitor, GPSFixMoving> track = competitorAndTrack.getValue();
            storeTrack(competitor, track, getEvent().getName());
        }
    }

}
