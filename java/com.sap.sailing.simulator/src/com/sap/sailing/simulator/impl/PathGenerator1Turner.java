package com.sap.sailing.simulator.impl;

import java.util.LinkedList;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class PathGenerator1Turner extends PathGeneratorBase {

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

    private SimulationParameters simulationParameters;
    private boolean leftSide;
    private result1Turn result;
    private Position evalStartPoint;
    private TimePoint evalStartTime;
    private long evalTimeStep;
    private int evalStepMax;

    private static final int DEFAULT_STEP_MAX = 800;
    private static final long DEFAULT_TIMESTEP = 6666;

    private static final double TRESHOLD_MINIMUM_DISTANCE_METERS = 10.0;

    public PathGenerator1Turner(SimulationParameters params) {
        this.simulationParameters = params;
    }

    public void setEvaluationParameters(boolean leftSideVal, Position startPoint, TimePoint startTime, long timeStep, int stepMax) {
        this.leftSide = leftSideVal;
        this.evalStartPoint = startPoint;
        this.evalStartTime = startTime;
        this.evalTimeStep = timeStep;
        this.evalStepMax = stepMax;
    }

    public int getMiddle() {
        return this.result.middle;
    }

    public TimedPositionWithSpeed get1Turner(WindFieldGenerator windField, PolarDiagram polarDiagram, Position start, Position end, TimePoint startTime,
            boolean leftSide, int stepMax, long timeStep) {

        // System.out.println("inside get1Turner");
        // System.out.println("segment: (" + start.getLatDeg() + "," + start.getLngDeg() + ") and (" + end.getLatDeg() +
        // "," + end.getLngDeg() + ")");
        // System.out.println("segment length: " + start.getDistance(end).getMeters() + " meters");
        // System.out.println("starting at " + startTime.asMillis() + " milliseconds");

        long turnloss = polarDiagram.getTurnLoss(); // 4000;

        final Distance courseLength = start.getDistance(end);
        Bearing bearStart2End = start.getBearingGreatCircle(end);
        Position currentPosition = start;
        TimePoint currentTime = startTime;
        TimePoint nextTime;

        double[] reachTime = new double[stepMax];
        boolean targetFound;
        Bearing direction;

        double distanceToEnd = 0.0;
        double newDistance = 0.0;
        double minimumDistance = courseLength.getMeters();
        double overallMinimumDistance = courseLength.getMeters();
        int stepOfOverallMinimumDistance = stepMax;
        LinkedList<TimedPositionWithSpeed> path = null;

        LinkedList<TimedPositionWithSpeed> allminpath = null;
        SpeedWithBearing currentWind = null;
        SpeedWithBearing currSpeed = null;
        Position nextPosition = null;

        for (int step = 0; step < stepMax; step++) {

            // System.out.println("------------------------------");
            // System.out.println("step = " + step);

            currentPosition = start;
            currentTime = startTime;
            reachTime[step] = courseLength.getMeters();
            targetFound = false;
            minimumDistance = courseLength.getMeters();
            path = new LinkedList<TimedPositionWithSpeed>();
            path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, currSpeed));

            int stepLeft = 0;
            while ((stepLeft < step) && (!targetFound)) {

                // System.out.println("stepLeft = " + stepLeft + " targetFound = " + targetFound);

                currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currentWind);
                direction = polarDiagram.optimalDirectionsUpwind()[leftSide ? 0 : 1];

                currSpeed = polarDiagram.getSpeedAtBearing(direction);
                nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
                nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                newDistance = nextPosition.getDistance(end).getMeters();

                if (newDistance < minimumDistance) {
                    minimumDistance = newDistance;
                }

                currentPosition = nextPosition;
                currentTime = nextTime;
                path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, currSpeed));

                // if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
                distanceToEnd = currentPosition.getDistance(end).getMeters();

                // System.out.println("distanceToEnd = " + distanceToEnd + " meters");

                if (distanceToEnd < TRESHOLD_MINIMUM_DISTANCE_METERS) {
                    reachTime[step] = minimumDistance;
                    targetFound = true;
                    if (minimumDistance < overallMinimumDistance) {
                        overallMinimumDistance = minimumDistance;
                        stepOfOverallMinimumDistance = step;
                        allminpath = path;
                    }
                }
                stepLeft++;
            }

            currentTime = new MillisecondsTimePoint(currentTime.asMillis() + turnloss);

            int stepRight = 0;
            while ((stepRight < (stepMax - step)) && (!targetFound)) {

                // System.out.println("stepRight = " + stepLeft + " targetFound = " + targetFound);

                currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currentWind);
                direction = polarDiagram.optimalDirectionsUpwind()[leftSide ? 1 : 0];

                currSpeed = polarDiagram.getSpeedAtBearing(direction);
                nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
                nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                newDistance = nextPosition.getDistance(end).getMeters();

                if (newDistance < minimumDistance) {
                    minimumDistance = newDistance;
                }

                currentPosition = nextPosition;
                currentTime = nextTime;
                path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, currSpeed));

                // if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
                distanceToEnd = currentPosition.getDistance(end).getMeters();

                // System.out.println("distanceToEnd = " + distanceToEnd + " meters");

                if (distanceToEnd < TRESHOLD_MINIMUM_DISTANCE_METERS) {

                    Bearing bearPath2End = currentPosition.getBearingGreatCircle(end);
                    double bearDiff = bearPath2End.getDegrees() - bearStart2End.getDegrees();

                    reachTime[step] = minimumDistance * Math.signum(bearDiff);

                    if (start.getDistance(currentPosition).getMeters() > start.getDistance(end).getMeters()) {
                        targetFound = true;
                    }
                    if (minimumDistance < overallMinimumDistance) {
                        overallMinimumDistance = minimumDistance;
                        stepOfOverallMinimumDistance = step;
                        allminpath = new LinkedList<TimedPositionWithSpeed>(path);
                    }
                }
                stepRight++;
            }
        }

        PathImpl[] paths = new PathImpl[1];
        paths[0] = new PathImpl(allminpath, windField);
        this.result = new result1Turn(paths, (leftSide ? 'L' : 'R'), stepOfOverallMinimumDistance);

        return allminpath.get(stepOfOverallMinimumDistance);

    }

    @Override
    public Path getPath() {

        WindFieldGenerator windField = this.simulationParameters.getWindField();

        PolarDiagram polarDiagram = this.simulationParameters.getBoatPolarDiagram();

        Position start = (this.evalStartPoint == null) ? this.simulationParameters.getCourse().get(0) : this.evalStartPoint;

        Position end = this.simulationParameters.getCourse().get(1);

        TimePoint startTime = (this.evalStartTime == null) ? windField.getStartTime() : this.evalStartTime;

        // double reachingTolerance = (this.evalTolerance == 0) ? DEFAULT_REACHING_TOLERANCE : this.evalTolerance;

        int stepMax = (this.evalStepMax == 0) ? DEFAULT_STEP_MAX : this.evalStepMax;

        long timeStep = (this.evalTimeStep == 0) ? (windField.getTimeStep() == null ? DEFAULT_TIMESTEP : windField.getTimeStep().asMillis() / 3)
                : this.evalTimeStep;

        this.get1Turner(windField, polarDiagram, start, end, startTime, leftSide, stepMax, timeStep);

        return this.result.paths[0];
    }

    // @Override
    // public Path getPath() {
    //
    // WindFieldGenerator windField = simulationParameters.getWindField();
    // PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
    //
    // Position start;
    // if (this.evalStartPoint == null) {
    // start = simulationParameters.getCourse().get(0);
    // } else {
    // start = this.evalStartPoint;
    // }
    // Position end = simulationParameters.getCourse().get(1);
    // TimePoint startTime;
    // if (this.evalStartTime == null) {
    // startTime = windField.getStartTime();// new MillisecondsTimePoint(0);
    // } else {
    // startTime = this.evalStartTime;
    // }
    //
    // long turnloss = polarDiagram.getTurnLoss(); // 4000;
    //
    // final Distance courseLength = start.getDistance(end);
    // Bearing bearStart2End = start.getBearingGreatCircle(end);
    // Position currentPosition = start;
    // TimePoint currentTime = startTime;
    // TimePoint nextTime;
    //
    // double reachingTolerance;
    // if (this.evalTolerance == 0) {
    // reachingTolerance = DEFAULT_REACHING_TOLERANCE;
    // } else {
    // reachingTolerance = this.evalTolerance;
    // }
    // int stepMax;
    // if (this.evalStepMax == 0) {
    // stepMax = DEFAULT_STEP_MAX;
    // } else {
    // stepMax = this.evalStepMax;
    // }
    // double[] reachTime = new double[stepMax];
    // boolean targetFound;
    // long timeStep;
    // if (this.evalTimeStep == 0) {
    // timeStep = windField.getTimeStep().asMillis() / 3;
    // } else {
    // timeStep = this.evalTimeStep;
    // }
    // Bearing direction;
    //
    // double newDistance;
    // double minimumDistance = courseLength.getMeters();
    // double overallMinimumDistance = courseLength.getMeters();
    // int stepOfOverallMinimumDistance = stepMax;
    // LinkedList<TimedPositionWithSpeed> path = null;
    // // LinkedList<TimedPositionWithSpeed> prevpath = null;
    // // LinkedList<TimedPositionWithSpeed> xpath1 = null;
    // // LinkedList<TimedPositionWithSpeed> xpath2 = null;
    //
    // LinkedList<TimedPositionWithSpeed> allminpath = null;
    //
    // System.out.println("ANDU startPosition at " + start.toString());
    // System.out.println("ANDU endPosition at " + end.toString());
    // System.out.println("ANDU startTime =" + startTime.asMillis());
    //
    // for (int step = 0; step < stepMax; step++) {
    //
    // currentPosition = start;
    // currentTime = startTime;
    // reachTime[step] = courseLength.getMeters();
    // targetFound = false;
    // minimumDistance = courseLength.getMeters();
    // path = new LinkedList<TimedPositionWithSpeed>();
    // path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));
    //
    // int stepLeft = 0;
    // while ((stepLeft < step) && (!targetFound)) {
    //
    // SpeedWithBearing currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
    // //System.out.println("Wind: " + currentWind.getKnots() + "kn, " + currentWind.getBearing().getDegrees() + "deg");
    // polarDiagram.setWind(currentWind);
    // if (leftSide) {
    // direction = polarDiagram.optimalDirectionsUpwind()[0];
    // } else {
    // direction = polarDiagram.optimalDirectionsUpwind()[1];
    // }
    // SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
    // //System.out.println("Boat: " + currSpeed.getKnots() + "kn, " + currSpeed.getBearing().getDegrees() + "deg");
    // nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
    // Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
    // //System.out.println("Dist: " + currentPosition.getDistance(nextPosition).getMeters() + "m");
    // newDistance = nextPosition.getDistance(end).getMeters();
    // if (newDistance < minimumDistance) {
    // minimumDistance = newDistance;
    // }
    // currentPosition = nextPosition;
    // currentTime = nextTime;
    // path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));
    //
    // if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
    // reachTime[step] = minimumDistance;
    // targetFound = true;
    // if (minimumDistance < overallMinimumDistance) {
    // overallMinimumDistance = minimumDistance;
    // stepOfOverallMinimumDistance = step;
    // allminpath = path;
    // }
    // }
    // stepLeft++;
    // }
    //
    // currentTime = new MillisecondsTimePoint(currentTime.asMillis() + turnloss);
    //
    // int stepRight = 0;
    // while ((stepRight < (stepMax - step)) && (!targetFound)) {
    //
    // SpeedWithBearing currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
    // polarDiagram.setWind(currentWind);
    // if (leftSide) {
    // direction = polarDiagram.optimalDirectionsUpwind()[1];
    // } else {
    // direction = polarDiagram.optimalDirectionsUpwind()[0];
    // }
    // SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
    // nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
    // Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
    // newDistance = nextPosition.getDistance(end).getMeters();
    // /*if (this.evalStartPoint != null) {
    // System.out.println("newDistance: "+newDistance);
    // }*/
    // if (newDistance < minimumDistance) {
    // minimumDistance = newDistance;
    // }
    // currentPosition = nextPosition;
    // currentTime = nextTime;
    // path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null)); // currSpeed));
    // /*if (this.evalStartPoint != null) {
    // System.out.println("s: "+step+" dist: "+currentPosition.getDistance(end).getMeters()+" ?<? "+reachingTolerance *
    // courseLength.getMeters());
    // }*/
    // if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
    // // System.out.println(""+s+":"+path.size()+" dist:"+mindist);
    // Bearing bearPath2End = currentPosition.getBearingGreatCircle(end);
    // double bearDiff = bearPath2End.getDegrees() - bearStart2End.getDegrees();
    // // System.out.println(""+s+": "+mindist+" bearDiff: "+bearDiff);
    // reachTime[step] = minimumDistance * Math.signum(bearDiff);
    // /*if ((prevpath != null) && (Math.signum(reachTime[step]) != Math.signum(reachTime[step - 1]))) {
    // xpath1 = path;
    // xpath2 = prevpath;
    // }
    // prevpath = path;*/
    // if (start.getDistance(currentPosition).getMeters() > start.getDistance(end).getMeters()) {
    // targetFound = true;
    // }
    // if (minimumDistance < overallMinimumDistance) {
    // overallMinimumDistance = minimumDistance;
    // stepOfOverallMinimumDistance = step;
    // allminpath = new LinkedList<TimedPositionWithSpeed>(path);
    // }
    // }
    // stepRight++;
    // }
    // }
    //
    // System.out.println("ANDU stepOfOverallMinimumDistance = " + stepOfOverallMinimumDistance);
    // System.out.println("ANDU allminpath is " + (allminpath == null ? "" : "not") + "null");
    // if (allminpath != null) {
    // System.out.println("ANDU allMinPath.size() =" + allminpath.size());
    // }
    // System.out.println("ANDU path is " + (path == null ? "" : "not") + "null");
    // if (allminpath != null) {
    // System.out.println("ANDU path.size() =" + path.size());
    // }
    // /*
    // * for (int i=0; i<reachTime.length; i++) { System.out.println(""+i+": "+reachTime[i]); }
    // */
    //
    // // PathImpl[] paths = new PathImpl[2];
    // // paths[0] = new PathImpl(xpath1,windField);
    // // paths[1] = new PathImpl(xpath2, windField);
    // PathImpl[] paths = new PathImpl[1];
    // paths[0] = new PathImpl(allminpath, windField);
    // char side;
    // if (leftSide) {
    // side = 'L';
    // } else {
    // side = 'R';
    // }
    // this.result = new result1Turn(paths, side, stepOfOverallMinimumDistance);
    //
    // return result.paths[0];
    //
    // }
}
