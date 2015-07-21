package com.sap.sailing.dashboards.gwt.server.startlineadvantages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
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
import com.sap.sailing.domain.tracking.LineDetails;
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
    private Position startline;
    private Position firstMarkPosition;
    private MovingAverage advantageMaximumAverage;
    private double startlineAdvantage;
    private double startlineLenght;
    private double windSpeed;
    private double windDirection;
    private double tackingAngle;
    private double jibingAngle;
    
    private static final double DEFAULT_TACKING_ANGLE = 60.0;
    private static final double DEFAULT_JIBING_ANGLE = 45.0;
    private static final double DEFAULT_WIND_DIRECTION = 0.0;
    private static final double DEFAULT_WIND_SPEED_IN_KTS = 12.0;
    
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesCalculator.class.getName());
    
    public StartlineAdvantagesCalculator(){
        advantageMaximumAverage = new MovingAverage(500);
    }
    
    // Get Course Buoys
    // Get Wind
    // Get Maneuver Angle and Boat Speed at Angle
    // Get Positions on start line with normal upwind and overlaying lay line upwind
    // Calculate intersection point(s) between start line and lay lines
    // Calculate duration from start line to first mark for overlaying positions
    // Calculate intersection points from normal upwind start positions to first mark
    // Calculate duration from two new distances for normal upwind starts
    // Put all durations in one set
    // Get the smallest of the durations and subtract number from every other number
    public StartlineAdvantagesWithMaxAndAverageDTO getStartLineAdvantagesAccrossLineAtTimePoint(TimePoint timepoint){
        StartlineAdvantagesWithMaxAndAverageDTO result = new StartlineAdvantagesWithMaxAndAverageDTO();
        List<StartLineAdvantageDTO> advantages;
        if (currentLiveTrackedRace != null) {
            collectDataForCalculation();
            advantages = calculateStartlineAdvantages();
        } else {
            advantages = generateRandomAdvantagesOnStartline();
            logger.log(Level.INFO, "No live race available for startlineadvantages calculation");
        }
        result.advantages = advantages;
        double maximum = getMaximumAdvantageOfStartlineAdvantageDTOs(advantages);
        result.maximum = maximum;
        advantageMaximumAverage.add(maximum);
        result.average = advantageMaximumAverage.getAverage();
        return result;
    }

    private void collectDataForCalculation() {
        retrieveFirstLegWayPoints();
        retrieveStartlineAdvantageAndLenght();
        retrieveWind();
        retrieveTrackingAndJibingAngle();
    }
    
    private void retrieveFirstLegWayPoints() {
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
    
    private void retrieveStartlineMarkPositionsFromStartLineWayPoint(Waypoint startLineWayPoint) {
        Iterator<Mark> markIterator = startLineWayPoint.getMarks().iterator();
        if (markIterator.hasNext()) {
            Mark startboat = (Mark) markIterator.next();
            if (markIterator.hasNext()) {
                Mark pinEnd = (Mark) markIterator.next();
                TimePoint now = MillisecondsTimePoint.now();
                Position startBoatPosition = getPositionFromMarkAtTimePoint(currentLiveTrackedRace, startboat, now);
                Position pinEndPosition = getPositionFromMarkAtTimePoint(currentLiveTrackedRace, pinEnd, now);
                startlineMarkPositions =  new Pair<Position, Position>(startBoatPosition, pinEndPosition);
            }
        }
    }
    
    private void retrieveFirstMarkPositionFromFirstMarkWayPoint(Waypoint firstMarkWayPoint) {
        if (firstMarkWayPoint.getMarks().iterator().hasNext()) {
            Mark firstMark = firstMarkWayPoint.getMarks().iterator().next();
            TimePoint now = MillisecondsTimePoint.now();
            firstMarkPosition = getPositionFromMarkAtTimePoint(currentLiveTrackedRace, firstMark, now);
            logger.log(Level.INFO, "Firstmark: "+firstMarkPosition);
        }
    }
    
    private void retrieveStartlineAdvantageAndLenght() {
        LineDetails startline = currentLiveTrackedRace.getStartLine(MillisecondsTimePoint.now());
        startlineLenght = startline.getLength().getMeters();
        if (startline != null && startline.getAdvantage() != null) {
            startlineAdvantage = startline.getAdvantage().getMeters();
        }
    }
    
    private void retrieveWind() {
        windSpeed = DEFAULT_WIND_SPEED_IN_KTS;
        windDirection = DEFAULT_WIND_DIRECTION;
    }
    
    private void retrieveTrackingAndJibingAngle() {
        tackingAngle = DEFAULT_TACKING_ANGLE;
        jibingAngle = DEFAULT_JIBING_ANGLE;
    }
    
    private Position getPositionFromMarkAtTimePoint(TrackedRace trackedRace, Mark mark, TimePoint timePoint){
        GPSFixTrack<Mark, GPSFix> fixTrack = trackedRace.getTrack(mark);
        return fixTrack.getEstimatedPosition(timePoint, true);
    }
    
    private List<StartLineAdvantageDTO> calculateStartlineAdvantages() {
        List<StartLineAdvantageDTO> result = new ArrayList<StartLineAdvantageDTO>();
        List<StartLineAdvantageDTO> startlineAdvantagesUnderneathLayline = calculateStartlineAdvantagesUnderneathLaylines();
        result.addAll(startlineAdvantagesUnderneathLayline);
        return result;
    }
    
    private List<StartLineAdvantageDTO> calculateStartlineAdvantagesUnderneathLaylines() {
        List<StartLineAdvantageDTO> result = new ArrayList<StartLineAdvantageDTO>();
        StartLineAdvantageDTO startlineAdvantagePinEnd = new StartLineAdvantageDTO();
        startlineAdvantagePinEnd.confidence = 1.0;
        startlineAdvantagePinEnd.distanceToRCBoatInMeters = startlineLenght;
        StartLineAdvantageDTO startlineAdvantageRCBoat = new StartLineAdvantageDTO();
        startlineAdvantageRCBoat.confidence = 1.0;
        startlineAdvantageRCBoat.distanceToRCBoatInMeters = 0.0;
        logger.log(Level.INFO, "Startline Advantage "+startlineAdvantage);
        if(startlineAdvantage > 0) {
            startlineAdvantagePinEnd.startLineAdvantage = startlineAdvantage;
            startlineAdvantageRCBoat.startLineAdvantage = 0.0;
        } else {
            startlineAdvantagePinEnd.startLineAdvantage = 0.0;
            startlineAdvantageRCBoat.startLineAdvantage = Math.abs(startlineAdvantage);
        }
        result.add(startlineAdvantageRCBoat);
        result.add(startlineAdvantagePinEnd);
        return result;
        
    }
    
    private Position getIntersectionPositionBetweenStartlineAndLayline() {
        Position result = null;
        
        return result;
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
    
    //Dummy data generation
    
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
        randomAdvantagesOnStartline.add(getRandomStartLineAdvantageDTOWithX(0));
        randomAdvantagesOnStartline.add(getRandomStartLineAdvantageDTOWithX(33));
        return randomAdvantagesOnStartline;
    }
    
    private StartLineAdvantageDTO getRandomStartLineAdvantageDTOWithX(int x) {
        StartLineAdvantageDTO result = new StartLineAdvantageDTO();
        result.distanceToRCBoatInMeters = x;
        result.startLineAdvantage = (x+1)/2+(0 + (int)(Math.random()*5));
        result.confidence = 1;
        return result;
    }
}
