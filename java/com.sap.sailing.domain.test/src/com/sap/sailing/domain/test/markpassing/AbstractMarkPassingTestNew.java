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
import java.util.LinkedHashSet;

import org.junit.Before;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstructions;
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
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public abstract class AbstractMarkPassingTestNew extends OnlineTracTracBasedTest {

    private final DetectorMarkPassing detector;

    private static final int tolerance = 10000;

    // ///!!!!!!!!!!!!!!!!!!!!
    private boolean forceReload = false;

    public AbstractMarkPassingTestNew(/* Mine */DetectorMarkPassing detector) throws MalformedURLException,
            URISyntaxException {
        super();
        /**/this.detector = detector;
    }

    @Before
    public void setUp() throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        /*
         * 505 Race 2: 357c700a-9d9a-11e0-85be-406186cbf87c 505 Race 7: cb043bb4-9e92-11e0-85be-406186cbf87c 505 Race
         * 10: 829bd366-9f53-11e0-85be-406186cbf87c
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

    // //////!!!!!!!!!!!!!!!!!!!

    protected void compareMarkpasses() {
        System.out.println("Started comparison");

        int correctPasses = 0;
        int incorrectPasses = 0;
        int missingGivenMarkPassings = 0;
        int missingCalculatedMarkPassings = 0;

        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        ArrayList<Waypoint> waypointsWithPassingInstructions = new ArrayList<Waypoint>();
        LinkedHashMap<ControlPoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> controlPointTracks = new LinkedHashMap<ControlPoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>>();

        // ///// Get Waypoints (Iterable of all Waypoints) /////

        try {
            getRace().getCourse().lockForRead();

            for (Waypoint w : getRace().getCourse().getWaypoints()) {

                waypoints.add(w);


            }

        } finally {
            getRace().getCourse().unlockAfterRead();
        }


        // Give Waypoints Passing Instructions

        for (int i = 0; i < waypoints.size(); i++) {

            WaypointImpl waypointWithPassingInstructions = null;
            if (i == 0 || i == waypoints.size() - 1) {

                waypointWithPassingInstructions = new WaypointImpl(waypoints.get(i).getControlPoint(),
                        PassingInstructions.LINE);

            } else {
                int numberofMarks = 0;
                Iterator<Mark> it = waypoints.get(i).getMarks().iterator();
                while (it.hasNext()) {
                    it.next();
                    numberofMarks++;
                }
                if (numberofMarks == 2) {

                    waypointWithPassingInstructions = new WaypointImpl(waypoints.get(i).getControlPoint(),
                            PassingInstructions.GATE);

                }
                if (numberofMarks == 1) {

                    waypointWithPassingInstructions = new WaypointImpl(waypoints.get(i).getControlPoint(),
                            PassingInstructions.PORT);

                }
            }
            waypointsWithPassingInstructions.add(waypointWithPassingInstructions);

        }

        // /// Fill controlPointTracks (HashMap of ControlPoints and their Tracks) //////

        LinkedHashSet<ControlPoint> controlPoints = new LinkedHashSet<ControlPoint>();

        for (Waypoint w : waypoints) {

            controlPoints.add(w.getControlPoint());
        }

        System.out.println("Number of ControlPoints: " + controlPoints.size());

        for (ControlPoint cp : controlPoints) {
            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> marks = new ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>();
            for (Mark mark : cp.getMarks()) {
                DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(mark);
                marks.add(markTrack);
            }
            controlPointTracks.put(cp, marks);
        }

        // For each competitor:
        for (Competitor c : getRace().getCompetitors()) {

            System.out.println("\n" + c.getName() + ":\n");

            LinkedHashMap<Waypoint, MarkPassing> givenPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            LinkedHashMap<Waypoint, MarkPassing> computedPasses = new LinkedHashMap<Waypoint, MarkPassing>();

            // Get GPSFixes

            Iterable<GPSFixMoving> getGPSFixes;
            ArrayList<GPSFixMoving> gpsFixes = new ArrayList<GPSFixMoving>();
            try {
                getTrackedRace().getTrack(c).lockForRead();
                getGPSFixes = getTrackedRace().getTrack(c).getFixes();
                Iterator<GPSFixMoving> it = getGPSFixes.iterator();
                while (it.hasNext()) {

                    gpsFixes.add(it.next());

                }

            } finally {
                getTrackedRace().getTrack(c).unlockAfterRead();
            }
            // Get given Markpasses
            for (Waypoint w : waypoints) {

                MarkPassing markPassing = getTrackedRace().getMarkPassing(c, w);

                givenPasses.put(w, markPassing);
                try {
                    givenPasses.get(w).getTimePoint();
                } catch (NullPointerException e) {
                    missingGivenMarkPassings++;

                }
            }

            // Compute MarkPassings

            if (!(gpsFixes.size() == 0)) {

                // System.out.println("LastWaypoint: " + lastWayPoint);

                LinkedHashMap<ControlPoint, ArrayList<LinkedHashMap<Double, GPSFixMoving>>> examineFixes = detector.examineFixes(gpsFixes,
                        controlPointTracks);



                

                // Compare computed and calculated MarkPassings

                for (Waypoint w : computedPasses.keySet())

                    try {
                        givenPasses.get(w).getTimePoint();

                        long timedelta = givenPasses.get(w).getTimePoint().asMillis()
                                - computedPasses.get(w).getTimePoint().asMillis();

                        if ((Math.abs(timedelta) < tolerance)) {

                            correctPasses++;

                        } else {
                            System.out.println("Calculated: " + computedPasses.get(w));
                            System.out.println("Given: " + givenPasses.get(w) + "\n");

                            incorrectPasses++;
                        }

                    } catch (NullPointerException e) {
                        // missingGivenMarkPassings++;

                    }

                    finally {
                    }
            }

            else {
                System.out.println("Competitor has no GPSFixes");
            }
        }
        System.out.println("Missing Given MarkPass: " + missingGivenMarkPassings);
        double givenMarkPasses = 240 - missingGivenMarkPassings;
        System.out.println("Failed Calculation: " + missingCalculatedMarkPassings);

        System.out.println("Incorrect comparison: " + incorrectPasses);
        System.out.println("Correct comparison: " + correctPasses);
        System.out.println("Total given MarkPasses: " + givenMarkPasses);
        double accuracy = (double) correctPasses / givenMarkPasses;
        System.out.println(correctPasses + " / " + givenMarkPasses);
        System.out.println("accuracy: " + accuracy);

        assertTrue(accuracy > 0.9);

    }
}
