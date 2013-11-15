package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class MarkPassingCalculator {
    CandidateFinder finder = new CandidateFinder();
    CandidateChooser chooser = new CandidateChooser();


    @SuppressWarnings("serial")
    public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> calculateMarkpasses(

    LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> wayPointTracks,
            LinkedHashMap<Competitor, ArrayList<GPSFixMoving>> competitorTracks, TimePoint startOfRace,
            ArrayList<String> legs, LinkedHashMap<Waypoint, Double> averageLegLengths, Double boatLength,
            ArrayList<Waypoint> waypoints) {
        LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> calculatedMarkpasses = new LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>>();
        
        //TODO Start Problem
        
        for (Competitor c : competitorTracks.keySet()) {
            
            //TODO Feldmann issue
            if (!c.getName().contains("Feldmann")) {

                // System.out.println(c.getName());
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
                    LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> waypointCandidates = finder
                            .findCandidates(competitorTracks.get(c), wayPointPositions, boatLength, averageLegLengths);

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
                            wayPointPositions, legs);
                    for (Waypoint w : wayPointTracks.keySet()) {
                        computedPasses.put(w, new MarkPassingImpl(markPasses.get(waypoints.indexOf(w) + 1), w, c));
                    }
                }
                calculatedMarkpasses.put(c, computedPasses);
            }
        }
        return calculatedMarkpasses;
    }
}