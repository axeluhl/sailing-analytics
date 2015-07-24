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
            Pair<Double, Double> polarBasedStartlineAdvatagesRange = getStartAndEndPointOfPolarBasedStartlineAdvatagesInDistancesToRCBoat(
                    intersectionOfRightLaylineAndStartline, intersectionOfleftLaylineAndStartline);
            Pair<Double, Double> pinEndStartlineAdvatagesRange = getPinEndStartlineAdvantagesRangeFromPolarAdvantagesRange(polarBasedStartlineAdvatagesRange);
            result.advantages = calculateStartlineAdvantages(polarBasedStartlineAdvatagesRange, pinEndStartlineAdvatagesRange);
        } else {
            logger.log(Level.INFO, "No live race available for startlineadvantages calculation");
        }
//        double maximum = getMaximumAdvantageOfStartlineAdvantageDTOs(result.advantages);
//        result.maximum = maximum;
//        advantageMaximumAverage.add(maximum);
//        result.average = advantageMaximumAverage.getAverage();
        return result;
    }
    
    private Pair<Double, Double> getStartAndEndPointOfPolarBasedStartlineAdvatagesInDistancesToRCBoat(Position rightIntersection, Position leftIntersection) {
        Pair<Double, Double> result = null;
        if(rightIntersection != null && leftIntersection == null) {
            double distanceFromIntersectionToRCBoatInMeters = rightIntersection.getDistance(startlineAndFirstMarkPositions.startBoatPosition).getMeters();
            result = new Pair<Double, Double>(0.0, distanceFromIntersectionToRCBoatInMeters);
        } else if(rightIntersection == null && leftIntersection != null) {
            double distanceFromIntersectionToRCBoatInMeters = leftIntersection.getDistance(startlineAndFirstMarkPositions.startBoatPosition).getMeters();
            result = new Pair<Double, Double>(distanceFromIntersectionToRCBoatInMeters, startlineLenghtInMeters);
        } else if(rightIntersection != null && leftIntersection != null) {
            result = new Pair<Double, Double>(0.0, startlineLenghtInMeters);
        }
        return result;
    }
    
    private Position getIntersectionOfRightLaylineAndStartline () {
        Position result = null;
        Bearing bearingOfRightLaylineInDeg = new DegreeBearingImpl(wind.getBearing().getDegrees() - meouvreAngle / 2);
        result = calculateIntersectionPointsOfStartlineAndLaylineWithBearing(bearingOfRightLaylineInDeg);
        return result;
    }
    
    private Position getIntersectionOfLeftLaylineAndStartline () {
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

    private List<StartLineAdvantageDTO> calculateStartlineAdvantages(Pair<Double, Double> rangePolarBasedStartlineAdvatages, Pair<Double, Double> rangePinEndStartlineAdvantage) {
        List<StartLineAdvantageDTO> result = new ArrayList<StartLineAdvantageDTO>();
        List<StartLineAdvantageDTO> startlineAdvantagesUnderneathLayline = calculateStartlineAdvantagesUnderneathLaylinesInRange(rangePinEndStartlineAdvantage);
        result.addAll(startlineAdvantagesUnderneathLayline);
        return result;
    }
    
    private Pair<Double, Double> getPinEndStartlineAdvantagesRangeFromPolarAdvantagesRange(Pair<Double, Double> rangePolarBasedStartlineAdvatages) {
        Pair<Double, Double> result = null;
        double pinEndStartlineAdvantagesStart;
        double pinEndStartlineAdvantagesEnd;
        if(rangePolarBasedStartlineAdvatages.getA().doubleValue() == 0.0 && rangePolarBasedStartlineAdvatages.getB().doubleValue() != startlineLenghtInMeters) {
            pinEndStartlineAdvantagesStart = rangePolarBasedStartlineAdvatages.getB().doubleValue();
            pinEndStartlineAdvantagesEnd = startlineLenghtInMeters;
            result = new Pair<Double, Double>(pinEndStartlineAdvantagesStart, pinEndStartlineAdvantagesEnd);
        } else if (rangePolarBasedStartlineAdvatages.getA().doubleValue() != 0.0 && rangePolarBasedStartlineAdvatages.getB().doubleValue() == startlineLenghtInMeters) {
            pinEndStartlineAdvantagesStart = 0.0;
            pinEndStartlineAdvantagesEnd = rangePolarBasedStartlineAdvatages.getB().doubleValue();
            result = new Pair<Double, Double>(pinEndStartlineAdvantagesStart, pinEndStartlineAdvantagesEnd);
        }
        return result;
    }
    
    private List<StartLineAdvantageDTO> calculateStartlineAdvantagesUnderneathLaylinesInRange(Pair<Double, Double> rangePinEndStartlineAdvantage) {
        List<StartLineAdvantageDTO> result = new ArrayList<StartLineAdvantageDTO>();
        if(rangePinEndStartlineAdvantage != null) {
        StartLineAdvantageDTO rightEdgeAdvantage = new StartLineAdvantageDTO();
        rightEdgeAdvantage.confidence = 1.0;
        rightEdgeAdvantage.distanceToRCBoatInMeters = rangePinEndStartlineAdvantage.getA();
        StartLineAdvantageDTO leftEdgeAdvantage = new StartLineAdvantageDTO();
        leftEdgeAdvantage.confidence = 1.0;
        leftEdgeAdvantage.distanceToRCBoatInMeters = rangePinEndStartlineAdvantage.getB();
        if(startlineAdvantageAtPinEndInMeters > 0) {
            leftEdgeAdvantage.startLineAdvantage = startlineAdvantageAtPinEndInMeters;
            rightEdgeAdvantage.startLineAdvantage = 0.0;
        } else {
            leftEdgeAdvantage.startLineAdvantage = 0.0;
            rightEdgeAdvantage.startLineAdvantage = Math.abs(startlineAdvantageAtPinEndInMeters);
        }
        result.add(rightEdgeAdvantage);
        result.add(leftEdgeAdvantage);
        }
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
