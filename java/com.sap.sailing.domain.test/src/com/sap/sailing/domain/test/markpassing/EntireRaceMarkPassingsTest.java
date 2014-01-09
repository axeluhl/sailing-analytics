package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.tracking.MarkPassing;

public class EntireRaceMarkPassingsTest extends AbstractRealRaceMarkPassingTest {
    
    // TODO How should Edges between the proxy start and end be treated
    // TODO No Start Time
    // TODO Start-analysis is wrong for gate starts

    // TODO Feldmann issue, also for marks

    // TODO Make sure the functions return the right probability

    // TODO Use Wind/Maneuver analysis
    // TODO Splining

    // TODO Build good test framework that test incremental calculation, tricky cases, ...
    // TODO Document everything

    public EntireRaceMarkPassingsTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Override
    void testRace() throws IOException, InterruptedException, URISyntaxException {
        
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> computedPasses = new LinkedHashMap<>();
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> givenPasses = new LinkedHashMap<>();

        // Get Waypoints
        for (Waypoint w : getRace().getCourse().getWaypoints()) {
            waypoints.add(w);
        }

        // Get given Markpasses
        for (Competitor c : getRace().getCompetitors()) {
            LinkedHashMap<Waypoint, MarkPassing> givenMarkPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            for (Waypoint wp : waypoints) {
                MarkPassing markPassing = getTrackedRace().getMarkPassing(c, wp);
                givenMarkPasses.put(wp, markPassing);
            }
            givenPasses.put(c, givenMarkPasses);
        }

        // Get calculatedMarkPasses
        long time = System.currentTimeMillis();
        final MarkPassingCalculator markPassCreator = new MarkPassingCalculator(getTrackedRace(), true);
        time = System.currentTimeMillis()-time;
        computedPasses = markPassCreator.getAllPasses();

        // Compare computed and calculated MarkPassings
        final int tolerance = 20000;
        int numberOfCompetitors = 0;
        int wronglyComputed = 0;
        int wronglyNotComputed = 0;
        int correctlyNotComputed = 0;
        int correctPasses = 0;
        int incorrectPasses = 0;

        boolean printRight = false;
        boolean printWrong = true;

        for (Competitor c : getRace().getCompetitors()) {
            numberOfCompetitors++;
            System.out.println(c.getName() + "\n");
            for (Waypoint w : waypoints) {
                if (givenPasses.get(c).get(w) == null && !(computedPasses.get(c).get(w) == null)) {
                    wronglyComputed++;
                    if (printWrong) {
                        System.out.println(waypoints.indexOf(w));
                        System.out.println("Given is null");
                        System.out.println(computedPasses.get(c).get(w) + "\n");
                    }
                } else if (computedPasses.get(c).get(w) == null && !(givenPasses.get(c).get(w) == null)) {
                    wronglyNotComputed++;
                    if (printWrong) {
                        System.out.println(waypoints.indexOf(w));
                        System.out.println("Computed is null");
                        System.out.println(givenPasses.get(c).get(w) + "\n");
                    }
                } else if (givenPasses.get(c).get(w) == null && computedPasses.get(c).get(w) == null) {
                    correctlyNotComputed++;
                    if (printRight) {
                        System.out.println(waypoints.indexOf(w));
                        System.out.println("Both null" + "\n");
                    }
                } else {
                    long timedelta = givenPasses.get(c).get(w).getTimePoint().asMillis()
                            - computedPasses.get(c).get(w).getTimePoint().asMillis();
                    if ((Math.abs(timedelta) < tolerance)) {
                        correctPasses++;
                        if (printRight) {
                            System.out.println(waypoints.indexOf(w));
                            System.out.println("Calculated: " + computedPasses.get(c).get(w));
                            System.out.println("Given: " + givenPasses.get(c).get(w));
                            System.out.println(timedelta / 1000 + " s\n");
                        }
                    } else {
                        if (printWrong) {
                            System.out.println(waypoints.indexOf(w));
                            System.out.println("Calculated: " + computedPasses.get(c).get(w));
                            System.out.println("Given: " + givenPasses.get(c).get(w));
                            System.out.println(timedelta / 1000 + "\n");
                        }
                        incorrectPasses++;
                    }
                }
            }
        }

        int totalMarkPasses = numberOfCompetitors * waypoints.size();
        assertEquals(totalMarkPasses, incorrectPasses + correctPasses + wronglyNotComputed + correctlyNotComputed
                + wronglyComputed);
        System.out.println("Total theoretical Passes: " + totalMarkPasses);
        double accuracy = (double) (correctPasses + correctlyNotComputed) / totalMarkPasses;
        System.out.println("Correct comparison: " + correctPasses);
        System.out.println("Incorrect comparison: " + incorrectPasses);
        System.out.println("Correctly Null: " + correctlyNotComputed);
        System.out.println("Should be null but arent:" + wronglyComputed);
        System.out.println("Should not be null but are: " + wronglyNotComputed);
        System.out.println("accuracy: " + accuracy);
        System.out.println("Computation time: " + time + " ms");
        assertTrue(accuracy >= 0.9);
    }
}
