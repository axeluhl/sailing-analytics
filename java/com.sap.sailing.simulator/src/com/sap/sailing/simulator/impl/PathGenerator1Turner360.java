package com.sap.sailing.simulator.impl;

import java.util.LinkedList;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.simulator.BoatDirection;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PointOfSail;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class PathGenerator1Turner360 extends PathGeneratorBase {

    class result1Turn {
        public result1Turn(Path[] p, char s, int n) {
            paths = p;
            side = s;
            middle = n;
        }

        Path[] paths;
        char side;
        int middle;
    }

    // private static Logger logger = Logger.getLogger("com.sap.sailing");
    private SimulationParameters simulationParameters;
    private boolean leftSide;
    private result1Turn result;
    private Position evalStartPoint;
    private Position evalEndPoint;
    private TimePoint evalStartTime;
    private long evalTimeStep;
    private int evalStepMax;
    private double evalTolerance;

    public PathGenerator1Turner360(SimulationParameters params) {
        simulationParameters = params;
    }

    public void setEvaluationParameters(boolean leftSideVal, Position startPoint, Position endPoint,
            TimePoint startTime, long timeStep, int stepMax, double tolerance, boolean upwindLeg) {
        this.leftSide = leftSideVal;
        this.evalStartPoint = startPoint;
        this.evalEndPoint = endPoint;
        this.evalStartTime = startTime;
        this.evalTimeStep = timeStep;
        this.evalStepMax = stepMax;
        this.evalTolerance = tolerance;
    }

    public int getMiddle() {
        return this.result.middle;
    }

    @Override
    public Path getPath() {
        this.algorithmStartTime = MillisecondsTimePoint.now();

        WindFieldGenerator windField = simulationParameters.getWindField();
        PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();

        Position posStart;
        if (this.evalStartPoint == null) {
            posStart = simulationParameters.getCourse().get(0);
        } else {
            posStart = this.evalStartPoint;
        }
        Position posEnd;
        if (this.evalEndPoint == null) {
            posEnd = simulationParameters.getCourse().get(1);
        } else {
            posEnd = this.evalEndPoint;
        }

        TimePoint startTime;
        if (this.evalStartTime == null) {
            startTime = windField.getStartTime();// new MillisecondsTimePoint(0);
        } else {
            startTime = this.evalStartTime;
        }

        long turnloss = polarDiagram.getTurnLoss(); // 4000;

        Distance courseLength = posStart.getDistance(posEnd);
        Bearing bearStart2End = posStart.getBearingGreatCircle(posEnd);
        Position currentPosition = posStart;
        TimePoint currentTime = startTime;
        TimePoint nextTime;

        double reachingTolerance;
        if (this.evalTolerance == 0) {
            reachingTolerance = 0.03;
        } else {
            reachingTolerance = this.evalTolerance;
        }
        int stepMax;
        if (this.evalStepMax == 0) {
            stepMax = 800;
        } else {
            stepMax = this.evalStepMax;
        }
        double[] reachTime = new double[stepMax];
        boolean targetFound;
        long timeStep;
        if (this.evalTimeStep == 0) {
            timeStep = windField.getTimeStep().asMillis() / 3;
        } else {
            timeStep = this.evalTimeStep;
        }
        Bearing direction;

        double newDistance;
        double minimumDistance = courseLength.getMeters();
        //double overallMinimumDistance = courseLength.getMeters();
        //int stepOfOverallMinimumDistance = stepMax;
        LinkedList<TimedPositionWithSpeed> path = null;
        LinkedList<TimedPositionWithSpeed> allminpath = null;
        TimePoint minimumTime = startTime.plus(24*60*60*1000);
        for (int step = 0; step < stepMax; step++) {

            currentPosition = posStart;
            currentTime = startTime;
            reachTime[step] = courseLength.getMeters();
            targetFound = false;
            minimumDistance = courseLength.getMeters();
            path = new LinkedList<TimedPositionWithSpeed>();
            path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));

            if (this.isTimedOut()) {
                break;
            }

            int stepLeft = 0;
            PointOfSail prevPointOfSail = PointOfSail.TACKING;
            while ((stepLeft < step) && (!targetFound) && (!this.isTimedOut())) {
                // get bearing to target            
                Bearing bearTarget = currentPosition.getBearingGreatCircle(posEnd);
                // set wind at current position
                Wind currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currentWind);
                // get point-of-sail and reaching-side
                Util.Pair<PointOfSail, BoatDirection> pointOfSailAndReachingSide = polarDiagram.getPointOfSail(bearTarget);
                PointOfSail pointOfSail = pointOfSailAndReachingSide.getA();
                BoatDirection reachingSide = pointOfSailAndReachingSide.getB();
                if (reachingSide == (leftSide?BoatDirection.REACH_RIGHT:BoatDirection.REACH_LEFT)) {
                    pointOfSail = prevPointOfSail;
                }
                if ((pointOfSail == PointOfSail.TACKING) || (pointOfSail == PointOfSail.JIBING) || (reachingSide == (leftSide?BoatDirection.REACH_LEFT:BoatDirection.REACH_RIGHT))) {
                    if (pointOfSail == PointOfSail.TACKING) {
                        if (leftSide) {
                            direction = polarDiagram.optimalDirectionsUpwind()[0];
                        } else {
                            direction = polarDiagram.optimalDirectionsUpwind()[1];
                        }
                        prevPointOfSail = pointOfSail;
                    } else if (pointOfSail == PointOfSail.JIBING) {
                        if (leftSide) {
                            direction = polarDiagram.optimalDirectionsDownwind()[1];
                        } else {
                            direction = polarDiagram.optimalDirectionsDownwind()[0];
                        }
                        prevPointOfSail = pointOfSail;
                    } else {
                        direction = bearTarget;                                                
                    }
                    SpeedWithBearing currSpeed;
                    if ((pointOfSail != PointOfSail.REACHING) || !polarDiagram.hasCurrent()) {
                        currSpeed = polarDiagram.getSpeedAtBearing(direction);
                    } else {
                        currSpeed = polarDiagram.getSpeedAtBearingOverGround(direction);
                    }
                    nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
                    Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                    newDistance = nextPosition.getDistance(posEnd).getMeters();
                    if (newDistance < minimumDistance) {
                        minimumDistance = newDistance;
                    }
                    path.addLast(new TimedPositionWithSpeedImpl(nextTime, nextPosition, currentWind));
                    currentPosition = nextPosition;
                    currentTime = nextTime;
                }                
                if (currentPosition.getDistance(posEnd).getMeters() < reachingTolerance * courseLength.getMeters()) {
                    reachTime[step] = minimumDistance;
                    targetFound = true;
                }
                //if (minimumDistance < overallMinimumDistance) {
                if (targetFound&&(currentTime.before(minimumTime))) {
                    //overallMinimumDistance = minimumDistance;
                    minimumTime = currentTime;
                    //stepOfOverallMinimumDistance = step;
                    allminpath = path;
                }
                stepLeft++;
            }

            if (step > 0) {
                currentTime = new MillisecondsTimePoint(currentTime.asMillis() + turnloss);
            }
            
            int stepRight = 0;
            while ((stepRight < (stepMax - step)) && (!targetFound) && (!this.isTimedOut())) {
                // get bearing to target
                Bearing bearTarget = currentPosition.getBearingGreatCircle(posEnd);
                // set wind at current position
                SpeedWithBearing currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currentWind);
                // get point-of-sail and reaching-side
                Pair<PointOfSail, BoatDirection> pointOfSailAndReachingSide = polarDiagram.getPointOfSail(bearTarget);
                PointOfSail pointOfSail = pointOfSailAndReachingSide.getA();
                BoatDirection reachingSide = pointOfSailAndReachingSide.getB();
                if ((pointOfSail == PointOfSail.TACKING) || (pointOfSail == PointOfSail.JIBING) || (reachingSide == (leftSide?BoatDirection.REACH_RIGHT:BoatDirection.REACH_LEFT))) {
                    if (pointOfSail == PointOfSail.TACKING) {
                        if (leftSide) {
                            direction = polarDiagram.optimalDirectionsUpwind()[1];
                        } else {
                            direction = polarDiagram.optimalDirectionsUpwind()[0];
                        }
                    } else if (pointOfSail == PointOfSail.JIBING) {
                        if (leftSide) {
                            direction = polarDiagram.optimalDirectionsDownwind()[0];
                        } else {
                            direction = polarDiagram.optimalDirectionsDownwind()[1];
                        }
                    } else {
                        direction = bearTarget;                        
                    }
                    SpeedWithBearing currSpeed;
                    if ((pointOfSail != PointOfSail.REACHING) || !polarDiagram.hasCurrent()) {
                        currSpeed = polarDiagram.getSpeedAtBearing(direction);
                    } else {
                        currSpeed = polarDiagram.getSpeedAtBearingOverGround(direction);
                    }
                    nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
                    Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                    newDistance = nextPosition.getDistance(posEnd).getMeters();
                    if (newDistance < minimumDistance) {
                        minimumDistance = newDistance;
                    }
                    path.addLast(new TimedPositionWithSpeedImpl(nextTime, nextPosition, currentWind));
                    currentPosition = nextPosition;
                    currentTime = nextTime;
                }
                if (currentPosition.getDistance(posEnd).getMeters() < reachingTolerance * courseLength.getMeters()) {
                    Bearing bearPath2End = currentPosition.getBearingGreatCircle(posEnd);
                    double bearDiff = bearPath2End.getDegrees() - bearStart2End.getDegrees();
                    reachTime[step] = minimumDistance * Math.signum(bearDiff);
                    if (posStart.getDistance(currentPosition).getMeters() > posStart.getDistance(posEnd).getMeters()) {
                        targetFound = true;
                    }
                }
                //if (minimumDistance < overallMinimumDistance) {
                if (targetFound&&(currentTime.before(minimumTime))) {
                    //overallMinimumDistance = minimumDistance;
                    minimumTime = currentTime;
                    //stepOfOverallMinimumDistance = step;
                    allminpath = new LinkedList<TimedPositionWithSpeed>(path);
                }
                stepRight++;
            }
        }

        return new PathImpl(allminpath, windField, this.algorithmTimedOut);

    }
    
}