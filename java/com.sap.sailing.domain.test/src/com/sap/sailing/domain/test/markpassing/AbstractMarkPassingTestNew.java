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

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstructions;
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

    private final AbstractCandidateFinder candidateFinder;

    private static final int tolerance = 10000;

    // ///!!!!!!!!!!!!!!!!!!!!
    private boolean forceReload = false;

    public AbstractMarkPassingTestNew(/* Mine */AbstractCandidateFinder candidateFinder) throws MalformedURLException,
            URISyntaxException {
        super();
        /**/this.candidateFinder = candidateFinder;
    }

    @Before
    public void setUp() throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        /*
         * 505 Race 2:  357c700a-9d9a-11e0-85be-406186cbf87c 
         * 505 Race 7:  cb043bb4-9e92-11e0-85be-406186cbf87c 
         * 505 Race 10: 829bd366-9f53-11e0-85be-406186cbf87c
         * 
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

        int correctPasses = 0;
        int incorrectPasses = 0;
        int missingGivenMarkPassings = 0;
        int missingCalculatedMarkPassings = 0;

        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        ArrayList<Waypoint> waypointsWithPassingInstructions = new ArrayList<Waypoint>();
        LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> controlPointTracks = new LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>>();

        // ///// Get Waypoints (Iterable of all Waypoints) /////

        try {
            getRace().getCourse().lockForRead();

            for (Waypoint w : getRace().getCourse().getWaypoints()) {

                waypoints.add(w);

            }

        } finally {
            getRace().getCourse().unlockAfterRead();
        }

        getRace().getCourse().getWaypoints();

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

        
        for (Waypoint w : waypointsWithPassingInstructions) {

            ArrayList<DynamicGPSFixTrack<Mark, GPSFix>> marks = new ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>();
            for (Mark mark : w.getControlPoint().getMarks()) {
                DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(mark);
                marks.add(markTrack);
            }
            controlPointTracks.put(w, marks);
        }

        // For each competitor:

        for (Competitor c : getRace().getCompetitors()) {
            if (!c.getName().equals("Feldmann")) {
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

                // Get Candidates for each ControlPoint
                if (!(gpsFixes.size() == 0)) {

                    LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> waypointCandidates = candidateFinder
                            .findCandidates(gpsFixes, controlPointTracks);

                   

                    // Create "Candidates" and all legal Edges
                    ArrayList<Candidate> candidates = new ArrayList<Candidate>();
                    ArrayList<Edge> edges = new ArrayList<Edge>();

                    Candidate start = new Candidate(0, getTrackedRace().getStartOfRace().minus(60000), 0);
                    candidates.add(start);
                    Candidate end = new Candidate(waypoints.size() + 1, getTrackedRace().getEndOfRace().plus(1800000),
                            0);

                    for (Waypoint w : waypointCandidates.keySet()) {

                        for (GPSFixMoving gps : waypointCandidates.get(w).keySet()) {

                            Candidate ca = new Candidate((Integer) w.getId(), gps.getTimePoint(), waypointCandidates
                                    .get(w).get(gps));
                            candidates.add(ca);
                        }
                    }
                    candidates.add(end);

                    for (Candidate ca1 : candidates) {

                        for (Candidate ca2 : candidates) {

                            if (ca2.getWaypointID() - ca1.getWaypointID() > 0
                                    && ca1.getTimePoint().before(ca2.getTimePoint())) {

                                Edge e = new Edge(ca1, ca2);
                                edges.add(e);
                            }
                        }
                    }

                    ArrayList<TimePoint> markPasses = findShortestPath(edges, start, end);

                    for (int i = markPasses.size() - 1, j = 0; i >= 0; i--, j++) {
                        computedPasses.put(waypoints.get(j),
                                new MarkPassingImpl(markPasses.get(i), waypoints.get(j), c));

                    }

                    // Compare computed and calculated MarkPassings

                    for (Waypoint w : waypoints)

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

                        }

                        finally {
                            // System.out.println("Calculated: " + computedPasses.get(w));
                            // System.out.println("Given: " + givenPasses.get(w) + "\n");
                        }
                }

                else {
                    System.out.println("Competitor has no GPSFixes");
                }
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

    private ArrayList<TimePoint> findShortestPath(ArrayList<Edge> edges, Candidate start, Candidate end) {
        ArrayList<TimePoint> markPasses = new ArrayList<TimePoint>();

        LinkedHashMap<Candidate, Candidate> candidateWithParent = new LinkedHashMap<Candidate, Candidate>();
        candidateWithParent.put(start, start);

        while (!candidateWithParent.containsKey(end)) {

            Edge startingEdge = new Edge(start, start);
            Edge newCheapestEdge = startingEdge;

            for (Candidate c : candidateWithParent.keySet()) {

                for (Edge e : edges) {

                    if (newCheapestEdge.equals(startingEdge)) {
                        newCheapestEdge = e;

                    } else {
                        if (e.getStart().equals(c) && e.getCost() < newCheapestEdge.getCost()) {

                            newCheapestEdge = e;
                        }
                    }
                }

            }
            edges.remove(newCheapestEdge);
            candidateWithParent.put(newCheapestEdge.getEnd(), newCheapestEdge.getStart());
        }

        Candidate onPath = end;
        while (!onPath.equals(start)) {
            Candidate nextStep = candidateWithParent.get(onPath);
            markPasses.add(nextStep.getTimePoint());
            onPath = nextStep;
        }
        markPasses.remove(start.getTimePoint());
        return markPasses;
    }
}
