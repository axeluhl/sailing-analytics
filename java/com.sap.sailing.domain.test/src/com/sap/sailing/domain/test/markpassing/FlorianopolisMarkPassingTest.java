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
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class FlorianopolisMarkPassingTest extends OnlineTracTracBasedTest {

    public  FlorianopolisMarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    private boolean forceReload = true;
    
    protected void testRace() {
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
        boolean printWrong = false;

        for (Competitor c : getRace().getCompetitors()) {
            numberOfCompetitors++;
          //  System.out.println(c.getName() + "\n");
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
        double accuracy = (double) (correctPasses + correctlyNotComputed) / totalMarkPasses;
        System.out.println("Total theoretical Passes: " + totalMarkPasses);
        System.out.println("Correct comparison: " + correctPasses);
        System.out.println("Incorrect comparison: " + incorrectPasses);
        System.out.println("Correctly Null: " + correctlyNotComputed);
        System.out.println("Should be null but arent:" + wronglyComputed);
        System.out.println("Should not be null but are: " + wronglyNotComputed);
        System.out.println("accuracy: " + accuracy);
        System.out.println("Computation time: " + time + " ms");
        assertTrue(accuracy >= 0.9);
    
    }

    public void testRace1() throws IOException, InterruptedException, URISyntaxException {
        setUp("bca3b490-2dce-0131-27f0-60a44ce903c3");
        testRace();
    }
    public void testRace2() throws IOException, InterruptedException, URISyntaxException {
        setUp("52697ec0-2dd0-0131-2802-60a44ce903c3");
        testRace();
    }
    public void testRace3() throws IOException, InterruptedException, URISyntaxException {
        setUp("528a0f30-2dd0-0131-2819-60a44ce903c3");
        testRace();
    }
    public void testRace4() throws IOException, InterruptedException, URISyntaxException {
        setUp("529a4150-2dd0-0131-2830-60a44ce903c3");
        testRace();
    }
    
    protected void setUp(String raceID) throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        //is reload is forced, or otherwise if the data didn't load correctly from file -> reload from server
        if (forceReload || !loadData(raceID)) {
            System.out.println("Downloading new data from the web.");
            setUp("event_20131112_ESSFlorian",
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

    @Override
    protected String getExpectedEventName() {
        return "ESS Florianopolis 2013";
    }
}
