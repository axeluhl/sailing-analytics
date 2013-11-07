package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class MarkPassingCalculatorTest extends OnlineTracTracBasedTest {
    private boolean forceReload = false;

    public MarkPassingCalculatorTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Before
    public void setUp() throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        /*
         * 505 Race 2: 357c700a-9d9a-11e0-85be-406186cbf87c
         * 
         * 505 Race 7: cb043bb4-9e92-11e0-85be-406186cbf87c
         * 
         * 505 Race 10: 357c700a-9d9a-11e0-85be-406186cbf87c
         * 
         * To set by hand: Average Speeds
         */
        String raceID = "357c700a-9d9a-11e0-85be-406186cbf87c";
        if (!loadData(raceID) && !forceReload) {
            System.out.println("Downloading new data from the web.");
            this.setUp("event_20110609_KielerWoch",
            /* raceId */raceID, new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS,
                    ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
            OnlineTracTracBasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace(),
                    new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
            getTrackedRace().recordWind(
                    new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                            new DegreeBearingImpl(65))), new WindSourceImpl(WindSourceType.WEB));
            saveData();
        }
    }

    /**
     * Loads stored data for the given raceID or returns false if no data is present.
     * 
     * @param raceID
     *            - ID of the race to load from disk
     * @return true if data was loaded, false if not
     */
    private boolean loadData(String raceID) {
        String path = null;
        File file = new File("resources/");
        if (file.exists() && file.isDirectory()) {
            for (String fileName : file.list()) {
                if (fileName.endsWith(".data") && fileName.contains(raceID)) {
                    path = "resources/" + fileName;
                    break;
                }
            }
        }
        if (path == null)
            return false;
        FileInputStream fs = null;
        ObjectInputStream os = null;
        Object obj = null;

        try {
            System.out.print("Loading cached data for raceID " + raceID + "...");
            fs = new FileInputStream(path);
            os = new ObjectInputStream(fs);
            obj = os.readObject();
            System.out.println("done!");
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        if (obj != null && obj instanceof DynamicTrackedRace) {
            setTrackedRace((DynamicTrackedRace) obj);
            setRace(getTrackedRace().getRace());
            return true;
        }
        return false;
    }

    /**
     * Saves current result of getTrackedRace to disk for future reuse.
     */
    private void saveData() {

        DynamicTrackedRace trackedRace = getTrackedRace();
        String racePath = "resources/" + trackedRace.getRace().getId() + ".data";
        FileOutputStream fs = null;
        ObjectOutputStream os = null;
        try {
            System.out.println("Caching data for raceID " + trackedRace.getRace().getId());
            File f = new File(racePath);
            f.createNewFile();
            fs = new FileOutputStream(f);
            os = new ObjectOutputStream(fs);
            os.writeObject(trackedRace);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    @Test
    public void compareMarkpasses() {

        final MarkPassingCalculator markPassCreator = new MarkPassingCalculator();
        final int tolerance = 21000;
        int correctPasses = 0;
        int incorrectPasses = 0;
        int missingGivenMarkPassings = 0;
        int missingMarkPasses = 0;
        int numberOfCompetitors = 0;
        TimePoint start = getTrackedRace().getStartOfRace();
        TimePoint end = getTrackedRace().getEndOfRace();
        TimePoint middle = start.plus(end.minus(start.asMillis()).asMillis() / 2);
        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        ArrayList<Waypoint> waypointsToReplace = new ArrayList<>();
        LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> wayPointTracks = new LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>>();
        LinkedHashMap<Competitor, ArrayList<GPSFixMoving>> competitorTracks = new LinkedHashMap<Competitor, ArrayList<GPSFixMoving>>();
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> computedPasses = new LinkedHashMap<>();
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> givenPasses = new LinkedHashMap<>();
        LinkedHashMap<Waypoint, Double> averageLegLength = new LinkedHashMap<>();
        
        System.out.println("Start: " + start + ", End: " + end);

        // Get Waypoints
        try {
            getRace().getCourse().lockForRead();
            for (Waypoint w : getRace().getCourse().getWaypoints()) {
                waypoints.add(w);
            }
        } finally {
            getRace().getCourse().unlockAfterRead();
        }
        getRace().getCourse().getWaypoints();
        System.out.println(waypoints.size() + " Waypoints: ");

        // Get LegTypes
        ArrayList<String> legs = new ArrayList<>();
        Iterator<TrackedLeg> itl = getTrackedRace().getTrackedLegs().iterator();

        while (itl.hasNext()) {
            try {
                legs.add(itl.next().getLegType(middle).toString());
            } catch (NoWindException e) {
            }
        }

        // Get given Markpasses
        for (Competitor c : getRace().getCompetitors()) {

            LinkedHashMap<Waypoint, MarkPassing> givenMarkPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            for (Waypoint wp : waypoints) {
                MarkPassing markPassing = getTrackedRace().getMarkPassing(c, wp);
                givenMarkPasses.put(wp, markPassing);
                try {
                    givenMarkPasses.get(wp).getTimePoint();
                } catch (NullPointerException e) {
                    missingGivenMarkPassings++;
                }
            }
            givenPasses.put(c, givenMarkPasses);
        }

        for (Waypoint wp : waypoints) {
            int index = waypoints.indexOf(wp);
            
            // Get Leg Lengths
            double legBefore = 0;
            double legAfter = 0;
            int number = 0;
            if (index != 0) {
                legBefore = getTrackedRace().getTrackedLegFinishingAt(wp).getGreatCircleDistance(middle).getMeters();
                number++;
            }
            if (index != (waypoints.size() - 1)) {
                legAfter = getTrackedRace().getTrackedLegStartingAt(wp).getGreatCircleDistance(middle).getMeters();
                number++;
            }
            double averageLength = (legBefore + legAfter) / number;
            averageLegLength.put(wp, averageLength);

            // Give Waypoints Passing Instructions
            try {
                // TODO get rid of try/catch
                wp.getPassingInstructions().equals(null);
            } catch (NullPointerException e) {
                final WaypointImpl waypointWithPassingInstructions;
                if (index == 0 || index == waypoints.size() - 1) {
                    waypointWithPassingInstructions = new WaypointImpl(wp.getControlPoint(), PassingInstruction.Line);
                } else {
                    int numberofMarks = 0;
                    Iterator<Mark> it = wp.getMarks().iterator();
                    while (it.hasNext()) {
                        it.next();
                        numberofMarks++;
                    }
                    if (numberofMarks == 2) {
                        waypointWithPassingInstructions = new WaypointImpl(wp.getControlPoint(),
                                PassingInstruction.Gate);
                    } else {
                        if (numberofMarks == 1) {
                            waypointWithPassingInstructions = new WaypointImpl(wp.getControlPoint(),
                                    PassingInstruction.Port);
                        } else {
                            waypointWithPassingInstructions = new WaypointImpl(wp.getControlPoint(),
                                    PassingInstruction.None);
                        }
                    }
                }
                waypointsToReplace.add(index, waypointWithPassingInstructions);
            }
        }

        for (Waypoint wp : waypointsToReplace) {
            Waypoint oldWaypoint = waypoints.get(waypointsToReplace.indexOf(wp));
            double averageLength = averageLegLength.get(oldWaypoint);
            for (Competitor c : getRace().getCompetitors()) {
                MarkPassing m = givenPasses.get(c).get(waypoints.get(waypointsToReplace.indexOf(wp)));
                givenPasses.get(c).remove(m);
                givenPasses.get(c).put(wp, m);
            }
            waypoints.remove(oldWaypoint);
            averageLegLength.remove(oldWaypoint);
            waypoints.add(waypointsToReplace.indexOf(wp), wp);
            averageLegLength.put(wp, averageLength);
        }

        for (Waypoint wp : waypoints) {
            // Get Waypoint Tracks
            System.out.println(wp + ": " + wp.getPassingInstructions());
            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> marks = new ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>();
            for (Mark mark : wp.getControlPoint().getMarks()) {
                DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(mark);
                marks.add(markTrack);
            }
            wayPointTracks.put(wp, marks);
        }

        // Get Competitor GPSFixes
        for (Competitor c : getRace().getCompetitors()) {
            numberOfCompetitors++;
            ArrayList<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
            try {
                getTrackedRace().getTrack(c).lockForRead();
                Iterator<GPSFixMoving> GPSFixes = getTrackedRace().getTrack(c).getFixes().iterator();
                while (GPSFixes.hasNext()) {
                    fixes.add(GPSFixes.next());
                }
            } finally {
                getTrackedRace().getTrack(c).unlockAfterRead();
            }
            competitorTracks.put(c, fixes);
        }
        System.out.println(numberOfCompetitors + " Competitors");

        // Calculate MarkPasses!!
        long n = System.currentTimeMillis();
        computedPasses = markPassCreator.calculateMarkpasses(wayPointTracks, competitorTracks, start, end, legs,
                averageLegLength, getRace().getBoatClass().getHullLength().getMeters(), waypoints);
        System.out.println("Computation time: " + (System.currentTimeMillis() - n));

        // Compare computed and calculated MarkPassings
        boolean printAll = false;
        boolean printWrong = true;
        boolean printNull = true;
        for (Competitor c : getRace().getCompetitors()) {

            System.out.println(c.getName() + "\n");
            for (Waypoint w : waypoints) {
                try {
                    long timedelta = givenPasses.get(c).get(w).getTimePoint().asMillis()
                            - computedPasses.get(c).get(w).getTimePoint().asMillis();
                    if ((Math.abs(timedelta) < tolerance)) {
                        correctPasses++;
                    } else {
                        if (printWrong) {
                            System.out.println(waypoints.indexOf(w));
                            System.out.println("Calculated: " + computedPasses.get(c).get(w));
                            System.out.println("Given: " + givenPasses.get(c).get(w));
                            System.out.println(timedelta / 1000 + "\n");
                        }
                        incorrectPasses++;
                    }
                } catch (NullPointerException e) {
                    missingMarkPasses++;
                    if (printNull) {
                        System.out.println(waypoints.indexOf(w));
                        System.out.println("Calculated: " + computedPasses.get(c).get(w));
                        System.out.println("Given: " + givenPasses.get(c).get(w) + "\n");
                    }
                } finally {
                    if (printAll) {
                        System.out.println(waypoints.indexOf(w));
                        System.out.println("Calculated: " + computedPasses.get(c).get(w));
                        System.out.println("Given: " + givenPasses.get(c).get(w) + "\n");
                    }
                }

            }
        }
        System.out.println("Missing Given MarkPass: " + missingGivenMarkPassings);
        double givenMarkPasses = numberOfCompetitors * waypoints.size() - missingGivenMarkPassings;
        System.out.println("Incorrect comparison: " + incorrectPasses);
        System.out.println("Correct comparison: " + correctPasses);
        System.out.println("One markpass missing: " + missingMarkPasses);
        System.out.println("Total given MarkPasses: " + givenMarkPasses);
        double accuracy = (double) correctPasses / givenMarkPasses;
        System.out.println(correctPasses + " / " + givenMarkPasses);
        System.out.println("accuracy: " + accuracy);
        assertTrue(accuracy > 0.7);
    }
}
