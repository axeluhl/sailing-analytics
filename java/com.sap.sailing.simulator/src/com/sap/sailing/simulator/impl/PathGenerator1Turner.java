package com.sap.sailing.simulator.impl;

import java.util.LinkedList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindFieldGenerator;

public class PathGenerator1Turner implements PathGenerator {

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
    
    public PathGenerator1Turner(SimulationParameters params) {
        simulationParameters = params;
    }

    @Override
    public void setSimulationParameters(SimulationParameters params) {
        simulationParameters = params;
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return simulationParameters;
    }

    public void setEvaluationParameters(boolean leftSideVal) {
        this.leftSide = leftSideVal;
    }

    public int getMiddle() {
        return this.result.middle;
    }

    @Override
    public Path getPath() {

        WindFieldGenerator windField = simulationParameters.getWindField();
        PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = windField.getStartTime();// new MillisecondsTimePoint(0);

        long turnloss = polarDiagram.getTurnLoss(); // 4000;

        Distance courseLength = start.getDistance(end);
        Bearing bearStart2End = start.getBearingGreatCircle(end);
        Position currentPosition = start;
        TimePoint currentTime = startTime;
        TimePoint nextTime;

        double reachingTolerance = 0.03;
        int stepMax = 800;
        double[] reachTime = new double[stepMax];
        boolean targetFound;
        long timeStep = windField.getTimeStep().asMillis() / 5;
        Bearing direction;

        double newDistance;
        double minimumDistance = courseLength.getMeters();
        double overallMinimumDistance = courseLength.getMeters();
        int stepOfOverallMinimumDistance = stepMax;
        LinkedList<TimedPositionWithSpeed> path = null;
        // LinkedList<TimedPositionWithSpeed> prevpath = null;
        // LinkedList<TimedPositionWithSpeed> xpath1 = null;
        // LinkedList<TimedPositionWithSpeed> xpath2 = null;

        LinkedList<TimedPositionWithSpeed> allminpath = null;

        for (int step = 0; step < stepMax; step++) {

            currentPosition = start;
            currentTime = startTime;
            reachTime[step] = courseLength.getMeters();
            targetFound = false;
            minimumDistance = courseLength.getMeters();
            path = new LinkedList<TimedPositionWithSpeed>();
            path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));

            int stepLeft = 0;
            while ((stepLeft < step) && (!targetFound)) {

                SpeedWithBearing currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currentWind);
                if (leftSide) {
                    direction = polarDiagram.optimalDirectionsUpwind()[0];
                } else {
                    direction = polarDiagram.optimalDirectionsUpwind()[1];
                }
                SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
                nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
                Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                newDistance = nextPosition.getDistance(end).getMeters();
                if (newDistance < minimumDistance) {
                    minimumDistance = newDistance;
                }
                currentPosition = nextPosition;
                currentTime = nextTime;
                path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));

                if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
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

                SpeedWithBearing currentWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currentWind);
                if (leftSide) {
                    direction = polarDiagram.optimalDirectionsUpwind()[1];
                } else {
                    direction = polarDiagram.optimalDirectionsUpwind()[0];
                }
                SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
                nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeStep);
                Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                newDistance = nextPosition.getDistance(end).getMeters();
                if (newDistance < minimumDistance) {
                    minimumDistance = newDistance;
                }
                currentPosition = nextPosition;
                currentTime = nextTime;
                path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null)); // currSpeed));

                if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
                    // System.out.println(""+s+":"+path.size()+" dist:"+mindist);
                    Bearing bearPath2End = currentPosition.getBearingGreatCircle(end);
                    double bearDiff = bearPath2End.getDegrees() - bearStart2End.getDegrees();
                    // System.out.println(""+s+": "+mindist+" bearDiff: "+bearDiff);
                    reachTime[step] = minimumDistance * Math.signum(bearDiff);
                    /*if ((prevpath != null) && (Math.signum(reachTime[step]) != Math.signum(reachTime[step - 1]))) {
                        xpath1 = path;
                        xpath2 = prevpath;
                    }
                    prevpath = path;*/
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

        /*
         * for (int i=0; i<reachTime.length; i++) { System.out.println(""+i+": "+reachTime[i]); }
         */

        // PathImpl[] paths = new PathImpl[2];
        // paths[0] = new PathImpl(xpath1,windField);
        // paths[1] = new PathImpl(xpath2, windField);
        PathImpl[] paths = new PathImpl[1];
        paths[0] = new PathImpl(allminpath, windField);
        char side;
        if (leftSide) {
            side = 'L';
        } else {
            side = 'R';
        }
        this.result = new result1Turn(paths, side, stepOfOverallMinimumDistance);

        return result.paths[0];

    }

    @Override
    public List<TimedPositionWithSpeed> getPathEvenTimed(long millisecondsStep) {

        Path path = this.getPath();
        return path.getEvenTimedPoints(millisecondsStep);

    }

}
