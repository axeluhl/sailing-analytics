package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sse.common.Util;

public class PathGeneratorTreeGrowWind extends PathGeneratorBase {

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    private boolean debugMsgOn = false;

    int maxTurns = 0;
    String initPathStr = "0";
    PathCandidate bestCand = null;

    public PathGeneratorTreeGrowWind(SimulationParameters params) {
        this.parameters = params;
    }

    public void setEvaluationParameters(String startDirection, int maxTurns) {
        if (startDirection != null) {
            this.initPathStr = "0" + startDirection;
        } else {
            this.initPathStr = "0";
        }
        this.maxTurns = maxTurns;
    }

    class PathCandidate implements Comparable<PathCandidate> {
        public PathCandidate(TimedPosition pos, double vrt, double hrz, int trn, String path) {
            this.pos = pos;
            this.vrt = vrt;
            this.hrz = hrz;
            this.trn = trn;
            this.path = path;
        }

        TimedPosition pos;
        double vrt;
        double hrz;
        int trn;
        String path;

        /*@Override
        // sort ascending by horizontal distance
        public int compareTo(PathCand other) {
            if (Math.abs(this.hrz) == Math.abs(other.hrz))
                return 0;
            return (Math.abs(this.hrz) < Math.abs(other.hrz) ? -1 : +1);
        }*/
        @Override
        // sort descending by height
        public int compareTo(PathCandidate other) {
            if (this.vrt == other.vrt) {
                if (Math.abs(this.hrz) == Math.abs(other.hrz)) {
                    return 0;
                } else {
                    return (Math.abs(this.hrz) < Math.abs(other.hrz) ? -1 : +1);
                }
            } else {
                return (this.vrt > other.vrt ? -1 : +1);
            }
        }

    }

    // getter for evaluating best path cand propoerties further
    PathCandidate getBestCand() {
        return this.bestCand;
    }


    // generate step in one of the possible directions
    // default: L - left, R - right
    // extended: M - wide left, S - wide right
    Util.Pair<TimedPosition,Wind> getStep(TimedPosition pos, long timeStep, long turnLoss, boolean sameBaseDirection, char nextDirection) {

        double offDeg = 5.0;
        WindFieldGenerator wf = this.parameters.getWindField();
        TimePoint curTime = pos.getTimePoint();
        Position curPosition = pos.getPosition();
        Wind posWind = wf.getWind(new TimedPositionWithSpeedImpl(curTime, curPosition, null));

        PolarDiagram pd = this.parameters.getBoatPolarDiagram();
        pd.setWind(posWind);
        Wind appWind = new WindImpl(posWind.getPosition(), posWind.getTimePoint(), pd.getWind());;

        // get beat-angle left and right
        Bearing travelBearing = null;
        Bearing tmpBearing = null;
        if (nextDirection == 'L') {
            travelBearing = pd.optimalDirectionsUpwind()[0];
        } else if (nextDirection == 'R') {
            travelBearing = pd.optimalDirectionsUpwind()[1];
        } else if (nextDirection == 'M') {
            tmpBearing = pd.optimalDirectionsUpwind()[0];
            travelBearing = tmpBearing.add(new DegreeBearingImpl(+offDeg));
        } else if (nextDirection == 'S') {
            tmpBearing = pd.optimalDirectionsUpwind()[1];
            travelBearing = tmpBearing.add(new DegreeBearingImpl(-offDeg));
        }

        // determine beat-speed left and right
        SpeedWithBearing travelSpeed = pd.getSpeedAtBearing(travelBearing);

        TimePoint travelTime;
        TimePoint nextTime = new MillisecondsTimePoint(curTime.asMillis()+timeStep);
        if (sameBaseDirection) {
            travelTime = nextTime;
        } else {
            travelTime = new MillisecondsTimePoint(nextTime.asMillis() - turnLoss);
        }

        return new Util.Pair<TimedPosition,Wind>(new TimedPositionImpl(nextTime, travelSpeed.travelTo(curPosition, curTime, travelTime)), appWind);
    }

    // use base direction to distinguish direction changes that do or don't require a turn
    char getBaseDirection(char direction) {
        char baseDirection = direction;

        if (direction == 'M') {
            baseDirection = 'L';
        }
        if (direction == 'S') {
            baseDirection = 'R';
        }

        return baseDirection;
    }

    // check whether nextDirection is same base direction as previous direction, i.e. no turn
    boolean isSameDirection(char prevDirection, char nextDirection) {

        char prevBaseDirection = this.getBaseDirection(prevDirection);
        char nextBaseDirection = this.getBaseDirection(nextDirection);

        return ((nextBaseDirection == prevBaseDirection)||(prevBaseDirection == '0'));
    }

    // get path candidate measuring height towards (local, current-apparent) wind
    PathCandidate getPathCandWind(PathCandidate path, char nextDirection, long timeStep, long turnLoss, Position posStart, Position posEnd, double tgtHeight) {

        char prevDirection = path.path.charAt(path.path.length()-1);
        boolean sameBaseDirection = this.isSameDirection(prevDirection, nextDirection);

        int turnCount = path.trn;
        if (!sameBaseDirection) {
            turnCount++;
        }

        // calculate next path position (taking turn-loss into account)
        Util.Pair<TimedPosition,Wind> nextStep = this.getStep(path.pos, timeStep, turnLoss, sameBaseDirection, nextDirection);
        TimedPosition pathPos = nextStep.getA();
        Wind posWind = nextStep.getB();

        // calculate height-position with reference to race course
        Position posHeight = pathPos.getPosition().projectToLineThrough(posEnd, posWind.getBearing());
        Bearing bearVrt = posStart.getBearingGreatCircle(posEnd);
        Position posHeightTrgt = pathPos.getPosition().projectToLineThrough(posStart, bearVrt);
        //Position posHeightWind = pathPos.getPosition().projectToLineThrough(posRef, posWind.getBearing());

        // calculate vertical distance as distance of height-position to start
        //double vrtDist = tgtHeight - Math.round(posHeight.getDistance(posEnd).getMeters()*100.0)/100.0;
        double vrtDist = Math.round(posHeight.getDistance(posStart).getMeters()*100.0)/100.0;

        /*if (vrtDist > tgtHeight) {
        // scale last step so that vrtDist ~ tgtHeight
        Position prevPos = path.pos.getPosition();
        TimePoint prevTime = path.pos.getTimePoint();
        double heightFrac = (tgtHeight - path.vrt) / (vrtDist - path.vrt);
        Position newPos = prevPos.translateGreatCircle(prevPos.getBearingGreatCircle(pathPos.getPosition()), prevPos.getDistance(pathPos.getPosition()).scale(heightFrac));
        TimePoint newTime = new MillisecondsTimePoint(Math.round(prevTime.asMillis() + (pathPos.getTimePoint().asMillis()-prevTime.asMillis())*heightFrac));
        pathPos = new TimedPositionImpl(newTime, newPos);
        posHeight = pathPos.getPosition().projectToLineThrough(posStart, bearVrt);
        }*/

        // calculate horizontal side: left or right in reference to race course
        double posSide = 1;
        //double posBear = posWind.getBearing().getDegrees() - posEnd.getBearingGreatCircle(pathPos.getPosition()).getDegrees();
        double posBear = posStart.getBearingGreatCircle(pathPos.getPosition()).getDegrees();
        if ((posBear < 0.0)||(posBear > 180.0)) {
            posSide = -1;
        } else if ((posBear == 0.0)||(posBear == 180.0)) {
            posSide = 0;
        }
        // calculate horizontal distance as distance of height-position to current position
        //double hrzDist = Math.round(posSide*posHeight.getDistance(pathPos.getPosition()).getMeters()*100.0)/100.0;
        double hrzDist = Math.round(posSide*posHeightTrgt.getDistance(pathPos.getPosition()).getMeters()*100.0)/100.0;

        //System.out.println(""+hrzDist+", "+vrtDist+", "+pathPos.getPosition().getLatDeg()+", "+pathPos.getPosition().getLngDeg()+", "+posHeight.getLatDeg()+", "+posHeight.getLngDeg());

        // extend path-string by step-direction
        String pathStr = path.path + nextDirection;

        return (new PathCandidate(pathPos, vrtDist, hrzDist, turnCount, pathStr));
    }


    // generate path candidates based on beat angles
    List<PathCandidate> getPathCandsBeatWind(PathCandidate path, long timeStep, long turnLoss, Position posStart, Position posEnd, double tgtHeight) {

        List<PathCandidate> result = new ArrayList<PathCandidate>();
        PathCandidate newPathCand;

        if (this.maxTurns > 0) {

            char prevDirection = path.path.charAt(path.path.length()-1);
            if ((path.trn < this.maxTurns)||(this.isSameDirection(prevDirection, 'L'))) {
                newPathCand = getPathCandWind(path, 'L', timeStep, turnLoss, posStart, posEnd, tgtHeight);
                result.add(newPathCand);
            }

            if ((path.trn < this.maxTurns)||(this.isSameDirection(prevDirection, 'R'))) {
                newPathCand = getPathCandWind(path, 'R', timeStep, turnLoss, posStart, posEnd, tgtHeight);
                result.add(newPathCand);
            }

        } else {

            // step left
            newPathCand = getPathCandWind(path, 'L', timeStep, turnLoss, posStart, posEnd, tgtHeight);
            result.add(newPathCand);

            // step wide left
            //newPathCand = getPathCandWind(path, 'M', timeStep, turnLoss, posStart, posEnd, tgtHeight);
            //result.add(newPathCand);

            // step right
            newPathCand = getPathCandWind(path, 'R', timeStep, turnLoss, posStart, posEnd, tgtHeight);
            result.add(newPathCand);

            // step wide right
            //newPathCand = getPathCandWind(path, 'S', timeStep, turnLoss, posStart, posEnd, tgtHeight);
            //result.add(newPathCand);

        }

        return result;
    }

    @Override
    public Path getPath() {

        WindFieldGenerator wf = this.parameters.getWindField();
        PolarDiagram pd = this.parameters.getBoatPolarDiagram();
        Position startPos = this.parameters.getCourse().get(0);
        Position endPos = this.parameters.getCourse().get(1);
        TimePoint startTime = wf.getStartTime();// new MillisecondsTimePoint(0);
        List<TimedPositionWithSpeed> path = new ArrayList<TimedPositionWithSpeed>();

        Position currentPosition = startPos;
        TimePoint currentTime = startTime;

        Distance distStartEnd = startPos.getDistance(endPos);
        double distStartEndMeters = distStartEnd.getMeters();

        long timeStep = wf.getTimeStep().asMillis()/3;
        logger.info("Time step :" + timeStep);
        long turnLoss = pd.getTurnLoss(); // 4000; // time lost when doing a turn

        Wind wndStart = wf.getWind(new TimedPositionWithSpeedImpl(startTime, startPos, null));
        logger.fine("wndStart speed:" + wndStart.getKnots() + " angle:" + wndStart.getBearing().getDegrees());
        pd.setWind(wndStart);
        Bearing bearVrt = startPos.getBearingGreatCircle(endPos);
        //Bearing bearHrz = bearVrt.add(new DegreeBearingImpl(90.0));
        Position middlePos = startPos.translateGreatCircle(bearVrt, distStartEnd.scale(0.5));

        if (debugMsgOn) {
            System.out.println("start : "+startPos.getLatDeg()+", "+startPos.getLngDeg());
            System.out.println("middle: "+middlePos.getLatDeg()+", "+middlePos.getLngDeg());
            System.out.println("end   : "+endPos.getLatDeg()+", "+endPos.getLngDeg());
        }

        // calculate initial position according to initPathStr
        PathCandidate initPath = new PathCandidate(new TimedPositionImpl(currentTime, currentPosition), 0.0, 0.0, 0, "0");
        if (initPathStr.length()>1) {
            char nextDirection = '0';
            for(int idx=1; idx<initPathStr.length(); idx++) {
                nextDirection = initPathStr.charAt(idx);
                PathCandidate newPathCand = getPathCandWind(initPath, nextDirection, timeStep, turnLoss, startPos, endPos, distStartEndMeters);
                initPath = newPathCand;
            }
        }
        List<PathCandidate> allPaths = new ArrayList<PathCandidate>();
        List<PathCandidate> trgPaths = new ArrayList<PathCandidate>();
        allPaths.add(initPath);


        TimedPosition tstPosition = this.getStep(new TimedPositionImpl(startTime, startPos), timeStep, turnLoss, true, 'L').getA();
        double tstDist1 = startPos.getDistance(tstPosition.getPosition()).getMeters();
        tstPosition = this.getStep(new TimedPositionImpl(startTime, startPos), timeStep, turnLoss, true, 'R').getA();
        double tstDist2 = startPos.getDistance(tstPosition.getPosition()).getMeters();

        double hrzBinSize = (tstDist1 + tstDist2)/4; // horizontal bin size in meters
        if (debugMsgOn) {
            System.out.println("Horizontal Bin Size: "+hrzBinSize);
        }

        double oobFact = 0.75; // out-of-bounds factor
        boolean reachedEnd = false;
        int addSteps = 0;
        int finalSteps = 0; // maximum number of additional steps after first target-path found

        while ((!reachedEnd)||(addSteps<finalSteps)) {

            if (reachedEnd) {
                addSteps++;
            }

            // generate new paths
            double hrzMin = 0;
            double hrzMax = 0;
            List<PathCandidate> newPathCands;
            List<PathCandidate> newPaths = new ArrayList<PathCandidate>();
            for(PathCandidate curPath : allPaths) {

                if ((curPath.vrt > distStartEndMeters)) {
                    continue;
                } else {
                    //newPathCands = this.getPathCandsBeat(curPath, timeStep, turnLoss, startPos, bearVrt, distStartEndMeters);
                    newPathCands = this.getPathCandsBeatWind(curPath, timeStep, turnLoss, startPos, endPos, distStartEndMeters);
                }

                for(PathCandidate newPath : newPathCands) {

                    newPaths.add(newPath);
                    if (newPath.hrz < hrzMin) {
                        hrzMin = newPath.hrz;
                    }
                    if (newPath.hrz > hrzMax) {
                        hrzMax = newPath.hrz;
                    }
                }
            }

            int hrzLeft = (int)Math.round(Math.floor( (hrzMin + hrzBinSize/2.0) / hrzBinSize ));
            int hrzRight = (int)Math.round(Math.floor( (hrzMax + hrzBinSize/2.0) / hrzBinSize ));
            //System.out.println("hrzLeft: "+hrzLeft+", hrzRight: "+hrzRight);

            // build map of best path per hrz-bin
            int countPath = 0;
            int mapSize = hrzRight-hrzLeft+1;
            //System.out.println("MapSize: "+mapSize);

            int[] binIdx = new int[mapSize]; // TODO: init with zero?
            double[] binVal = new double[mapSize];
            // TODO: init of binVal
            /*for(int idx = 0; idx < mapSize; idx++) {
                binVal[idx] = 2*distStartEndMeters;
            }*/
            for(int curIdx=0; curIdx<newPaths.size(); curIdx++) {
                PathCandidate curPath = newPaths.get(curIdx);
                //if (curPath.vrt > 3900) {
                if (debugMsgOn) {
                    System.out.println(""+curPath.path+": "+curPath.hrz+", "+curPath.vrt+", "+curPath.pos.getPosition().getLatDeg()+", "+curPath.pos.getPosition().getLngDeg());
                }
                //}

                // check whether path is *outside* regatta-area
                double distFromMiddleMeters = middlePos.getDistance(curPath.pos.getPosition()).getMeters();
                if (distFromMiddleMeters > oobFact*distStartEndMeters) {
                    continue; // ignore curPath
                }

                // increase path-counter
                countPath++;

                // determine map-key
                int curKey = (int)Math.round(Math.floor( (curPath.hrz + hrzBinSize/2.0) / hrzBinSize )) - hrzLeft;

                /*String allR = "0" + (new String(new char[curPath.path.length()-1]).replace('\0', 'R'));
                if (curPath.path.equals(allR)) {
                    System.out.println(""+curPath.path+": "+curPath.hrz+", "+curPath.vrt+", "+curPath.pos.getPosition().getLatDeg()+", "+curPath.pos.getPosition().getLngDeg());
                }*/

                // check whether curPath is better then the ones looked at
                if ((curKey>=0)&&(curKey < mapSize)) {
                    if (curPath.vrt > binVal[curKey]) {
                        binVal[curKey] = curPath.vrt;
                        binIdx[curKey] = curIdx+1;
                    }
                }
            }

            // check if there are still paths inside regatta-area
            if (countPath > 0) {
                // take best from each horizontal-bin and check if target is reached
                allPaths = new ArrayList<PathCandidate>();
                for (int curKey = 0; curKey < mapSize; curKey++) {

                    if (binIdx[curKey] > 0) {
                        PathCandidate curPath = newPaths.get(binIdx[curKey] - 1);
                        allPaths.add(curPath);

                        // debug output
                        /*if ((Math.abs(curKey + hrzLeft) <= 10)) {
                            System.out.println("" + curPath.path + ": " + (curKey + hrzLeft) + ", vrt:" + curPath.vrt + ", hrz:" + curPath.hrz + ", bin:" + (Math.floor((curPath.hrz + hrzBinSize / 2.0) / hrzBinSize)));
                        }*/

                        // terminate path-search if paths close enough to target are found
                        if ((curPath.vrt > distStartEndMeters)) {
                            if ((Math.abs(curKey + hrzLeft) <= 3)) {
                                reachedEnd = true;
                                trgPaths.add(curPath); // add path to list of target-paths
                            }
                        }
                    }
                }
            } else {
                // terminate path-search as no path inside regatta-area are left
                reachedEnd = true;
            }
        }


        // if no target-paths were found, return empty path
        if (trgPaths.size() == 0) {
            //trgPaths = allPaths; // TODO: only for testing; remove lateron
            TimedPositionWithSpeed curPosition = new TimedPositionWithSpeedImpl(startTime, startPos, null);
            path.add(curPosition);
            return new PathImpl(path, wf); // return empty path
        }

        // sort target-paths ascending by distance-to-target
        Collections.sort(trgPaths);

        // debug output
        //if (debugMsgOn) {
        for(PathCandidate curPath : trgPaths) {
            logger.info("\nPath: " + curPath.path + "\n      Time: " + (Math.round((curPath.pos.getTimePoint().asMillis()-startTime.asMillis())/1000.0/60.0*10.0)/10.0)+", Height: "+curPath.vrt+" of "+(Math.round(startPos.getDistance(endPos).getMeters()*100.0)/100.0)+", Dist: "+curPath.hrz+"m ~ "+(Math.round(curPath.pos.getPosition().getDistance(endPos).getMeters()*100.0)/100.0)+"m");
            //System.out.print(""+curPath.path+": "+curPath.pos.getTimePoint().asMillis()+", "+curPath.pos.getPosition().getLatDeg()+", "+curPath.pos.getPosition().getLngDeg()+", ");
            //System.out.println(" height:"+curPath.vrt+" of "+startPos.getDistance(endPos).getMeters()+", dist:"+curPath.hrz+" ~ "+curPath.pos.getPosition().getDistance(endPos));
        }
        //}

        //
        // fill gwt-path
        //

        // generate intermediate steps
        bestCand = trgPaths.get(0); // target-path ending closest to target
        TimedPositionWithSpeed curPosition = null;
        char nextDirection = '0';
        char prevDirection = '0';
        for(int step=0; step<(bestCand.path.length()-1); step++) {

            nextDirection = bestCand.path.charAt(step);

            if (nextDirection == '0') {

                curPosition = new TimedPositionWithSpeedImpl(startTime, startPos, null);
                path.add(curPosition);

            } else {

                boolean sameBaseDirection = this.isSameDirection(prevDirection, nextDirection);
                TimedPosition newPosition = this.getStep(curPosition, timeStep, turnLoss, sameBaseDirection, nextDirection).getA();
                curPosition = new TimedPositionWithSpeedImpl(newPosition.getTimePoint(), newPosition.getPosition(), null);
                path.add(curPosition);

            }

            prevDirection = nextDirection;
        }

        // add final position (rescaled before to end on height of target)
        path.add(new TimedPositionWithSpeedImpl(bestCand.pos.getTimePoint(), bestCand.pos.getPosition(), null));

        return new PathImpl(path, wf);

    }

}
