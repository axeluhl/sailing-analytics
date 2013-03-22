package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class PathGeneratorTreeGrowTarget extends PathGeneratorBase {

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    // SimulationParameters simulationParameters;
    int maxLeft;
    int maxRight;
    boolean startLeft;

    public PathGeneratorTreeGrowTarget(SimulationParameters params) {
        this.parameters = params;
    }

    public void setEvaluationParameters(int maxLeftVal, int maxRightVal, boolean startLeftVal) {
        this.maxLeft = maxLeftVal;
        this.maxRight = maxRightVal;
        this.startLeft = startLeftVal;
    }

    class PathCand implements Comparable<PathCand> {
        public PathCand(TimedPosition pos, double vrt, double hrz, String path) {
            this.pos = pos;
            this.vrt = vrt;
            this.hrz = hrz;
            this.path = path;
        }

        TimedPosition pos;
        double vrt;
        double hrz;
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
        public int compareTo(PathCand other) {
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

    // generate step
    TimedPosition getStep(TimedPosition pos, long timeStep, long turnLoss, char prevDirection, char nextDirection) {

        double offDeg = 5.0;
        WindFieldGenerator wf = this.parameters.getWindField();
        TimePoint curTime = pos.getTimePoint();
        Position curPosition = pos.getPosition();
        Wind posWind = wf.getWind(new TimedPositionWithSpeedImpl(curTime, curPosition, null));

        PolarDiagram pd = this.parameters.getBoatPolarDiagram();
        pd.setWind(posWind);

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
        char prevBaseDirection = prevDirection;
        if (prevBaseDirection == 'M') {
            prevBaseDirection = 'L';
        }
        if (prevBaseDirection == 'S') {
            prevBaseDirection = 'R';
        }
        char nextBaseDirection = nextDirection;
        if (nextBaseDirection == 'M') {
            nextBaseDirection = 'L';
        }
        if (nextBaseDirection == 'S') {
            nextBaseDirection = 'R';
        }
        boolean sameBaseDirection = (nextBaseDirection == prevBaseDirection)||(prevBaseDirection == '0');
        if (sameBaseDirection) {
            travelTime = nextTime;
        } else {
            travelTime = new MillisecondsTimePoint(nextTime.asMillis() - turnLoss);
        }

        return new TimedPositionImpl(nextTime, travelSpeed.travelTo(curPosition, curTime, travelTime));
    }

    PathCand getPathCand(PathCand path, char nextDirection, long timeStep, long turnLoss, Position posStart, Bearing bearVrt, double tgtHeight) {

        // calculate next path position (taking turn-loss into account)
        TimedPosition pathPos = this.getStep(path.pos, timeStep, turnLoss, path.path.charAt(path.path.length()-1), nextDirection);

        // calculate height-position with reference to race course
        Position posHeight = pathPos.getPosition().projectToLineThrough(posStart, bearVrt);

        // calculate vertical distance as distance of height-position to start
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
        double posBear = posStart.getBearingGreatCircle(pathPos.getPosition()).getDegrees();
        if ((posBear < 0.0)||(posBear > 180.0)) {
            posSide = -1;
        } else if ((posBear == 0.0)||(posBear == 180.0)) {
            posSide = 0;
        }
        // calculate horizontal distance as distance of height-position to current position
        double hrzDist = Math.round(posSide*posHeight.getDistance(pathPos.getPosition()).getMeters()*100.0)/100.0;

        //System.out.println(""+hrzDist+", "+vrtDist+", "+pathPos.getPosition().getLatDeg()+", "+pathPos.getPosition().getLngDeg()+", "+posHeight.getLatDeg()+", "+posHeight.getLngDeg());

        // extend path-string by step-direction
        String pathStr = path.path + nextDirection;

        return (new PathCand(pathPos, vrtDist, hrzDist, pathStr));
    }

    // generate path candidates based on beat angles
    List<PathCand> getPathCandsBeat(PathCand path, long timeStep, long turnLoss, Position posStart, Bearing bearVrt, double tgtHeight) {

        List<PathCand> result = new ArrayList<PathCand>();
        PathCand newPathCand;

        // step left
        newPathCand = getPathCand(path, 'L', timeStep, turnLoss, posStart, bearVrt, tgtHeight);
        result.add(newPathCand);

        // step wide left
        newPathCand = getPathCand(path, 'M', timeStep, turnLoss, posStart, bearVrt, tgtHeight);
        result.add(newPathCand);

        // step right
        newPathCand = getPathCand(path, 'R', timeStep, turnLoss, posStart, bearVrt, tgtHeight);
        result.add(newPathCand);

        // step wide right
        newPathCand = getPathCand(path, 'S', timeStep, turnLoss, posStart, bearVrt, tgtHeight);
        result.add(newPathCand);

        return result;
    }

    @Override
    public Path getPathLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {
        return null;
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

        System.out.println("start : "+startPos.getLatDeg()+", "+startPos.getLngDeg());
        System.out.println("middle: "+middlePos.getLatDeg()+", "+middlePos.getLngDeg());
        System.out.println("end   : "+endPos.getLatDeg()+", "+endPos.getLngDeg());

        // initialize list of paths
        PathCand initPath = new PathCand(new TimedPositionImpl(currentTime, currentPosition), 0.0, 0.0, "0");
        String initPathStr = "0";
        //String initPathStr = "0RRRRRRRRRRRRRRRLLL";
        //String initPathStr = "0RRRRRRRRRRRRRRRRRR";
        //PathCand initPath = new PathCand(new TimedPositionImpl(currentTime, currentPosition), 0.0, 0.0, ); //"0RRRRRRRRRRRRRRR");
        if (initPathStr.length()>1) {
            //char prevDirection = '0';
            char nextDirection = '0';
            for(int idx=1; idx<initPathStr.length(); idx++) {
                nextDirection = initPathStr.charAt(idx);
                //TimedPosition newPosition = this.getStep(initPath.pos, timeStep, turnLoss, prevDirection, nextDirection);
                //initPath.pos = newPosition;
                PathCand newPathCand = getPathCand(initPath, nextDirection, timeStep, turnLoss, startPos, bearVrt, distStartEndMeters);
                initPath = newPathCand;
                //prevDirection = nextDirection;
            }
        }
        List<PathCand> allPaths = new ArrayList<PathCand>();
        List<PathCand> trgPaths = new ArrayList<PathCand>();
        allPaths.add(initPath);


        TimedPosition tstPosition = this.getStep(new TimedPositionImpl(startTime, startPos), timeStep, turnLoss, '0', 'L');
        double tstDist1 = startPos.getDistance(tstPosition.getPosition()).getMeters();
        tstPosition = this.getStep(new TimedPositionImpl(startTime, startPos), timeStep, turnLoss, '0', 'R');
        double tstDist2 = startPos.getDistance(tstPosition.getPosition()).getMeters();

        double hrzBinSize = (tstDist1 + tstDist2)/3; // horizontal bin size in meters
        System.out.println("Horizontal Bin Size: "+hrzBinSize);

        double oobFact = 0.75; // out-of-bounds factor
        //int hrzLeft = (int)Math.round(Math.floor( -distStartEndMeters / 2.0 / hrzBinSize / oobFact ));
        //int hrzRight = (int)Math.round(Math.floor( distStartEndMeters / 2.0 / hrzBinSize / oobFact ));

        boolean reachedEnd = false;
        int addSteps = 0;
        int finalSteps = 0; // maximum number of additional steps after first target-path found
        //for(int count=0; count<10; count++) {
        while ((!reachedEnd)||(addSteps<finalSteps)) {

            if (reachedEnd) {
                addSteps++;
            }

            // generate new paths
            double hrzMin = 0;
            double hrzMax = 0;
            List<PathCand> newPathCands;
            List<PathCand> newPaths = new ArrayList<PathCand>();
            for(PathCand curPath : allPaths) {

                if ((curPath.vrt > distStartEndMeters)) {
                    continue;
                } else {
                    newPathCands = this.getPathCandsBeat(curPath, timeStep, turnLoss, startPos, bearVrt, distStartEndMeters);
                }

                for(PathCand newPath : newPathCands) {

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
            double[] binVal = new double[mapSize]; // TODO: init with zero?
            for(int curIdx=0; curIdx<newPaths.size(); curIdx++) {
                PathCand curPath = newPaths.get(curIdx);

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
                allPaths = new ArrayList<PathCand>();
                for (int curKey = 0; curKey < mapSize; curKey++) {

                    if (binIdx[curKey] > 0) {
                        PathCand curPath = newPaths.get(binIdx[curKey] - 1);
                        allPaths.add(curPath);

                        // debug output
                        /*if ((Math.abs(curKey + hrzLeft) <= 3)) {
                            System.out.println("" + curPath.path + ": " + (curKey + hrzLeft) + ", vrt:" + curPath.vrt + ", hrz:" + curPath.hrz + ", bin:" + (Math.floor((curPath.hrz + hrzBinSize / 2.0) / hrzBinSize)));
                        }*/

                        // terminate path-search if paths close enough to target are found
                        if ((curPath.vrt > distStartEndMeters)) {
                            if ((Math.abs(curKey + hrzLeft) <= 2)) {
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
            return new PathImpl(path, wf); // return empty path
        }

        // sort target-paths ascending by distance-to-target
        Collections.sort(trgPaths);

        // debug output
        for(PathCand curPath : trgPaths) {
            System.out.print(""+curPath.path+": "+curPath.pos.getTimePoint().asMillis()+", "+curPath.pos.getPosition().getLatDeg()+", "+curPath.pos.getPosition().getLngDeg()+", ");
            System.out.println(" height:"+curPath.vrt+" of "+startPos.getDistance(endPos).getMeters()+", dist:"+curPath.hrz+" ~ "+curPath.pos.getPosition().getDistance(endPos));
        }

        //
        // fill gwt-path
        //

        // generate intermediate steps
        PathCand bstPath = trgPaths.get(0); // target-path ending closest to target
        TimedPositionWithSpeed curPosition = null;
        char nextDirection = '0';
        char prevDirection = '0';
        for(int step=0; step<(bstPath.path.length()-1); step++) {

            nextDirection = bstPath.path.charAt(step);

            if (nextDirection == '0') {

                curPosition = new TimedPositionWithSpeedImpl(startTime, startPos, null);
                path.add(curPosition);

            } else {

                TimedPosition newPosition = this.getStep(curPosition, timeStep, turnLoss, prevDirection, nextDirection);
                curPosition = new TimedPositionWithSpeedImpl(newPosition.getTimePoint(), newPosition.getPosition(), null);
                path.add(curPosition);

            }

            prevDirection = nextDirection;
        }

        // add final position (rescaled before to end on height of target)
        path.add(new TimedPositionWithSpeedImpl(bstPath.pos.getTimePoint(), bstPath.pos.getPosition(), null));

        return new PathImpl(path, wf);

    }

}
