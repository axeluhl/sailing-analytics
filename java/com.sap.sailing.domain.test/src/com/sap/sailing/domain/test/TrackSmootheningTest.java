package com.sap.sailing.domain.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
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
public class TrackSmootheningTest extends AbstractTracTracLiveTest {
    final private Object semaphor = new Object();
    private final Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks;
    private boolean trackComplete;

    public TrackSmootheningTest() throws URISyntaxException, MalformedURLException {
        super(Boolean.valueOf(System.getProperty("tractrac.tunnel", "false")) ? new URL("http://localhost:12348/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race=357c700a-9d9a-11e0-85be-406186cbf87c") :
            new URL("http://germanmaster.traclive.dk/events/event_20110609_KielerWoch/clientparams.php?event=event_20110609_KielerWoch&race=357c700a-9d9a-11e0-85be-406186cbf87c"),
            Boolean.valueOf(System.getProperty("tractrac.tunnel", "false")) ? new URI("tcp://localhost:1520") : new URI("tcp://germanmaster.traclive.dk:1520"),
                    Boolean.valueOf(System.getProperty("tractrac.tunnel", "false")) ? new URI("tcp://localhost:1521") : new URI("tcp://germanmaster.traclive.dk:1521"));
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
            ObjectOutput oo = getOutputStream(competitorAndTrack.getKey());
            for (GPSFixMoving fix : competitorAndTrack.getValue().getFixes()) {
                write(fix, oo);
            }
            oo.close();
        }
    }

    private void write(GPSFixMoving fix, ObjectOutput oo) throws IOException {
        oo.writeLong(fix.getTimePoint().asMillis());
        oo.writeDouble(fix.getPosition().getLatDeg());
        oo.writeDouble(fix.getPosition().getLngDeg());
        oo.writeDouble(fix.getSpeed().getKnots());
        oo.writeDouble(fix.getSpeed().getBearing().getDegrees());
    }
    
    private GPSFixMoving read(ObjectInput oi) throws IOException {
        TimePoint timePoint = new MillisecondsTimePoint(oi.readLong());
        Position position = new DegreePosition(oi.readDouble(), oi.readDouble());
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(oi.readDouble(), new DegreeBearingImpl(oi.readDouble()));
        return new GPSFixMovingImpl(position, timePoint, speedWithBearing);
    }

    private ObjectOutput getOutputStream(Competitor competitor) throws FileNotFoundException, IOException {
        return new ObjectOutputStream(new FileOutputStream(getFile(competitor)));
    }

    private File getFile(Competitor competitor) {
        return new File("resources/"+getEvent().getName()+"-"+competitor.getName());
    }

    protected String getExpectedEventName() {
        return "Kieler Woche";
    }
}
