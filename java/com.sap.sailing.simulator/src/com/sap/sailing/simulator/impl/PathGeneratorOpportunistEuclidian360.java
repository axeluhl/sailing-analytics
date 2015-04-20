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
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class PathGeneratorOpportunistEuclidian360 extends PathGeneratorBase {

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    SimulationParameters simulationParameters;
    int turns;
    int maxLeft;
    int maxRight;
    MaximumTurnTimes maxTurnTimes;
    boolean startLeft;
    boolean upwindLeg = false;

    public PathGeneratorOpportunistEuclidian360(SimulationParameters params) {
        PolarDiagram polarDiagramClone = new PolarDiagramBase((PolarDiagramBase)params.getBoatPolarDiagram());
        simulationParameters = new SimulationParametersImpl(params.getCourse(), polarDiagramClone, params.getWindField(),
                params.getSimuStep(), params.getMode(), params.showOmniscient(), params.showOpportunist(), params.getLegType());
    }

    public void setEvaluationParameters(int maxLeft, int maxRight, boolean startLeft) {
        this.maxLeft = maxLeft;
        this.maxRight = maxRight;
        this.startLeft = startLeft;
    }

    public void setEvaluationParameters(MaximumTurnTimes maxTurnTimes, boolean startLeft) {
        this.maxTurnTimes = maxTurnTimes;
        this.startLeft = startLeft;
    }

    public int getTurns() {
        return turns;
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

        int stepsLeft = 0;
        int stepsRight = 0;
        boolean allLeft = true;
        boolean allRight = true;

        BoatDirection prevDirection = BoatDirection.NONE;
        long turnLoss = pd.getTurnLoss(); // time lost when doing a turn
        long windpred = 1000; // time used to predict wind, i.e. hypothetical sailors prediction
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

        if (maxTurnTimes != null) {
            if ((maxTurnTimes.left > 0) || (maxTurnTimes.right > 0)) {
                this.maxLeft = (int) Math.floor((double) maxTurnTimes.left / (double) timeStep);
                this.maxRight = (int) Math.floor((double) maxTurnTimes.right / (double) timeStep);
            }
        }
        logger.fine("Leg Direction: " + legType);

        //
        // StrategicPhase: start & intermediate course until close to target
        //
        turns = 0;
        SpeedWithBearing predBoatSpeedLeft = null;
        SpeedWithBearing predBoatSpeedRight = null;
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
            
            // compare target bearing to upwind bearings
            Bearing[] bearOptimalUpwind = pd.optimalDirectionsUpwind();
            Bearing upwindLeftRight = bearOptimalUpwind[0].getDifferenceTo(bearOptimalUpwind[1]);
            Bearing upwindLeftTarget = bearOptimalUpwind[0].getDifferenceTo(bearTarget);
            PointOfSail pointOfSail = PointOfSail.REACHING;
            BoatDirection reachingSide = BoatDirection.NONE;
            // check whether boat is in "tacking area"
            if ((upwindLeftTarget.getDegrees() >= -1) && (upwindLeftTarget.getDegrees() <= upwindLeftRight.getDegrees()+1)) {
                logger.fine("point-of-sail: tacking (diffLeftTarget: " + upwindLeftTarget.getDegrees() + ", diffLeftRight: "
                        + upwindLeftRight.getDegrees() + ", " + currentPosition + ")");
                pointOfSail = PointOfSail.TACKING;
            } else {
                Bearing[] bearOptimalDownwind = pd.optimalDirectionsDownwind();
                Bearing downwindLeftRight = bearOptimalDownwind[0].getDifferenceTo(bearOptimalDownwind[1]);
                Bearing downwindLeftTarget = bearOptimalDownwind[0].getDifferenceTo(bearTarget);
                // check whether boat is in "non-sailable area"
                if ((downwindLeftTarget.getDegrees() >= -1) && (downwindLeftTarget.getDegrees() <= downwindLeftRight.getDegrees()+1)) {
                    logger.fine("point-of-sail: jibing (diffLeftTarget: " + downwindLeftTarget.getDegrees()
                            + ", diffLeftRight: " + downwindLeftRight.getDegrees() + ", " + currentPosition + ")");
                    pointOfSail = PointOfSail.JIBING;
                } else {
                    //logger.info("path: "+path.path);
                    Bearing windBoat = currentWind.getBearing().getDifferenceTo(bearTarget);
                    if (windBoat.getDegrees() > 0) {
                        reachingSide = BoatDirection.REACH_LEFT; // left-sided reaching
                    } else {
                        reachingSide = BoatDirection.REACH_RIGHT; // right-sided reaching
                    }
                }
            }
            if ((pointOfSail == PointOfSail.TACKING)||(pointOfSail == PointOfSail.JIBING)) {
            // get optimal bearings at current position
            Bearing bearLeft;
            Bearing bearRight;
            if (pointOfSail == PointOfSail.TACKING) {
                bearLeft = pd.optimalDirectionsUpwind()[0];
                bearRight = pd.optimalDirectionsUpwind()[1];
            } else {
                bearLeft = pd.optimalDirectionsDownwind()[0];
                bearRight = pd.optimalDirectionsDownwind()[1];
            }
            // get boat speed at current position
            SpeedWithBearing boatSpeedLeft = pd.getSpeedAtBearing(bearLeft);
            SpeedWithBearing boatSpeedRight = pd.getSpeedAtBearing(bearRight);
            logger.finest("left boat speed:" + boatSpeedLeft.getKnots() + " angle:" + boatSpeedLeft.getBearing().getDegrees()
                    + "  right boat speed:" + boatSpeedRight.getKnots() + " angle:" + boatSpeedRight.getBearing().getDegrees());
            TimePoint predTime = new MillisecondsTimePoint(currentTime.asMillis() + windpred);
            // get boat test-positions after (windpred) time used to spatially predict wind
            Position predPositionLeft = boatSpeedLeft.travelTo(currentPosition, currentTime, predTime);
            Position predPositionRight = boatSpeedRight.travelTo(currentPosition, currentTime, predTime);
            logger.finest("current Pos:" + currentPosition.getLatDeg() + "," + currentPosition.getLngDeg());
            logger.finest("left    Pos:" + predPositionLeft.getLatDeg() + "," + predPositionLeft.getLngDeg());
            logger.finest("right   Pos:" + predPositionRight.getLatDeg() + "," + predPositionRight.getLngDeg());
            // get wind at left test-position
            Wind predWindLeft = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, predPositionLeft, null));
            logger.finest("lWind speed:" + predWindLeft.getKnots() + " angle:" + predWindLeft.getBearing().getDegrees());
            // set wind at left test-position
            pd.setWind(predWindLeft);
            Bearing predBearLeft;
            if (pointOfSail == PointOfSail.TACKING) {
                predBearLeft = pd.optimalDirectionsUpwind()[0];
            } else {
                predBearLeft = pd.optimalDirectionsDownwind()[0];
            }
            // get boat speed for left-side
            predBoatSpeedLeft = pd.getSpeedAtBearing(predBearLeft);
            // get wind at right test-position
            Wind predWindRight = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, predPositionRight, null));
            logger.finest("rWind speed:" + predWindRight.getKnots() + " angle:" + predWindRight.getBearing().getDegrees());
            // set wind at right test-position
            pd.setWind(predWindRight);
            Bearing predBearRight;
            if (pointOfSail == PointOfSail.TACKING) {
                predBearRight = pd.optimalDirectionsUpwind()[1];
            } else {
                predBearRight = pd.optimalDirectionsDownwind()[1];
            }
            // get boat speed for right-side
            predBoatSpeedRight = pd.getSpeedAtBearing(predBearRight);
            logger.finest("left boat speed:" + predBoatSpeedLeft.getKnots() + " angle:" + predBoatSpeedLeft.getBearing().getDegrees()
                    + "  right boat speed:" + predBoatSpeedRight.getKnots() + " angle:" + predBoatSpeedRight.getBearing().getDegrees());
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
            Position nextBoatPositionLeft = predBoatSpeedLeft.travelTo(currentPosition, currentTime, travelTimeLeft);
            Position nextBoatPositionRight = predBoatSpeedRight.travelTo(currentPosition, currentTime, travelTimeRight);
            // calculate distance to target left and right
            Distance targetDistanceLeft = nextBoatPositionLeft.getDistance(posEnd);
            Distance targetDistanceRight = nextBoatPositionRight.getDistance(posEnd);
            double targetDistanceMetersLeft = Math.round(targetDistanceLeft.getMeters() * 1000.) / 1000.;
            double targetDistanceMetersRight = Math.round(targetDistanceRight.getMeters() * 1000.) / 1000.;

            if (prevDirection == BoatDirection.NONE) {

                if (startLeft) {
                    allRight = false;
                    path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionLeft, predWindLeft));
                    currentPosition = nextBoatPositionLeft;
                    stepsLeft++;
                    if (pointOfSail == PointOfSail.TACKING) {
                        prevDirection = BoatDirection.BEAT_LEFT;
                    } else {
                        prevDirection = BoatDirection.JIBE_LEFT;                        
                    }
                } else {
                    allLeft = false;
                    path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionRight, predWindRight));
                    currentPosition = nextBoatPositionRight;
                    stepsRight++;
                    if (pointOfSail == PointOfSail.TACKING) {
                        prevDirection = BoatDirection.BEAT_RIGHT;
                    } else {
                        prevDirection = BoatDirection.JIBE_RIGHT;                        
                    }
                }

            } else {

                if (((targetDistanceMetersLeft <= targetDistanceMetersRight) && (!allLeft || (stepsLeft < maxLeft)))
                        || (allRight && (stepsRight >= maxRight))) {
                    path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionLeft, predWindLeft));
                    currentPosition = nextBoatPositionLeft;
                    if (isBaseDirectionRight(prevDirection)) {
                        allLeft = false;
                        turns++;
                    } else {
                        stepsLeft++;
                    }
                    if (pointOfSail == PointOfSail.TACKING) {
                        prevDirection = BoatDirection.BEAT_LEFT;
                    } else {
                        prevDirection = BoatDirection.JIBE_LEFT;                        
                    }
                } else {
                    // if (((drght.compareTo(dlft) < 0)&&(stepsRight < maxRight))||(stepsLeft >= maxLeft)) {
                    path.add(new TimedPositionWithSpeedImpl(nextTime, nextBoatPositionRight, predWindRight));
                    currentPosition = nextBoatPositionRight;
                    if (isBaseDirectionLeft(prevDirection)) {
                        allRight = false;
                        turns++;
                    } else {
                        stepsRight++;
                    }
                    if (pointOfSail == PointOfSail.TACKING) {
                        prevDirection = BoatDirection.BEAT_RIGHT;
                    } else {
                        prevDirection = BoatDirection.JIBE_RIGHT;                        
                    }
                }
            }
            
            }

            currentTime = nextTime;
            Position posHeight = currentPosition.projectToLineThrough(posStart, bearStart);
            currentHeight = posStart.getDistance(posEnd).getMeters() - posHeight.getDistance(posStart).getMeters();
        }

        if (!this.isTimedOut()) {
            //
            // FinishPhase: get 1-turners to finalize course
            //
            PathGenerator1Turner gen1Turner = new PathGenerator1Turner(simulationParameters);
            TimePoint leftGoingTime;
            TimePoint rightGoingTime;
            if (isBaseDirectionLeft(prevDirection)) {
                leftGoingTime = currentTime;
                rightGoingTime = new MillisecondsTimePoint(currentTime.asMillis() + turnLoss);
            } else {
                leftGoingTime = new MillisecondsTimePoint(currentTime.asMillis() + turnLoss);
                rightGoingTime = currentTime;
            }

            gen1Turner.setEvaluationParameters(true, currentPosition, posEnd, leftGoingTime, timeStep / (5 * 3), 100, 0.1,
                    this.upwindLeg);
            Path leftPath = gen1Turner.getPath();

            gen1Turner.setEvaluationParameters(false, currentPosition, posEnd, rightGoingTime, timeStep / (5 * 3), 100,
                    0.1, this.upwindLeg);
            Path rightPath = gen1Turner.getPath();

            if ((leftPath.getPathPoints() != null) && (rightPath.getPathPoints() != null)) {
                if (leftPath.getPathPoints().get(leftPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= rightPath
                        .getPathPoints().get(rightPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
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
