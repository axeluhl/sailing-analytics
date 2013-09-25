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
        int totalPasses = 0;
        LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> waypointTracks = new LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>>();
        Iterable<Waypoint> waypoints;

        try {
            getRace().getCourse().lockForRead();
            waypoints = getRace().getCourse().getWaypoints();

        } finally {
            getRace().getCourse().unlockAfterRead();
        }

        for (Waypoint w : waypoints) {

            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> marks = new ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>();

            for (Mark mark : w.getMarks()) {

                DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(mark);

                marks.add(markTrack);
            }
            waypointTracks.put(w, marks);
        }

        for (Competitor c : getRace().getCompetitors()) {
            System.out.println("Competitor " + c.getName());

            LinkedHashMap<Waypoint, MarkPassing> givenPasses = new LinkedHashMap<Waypoint, MarkPassing>();
            {
                for (Waypoint w : waypoints) {
                    MarkPassing markPassing = getTrackedRace().getMarkPassing(c, w);

                    givenPasses.put(w, markPassing);
                }
            }

            DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes;

            try {
                getTrackedRace().getTrack(c).lockForRead();
                gpsFixes = getTrackedRace().getTrack(c);
            } finally {
                getTrackedRace().getTrack(c).unlockAfterRead();
            }
            TimePoint startOfRace = getTrackedRace().getStartOfRace();
            LinkedHashMap<Waypoint, MarkPassing> computed = detector.computeMarkpasses(gpsFixes, waypointTracks, startOfRace);

            for (Waypoint w : computed.keySet()){

                long timedelta = givenPasses.get(w).getTimePoint().asMillis()
                        - computed.get(w).getTimePoint()
                                .asMillis();

                if ((Math.abs(timedelta) < tolerance)) {

                    correctPasses++;

                }

                totalPasses++;

            }

        }

        double accuracy = correctPasses / totalPasses;
        System.out.println(correctPasses + " / " + totalPasses);
        System.out.println(accuracy);
        assertTrue(accuracy > 0.9);

    }
}
