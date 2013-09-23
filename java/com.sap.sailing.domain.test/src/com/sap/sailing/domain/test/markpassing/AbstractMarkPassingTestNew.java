package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.lang.Iterable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;

public abstract class AbstractMarkPassingTestNew {

    private DynamicTrackedRace race;

    private final DetectorMarkPassing detector;

    private static final int tolerance = 10000;

    protected AbstractMarkPassingTestNew(DetectorMarkPassing detector) throws MalformedURLException, URISyntaxException {

        this.detector = detector;

    }

    Iterable<Competitor> competitors = race.getRace().getCompetitors();

    LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> waypointTracks = new LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>>();
    {

        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            

            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> marks = new ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>();

            for (Mark mark : w.getMarks()) {

                DynamicGPSFixTrack<Mark, GPSFix> markTrack = race.getOrCreateTrack(mark);

                marks.add(markTrack);
            }

            waypointTracks.put(w, marks);

        }
    }

    protected void compareMarkpasses() {

        int correctPasses = 0;
        int totalPasses = 0;

        for (Competitor c : competitors) {

            LinkedHashMap<Waypoint, MarkPassing> givenPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            {
                for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
                    MarkPassing markPassing = race.getMarkPassing(c, w);

                    givenPasses.put(w, markPassing);
                }
            }

            for (Waypoint w : detector.computeMarkpasses(race.getTrack(c), waypointTracks, race.getStartOfRace()).keySet()) {

                if (givenPasses.containsKey(w)) {

                    long timedelta = givenPasses.get(w).getTimePoint().asMillis()
                            - detector.computeMarkpasses(race.getTrack(c), waypointTracks,race.getStartOfRace()).get(w).getTimePoint()
                                    .asMillis();

                    if ((Math.abs(timedelta) < tolerance)) {

                        correctPasses++;

                    }

                    totalPasses++;
                }

            }

            double accuracy = correctPasses / totalPasses;

            assertTrue(accuracy > 0.9);
        }

    }

}
