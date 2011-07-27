package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

/**
 * Receives GPS tracks from a race. One test (if not ignored) stores these tracks in the resources/
 * folder for later (fast, off-line and reproducible) use by other tests. The other tests apply
 * smoothening and ensure that smoothening filters out serious outliers but doesn't alter good
 * tracks that don't have outliers.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TrackSmootheningTest extends StoredTrackBasedTest {
    private final Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks;

    public TrackSmootheningTest() throws URISyntaxException, MalformedURLException {
        tracks = new HashMap<Competitor, DynamicTrack<Competitor,GPSFixMoving>>();
    }
    
    /**
     * Sets up a single listener so that the rather time-consuming race setup is received only once, and all
     * tests in this class share a single feed execution. The listener fills in the first event received
     * into {@link #firstTracked} and {@link #firstData}. All events are converted into {@link GPSFixMovingImpl}
     * objects and appended to the {@link DynamicTrackedRace}s.
     */
    @Before
    public void setupListener() throws InterruptedException, FileNotFoundException, IOException {
        loadTracks();
    }

    private void loadTracks() throws FileNotFoundException, IOException {
        for (String competitorName : new String[] { "Achterberg", "Anton", "Barop", "Birkner", "Böger", "Böhm",
                "boite", "Bøjland", "Brill", "Broise", "Buhl", "Dasenbrook", "de Lisle", "Dr.Plattner", "Feldmann",
                "Findel", "Fischer", "Goedeking", "Gosch", "Hastenpflug", "Henge", "Hunger", "Kellner", "Kevin",
                "Köchlin", "Kraft", "Lehmann", "Lietz", "Menge", "Neulen", "Pleßmann", "Rasenack", "Reincke",
                "Saugmann", "Schomäker", "van Wonterghem" }) {
            Competitor c = new CompetitorImpl(competitorName, competitorName, /* team */ null, new BoatImpl(competitorName,
                    new BoatClassImpl("505")));
            DynamicTrack<Competitor, GPSFixMoving> track = readTrack(c, "Kieler Woche");
            if (track != null) {
                tracks.put(c, track);
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

    protected void assertOutlierInTrack(DynamicTrack<Competitor, GPSFixMoving> track) {
        GPSFixMoving outlier = getAnyOutlier(track.getRawFixes());
        assertNotNull(outlier); // assert that we found an outlier
    }

    protected void assertNoOutlierInSmoothenedTrack(DynamicTrack<Competitor, GPSFixMoving> track) {
        Iterable<GPSFixMoving> fixes = track.getFixes();
        GPSFixMoving outlier = getAnyOutlier(fixes);
        assertNull("Found unexpected outlier "+outlier+" in smoothened track", outlier); // assert that we did not find an outlier
    }

    protected GPSFixMoving getAnyOutlier(Iterable<GPSFixMoving> fixes) {
        TimePoint lastTimePoint = null;
        GPSFixMoving lastFix = null;
        GPSFixMoving outlier = null;
        for (GPSFixMoving fix : fixes) {
            if (lastTimePoint != null) {
                TimePoint thisTimePoint = fix.getTimePoint();
                long intervalInMillis = thisTimePoint.asMillis()-lastTimePoint.asMillis();
                Distance distanceFromLast = lastFix.getPosition().getDistance(fix.getPosition());
                Speed speedBetweenFixes = distanceFromLast.inTime(intervalInMillis);
                if (speedBetweenFixes.getKnots() > 100) {
                    // then it's not an olympic-class sports boat but a GPS jump
                    outlier = fix;
                }
            }
            lastTimePoint = fix.getTimePoint();
            lastFix = fix;
        }
        return outlier;
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
        assertNoOutlierInSmoothenedTrack(track);
    }

    @Test
    public void assertPlattnersKielerFoerdeJump() {
        DynamicTrack<Competitor, GPSFixMoving> track = getTrackByCompetitorName("Dr.Plattner");
        assertNotNull(track);
        assertOutlierInTrack(track);
        assertNoOutlierInSmoothenedTrack(track);
    }

}
