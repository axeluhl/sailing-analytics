package com.sap.sailing.dashboards.gwt.server.startlineadvantages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.server.LiveTrackedRaceListener;
import com.sap.sailing.dashboards.gwt.server.RibDashboardServiceImpl;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesCalculator implements LiveTrackedRaceListener{

    private TrackedRace currentLiveTrackedRace;
    private Pair<Position, Position> startlineMarkPositions;
    private Position firstMarkPosition;
    
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesCalculator.class.getName());
    
    public StartlineAdvantagesCalculator(){
    }
    
    public Map<Double, Double> getStartLineAdvantagesAccrossLine(){
        Map<Double, Double> result = new HashMap<Double, Double>();
        // Get Course Buoys
        // Get Wind
        // Get Maneuver Angle and Boat Speed at Angle
        // Calculate intersection point(s) between start line and lay lines
        // Get Positions on start line with normal upwind and overlaying lay line upwind
        // Calculate duration from start line to first mark for overlaying positions
        // Calculate intersection points from normal upwind start positions to first mark
        // Calculate duration from two new distances for normal upwind starts
        // Put all durations in one set
        // Get the smallest of the durations and subtract number from every other number
        retrieveFirstLegWayPoints();
        logger.log(Level.INFO, "Startline Startboat : "+startlineMarkPositions.getA());
        logger.log(Level.INFO, "Startline PinEnd: "+startlineMarkPositions.getB());
        logger.log(Level.INFO, "Firstmark: "+firstMarkPosition);
        return result;
    }
    
    private void retrieveFirstLegWayPoints() {
        if (currentLiveTrackedRace != null) {
            Course course = currentLiveTrackedRace.getRace().getCourse();
            if (course != null) {
                Waypoint startlineWayPoint= course.getFirstLeg().getFrom();
                Waypoint firstmarkWayPoint = course.getFirstLeg().getTo();
                if (startlineWayPoint != null && firstmarkWayPoint != null) {
                    retrieveStartlineMarkPositionsFromStartLineWayPoint(startlineWayPoint);
                    retrieveFirstMarkPositionFromFirstMarkWayPoint(firstmarkWayPoint);
                }
            }
        }
    }
    
    private void retrieveStartlineMarkPositionsFromStartLineWayPoint(Waypoint startLineWayPoint){
        if(startLineWayPoint.getMarks().iterator().hasNext()){
           Mark startboat =  startLineWayPoint.getMarks().iterator().next();
           if(startLineWayPoint.getMarks().iterator().hasNext()){
               Mark pinEnd =  startLineWayPoint.getMarks().iterator().next();
               TimePoint now = MillisecondsTimePoint.now();
               Position startBoatPosition = getPositionFromMarkAtTimePoint(currentLiveTrackedRace, startboat, now);
               Position pinEndPosition = getPositionFromMarkAtTimePoint(currentLiveTrackedRace, pinEnd, now);
               startlineMarkPositions = new Pair<Position, Position>(startBoatPosition, pinEndPosition);
           }
        }
    }
    
    private void retrieveFirstMarkPositionFromFirstMarkWayPoint(Waypoint firstMarkWayPoint) {
        if (firstMarkWayPoint.getMarks().iterator().hasNext()) {
            Mark firstMark = firstMarkWayPoint.getMarks().iterator().next();
            TimePoint now = MillisecondsTimePoint.now();
            firstMarkPosition = getPositionFromMarkAtTimePoint(currentLiveTrackedRace, firstMark, now);
        }
    }
    
    private Position getPositionFromMarkAtTimePoint(TrackedRace trackedRace, Mark mark, TimePoint timePoint){
        GPSFixTrack<Mark, GPSFix> fixTrack = trackedRace.getTrack(mark);
        return fixTrack.getEstimatedPosition(timePoint, true);
    }
    
    private void getStartlineOfRace(){
        
    }
    
    private void getFirstMarkOfRace(){
        
    }
    
    private Set<Double> getPositionsLineWithStepWidthInMeter(Object line, int meter){
        return null;
    }
    
    private Position getIntersectionPointOfTwoGPSLines(Object line1, Object line2){
        return null;
    }

    @Override
    public void liveTrackedRaceDidChange(TrackedRace trackedRace) {
        this.currentLiveTrackedRace = trackedRace;
    }
}
