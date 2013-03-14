package com.sap.sailing.simulator.impl;

import java.util.LinkedList;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.Wind;
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

    // private SimulationParameters simulationParameters;
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
        this.parameters = params;
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

        TimedPositionWithSpeed oneTurnerPoint = allminpath.get(stepOfOverallMinimumDistance);
        if (oneTurnerPoint.getSpeed() == null) {
            // System.out.println("wind data is null for the one turner point");

            currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));

            oneTurnerPoint = new TimedPositionWithSpeedImpl(oneTurnerPoint.getTimePoint(), oneTurnerPoint.getPosition(), currentWind);
        }

        return oneTurnerPoint;

    }

    @Override
    public Path getPath(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        WindFieldGenerator windField = this.parameters.getWindField();

        PolarDiagram polarDiagram = this.parameters.getBoatPolarDiagram();

        Position start = (this.evalStartPoint == null) ? this.parameters.getCourse().get(0) : this.evalStartPoint;

        Position end = this.parameters.getCourse().get(1);

        TimePoint startTime = (this.evalStartTime == null) ? windField.getStartTime() : this.evalStartTime;

        int stepMax = (this.evalStepMax == 0) ? DEFAULT_STEP_MAX : this.evalStepMax;

        long timeStep = (this.evalTimeStep == 0) ? (windField.getTimeStep() == null ? DEFAULT_TIMESTEP : windField.getTimeStep().asMillis() / 3)
                : this.evalTimeStep;

        this.get1Turner(windField, polarDiagram, start, end, startTime, leftSide, stepMax, timeStep);

        return this.result.paths[0];
    }

    public TimedPositionWithSpeed getIntersectionOptimalTowardWind(WindFieldGenerator windField, PolarDiagram polarDiagram, Position edgeStart,
            Position edgeEnd, TimedPositionWithSpeed from, boolean leftSide) {

        TimePoint currentTime = from.getTimePoint();
        Position currentPosition = from.getPosition();

        Wind currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
        polarDiagram.setWind(currentWind);
        Bearing direction = polarDiagram.optimalDirectionsUpwind()[leftSide ? 0 : 1];

        SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
        TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + 1000);
        Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);

        Position intersectionPosition = this.getIntersection(currentPosition, nextPosition, edgeStart, edgeEnd);

        double distanceMeters = currentPosition.getDistance(nextPosition).getMeters();
        long timeMilliseconds = (long) ((distanceMeters / currSpeed.getMetersPerSecond()) * 1000);

        TimePoint intersectionTimePoint = new MillisecondsTimePoint(currentTime.asMillis() + timeMilliseconds);
        Wind intersectionWind = windField.getWind(new TimedPositionImpl(intersectionTimePoint, intersectionPosition));

        return new TimedPositionWithSpeedImpl(intersectionTimePoint, intersectionPosition, intersectionWind);
    }

    private Position getIntersection(Position segment1Start, Position segment1End, Position segment2Start, Position segment2End) {

        double xA = segment1Start.getLatDeg();
        double yA = segment1Start.getLngDeg();

        double xB = segment1End.getLatDeg();
        double yB = segment1End.getLngDeg();

        double xC = segment2Start.getLatDeg();
        double yC = segment2Start.getLngDeg();

        double xD = segment2End.getLatDeg();
        double yD = segment2End.getLngDeg();

        double m1 = (yB - yA) / (xB - xA);
        double m2 = (yD - yC) / (xD - xC);

        double x = (yC - yA + m1 * xA - m2 * xC) / (m1 - m2);
        double y = (yC - yA + m2 * (xA - xC)) * m1 / (m1 - m2) + yA;

        return new DegreePosition(x, y);
    }
}
