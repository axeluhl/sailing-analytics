package com.sap.sailing.dashboards.gwt.server.startlineadvantages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.server.LiveTrackedRaceListener;
import com.sap.sailing.dashboards.gwt.server.startlineadvantages.precalculation.AbstracPreCalculationDataRetriever;
import com.sap.sailing.dashboards.gwt.shared.MovingAverage;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * @author Alexander Ries (D062114)
 *
 */
public  class StartlineAdvantagesCalculator extends AbstracPreCalculationDataRetriever implements LiveTrackedRaceListener{

    private TrackedRace currentLiveTrackedRace;
    private MovingAverage advantageMaximumAverage;
    
    private final double LENGHT_OF_LAYLINES_IN_KILOMETERS = 200; 
    
    private PolarDataService polarDataService;
    
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesCalculator.class.getName());
    
    public StartlineAdvantagesCalculator(PolarDataService polarDataService){
        this.polarDataService = polarDataService;
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
        if (currentLiveTrackedRace != null) {
            retrieveDataForCalculation(currentLiveTrackedRace);
            Position intersectionOfRightLaylineAndStartline = getIntersectionOfRightLaylineAndStartline();
            Position intersectionOfleftLaylineAndStartline = getIntersectionOfLeftLaylineAndStartline();
            
            //result.advantages = calculateStartlineAdvantages();
        } else {
            logger.log(Level.INFO, "No live race available for startlineadvantages calculation");
        }
//        double maximum = getMaximumAdvantageOfStartlineAdvantageDTOs(result.advantages);
//        result.maximum = maximum;
//        advantageMaximumAverage.add(maximum);
//        result.average = advantageMaximumAverage.getAverage();
        return result;
    }
    
    public Position getIntersectionOfRightLaylineAndStartline () {
        Position result = null;
        Bearing bearingOfRightLaylineInDeg = new DegreeBearingImpl(wind.getBearing().getDegrees() - meouvreAngle / 2);
        result = calculateIntersectionPointsOfStartlineAndLaylineWithBearing(bearingOfRightLaylineInDeg);
        return result;
    }
    
    public Position getIntersectionOfLeftLaylineAndStartline () {
        Position result = null;
        Bearing bearingOfRightLaylineInDeg = new DegreeBearingImpl(wind.getBearing().getDegrees() + meouvreAngle / 2);
        result = calculateIntersectionPointsOfStartlineAndLaylineWithBearing(bearingOfRightLaylineInDeg);
        return result;
    }
    
    private Position calculateIntersectionPointsOfStartlineAndLaylineWithBearing(Bearing bearing) {
        Position result = null;
        Bearing bearingOfStartlineInRad = startlineAndFirstMarkPositions.startBoatPosition
                .getBearingGreatCircle(startlineAndFirstMarkPositions.pinEndPosition);
        Bearing bearingOfStartlineInDeg = new DegreeBearingImpl(bearingOfStartlineInRad.getDegrees());
        logger.log(Level.INFO, "bearingOfStartlineInDeg " + bearingOfStartlineInDeg);
        logger.log(Level.INFO, "bearingOfLaylineInDeg " + bearing);
        Position intersectionPointLaylineStartline = startlineAndFirstMarkPositions.firstMarkPosition
                .getIntersection(bearing, startlineAndFirstMarkPositions.pinEndPosition,
                        bearingOfStartlineInDeg);
        logger.log(Level.INFO, "rightIntersectionPointLaylineStartline " + intersectionPointLaylineStartline);
        Bearing bearingIntersectionPointToFirstMark = new DegreeBearingImpl(
                startlineAndFirstMarkPositions.firstMarkPosition.getBearingGreatCircle(
                        intersectionPointLaylineStartline).getDegrees());
        if (bearingIntersectionPointToFirstMark.getDegrees() < bearing.getDegrees() + 1 &&
            bearingIntersectionPointToFirstMark.getDegrees() > bearing.getDegrees() - 1 &&
            isOnStartline(intersectionPointLaylineStartline)) {
            result = intersectionPointLaylineStartline;
            logger.log(Level.INFO, "Layline crosses startline");
        }
        logger.log(Level.INFO, "bearingLeftIntersectionPointToFirstMark.getDegrees() "
                + bearingIntersectionPointToFirstMark.getDegrees());
        return result;
    }

    private boolean isOnStartline(Position position) {
        boolean result = false;
        Distance distanceToStartline = position.getDistanceToLine(startlineAndFirstMarkPositions.startBoatPosition, startlineAndFirstMarkPositions.pinEndPosition);
        if (distanceToStartline.getMeters() < 1) {
            result = true;
        }
        return result;
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
        startlineAdvantagePinEnd.distanceToRCBoatInMeters = startlineLenghtInMeters;
        StartLineAdvantageDTO startlineAdvantageRCBoat = new StartLineAdvantageDTO();
        startlineAdvantageRCBoat.confidence = 1.0;
        startlineAdvantageRCBoat.distanceToRCBoatInMeters = 0.0;
        logger.log(Level.INFO, "Startline Advantage "+startlineAdvantagePinEnd);
        if(startlineAdvantageAtPinEndInMeters > 0) {
            startlineAdvantagePinEnd.startLineAdvantage = startlineAdvantageAtPinEndInMeters;
            startlineAdvantageRCBoat.startLineAdvantage = 0.0;
        } else {
            startlineAdvantagePinEnd.startLineAdvantage = 0.0;
            startlineAdvantageRCBoat.startLineAdvantage = Math.abs(startlineAdvantageAtPinEndInMeters);
        }
        result.add(startlineAdvantageRCBoat);
        result.add(startlineAdvantagePinEnd);
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
   

    @Override
    public void liveTrackedRaceDidChange(TrackedRace trackedRace) {
        this.currentLiveTrackedRace = trackedRace;
    }
    
//    //Dummy data generation
//    

//    
//    private List<StartLineAdvantageDTO> generateRandomAdvantagesOnStartline(){
//        List<StartLineAdvantageDTO> randomAdvantagesOnStartline = new ArrayList<StartLineAdvantageDTO>();
//        randomAdvantagesOnStartline.add(getRandomStartLineAdvantageDTOWithX(0));
//        randomAdvantagesOnStartline.add(getRandomStartLineAdvantageDTOWithX(33));
//        return randomAdvantagesOnStartline;
//    }
//    
//    private StartLineAdvantageDTO getRandomStartLineAdvantageDTOWithX(int x) {
//        StartLineAdvantageDTO result = new StartLineAdvantageDTO();
//        result.distanceToRCBoatInMeters = x;
//        result.startLineAdvantage = (x+1)/2+(0 + (int)(Math.random()*5));
//        result.confidence = 1;
//        return result;
//    }

    @Override
    public PolarDataService getPolarDataService() {
        return this.polarDataService;
    }
}
