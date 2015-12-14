package com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.precalculation.AbstracPreCalculationDataRetriever;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * @author Alexander Ries (D062114)
 *
 */
public  class StartlineAdvantagesByWindCalculator extends AbstracPreCalculationDataRetriever {

    private DashboardDispatchContext dashboardDispatchContext;
    private DefaultPolarWindAngleBoatSpeedFunction defaultPolarSpeedWindAngleFunction;
    
    private  final Logger logger = Logger.getLogger(StartlineAdvantagesByWindCalculator.class.getName());
    
    public StartlineAdvantagesByWindCalculator(DashboardDispatchContext dashboardDispatchContext){
        this.dashboardDispatchContext = dashboardDispatchContext;
        this.defaultPolarSpeedWindAngleFunction = new DefaultPolarWindAngleBoatSpeedFunction();
    }
    
    public StartlineAdvantagesWithMaxAndAverageDTO getStartLineAdvantagesAccrossLineFromTrackedRaceAtTimePoint(TrackedRace trackedRace,TimePoint timepoint) {
        StartlineAdvantagesWithMaxAndAverageDTO result = new StartlineAdvantagesWithMaxAndAverageDTO();
        if (trackedRace != null) {
            retrieveDataForCalculation(trackedRace, dashboardDispatchContext.getPolarDataService());
            if (wind != null) {
                Pair<Number[][], Number[][]> startlineAdvantagesAndConfidencesAsArray = null;
                if (isStartlineCompletelyUnderneathLaylines()) {
                    logger.log(Level.INFO, "Startline is completely underneath laylines");
                    Pair<Double, Double> advantagesRange = new Pair<Double, Double>(0.0, startlineLenghtInMeters);
                    List<StartLineAdvantageDTO> startlineAdvantages = calculateStartlineAdvantagesUnderneathLaylinesInRange(advantagesRange);
                    double maximum = getMaximumAdvantageOfStartlineAdvantageDTOs(startlineAdvantages);
                    result.maximum = maximum;
                    startlineAdvantagesAndConfidencesAsArray = convertStartLineAdvantageDTOListToPointAndConfidenceArrays(startlineAdvantages);
                } else if (isStartlineCompletelyAboveLaylines()) {
                    logger.log(Level.INFO, "Startline is completely above laylines");
                    Pair<Double, Double> advantagesRange = new Pair<Double, Double>(0.0, startlineLenghtInMeters);
                    List<StartLineAdvantageDTO> startlineAdvantages = calculatePolarBasedStartlineAdvantagesInRange(advantagesRange);
                    subtractMinimumOfAllStartlineAdvantages(startlineAdvantages);
                    double maximum = getMaximumAdvantageOfStartlineAdvantageDTOs(startlineAdvantages);
                    result.maximum = maximum;
                    subtractAgainstMaximumOfAllStartlineAdvantages(startlineAdvantages, maximum);
                    startlineAdvantagesAndConfidencesAsArray = convertStartLineAdvantageDTOListToPointAndConfidenceArrays(startlineAdvantages);
                } else {
                    logger.log(Level.INFO, "Layline(s) cross startline");
                    Position intersectionOfRightLaylineAndStartline = getIntersectionOfRightLaylineAndStartline();
                    Position intersectionOfleftLaylineAndStartline = getIntersectionOfLeftLaylineAndStartline();
                    Pair<Double, Double> polarBasedStartlineAdvatagesRange = getStartAndEndPointOfPolarBasedStartlineAdvatagesInDistancesToRCBoat(
                            intersectionOfRightLaylineAndStartline, intersectionOfleftLaylineAndStartline);
                    Pair<Double, Double> pinEndStartlineAdvatagesRange = getPinEndStartlineAdvantagesRangeFromPolarAdvantagesRange(polarBasedStartlineAdvatagesRange);
                    List<StartLineAdvantageDTO> startlineAdvantages = calculatePolarBasedStartlineAdvantagesInRange(polarBasedStartlineAdvatagesRange);
                    subtractMinimumOfAllStartlineAdvantages(startlineAdvantages);
                    double maximum = getMaximumAdvantageOfStartlineAdvantageDTOs(startlineAdvantages);
                    result.maximum = maximum;
                    subtractAgainstMaximumOfAllStartlineAdvantages(startlineAdvantages, maximum);
                    addClosingZeroPointToMixedAdvantages(startlineAdvantages, pinEndStartlineAdvatagesRange);
                    startlineAdvantagesAndConfidencesAsArray = convertStartLineAdvantageDTOListToPointAndConfidenceArrays(startlineAdvantages);
                }
                if (startlineAdvantagesAndConfidencesAsArray != null) {
                    result.distanceToRCBoatToStartlineAdvantage = startlineAdvantagesAndConfidencesAsArray.getA();
                    result.distanceToRCBoatToConfidence = startlineAdvantagesAndConfidencesAsArray.getB();
                }
            }
        } else {
            logger.log(Level.INFO, "No live race available for startlineadvantages calculation");
        }
        return result;
    }
    
    private boolean isBearingAboveAdvantageLines(Bearing bearing){
        boolean result = false;
        Bearing bearingOfRightLaylineInDeg = new DegreeBearingImpl(wind.getBearing().getDegrees() - meouvreAngle / 2);
        Bearing bearingOfLeftLaylineInDeg = new DegreeBearingImpl(wind.getBearing().getDegrees() + meouvreAngle / 2);
        if (bearing.getDegrees() < bearingOfRightLaylineInDeg.getDegrees() && bearing.getDegrees() > 0 || 
            bearing.getDegrees() > bearingOfLeftLaylineInDeg.getDegrees() && bearing.getDegrees() < 360) {
            result = true;
        }
        return result;
    }
    
    private  boolean isBearingUnderneathAdvantageLines(Bearing bearing){
        boolean result = false;
        Bearing bearingOfRightLaylineInDeg = new DegreeBearingImpl(wind.getBearing().getDegrees() - meouvreAngle / 2);
        Bearing bearingOfLeftLaylineInDeg = new DegreeBearingImpl(wind.getBearing().getDegrees() + meouvreAngle / 2);
        logger.log(Level.INFO, "Underneath bearingOfRightLaylineInDeg?"+bearingOfRightLaylineInDeg);
        logger.log(Level.INFO, "Underneath bearingOfLeftLaylineInDeg?"+bearingOfLeftLaylineInDeg);
        if (bearing.getDegrees() > bearingOfRightLaylineInDeg.getDegrees() && bearing.getDegrees() < bearingOfLeftLaylineInDeg.getDegrees()) {
            result = true;
        }
        return result;
    }
    
    private boolean isStartlineCompletelyAboveLaylines() {
        boolean result = false;
        Bearing bearingRCBoatToFirstMark = new DegreeBearingImpl(
                startlineAndFirstMarkPositions.firstMarkPosition.getBearingGreatCircle(
                        startlineAndFirstMarkPositions.startBoatPosition).getDegrees());
        Bearing bearingPinEndToFirstMark = new DegreeBearingImpl(
                startlineAndFirstMarkPositions.firstMarkPosition.getBearingGreatCircle(
                        startlineAndFirstMarkPositions.pinEndPosition).getDegrees());
        if (isBearingAboveAdvantageLines(bearingRCBoatToFirstMark) && isBearingAboveAdvantageLines(bearingPinEndToFirstMark)) {
            result = true;
        }
        return result;
    }
    
    private boolean isStartlineCompletelyUnderneathLaylines() {
        boolean result = false;
        Bearing bearingFirstMarkToRCBoat = new DegreeBearingImpl(
                startlineAndFirstMarkPositions.firstMarkPosition.getBearingGreatCircle(
                        startlineAndFirstMarkPositions.startBoatPosition).getDegrees());
        Bearing bearingFirstMarkToPinEnd = new DegreeBearingImpl(
                startlineAndFirstMarkPositions.firstMarkPosition.getBearingGreatCircle(
                        startlineAndFirstMarkPositions.pinEndPosition).getDegrees());
        logger.log(Level.INFO, "Underneath RC?"+bearingFirstMarkToRCBoat);
        logger.log(Level.INFO, "Underneath PIN?"+bearingFirstMarkToPinEnd);
        if (isBearingUnderneathAdvantageLines(bearingFirstMarkToRCBoat) && isBearingUnderneathAdvantageLines(bearingFirstMarkToPinEnd)) {
            result = true;
        }
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

    private List<StartLineAdvantageDTO> addClosingZeroPointToMixedAdvantages(List<StartLineAdvantageDTO> advantages, Pair<Double, Double> rangePinEndStartlineAdvantage) {
        StartLineAdvantageDTO startLineAdvantageDTO = new StartLineAdvantageDTO();
        startLineAdvantageDTO.confidence = 1.0;
        if(rangePinEndStartlineAdvantage.getA() > 0) {
            startLineAdvantageDTO.distanceToRCBoatInMeters = rangePinEndStartlineAdvantage.getB();
            startLineAdvantageDTO.startLineAdvantage = 0.0;
            advantages.add(startLineAdvantageDTO);
        } else {
            startLineAdvantageDTO.distanceToRCBoatInMeters = rangePinEndStartlineAdvantage.getA();
            startLineAdvantageDTO.startLineAdvantage = 0.0;
            advantages.add(0, startLineAdvantageDTO);
        }
        return advantages;
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
    
    private List<StartLineAdvantageDTO> calculateStartlineAdvantagesUnderneathLaylinesInRange(
            Pair<Double, Double> rangePinEndStartlineAdvantage) {
        List<StartLineAdvantageDTO> result = new ArrayList<StartLineAdvantageDTO>();
        if (rangePinEndStartlineAdvantage != null) {
            logger.log(Level.INFO, "PinEnd startline advantages range " + rangePinEndStartlineAdvantage.getA() + " - "+ rangePinEndStartlineAdvantage.getB());
            StartLineAdvantageDTO rightEdgeAdvantage = new StartLineAdvantageDTO();
            rightEdgeAdvantage.confidence = 1.0;
            rightEdgeAdvantage.distanceToRCBoatInMeters = rangePinEndStartlineAdvantage.getA();
            StartLineAdvantageDTO leftEdgeAdvantage = new StartLineAdvantageDTO();
            leftEdgeAdvantage.confidence = 1.0;
            leftEdgeAdvantage.distanceToRCBoatInMeters = rangePinEndStartlineAdvantage.getB();
            if (startlineAdvantageAtPinEndInMeters >= 0) {
                leftEdgeAdvantage.startLineAdvantage = startlineAdvantageAtPinEndInMeters;
                rightEdgeAdvantage.startLineAdvantage = 0.0;
            } else {
                leftEdgeAdvantage.startLineAdvantage = 0.0;
                rightEdgeAdvantage.startLineAdvantage = Math.abs(startlineAdvantageAtPinEndInMeters);
            }
            result.add(rightEdgeAdvantage);
            result.add(leftEdgeAdvantage);
        } else {
            logger.log(Level.INFO, "PinEnd startline advantages range null");
        }
        return result;
    }
    
    private Pair<Number[][], Number[][]> convertStartLineAdvantageDTOListToPointAndConfidenceArrays(List<StartLineAdvantageDTO> startLineAdvantageDTOList) {
        Pair<Number[][], Number[][]> result = null;
        if(startLineAdvantageDTOList != null && startLineAdvantageDTOList.size() > 0) {
            Number[][] distanceToRCBoatToStartlineAdvantage = new Number[startLineAdvantageDTOList.size()][2];
            Number[][] distanceToRCBoatToConfidence = new Number[startLineAdvantageDTOList.size()][2];
            for(int i = 0; i < startLineAdvantageDTOList.size(); i++) {
                StartLineAdvantageDTO startLineAdvantageDTO = startLineAdvantageDTOList.get(i);
                distanceToRCBoatToStartlineAdvantage[i][0] = startLineAdvantageDTO.distanceToRCBoatInMeters;
                distanceToRCBoatToStartlineAdvantage[i][1] = startLineAdvantageDTO.startLineAdvantage;
                distanceToRCBoatToConfidence[i][0] = startLineAdvantageDTO.distanceToRCBoatInMeters;
                distanceToRCBoatToConfidence[i][1] = startLineAdvantageDTO.confidence;
            }
            result = new Pair<Number[][], Number[][]>(distanceToRCBoatToStartlineAdvantage, distanceToRCBoatToConfidence);
        }
        return result;
    } 
    
    private List<StartLineAdvantageDTO> calculatePolarBasedStartlineAdvantagesInRange(Pair<Double, Double> rangePinEndStartlineAdvantage) {
        List<StartLineAdvantageDTO> result = new ArrayList<StartLineAdvantageDTO>();
        if (rangePinEndStartlineAdvantage != null) {
            logger.log(Level.INFO, "PolarBased startline advantages range " + rangePinEndStartlineAdvantage.getA() + " - "+ rangePinEndStartlineAdvantage.getB());
            Bearing bearingOfStartlineInRad = startlineAndFirstMarkPositions.startBoatPosition
                    .getBearingGreatCircle(startlineAndFirstMarkPositions.pinEndPosition);
            Bearing bearingOfStartlineInDeg = new DegreeBearingImpl(bearingOfStartlineInRad.getDegrees());
            for(double i = rangePinEndStartlineAdvantage.getA().doubleValue(); i < rangePinEndStartlineAdvantage.getB().doubleValue()-1 ; i++) {
                StartLineAdvantageDTO startlineAdvantage = new StartLineAdvantageDTO();
                startlineAdvantage.confidence = 0.5;
                startlineAdvantage.distanceToRCBoatInMeters = i;
                Position startingPosition = startlineAndFirstMarkPositions.startBoatPosition.translateRhumb(bearingOfStartlineInDeg, new MeterDistance(i));
                Distance startingPositionToFirstMarkDistance = startingPosition.getDistance(startlineAndFirstMarkPositions.firstMarkPosition);
                Bearing bearingOfFirstMarkToStartPositionPositionInRad = startlineAndFirstMarkPositions.firstMarkPosition
                        .getBearingGreatCircle(startingPosition);
                Bearing bearingOfFirstMarkToStartPositionPositionInDeg = new DegreeBearingImpl(bearingOfFirstMarkToStartPositionPositionInRad.getDegrees());
                double angleToWind = Math.abs(wind.getBearing().getDifferenceTo(bearingOfFirstMarkToStartPositionPositionInDeg).getDegrees());
                logger.log(Level.INFO, "angleToWind"+angleToWind);
                Speed speed = defaultPolarSpeedWindAngleFunction.getBoatSpeedForWindAngleAndSpeed(angleToWind, wind.getBeaufort());
                logger.log(Level.INFO, "Speed"+speed.getKnots());
                logger.log(Level.INFO, "startingPositionToFirstMarkDistance.getMeters()"+startingPositionToFirstMarkDistance.getMeters());
                startlineAdvantage.startLineAdvantage = Math.abs((startingPositionToFirstMarkDistance.getMeters()/speed.getMetersPerSecond())*speed.getMetersPerSecond());
                result.add(startlineAdvantage);
            }
        } else {
            logger.log(Level.INFO, "PinEnd startline advantages range null");
        }
        return result;
    }
    
    private List<StartLineAdvantageDTO> subtractAgainstMaximumOfAllStartlineAdvantages(List<StartLineAdvantageDTO> advantages, double maximum) {
        for(StartLineAdvantageDTO startLineAdvantageDTO : advantages) {
            startLineAdvantageDTO.startLineAdvantage = maximum-startLineAdvantageDTO.startLineAdvantage;
        }
        return advantages;
    }
    
    private List<StartLineAdvantageDTO> subtractMinimumOfAllStartlineAdvantages(List<StartLineAdvantageDTO> advantages) {
        double minimum = getMinimumAdvantageOfStartlineAdvantageDTOs(advantages);
        for(StartLineAdvantageDTO startLineAdvantageDTO : advantages) {
            startLineAdvantageDTO.startLineAdvantage = startLineAdvantageDTO.startLineAdvantage-minimum;
        }
        return advantages;
    }
    
    private Double getMaximumAdvantageOfStartlineAdvantageDTOs(List<StartLineAdvantageDTO> advantages) {
        Double result = null;
        List<StartLineAdvantageDTO> sortedAdvantages = new ArrayList<StartLineAdvantageDTO>();
        sortedAdvantages.addAll(advantages);
        if (sortedAdvantages != null && sortedAdvantages.size() > 0) {
            Collections.sort(sortedAdvantages, StartLineAdvantageDTO.startlineAdvantageComparatorByAdvantageDesc);
            result = new Double(sortedAdvantages.get(0).startLineAdvantage);
        }
        return result;
    }
    
    private double getMinimumAdvantageOfStartlineAdvantageDTOs(List<StartLineAdvantageDTO> advantages) {
        double result = 0;
        List<StartLineAdvantageDTO> sortedAdvantages = new ArrayList<StartLineAdvantageDTO>();
        sortedAdvantages.addAll(advantages);
        if(sortedAdvantages != null && sortedAdvantages.size() > 0) {
        Collections.sort(sortedAdvantages, StartLineAdvantageDTO.startlineAdvantageComparatorByAdvantageDesc);
        result = sortedAdvantages.get(sortedAdvantages.size()-1).startLineAdvantage;
        }
        return result;
    }
}
