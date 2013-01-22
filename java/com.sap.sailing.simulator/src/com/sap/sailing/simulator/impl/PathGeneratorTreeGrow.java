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
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class PathGeneratorTreeGrow extends PathGeneratorBase {

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    SimulationParameters simulationParameters;
    int maxLeft;
    int maxRight;
    boolean startLeft;

    public PathGeneratorTreeGrow(SimulationParameters params) {
        simulationParameters = params;
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

        @Override
        public int compareTo(PathCand other) {
            if (Math.abs(this.hrz) == Math.abs(other.hrz))
                return 0;
            return (Math.abs(this.hrz) < Math.abs(other.hrz) ? -1 : +1);
        }

    }
    
    // generate step
    TimedPosition getStep(TimedPosition pos, long timeStep, long turnLoss, char prevDirection, char nextDirection) {
        
        WindFieldGenerator wf = simulationParameters.getWindField();
        TimePoint curTime = pos.getTimePoint();
        Position curPosition = pos.getPosition();
        Wind posWind = wf.getWind(new TimedPositionWithSpeedImpl(curTime, curPosition, null));
        
        PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
        pd.setWind(posWind);

        // get beat-angle left and right
        Bearing travelBearing = null;
        if (nextDirection == 'L') {
            travelBearing = pd.optimalDirectionsUpwind()[0];
        } else if (nextDirection == 'R') {
            travelBearing = pd.optimalDirectionsUpwind()[1];
        }
        
        // determine beat-speed left and right
        SpeedWithBearing travelSpeed = pd.getSpeedAtBearing(travelBearing);

        TimePoint travelTime;
        TimePoint nextTime = new MillisecondsTimePoint(curTime.asMillis()+timeStep);
        if ((nextDirection == prevDirection)||(prevDirection == '0')) {
            travelTime = nextTime;
        } else {
            travelTime = new MillisecondsTimePoint(nextTime.asMillis() - turnLoss);
        }

        return new TimedPositionImpl(nextTime, travelSpeed.travelTo(curPosition, curTime, travelTime));
    }
    
    
    // generate path candidates based on beat angles
    List<PathCand> getPathCandsBeat(PathCand path, long timeStep, long turnLoss, Position posStart, Bearing bearVrt, double tgtHeight) {
        
        // create path candidates
        List<PathCand> result = new ArrayList<PathCand>();
        
        //TimedPosition pathPos = new TimedPositionImpl(leftTime, beatSpeedLft.travelTo(curPosition, curTime, leftTime));
        TimedPosition pathPos = this.getStep(path.pos, timeStep, turnLoss, path.path.charAt(path.path.length()-1), 'L');
        Position posHeight = pathPos.getPosition().projectToLineThrough(posStart, bearVrt);
        //System.out.println(""+pathPos.getPosition().getLatDeg()+", "+pathPos.getPosition().getLngDeg());
        //System.out.println(""+posStart.getLatDeg()+", "+posStart.getLngDeg());
        
        double vrtDist = Math.round(posHeight.getDistance(posStart).getMeters()*100.0)/100.0;
        if (vrtDist > tgtHeight) {
            // scale last step so that vrtDist ~ tgtHeight
            Position prevPos = path.pos.getPosition();
            TimePoint prevTime = path.pos.getTimePoint();
            double heightFrac = (tgtHeight - path.vrt) / (vrtDist - path.vrt); 
            Position newPos = prevPos.translateGreatCircle(prevPos.getBearingGreatCircle(pathPos.getPosition()), prevPos.getDistance(pathPos.getPosition()).scale(heightFrac));
            TimePoint newTime = new MillisecondsTimePoint(Math.round(prevTime.asMillis() + (pathPos.getTimePoint().asMillis()-prevTime.asMillis())*heightFrac));
            pathPos = new TimedPositionImpl(newTime, newPos);
            posHeight = pathPos.getPosition().projectToLineThrough(posStart, bearVrt);
        }
        
        double posSide = 1;
        double posBear = posStart.getBearingGreatCircle(pathPos.getPosition()).getDegrees();
        if ((posBear < 0.0)||(posBear > 180.0)) {
            posSide = -1;
        } else if ((posBear == 0.0)||(posBear == 180.0)) {
            posSide = 0;
        }
        double hrzDist = Math.round(posSide*posHeight.getDistance(pathPos.getPosition()).getMeters()*100.0)/100.0;
        //System.out.println(""+path.path+": ");
        //System.out.println("vrtDistL:"+posHeight.getDistance(posStart).getCentralAngleRad());
        //System.out.println("hrzDistL:"+posSide*posHeight.getDistance(pathPos.getPosition()).getCentralAngleRad());
        String pathStr = path.path+"L";
        result.add(new PathCand(pathPos, vrtDist, hrzDist, pathStr));
        //System.out.println(""+posHeight.getLatDeg()+", "+posHeight.getLngDeg()+", "+pathStr);
        
        //pathPos = new TimedPositionImpl(rightTime, beatSpeedRght.travelTo(curPosition, curTime, rightTime));
        pathPos = this.getStep(path.pos, timeStep, turnLoss, path.path.charAt(path.path.length()-1), 'R');
        posHeight = pathPos.getPosition().projectToLineThrough(posStart, bearVrt);
        //System.out.println(""+pathPos.getPosition().getLatDeg()+", "+pathPos.getPosition().getLngDeg());
        //System.out.println(""+posStart.getLatDeg()+", "+posStart.getLngDeg());
        vrtDist = Math.round(posHeight.getDistance(posStart).getMeters()*100.0)/100.0;
        if (vrtDist > tgtHeight) {
            // scale last step so that vrtDist ~ tgtHeight
            Position prevPos = path.pos.getPosition();
            TimePoint prevTime = path.pos.getTimePoint();
            double heightFrac = (tgtHeight - path.vrt) / (vrtDist - path.vrt); 
            Position newPos = prevPos.translateGreatCircle(prevPos.getBearingGreatCircle(pathPos.getPosition()), prevPos.getDistance(pathPos.getPosition()).scale(heightFrac));
            TimePoint newTime = new MillisecondsTimePoint(Math.round(prevTime.asMillis() + (pathPos.getTimePoint().asMillis()-prevTime.asMillis())*heightFrac));
            pathPos = new TimedPositionImpl(newTime, newPos);
            posHeight = pathPos.getPosition().projectToLineThrough(posStart, bearVrt);
        }

        posSide = 1;
        posBear = posStart.getBearingGreatCircle(pathPos.getPosition()).getDegrees();
        if ((posBear < 0.0)||(posBear > 180.0)) {
            posSide = -1;
        } else if ((posBear == 0.0)||(posBear == 180.0)) {
            posSide = 0;
        }
        hrzDist = Math.round(posSide*posHeight.getDistance(pathPos.getPosition()).getMeters()*100.0)/100.0;
        //System.out.println("vrtDistR:"+posHeight.getDistance(posStart).getCentralAngleRad());
        //System.out.println("hrzDistR:"+posSide*posHeight.getDistance(pathPos.getPosition()).getCentralAngleRad());
        pathStr = path.path+"R";
        result.add(new PathCand(pathPos, vrtDist, hrzDist, pathStr));
        //System.out.println(""+posHeight.getLatDeg()+", "+posHeight.getLngDeg()+", "+pathStr);

        return result;
    }
    
    @Override
    public Path getPath() {

        WindFieldGenerator wf = simulationParameters.getWindField();
        PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
        Position startPos = simulationParameters.getCourse().get(0);
        Position endPos = simulationParameters.getCourse().get(1);
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
        List<PathCand> allPaths = new ArrayList<PathCand>();
        List<PathCand> trgPaths = new ArrayList<PathCand>();
        allPaths.add(initPath);
        
        
        TimedPosition tstPosition = this.getStep(new TimedPositionImpl(startTime, startPos), timeStep, turnLoss, '0', 'L');
        double tstDist1 = startPos.getDistance(tstPosition.getPosition()).getMeters();
        tstPosition = this.getStep(new TimedPositionImpl(startTime, startPos), timeStep, turnLoss, '0', 'R');
        double tstDist2 = startPos.getDistance(tstPosition.getPosition()).getMeters();
        
        double hrzBinSize = (tstDist1 + tstDist2)/4; // horizontal bin size in meters
        
        
        boolean reachedEnd = false;
        //for(int count=0; count<10; count++) {
        while (!reachedEnd) {

            // generate new paths
            double hrzMin = 0;
            double hrzMax = 0;
            List<PathCand> newPaths = new ArrayList<PathCand>();
            for(PathCand curPath : allPaths) {
            
                List<PathCand> newPathCands = this.getPathCandsBeat(curPath, timeStep, turnLoss, startPos, bearVrt, distStartEndMeters);                
                
                for(PathCand newPath : newPathCands) {
                    //System.out.println(""+newPath.pos.getPosition().getLatDeg()+", "+newPath.pos.getPosition().getLngDeg()+", "+newPath.path);

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
            
            // build map of best path per hrz-bin
            int mapSize = hrzRight-hrzLeft+1;
            int[] binIdx = new int[mapSize]; // TODO: init with zero?
            double[] binVal = new double[mapSize]; // TODO: init with zero?
            for(int curIdx=0; curIdx<newPaths.size(); curIdx++) {
                PathCand curPath = newPaths.get(curIdx);
                
                // check wether path is *outside* regatta area
                double distFromMiddleMeters = middlePos.getDistance(curPath.pos.getPosition()).getMeters();
                if (distFromMiddleMeters > 0.75*distStartEndMeters) {
                    continue; // ignore curPath
                }
                
                // determine map-key
                int curKey = (int)Math.round(Math.floor( (curPath.hrz + hrzBinSize/2.0) / hrzBinSize )) - hrzLeft;
                
                // check whether curPath is better then the ones looked at
                if (curPath.vrt > binVal[curKey]) {
                    binVal[curKey] = curPath.vrt;
                    binIdx[curKey] = curIdx+1;
                }
            }
            
            // take best from each hrz-bin and check if target is reached
            allPaths = new ArrayList<PathCand>();
            for(int curKey=0; curKey<mapSize; curKey++) {
                if (binIdx[curKey]>0) {
                    PathCand curPath = newPaths.get(binIdx[curKey]-1);
                    allPaths.add(curPath);
                    if ((curPath.vrt > distStartEndMeters)) {
                        if ((Math.abs(curKey + hrzLeft) <= 3)) {
                        //System.out.println(""+curPath.path+": "+(curKey+hrzLeft)+", hrz:"+curPath.hrz+", bin:"+(Math.floor( (curPath.hrz + hrzBinSize/2.0) / hrzBinSize )));
                        reachedEnd = true;
                        trgPaths.add(curPath);
                        }
                    }
                }
            }
        }
        

        if (trgPaths.size() == 0) {
            //trgPaths = allPaths; // TODO: only for testing; remove lateron
            return new PathImpl(path, wf); // return empty path
        }
        
        Collections.sort(trgPaths);
        
        // debug output
        for(PathCand curPath : trgPaths) {            
            System.out.print(""+curPath.path+": "+curPath.pos.getTimePoint().asMillis());
            System.out.println(" height:"+curPath.vrt+" of "+startPos.getDistance(endPos).getMeters()+", dist:"+curPath.pos.getPosition().getDistance(endPos));
        }
        
        //
        // fill gwt-path
        //
        
        // generate intermediate steps
        PathCand bstPath = trgPaths.get(0);
        TimedPositionWithSpeed curPosition = null;
        char curDirection = '0';
        char prevDirection = '0';
        for(int step=0; step<(bstPath.path.length()-1); step++) {
            
           curDirection = bstPath.path.charAt(step);
            
           if (curDirection == '0') {
               
               curPosition = new TimedPositionWithSpeedImpl(startTime, startPos, null);
               path.add(curPosition);
               
           } else {
               
               TimedPosition newPosition = this.getStep(curPosition, timeStep, turnLoss, prevDirection, curDirection);
               curPosition = new TimedPositionWithSpeedImpl(newPosition.getTimePoint(), newPosition.getPosition(), null);
               path.add(curPosition);
               
           }
            
           prevDirection = curDirection;
        }
        
        // add final position (rescaled before to end on height of target)
        path.add(new TimedPositionWithSpeedImpl(bstPath.pos.getTimePoint(), bstPath.pos.getPosition(), null));

        return new PathImpl(path, wf);

    }

}
