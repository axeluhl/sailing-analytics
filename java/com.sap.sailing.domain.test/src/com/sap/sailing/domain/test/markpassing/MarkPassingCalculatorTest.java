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
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
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

        final MarkPassingCalculator markPassCreator = new MarkPassingCalculator(getTrackedRace());
        final int tolerance = 20000;
        int correctPasses = 0;
        int incorrectPasses = 0;
        int missingGivenMarkPassings = 0;
        int missingMarkPasses = 0;
        int numberOfCompetitors = 0;
        Iterable<Waypoint> waypoints = getRace().getCourse().getWaypoints();
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> computedPasses = new LinkedHashMap<>();
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> givenPasses = new LinkedHashMap<>();

        // Get given Markpasses
        for (Competitor c : getRace().getCompetitors()) {
            LinkedHashMap<Waypoint, MarkPassing> givenMarkPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            for (Waypoint wp : waypoints) {
                MarkPassing markPassing = getTrackedRace().getMarkPassing(c, wp);
                givenMarkPasses.put(wp, markPassing);
                if (givenMarkPasses.get(wp) == null) {
                    missingGivenMarkPassings++;
                }
            }
            givenPasses.put(c, givenMarkPasses);
        }

        // Get Marks
        Set<Mark> marks = new HashSet<>();
        for (Waypoint w : waypoints) {
            Iterator<Mark> it = w.getMarks().iterator();
            while (it.hasNext()) {
                marks.add(it.next());
            }
        }
        // Pass in Mark Fixes
        for (Mark m : marks) {
            try {
                getTrackedRace().getOrCreateTrack(m).lockForRead();
                for (GPSFix fix : getTrackedRace().getOrCreateTrack(m).getFixes()) {
                    markPassCreator.markPositionChanged(fix, m);
                }
            } finally {
                getTrackedRace().getOrCreateTrack(m).unlockAfterRead();
            }
        }
        // Pass in Competitor Fixes
        for (Competitor c : getRace().getCompetitors()) {
            numberOfCompetitors++;
            try {
                getTrackedRace().getTrack(c).lockForRead();
                for (GPSFixMoving fix : getTrackedRace().getTrack(c).getFixes()) {
                    markPassCreator.competitorPositionChanged(fix, c);
                }
            } finally {
                getTrackedRace().getTrack(c).unlockAfterRead();
            }
        }
        // Get results
        for (Competitor c : getRace().getCompetitors()) {
            LinkedHashMap<Waypoint, MarkPassing> passes = new LinkedHashMap<>();
            for (Waypoint w : waypoints) {
                passes.put(w, markPassCreator.getMarkPass(c, w));
            }
            computedPasses.put(c, passes);
        }

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
                            System.out.println(getRace().getCourse().getIndexOfWaypoint(w));
                            System.out.println("Calculated: " + computedPasses.get(c).get(w));
                            System.out.println("Given: " + givenPasses.get(c).get(w));
                            System.out.println(timedelta / 1000 + "\n");
                        }
                        incorrectPasses++;
                    }
                } catch (NullPointerException e) {
                    missingMarkPasses++;
                    if (printNull) {
                        System.out.println(getRace().getCourse().getIndexOfWaypoint(w));
                        System.out.println("Calculated: " + computedPasses.get(c).get(w));
                        System.out.println("Given: " + givenPasses.get(c).get(w));
                        System.out.println("Null" + "\n");
                    }
                } finally {
                    if (printAll) {
                        System.out.println(getRace().getCourse().getIndexOfWaypoint(w));
                        System.out.println("Calculated: " + computedPasses.get(c).get(w));
                        System.out.println("Given: " + givenPasses.get(c).get(w) + "\n");
                    }

                }
            }
        }

        double givenMarkPasses = numberOfCompetitors
                * (getRace().getCourse().getIndexOfWaypoint(getRace().getCourse().getLastWaypoint()) + 1)
                - missingGivenMarkPassings;
        double accuracy = (double) correctPasses / givenMarkPasses;
        System.out.println("Missing Given MarkPass: " + missingGivenMarkPassings);
        System.out.println("Incorrect comparison: " + incorrectPasses);
        System.out.println("Correct comparison: " + correctPasses);
        System.out.println("One markpass missing: " + missingMarkPasses);
        System.out.println("Total given MarkPasses: " + givenMarkPasses);
        System.out.println(correctPasses + " / " + givenMarkPasses);
        System.out.println("accuracy: " + accuracy);
        assertTrue(accuracy > 0.8);
    }
}
