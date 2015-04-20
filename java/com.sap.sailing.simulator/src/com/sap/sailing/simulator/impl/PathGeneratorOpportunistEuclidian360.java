package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.BoatDirection;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PointOfSail;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class PathGeneratorOpportunistEuclidian360 extends PathGeneratorBase {

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    SimulationParameters simulationParameters;
    int turns;
    boolean startLeft;
    boolean upwindLeg = false;

    public PathGeneratorOpportunistEuclidian360(SimulationParameters params) {
        PolarDiagram polarDiagramClone = new PolarDiagramBase((PolarDiagramBase)params.getBoatPolarDiagram());
        simulationParameters = new SimulationParametersImpl(params.getCourse(), polarDiagramClone, params.getWindField(),
                params.getSimuStep(), params.getMode(), params.showOmniscient(), params.showOpportunist(), params.getLegType());
    }

    public void setEvaluationParameters(boolean startLeft) {
        this.startLeft = startLeft;
    }

    public int getTurns() {
        return turns;
    }
    
    public boolean isSameBaseDirection(BoatDirection direction1, BoatDirection direction2) {
        return (isBaseDirectionLeft(direction1)&&isBaseDirectionLeft(direction2))||(isBaseDirectionRight(direction1)&&isBaseDirectionRight(direction2));
    }
    
    public boolean isBaseDirectionLeft(BoatDirection direction) {
        return (direction == BoatDirection.BEAT_LEFT)||(direction == BoatDirection.JIBE_LEFT)||(direction == BoatDirection.REACH_LEFT);
    }

    public boolean isBaseDirectionRight(BoatDirection direction) {
        return (direction == BoatDirection.BEAT_RIGHT)||(direction == BoatDirection.JIBE_RIGHT)||(direction == BoatDirection.REACH_RIGHT);
    }

    
    @Override
    public Path getPath() {
        this.algorithmStartTime = MillisecondsTimePoint.now();

        WindFieldGenerator wf = simulationParameters.getWindField();
        PolarDiagram pd = simulationParameters.getBoatPolarDiagram();

        Position posStart = simulationParameters.getCourse().get(0);
        Position posEnd = simulationParameters.getCourse().get(1);

        TimePoint startTime = wf.getStartTime();
        List<TimedPositionWithSpeed> path = new ArrayList<TimedPositionWithSpeed>();

        Position currentPosition = posStart;
        TimePoint currentTime = startTime;
        double currentHeight = posStart.getDistance(posEnd).getMeters();

        BoatDirection prevDirection = BoatDirection.NONE;
        long turnLoss = pd.getTurnLoss(); // time lost when doing a turn
        double fracFinishPhase = 0.05;

        TimePoint travelTimeLeft;
        TimePoint travelTimeRight;

        Wind wndStart = wf.getWind(new TimedPositionWithSpeedImpl(startTime, posStart, null));
        logger.finest("wndStart speed:" + wndStart.getKnots() + " angle:" + wndStart.getBearing().getDegrees());
        pd.setWind(wndStart);
        Bearing bearStart = currentPosition.getBearingGreatCircle(posEnd);
        // SpeedWithBearing spdStart = pd.getSpeedAtBearing(bearStart);
        path.add(new TimedPositionWithSpeedImpl(startTime, posStart, wndStart));

        long timeStep = wf.getTimeStep().asMillis() / 2;
        logger.fine("Time step :" + timeStep);
        // while there is more than 5% of the total distance to the finish

        String legType = "none";
        if (this.simulationParameters.getLegType() == null) {
            Bearing bearRCWind = wndStart.getBearing().getDifferenceTo(bearStart);
            legType = "downwind";
            this.upwindLeg = false;
            if ((Math.abs(bearRCWind.getDegrees()) > 90.0) && (Math.abs(bearRCWind.getDegrees()) < 270.0)) {
                legType = "upwind";
                this.upwindLeg = true;
            }
        } else {
            if (this.simulationParameters.getLegType() == LegType.UPWIND) {
                legType = "upwind";
                this.upwindLeg = true;
            } else {
                legType = "downwind";
                this.upwindLeg = false;
            }
        }

        int timeStepScale = 1;
        if (!this.upwindLeg) {
            timeStepScale = 2;
            timeStep = timeStep / timeStepScale;
            turnLoss = turnLoss / timeStepScale;
        }

        logger.fine("Leg Direction: " + legType);

        //
        // StrategicPhase: start & intermediate course until close to target
        //
        turns = 0;
        while ((currentHeight > 0)
                && (currentPosition.getDistance(posEnd).compareTo(posStart.getDistance(posEnd).scale(fracFinishPhase)) > 0)
                && (path.size() < 500) && (!this.isTimedOut())) {

            long nextTimeVal = currentTime.asMillis() + timeStep;
            TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal);

            // get bearing to target            
            Bearing bearTarget = currentPosition.getBearingGreatCircle(posEnd);
            // set wind at current position
            Wind currentWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));
            logger.finest("cWind speed:" + currentWind.getKnots() + " angle:" + currentWind.getBearing().getDegrees());
            pd.setWind(currentWind);
            // get point-of-sail and reaching-side
            Pair<PointOfSail, BoatDirection> pointOfSailAndReachingSide = pd.getPointOfSail(bearTarget);
            PointOfSail pointOfSail = pointOfSailAndReachingSide.getA();
            BoatDirection reachingSide = pointOfSailAndReachingSide.getB();
            
            if ((pointOfSail == PointOfSail.TACKING)||(pointOfSail == PointOfSail.JIBING)) {
                // get optimal bearings at current position
                Bearing bearLeft;
                Bearing bearRight;
                if (pointOfSail == PointOfSail.TACKING) {
                    bearLeft = pd.optimalDirectionsUpwind()[0];
                    bearRight = pd.optimalDirectionsUpwind()[1];
                } else {
                    bearLeft = pd.optimalDirectionsDownwind()[1];
                    bearRight = pd.optimalDirectionsDownwind()[0];
                }
                // get boat speed at current position
                SpeedWithBearing boatSpeedLeft = pd.getSpeedAtBearing(bearLeft);
                SpeedWithBearing boatSpeedRight = pd.getSpeedAtBearing(bearRight);
                logger.finest("left boat speed:" + boatSpeedLeft.getKnots() + " angle:" + boatSpeedLeft.getBearing().getDegrees()
                        + "  right boat speed:" + boatSpeedRight.getKnots() + " angle:" + boatSpeedRight.getBearing().getDegrees());

                // get travel-time taking turn-loss into account
                if (isBaseDirectionLeft(prevDirection)) {
                    travelTimeLeft = new MillisecondsTimePoint(nextTimeVal);
                    travelTimeRight = new MillisecondsTimePoint(nextTimeVal - turnLoss);
                } else if (isBaseDirectionRight(prevDirection)) {
                    travelTimeLeft = new MillisecondsTimePoint(nextTimeVal - turnLoss);
                    travelTimeRight = new MillisecondsTimePoint(nextTimeVal);
                } else {
                    travelTimeLeft = new MillisecondsTimePoint(nextTimeVal);
                    travelTimeRight = new MillisecondsTimePoint(nextTimeVal);
                }
                
                // get next boat positions by travelling left and right
                Position nextBoatPositionLeft = boatSpeedLeft.travelTo(currentPosition, currentTime, travelTimeLeft);
                Position nextBoatPositionRight = boatSpeedRight.travelTo(currentPosition, currentTime, travelTimeRight);
                // calculate distance to target left and right
                Distance targetDistanceLeft = nextBoatPositionLeft.getDistance(posEnd);
                Distance targetDistanceRight = nextBoatPositionRight.getDistance(posEnd);
                double targetDistanceMetersLeft = Math.round(targetDistanceLeft.getMeters() * 1000.) / 1000.;
                double targetDistanceMetersRight = Math.round(targetDistanceRight.getMeters() * 1000.) / 1000.;

                if (prevDirection == BoatDirection.NONE) {

                    if (startLeft) {
                        if (pointOfSail == PointOfSail.TACKING) {
                            path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionLeft, currentWind));
                            currentPosition = nextBoatPositionLeft;
                            prevDirection = BoatDirection.BEAT_LEFT;
                        } else {
                            path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionRight, currentWind));
                            currentPosition = nextBoatPositionRight;
                            prevDirection = BoatDirection.JIBE_LEFT;                        
                        }
                    } else {
                        if (pointOfSail == PointOfSail.TACKING) {
                            path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionRight, currentWind));
                            currentPosition = nextBoatPositionRight;
                            prevDirection = BoatDirection.BEAT_RIGHT;
                        } else {
                            path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionLeft, currentWind));
                            currentPosition = nextBoatPositionLeft;
                            prevDirection = BoatDirection.JIBE_RIGHT;                        
                        }
                    }

                } else {

                    if (targetDistanceMetersLeft <= targetDistanceMetersRight) {
                        path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionLeft, currentWind));
                        currentPosition = nextBoatPositionLeft;
                        if (isBaseDirectionRight(prevDirection)) {
                            turns++;
                        }
                        if (pointOfSail == PointOfSail.TACKING) {
                            prevDirection = BoatDirection.BEAT_LEFT;
                        } else {
                            prevDirection = BoatDirection.JIBE_LEFT;                        
                        }
                    } else {
                        path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionRight, currentWind));
                        currentPosition = nextBoatPositionRight;
                        if (isBaseDirectionLeft(prevDirection)) {
                            turns++;
                        }
                        if (pointOfSail == PointOfSail.TACKING) {
                            prevDirection = BoatDirection.BEAT_RIGHT;
                        } else {
                            prevDirection = BoatDirection.JIBE_RIGHT;                        
                        }
                    }
                }
            // endif ((pointOfSail == PointOfSail.TACKING)||(pointOfSail == PointOfSail.JIBING))
            } else if (pointOfSail == PointOfSail.REACHING) {

                TimePoint travelTimeReach;
                // get travel-time taking turn-loss into account
                if (isSameBaseDirection(reachingSide, prevDirection)) {
                    travelTimeReach = new MillisecondsTimePoint(nextTimeVal);
                } else {
                    travelTimeReach = new MillisecondsTimePoint(nextTimeVal - turnLoss);
                }
                // get boat speed for right-side
                SpeedWithBearing boatSpeedTarget = pd.getSpeedAtBearing(bearTarget);
                // get next boat positions by travelling left and right
                Position nextBoatPositionReach = boatSpeedTarget.travelTo(currentPosition, currentTime, travelTimeReach);
                path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionReach, currentWind));
                currentPosition = nextBoatPositionReach;
                if (!isSameBaseDirection(reachingSide, prevDirection)) {
                    turns++;
                }
                
                prevDirection = reachingSide;
            }
            
            currentTime = nextTime;
            Position posHeight = currentPosition.projectToLineThrough(posStart, bearStart);
            currentHeight = posStart.getDistance(posEnd).getMeters() - posHeight.getDistance(posStart).getMeters();
        }

        if (!this.isTimedOut()) {
            //
            // FinishPhase: get 1-turners to finalize course
            //
            PathGenerator1Turner360 generator1Turner = new PathGenerator1Turner360(simulationParameters);
            TimePoint leftTurningTime;
            TimePoint rightTurningTime;
            if (isBaseDirectionLeft(prevDirection)) {
                leftTurningTime = currentTime;
                rightTurningTime = new MillisecondsTimePoint(currentTime.asMillis() + turnLoss);
            } else {
                leftTurningTime = new MillisecondsTimePoint(currentTime.asMillis() + turnLoss);
                rightTurningTime = currentTime;
            }

            generator1Turner.setEvaluationParameters(true, currentPosition, posEnd, leftTurningTime, timeStep / (5 * 3), 100, 0.05, this.upwindLeg);
            Path leftPath = generator1Turner.getPath();

            generator1Turner.setEvaluationParameters(false, currentPosition, posEnd, rightTurningTime, timeStep / (5 * 3), 100, 0.05, this.upwindLeg);
            Path rightPath = generator1Turner.getPath();

            if ((leftPath.getPathPoints() != null) && (rightPath.getPathPoints() != null)) {
                if (leftPath.getFinalTime().asMillis() <= rightPath.getFinalTime().asMillis()) {
                    path.addAll(leftPath.getPathPoints());
                } else {
                    path.addAll(rightPath.getPathPoints());
                }
            } else if (leftPath.getPathPoints() != null) {
                path.addAll(leftPath.getPathPoints());
            } else if (rightPath.getPathPoints() != null) {
                path.addAll(rightPath.getPathPoints());
            }
       }

        return new PathImpl(path, wf, getTurns(), this.algorithmTimedOut);
    }
}
