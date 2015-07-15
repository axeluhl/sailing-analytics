package com.sap.sailing.dashboards.gwt.server.startlineadvantages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.server.LiveTrackedRaceListener;
import com.sap.sailing.dashboards.gwt.shared.MovingAverage;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
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
    private MovingAverage advantageMaximumAverage;
    
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesCalculator.class.getName());
    
    public StartlineAdvantagesCalculator(){
        advantageMaximumAverage = new MovingAverage(500);
    }
    
    public StartlineAdvantagesWithMaxAndAverageDTO getStartLineAdvantagesAccrossLineAtTimePoint(TimePoint timepoint){
        StartlineAdvantagesWithMaxAndAverageDTO result = new StartlineAdvantagesWithMaxAndAverageDTO();
        List<StartLineAdvantageDTO> advantages = generateRandomAdvantagesOnStartline();
        result.advantages = advantages;
        double maximum = getMaximumAdvantageOfStartlineAdvantageDTOs(advantages);
        result.maximum = maximum;
        advantageMaximumAverage.add(maximum);
        result.average = advantageMaximumAverage.getAverage();
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
//        retrieveFirstLegWayPoints();
//        logger.log(Level.INFO, "Startline Startboat : "+startlineMarkPositions.getA());
//        logger.log(Level.INFO, "Startline PinEnd: "+startlineMarkPositions.getB());
//        logger.log(Level.INFO, "Firstmark: "+firstMarkPosition);
        return result;
    }

    private double getMaximumAdvantageOfStartlineAdvantageDTOs(List<StartLineAdvantageDTO> advantages) {
        double result = 0;
        if(advantages != null && advantages.size() > 0) {
        Collections.sort(advantages, StartLineAdvantageDTO.startlineAdvantageComparatorDesc);
        result = advantages.get(0).startLineAdvantage;
        }
        return result;
    }
    
    private List<StartLineAdvantageDTO> generateRandomAdvantagesOnStartline(){
        List<StartLineAdvantageDTO> randomAdvantagesOnStartline = new ArrayList<StartLineAdvantageDTO>();
        for(int i = 0; i <= 100; i = i+10){
            randomAdvantagesOnStartline.add(getRandomStartLineAdvantageDTOWithX(i));
        }
        return randomAdvantagesOnStartline;
    }
    
    private StartLineAdvantageDTO getRandomStartLineAdvantageDTOWithX(int x) {
        StartLineAdvantageDTO result = new StartLineAdvantageDTO();
        result.distanceToRCBoatInMeters = x;
        result.startLineAdvantage = (x+1)/2+(0 + (int)(Math.random()*5));
        if(x >= 50) {
            result.confidence = 1;
        }else{
            result.confidence = 0.3;
        } 
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
