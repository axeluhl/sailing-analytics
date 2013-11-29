package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertEquals;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

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
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class MarkPassingCalculatorTest extends OnlineTracTracBasedTest {
    private boolean forceReload = true;

    public MarkPassingCalculatorTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void testTornado16() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Tornado 16");
        testRace("04687b2a-9e68-11e0-85be-406186cbf87c");
    }

    
      @Test public void testTornado4() throws IOException, InterruptedException, URISyntaxException {
      System.out.println("Tornado Race 4"); testRace("5291b3ea-9934-11e0-85be-406186cbf87c"); }
      
      @Test public void testStarMedal() throws IOException, InterruptedException, URISyntaxException {
      System.out.println("Star Medal"); testRace("d591d808-9c48-11e0-85be-406186cbf87c"); }
      
      @Test public void test505_2() throws IOException, InterruptedException, URISyntaxException {
      System.out.println("505 2"); testRace("357c700a-9d9a-11e0-85be-406186cbf87c"); }
      
      @Test public void test505_7() throws IOException, InterruptedException, URISyntaxException {
      System.out.println("505 7"); testRace("cb043bb4-9e92-11e0-85be-406186cbf87c"); }
      
      @Test public void testStar4() throws IOException, InterruptedException, URISyntaxException {
      System.out.println("Star 4"); testRace("f5f531ec-99ed-11e0-85be-406186cbf87c"); }
     
    private void testRace(String raceID) throws IOException, InterruptedException, URISyntaxException {
        setUp(raceID);
        compareMarkpasses();
    }

    private void setUp(String raceID) throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        if (!loadData(raceID) && forceReload) {
            System.out.println("Downloading new data from the web.");
            this.setUp("event_20110609_KielerWoch",
            /* raceId */raceID, new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS,
                    ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
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

    private void compareMarkpasses() {
        double time = System.currentTimeMillis();
        final MarkPassingCalculator markPassCreator = new MarkPassingCalculator(getTrackedRace());
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

        // Get Marks
        Set<Mark> marks = new HashSet<>();
        for (Waypoint w : waypoints) {
            Iterator<Mark> it = w.getMarks().iterator();
            while (it.hasNext()) {
                marks.add(it.next());
            }
        }

        LinkedHashMap<Mark, GPSFix> nextMarkFix = new LinkedHashMap<>();
        LinkedHashMap<Mark, DynamicGPSFixTrack<Mark, GPSFix>> markTracks = new LinkedHashMap<>();
        for (Mark m : marks) {
            try {
                getTrackedRace().getOrCreateTrack(m).lockForRead();
                markTracks.put(m, getTrackedRace().getOrCreateTrack(m));
            } finally {
                getTrackedRace().getOrCreateTrack(m).unlockAfterRead();
            }
            nextMarkFix.put(m, markTracks.get(m).getFirstRawFix());
        }
        LinkedHashMap<Competitor, GPSFixMoving> nextCompetitorFix = new LinkedHashMap<>();
        LinkedHashMap<Competitor, DynamicGPSFixTrack<Competitor, GPSFixMoving>> competitorTracks = new LinkedHashMap<>();
        for (Competitor c : getRace().getCompetitors()) {
            try {
                getTrackedRace().getTrack(c).lockForRead();
                competitorTracks.put(c, getTrackedRace().getTrack(c));
            } finally {
                getTrackedRace().getTrack(c).unlockAfterRead();
            }
            nextCompetitorFix.put(c, competitorTracks.get(c).getFirstRawFix());
        }
        for (Mark m : marks) {
            GPSFix fix = nextMarkFix.get(m);
            while (fix != null) {
                markPassCreator.markPositionChanged(fix, m);
                nextMarkFix.put(m, markTracks.get(m).getFirstFixAfter(fix.getTimePoint()));
                fix = nextMarkFix.get(m);
            }
        }

        boolean done = false;
        while (!done) {
            done = true;
            for (Competitor c : getRace().getCompetitors()) {
                GPSFixMoving fix = nextCompetitorFix.get(c);
                if (fix != null) {
                    markPassCreator.competitorPositionChanged(fix, c);
                    fix = competitorTracks.get(c).getFirstFixAfter(fix.getTimePoint());
                    nextCompetitorFix.put(c, fix);
                }
            }
            for (Competitor c : getRace().getCompetitors()) {
                if (!(nextCompetitorFix.get(c) == null)) {
                    done = false;
                    break;
                }
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
        System.out.println("Computation time: " + (System.currentTimeMillis() - time) / 1000 + " s");
        assertTrue(accuracy > 0.8);

    }
}
