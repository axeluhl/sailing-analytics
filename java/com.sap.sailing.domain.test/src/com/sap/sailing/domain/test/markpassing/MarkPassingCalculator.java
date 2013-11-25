package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;

public class MarkPassingCalculator extends AbstractRaceChangeListener implements CourseListener {
    private CandidateFinder finder;
    private CandidateChooser chooser;
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private TimePoint startOfTracking;

    // TODO Add reload all method (Specifically for waypoint changes)
    // TODO Document everything
    // TODO Feldmann issue
    // TODO Use Wind/Maneuver analysis
    // TODO Weighting of Time and Start and Distance and together!!!!!!!
    // TODO Edges from proxy-start to anything besides the actual start have a wrong time estimation because the time
    // between start of tracking and start of race is included in actual but not in estimated

    public MarkPassingCalculator(TrackedRace race) {

        startOfTracking = race.getStartOfTracking();
        if(startOfTracking==null){
            startOfTracking = race.getTimePointOfOldestEvent();
        }
        for (Waypoint w : race.getRace().getCourse().getWaypoints()) {
            waypoints.add(w);
        }
        ArrayList<TrackedLeg> legs = new ArrayList<>();
        for(TrackedLeg l : race.getTrackedLegs()){
            legs.add(l);
        }
        chooser = new CandidateChooser(startOfTracking, race.getRace().getCompetitors(), legs);
        finder = new CandidateFinder(waypoints, race.getRace().getCompetitors(), chooser, legs);
        race.addListener(this);
    }    


    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
        finder.newMarkFix(mark, fix);
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
        finder.newCompetitorFix(fix, item);
    }

    @Override
    public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
        // TODO Auto-generated method stub
    }

    @Override
    public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
        // TODO Auto-generated method stub
    }
    
    public MarkPassing getMarkPass(Competitor c, Waypoint w){
        return chooser.getMarkPass(c, w);
    }

    /*
    @SuppressWarnings("serial")
    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> calculateMarkpasses(

    LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> wayPointTracks,
            LinkedHashMap<Competitor, ArrayList<GPSFixMoving>> competitorTracks, TimePoint startOfRace,
            ArrayList<String> legs, LinkedHashMap<Waypoint, Double> averageLegLengths, Double boatLength,
            ArrayList<Waypoint> waypoints) {
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> calculatedMarkpasses = new LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>>();
        


        
        for (Competitor c : competitorTracks.keySet()) {
            

            if (!c.getName().contains("Feldmann")) {

                // System.out.println(c.getName());
                
                //Get Waypoint Positions
                LinkedHashMap<Waypoint, MarkPassing> computedPasses = new LinkedHashMap<Waypoint, MarkPassing>();
                LinkedHashMap<Waypoint, ArrayList<LinkedHashMap<TimePoint, Position>>> wayPointPositions = new LinkedHashMap<>();
                if (!(competitorTracks.get(c).size() == 0)) {

                    List<Timed> timeds = new ArrayList<>();
                    for (GPSFixMoving gps : competitorTracks.get(c)) {
                        final TimePoint finalT = gps.getTimePoint();
                        timeds.add(new Timed() {
                            public TimePoint getTimePoint() {
                                return finalT;
                            }
                        });
                    }

                    for (Waypoint w : wayPointTracks.keySet()) {
                        ArrayList<LinkedHashMap<TimePoint, Position>> markPositions = new ArrayList<>();
                        for (int i = 0; i < wayPointTracks.get(w).size(); i++) {
                            LinkedHashMap<TimePoint, Position> markPosition = new LinkedHashMap<>();
                            Iterator<Timed> itTim = timeds.iterator();
                            wayPointTracks.get(w).get(i).lockForRead();
                            try {
                                Iterator<Position> itPos = wayPointTracks.get(w).get(i)
                                        .getEstimatedPositions(timeds, true);
                                while (itPos.hasNext()) {
                                    markPosition.put(itTim.next().getTimePoint(), itPos.next());
                                }
                                markPositions.add(markPosition);
                            } finally {
                                wayPointTracks.get(w).get(i).unlockAfterRead();
                            }
                        }
                        wayPointPositions.put(w, markPositions);

                    }
                    // Find GPSFix-Candidates for each ControlPoint
                    LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> waypointCandidates = new LinkedHashMap<>();

                //    finder.findCandidates(competitorTracks.get(c), wayPointPositions, boatLength, averageLegLengths);

                    // Create "Candidates"
                    ArrayList<Candidate> candidates = new ArrayList<Candidate>();
                    Candidate start = new Candidate(0, startOfRace, 0);
                    candidates.add(start);

                    for (Waypoint w : waypointCandidates.keySet()) {
                        for (GPSFixMoving gps : waypointCandidates.get(w).keySet()) {
                            Candidate ca = new Candidate(w, gps.getTimePoint(), waypointCandidates.get(w).get(gps),
                                    waypoints.indexOf(w) + 1);
                            candidates.add(ca);
                        }
                    }

                    Candidate end = new Candidate(wayPointTracks.keySet().size() + 1, null, 0);
                    candidates.add(end);

                    // Find shortest Path and create calculated MarkPasses
                    LinkedHashMap<Integer, TimePoint> markPasses = chooser.getMarkPasses(candidates, start, end,
                            wayPointPositions);
                    for (Waypoint w : wayPointTracks.keySet()) {
                        computedPasses.put(w, new MarkPassingImpl(markPasses.get(waypoints.indexOf(w) + 1), w, c));
                    }
                }
                calculatedMarkpasses.put(c, computedPasses);
            }
        }
        return calculatedMarkpasses;
    }

*/

}