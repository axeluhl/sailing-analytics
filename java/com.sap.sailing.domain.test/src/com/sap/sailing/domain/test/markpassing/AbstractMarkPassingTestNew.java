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
import java.util.LinkedHashMap;

import org.junit.Before;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
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
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
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

        int correctPasses = 0;
        int incorrectPasses = 0;
        int totalPasses = 0;
        int missingGivenMarkPassings = 0;
        int missingCalculatedMarkPassings = 0;
        Iterable<Waypoint> waypoints;
        LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> waypointTracks = new LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>>();

        // ///// Fill waypoints (Iterable of all Waypoints) /////

        try {
            getRace().getCourse().lockForRead();
            waypoints = getRace().getCourse().getWaypoints();

        } finally {
            getRace().getCourse().unlockAfterRead();
        }

        // / Fill waypointTracks (HashMap of Waypoints and their Tracks) //////

        for (Waypoint w : waypoints) {

            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> marks = new ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>();

            for (Mark mark : w.getMarks()) {

                DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(mark);

                marks.add(markTrack);
            }
            waypointTracks.put(w, marks);
        }

        // For each competitor:

        for (Competitor c : getRace().getCompetitors()) {

            System.out.println(c.getName());

            LinkedHashMap<Waypoint, MarkPassing> givenPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            LinkedHashMap<Waypoint, MarkPassing> computedPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            TimePoint lastWayPoint = getTrackedRace().getStartOfRace();

            // Get GPSFixes

            DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes;

            try {
                getTrackedRace().getTrack(c).lockForRead();
                gpsFixes = getTrackedRace().getTrack(c);
            } finally {
                getTrackedRace().getTrack(c).unlockAfterRead();
            }
            System.out.println(!gpsFixes.equals(null));

            // Get given MarkPasses

            for (Waypoint w : waypoints) {
                MarkPassing markPassing = getTrackedRace().getMarkPassing(c, w);

                givenPasses.put(w, markPassing);
                System.out.println("Given Markpass: ");
                System.out.println(givenPasses.get(w));
                System.out.println("Passing in MarkPass: ");
                System.out.println(lastWayPoint);
                // Actually compute MarkPassings
                TimePoint markPass;

                // try {
                markPass = detector.computeMarkpass(gpsFixes, waypointTracks.get(w), lastWayPoint);
                // } catch (NullPointerException e) {
                // markPass = null;
                // missingCalculatedMarkPassings++;

                // }

                MarkPassing m = new MarkPassingImpl(markPass, w, c);

                computedPasses.put(w, m);
                System.out.println("Calculated: " + computedPasses.get(w));

                // Set lastWayPoint to new lastWayPoint

                lastWayPoint = computedPasses.get(w).getTimePoint();

                // Compare computed and calculated MarkPassings

                try {
                    givenPasses.get(w).getTimePoint();

                    try {
                        computedPasses.get(w).getTimePoint();

                        long timedelta = givenPasses.get(w).getTimePoint().asMillis()
                                - computedPasses.get(w).getTimePoint().asMillis();

                        if ((Math.abs(timedelta) < tolerance)) {

                            correctPasses++;

                        } else {
                            incorrectPasses++;
                        }

                        totalPasses++;

                    } catch (NullPointerException e) {
                        missingCalculatedMarkPassings++;
                    } finally {
                    }

                } catch (NullPointerException e) {
                    missingGivenMarkPassings++;

                }

                finally {
                }

            }
        }

        System.out.println("Missing Given MarkPass: " + missingGivenMarkPassings);
        System.out.println("Failed Calculation: " + missingCalculatedMarkPassings);
        System.out.println("Incorrect calculation: " + incorrectPasses);
        System.out.println("Correct Calculation: " + correctPasses);
        double accuracy = (double) correctPasses / ((double) totalPasses - missingGivenMarkPassings);
        System.out.println(correctPasses + " / " + totalPasses);
        System.out.println(accuracy);
        assertTrue(accuracy > 0.9);

    }
}
