package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.CourseChangeImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindField;
import com.sap.sailing.simulator.WindFieldGenerator;

public class SailingSimulatorImpl implements SailingSimulator {

    SimulationParameters simulationParameters;

    public SailingSimulatorImpl(SimulationParameters params) {
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

    @Override
    public Path getOptimumPath() {

        // calls either createDummy or createHeuristic()
        // use getAllPaths() instead

        return createOneTurnLeft();
    }

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    private Path createDummy() {
        Boundary boundary = simulationParameters.getBoundaries();
        WindFieldGenerator wf = simulationParameters.getWindField();
        PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = wf.getStartTime();// new MillisecondsTimePoint(0);
        List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();

        pd.setWind(wf.getWind(new TimedPositionWithSpeedImpl(startTime, start, null)));
        Bearing direct = start.getBearingGreatCircle(end);
        TimedPositionWithSpeed p1 = new TimedPositionWithSpeedImpl(startTime, start, pd.getSpeedAtBearing(direct));
        TimedPositionWithSpeed p2 = new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(
                startTime.asMillis() + 5 * 30 * 1000), end, null);
        lst.add(p1);
        lst.add(p2);

        return new PathImpl(lst, wf);
    }

    private Path createHeuristic() {
        Boundary boundary = simulationParameters.getBoundaries();
        WindFieldGenerator wf = simulationParameters.getWindField();
        PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = wf.getStartTime();// new MillisecondsTimePoint(0);
        List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();

        Position currentPosition = start;
        TimePoint currentTime = startTime;

        int prevDirection = -1;
        long turnloss = 4000; // time lost when doing a turn
        long windpred = 3000; // time used to predict wind, i.e. hypothetical sailors prediction
        TimePoint directTime;
        TimePoint leftTime;
        TimePoint rightTime;

        Wind wndStart = wf.getWind(new TimedPositionWithSpeedImpl(startTime, start, null));
        logger.fine("wndStart speed:" + wndStart.getKnots() + " angle:" + wndStart.getBearing().getDegrees());
        pd.setWind(wndStart);
        Bearing bearStart = currentPosition.getBearingGreatCircle(end);
        SpeedWithBearing spdStart = pd.getSpeedAtBearing(bearStart);
        lst.add(new TimedPositionWithSpeedImpl(startTime, start, spdStart));
        long timeStep = wf.getTimeStep().asMillis();
        logger.info("Time step :" + timeStep);
        // while there is more than 5% of the total distance to the finish
        while (currentPosition.getDistance(end).compareTo(start.getDistance(end).scale(0.05)) > 0) {

            // TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + 30000);

            long nextTimeVal = currentTime.asMillis() + timeStep;// + 30000;
            TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal);

            Wind cWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));
            logger.fine("cWind speed:" + cWind.getKnots() + " angle:" + cWind.getBearing().getDegrees());
            pd.setWind(cWind);

            // get wind of direction
            Bearing wLft = pd.optimalDirectionsUpwind()[0];
            Bearing wRght = pd.optimalDirectionsUpwind()[1];
            Bearing wDirect = currentPosition.getBearingGreatCircle(end);

            SpeedWithBearing sWDirect = pd.getSpeedAtBearing(wDirect);
            SpeedWithBearing sWLft = pd.getSpeedAtBearing(wLft);
            SpeedWithBearing sWRght = pd.getSpeedAtBearing(wRght);
            logger.fine("left boat speed:" + sWLft.getKnots() + " angle:" + sWLft.getBearing().getDegrees()
                    + "  right boat speed:" + sWRght.getKnots() + " angle:" + sWRght.getBearing().getDegrees());

            TimePoint wTime = new MillisecondsTimePoint(currentTime.asMillis() + windpred);
            Position pWDirect = sWDirect.travelTo(currentPosition, currentTime, wTime);
            Position pWLft = sWLft.travelTo(currentPosition, currentTime, wTime);
            Position pWRght = sWRght.travelTo(currentPosition, currentTime, wTime);

            logger.fine("current Pos:" + currentPosition.getLatDeg() + "," + currentPosition.getLngDeg());
            logger.fine("left    Pos:" + pWLft.getLatDeg() + "," + pWLft.getLngDeg());
            logger.fine("right   Pos:" + pWRght.getLatDeg() + "," + pWRght.getLngDeg());

            // calculate next step
            Wind dWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWDirect, null));
            logger.fine("dWind speed:" + dWind.getKnots() + " angle:" + dWind.getBearing().getDegrees());
            pd.setWind(dWind);
            Bearing direct = currentPosition.getBearingGreatCircle(end);
            SpeedWithBearing sdirect = pd.getSpeedAtBearing(direct);

            Wind lWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWLft, null));
            logger.fine("lWind speed:" + lWind.getKnots() + " angle:" + lWind.getBearing().getDegrees());
            pd.setWind(lWind);
            Bearing lft = pd.optimalDirectionsUpwind()[0];
            SpeedWithBearing slft = pd.getSpeedAtBearing(lft);

            Wind rWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWRght, null));
            logger.fine("rWind speed:" + rWind.getKnots() + " angle:" + rWind.getBearing().getDegrees());
            pd.setWind(rWind);
            Bearing rght = pd.optimalDirectionsUpwind()[1];
            SpeedWithBearing srght = pd.getSpeedAtBearing(rght);

            logger.fine("left boat speed:" + slft.getKnots() + " angle:" + slft.getBearing().getDegrees()
                    + "  right boat speed:" + srght.getKnots() + " angle:" + srght.getBearing().getDegrees());

            if (prevDirection == 0) {
                directTime = new MillisecondsTimePoint(nextTimeVal);
                leftTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                rightTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
            } else if (prevDirection == 1) {
                directTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                leftTime = new MillisecondsTimePoint(nextTimeVal);
                rightTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
            } else if (prevDirection == 2) {
                directTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                leftTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                rightTime = new MillisecondsTimePoint(nextTimeVal);
            } else {
                directTime = new MillisecondsTimePoint(nextTimeVal);
                leftTime = new MillisecondsTimePoint(nextTimeVal);
                rightTime = new MillisecondsTimePoint(nextTimeVal);
            }

            Position pdirect = sdirect.travelTo(currentPosition, currentTime, directTime);
            Position plft = slft.travelTo(currentPosition, currentTime, leftTime);
            Position prght = srght.travelTo(currentPosition, currentTime, rightTime);

            Distance ddirect = pdirect.getDistance(end);
            Distance dlft = plft.getDistance(end);
            Distance drght = prght.getDistance(end);

            if (ddirect.compareTo(dlft) <= 0 && ddirect.compareTo(drght) <= 0) {
                lst.add(new TimedPositionWithSpeedImpl(nextTime, pdirect, sdirect));
                currentPosition = pdirect;
                prevDirection = 0;
            }

            if (dlft.compareTo(ddirect) <= 0 && dlft.compareTo(drght) <= 0) {
                lst.add(new TimedPositionWithSpeedImpl(nextTime, plft, slft));
                currentPosition = plft;
                prevDirection = 1;
            }

            if (drght.compareTo(dlft) <= 0 && drght.compareTo(ddirect) <= 0) {
                lst.add(new TimedPositionWithSpeedImpl(nextTime, prght, srght));
                currentPosition = prght;
                prevDirection = 2;
            }
            currentTime = nextTime;

        }

        long nextTimeVal = currentTime.asMillis() + timeStep;// 30000;
        TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal);

        Wind wndEnd = wf.getWind(new TimedPositionWithSpeedImpl(nextTime, end, null));
        logger.fine("wndEnd speed:" + wndEnd.getKnots() + " angle:" + wndEnd.getBearing().getDegrees());
        pd.setWind(wndEnd);
        Bearing bearEnd = currentPosition.getBearingGreatCircle(end);
        SpeedWithBearing spdEnd = pd.getSpeedAtBearing(bearEnd);
        lst.add(new TimedPositionWithSpeedImpl(nextTime, end, spdEnd));

        return new PathImpl(lst, wf);
    }

    //
    // dynamic programming approach with forward iteration along start-end line
    //

    // find minimum duration in alternate steps from h1=hidx to the calling h2
    private durP findMinDur(ArrayList<durP> alldur) {
        durP mindur = alldur.get(0);
        for (int i = 0; i < alldur.size(); i++) {
            if (alldur.get(i).duration < mindur.duration) {
                mindur = alldur.get(i);
            }
        }

        return mindur;
    }

    // calculate duration of step from position p1 to position p2 at time curtime from side of wind side1
    private durP calcDuration(TimePoint curtime, int side1, Position p1, Position p2, WindField windField,
            PolarDiagram polarDiagram) {

        // set turnloss to be approximately 4sec (until polardiagram has getTurnloss() method)
        long turnloss = 4000;

        // System.out.println("p1: "+p1+" p2:"+p2);

        // take wind of starting position p1 at current time
        TimedPosition currentTimedPosition = new TimedPositionImpl(curtime, p1);
        SpeedWithBearing currentWind = windField.getWind(currentTimedPosition);
        // alternate approach: take wind of target position p2 at current time
        // TimedPosition windTimedPosition = new TimedPositionImpl(curtime, p2);
        // SpeedWithBearing currentWind = windField.getWind(windTimedPosition);

        // System.out.println("wind: "+currentWind.getKnots()+", "+currentWind.getBearing().getDegrees()+"°");

        // set polar diagram to current wind
        polarDiagram.setWind(currentWind);

        // calculate bearing, distance and speed from p1 to p2
        Bearing bearingToP = p1.getBearingGreatCircle(p2);
        Distance distanceToP = p1.getDistance(p2);
        Speed speedToP = polarDiagram.getSpeedAtBearing(bearingToP);
        // System.out.println("p1 to p2: angle: "+bearingToP.getDegrees()+"° dist: "+distanceToP.getMeters()+"m speed: "+speedToP.getMetersPerSecond()+"m/s");

        // add time delta for sailing from p1 to p2 to current time
        double deltat;
        if (speedToP.getMetersPerSecond() <= 0.1) {
            deltat = 86400; // set a high value for small times
        } else {
            deltat = (distanceToP.getMeters() / speedToP.getMetersPerSecond());
        }
        long timeToP = curtime.asMillis() + (long) (deltat) * 1000;
        // System.out.println("time: "+deltat/60.+"min");

        // identify turn based on change of sign of relative bearing of p1 to p2 towards the wind
        Bearing relativeBearing = currentWind.getBearing().reverse().getDifferenceTo(bearingToP);
        int side2;
        if (relativeBearing.getDegrees() == 0) {
            side2 = 0;
        } else {
            side2 = (int) Math.signum(relativeBearing.getDegrees());
        }
        if ((side1 != 0)&&(side2 != 0)&&(side1 == -side2)) {
            timeToP = timeToP + turnloss;
        }

        // return arrival time at p2 and wind side at p2 as durP instance
        durP durToP = new durP(timeToP, side2, 0, 0);

        return durToP;
    }

    // duration parameters: duration in millis, side of the wind, referencing horizontal grid column hidx,
    // and path index pidx (pidx currently unused, maybe used for structuring in groups of equivalent time, see
    // prototype R-script)
    class durP {
        public durP(long dur, int sid, int h, int p) {
            duration = dur;
            side = sid;
            hidx = h;
            pidx = p;
        }

        long duration;
        int side;
        int hidx;
        int pidx;
    }

    // location parameters: horizontal grid index idx, duration dur to reach this point (within path)
    class locP {
        public locP(int i, long d) {
            idx = i;
            dur = d;
        }

        int idx;
        long dur;
    }

    // dynamic programming forward iteration main algorithm (see also R-script prototype)
    private Path createDP() {

        // retrieve simulation parameters
        Boundary boundary = new RectangularBoundary(simulationParameters.getCourse().get(0), simulationParameters
                .getCourse().get(1));// simulationParameters.getBoundaries();
        WindFieldGenerator windField = simulationParameters.getWindField();
        PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = windField.getStartTime();

        // the solution path
        LinkedList<TimedPositionWithSpeed> lst = new LinkedList<TimedPositionWithSpeed>();

        // initiate grid: since performance is good, make it somewhat larger than what we do with dijkstra
        int sgridv = (int) Math.round(1.5 * simulationParameters.getProperty("Djikstra.gridv[int]").intValue()); // number
                                                                                                                 // of
                                                                                                                 // vertical
                                                                                                                 // grid
                                                                                                                 // steps
        int sgridh = (int) Math.round(1.5 * simulationParameters.getProperty("Djikstra.gridh[int]").intValue()); // number
                                                                                                                 // of
                                                                                                                 // horizontal
                                                                                                                 // grid
                                                                                                                 // steps

        // make horizontal grid size uneven, to have an index for the middle line
        if (sgridh % 2 == 0) {
            sgridh++;
        }

        // generate grid positions using sgridh and sgridv
        Position[][] sailGrid = boundary.extractGrid(sgridh, sgridv);

        // optimization grid indexes:
        // vertical steps: 0, ..., v-1
        // horizontal steps: -gridh, ..., 0, ..., +gridh
        int gridv = sgridv;
        int gridh = sgridh / 2;

        ArrayList<durP> alldur;
        ArrayList<ArrayList<Vector<locP>>> paths = new ArrayList<ArrayList<Vector<locP>>>();
        ArrayList<durP> duras = new ArrayList<durP>();

        // in each vertical step, paths having the same minimum duration are kept
        double mintol = 1.0; // threshold of closeness to realy minimum duration for keeping paths
        int maxeqpaths = 2; // maximum nmber of equal paths to keep [currently set to low values, to save Java heap
                            // memory]

        // loop over vertical steps
        for (int idxv = 0; idxv < (gridv - 1); idxv++) {

            // System.out.println("  vstep: "+idxv);

            ArrayList<ArrayList<Vector<locP>>> pathsnew = new ArrayList<ArrayList<Vector<locP>>>();
            ArrayList<durP> durasnew = new ArrayList<durP>();

            // get start boundary correct
            int range1;
            if (idxv == 0) {
                range1 = 0;
                duras.add(new durP(startTime.asMillis(), 0, 0, 0));
            } else {
                range1 = gridh;
            }

            // get end boundary correct
            int range2;
            if (idxv == (gridv - 2)) {
                range2 = 0;
            } else {
                range2 = gridh;
            }

            // target loop: idxh2
            for (int idxh2 = -range2; idxh2 <= range2; idxh2++) {

                alldur = new ArrayList<durP>();

                // origin loop: idxh1
                for (int idxh1 = -range1; idxh1 <= range1; idxh1++) {

                    // calculate the time to go from [idxv,idxh1] to [idxv+1,idxh2]
                    durP durationh1h2 = calcDuration(new MillisecondsTimePoint(duras.get(idxh1 + range1).duration),
                            duras.get(idxh1 + range1).side, sailGrid[idxv][idxh1 + gridh], sailGrid[idxv + 1][idxh2
                                    + gridh], windField, polarDiagram);

                    // *forbidden* angles
                    if (durationh1h2.duration < 0) {
                        continue;
                    }

                    // remember durations of current vertical step in alldur
                    durationh1h2.hidx = idxh1 + range1;
                    alldur.add(durationh1h2);

                } // horizontal origin loop: idxh1

                if (idxv > 0) { // if not at start of the grid

                    if (idxv != (gridv - 2)) { // if not at end of the grid

                        // find minimum duration in alldur
                        durP mindur = findMinDur(alldur);

                        // remember minimum duration for reaching [idxv,idxh2]
                        durP tdur = new durP(mindur.duration, mindur.side, idxh2 + gridh, 0);
                        durasnew.add(tdur);

                        // keep all (see mintol, maxeqpaths) paths with minimum duration for reaching [idxv,idxh2]
                        int k = 0;
                        for (int j = 0; j < alldur.size(); j++) {

                            if ((alldur.get(j).duration <= mintol * mindur.duration) && (k < maxeqpaths)) {
                                ArrayList<Vector<locP>> npaths;
                                if ((pathsnew.size() > 0) && (pathsnew.size() == (idxh2 + gridh + 1))) {
                                    npaths = pathsnew.get(idxh2 + gridh);
                                } else {
                                    npaths = new ArrayList<Vector<locP>>();
                                    pathsnew.add(npaths);
                                }
                                ArrayList<Vector<locP>> tpaths = paths.get(alldur.get(j).hidx);
                                for (int i = 0; i < tpaths.size(); i++) {
                                    Vector<locP> tpath;
                                    tpath = (Vector<locP>) (tpaths.get(i)).clone();
                                    tpath.add(new locP(idxh2, mindur.duration));
                                    npaths.add(tpath);
                                }
                                k++;
                            }

                        }

                    } else {

                        // find minimum in alldur
                        durP mindur = findMinDur(alldur);

                        // remember minimum duration for reaching [idxv,idxh2]
                        durP tdur = new durP(mindur.duration, mindur.side, 0, 0);
                        durasnew.add(tdur);

                        // keep all (see mintol, maxeqpaths) paths with minimum duration for reaching [idxv,idxh2]
                        int k = 0;
                        for (int j = 0; j < alldur.size(); j++) {

                            if ((alldur.get(j).duration <= mintol * mindur.duration) && (k < maxeqpaths)) {
                                ArrayList<Vector<locP>> npaths;
                                if ((pathsnew.size() > 0) && (pathsnew.size() == (idxh2 + range2 + 1))) {
                                    npaths = pathsnew.get(idxh2 + range2);
                                } else {
                                    npaths = new ArrayList<Vector<locP>>();
                                    pathsnew.add(npaths);
                                }
                                ArrayList<Vector<locP>> tpaths = paths.get(alldur.get(j).hidx);
                                for (int i = 0; i < tpaths.size(); i++) {
                                    Vector<locP> tpath;
                                    tpath = (Vector<locP>) (tpaths.get(i)).clone();
                                    tpath.add(new locP(0, mindur.duration));
                                    npaths.add(tpath);
                                }
                                k++;
                            }

                        }
                    }

                } else { // start of grid

                    Vector<locP> tpath = new Vector<locP>();
                    tpath.add(new locP(0, startTime.asMillis()));
                    ArrayList<Vector<locP>> tpaths = new ArrayList<Vector<locP>>();
                    tpaths.add(tpath);
                    pathsnew.add(tpaths);
                    durP tdur = calcDuration(startTime, 0, sailGrid[0][gridh], sailGrid[1][idxh2 + gridh], windField,
                            polarDiagram);
                    tdur.hidx = idxh2 + gridh;
                    durasnew.add(tdur);
                    tpath.add(new locP(idxh2, tdur.duration));
                }

            } // horizontal target loop: idxh2

            paths = pathsnew;
            duras = durasnew;

            /*
             * if (idxv == (gridv-2)) { System.out.print("    optimal path:"); for(int i=0; i<1;i++) {//paths.size();
             * i++) { //System.out.print(""+duras.get(i).duration+" - "); for(int j=0; j<1;j++) {//paths.get(i).size();
             * j++) { int kmax = paths.get(i).get(j).size();
             * System.out.print(""+((paths.get(i).get(j).get(kmax-1).dur-paths
             * .get(i).get(j).get(0).dur)/1000./60.)+"min: "); for(int k=0;k<kmax; k++) {
             * System.out.print(" "+paths.get(i).get(j).get(k).idx); } System.out.println(); } } }
             */

        }

        // generate timed position path
        for (int i = 0; i < paths.size(); i++) {
            // for(int j=0; j<paths.get(i).size(); j++) {
            for (int j = 0; j < 1; j++) { // restrict to first path
                for (int k = 0; k < paths.get(i).get(j).size(); k++) {
                    Position p = sailGrid[k][paths.get(i).get(j).get(k).idx + gridh];
                    TimePoint t = new MillisecondsTimePoint(paths.get(i).get(j).get(k).dur);
                    // System.out.println("deltaT: "+(t.asMillis()-startTime.asMillis())/1000.);
                    lst.add(new TimedPositionWithSpeedImpl(t, p, null));
                }
            }
        }

        return new PathImpl(lst, windField);
    }

    private Path createDjikstra() {
        // retrieve simulation parameters
        Boundary boundary = new RectangularBoundary(simulationParameters.getCourse().get(0), simulationParameters
                .getCourse().get(1));// simulationParameters.getBoundaries();
        WindFieldGenerator windField = simulationParameters.getWindField();
        PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = windField.getStartTime();// new MillisecondsTimePoint(0);

        // the solution path
        LinkedList<TimedPositionWithSpeed> lst = new LinkedList<TimedPositionWithSpeed>();

        // initiate grid
        int gridv = simulationParameters.getProperty("Djikstra.gridv[int]").intValue(); // number of vertical grid steps
        int gridh = simulationParameters.getProperty("Djikstra.gridh[int]").intValue(); // number of horizontal grid
                                                                                        // steps
        Position[][] sailGrid = boundary.extractGrid(gridh, gridv);

        // create adjacency graph including start and end
        Map<Position, List<Position>> graph = new HashMap<Position, List<Position>>();
        graph.put(start, Arrays.asList(sailGrid[1]));
        for (int i = 1; i < gridv - 2; i++) {
            for (Position p : sailGrid[i]) {
                graph.put(p, Arrays.asList(sailGrid[i + 1]));
            }
        }
        for (Position p : sailGrid[gridv - 2]) {
            graph.put(p, Arrays.asList(end));
        }

        /*
         * //create backwards adjacency graph, required to reconstruct the optimal path Map<Position, List<Position>>
         * backGraph = new HashMap<Position, List<Position>>(); backGraph.put(end, Arrays.asList(sailGrid[gridv-2]));
         * for(int i = gridv-2; i > 1; i--) { for(Position p: sailGrid[i]) { backGraph.put(p,
         * Arrays.asList(sailGrid[i-1])); } } for(Position p : sailGrid[1]) { backGraph.put(p, Arrays.asList(start)); }
         */

        // create tentative distance matrix
        // additional to tentative distances, the matrix also contains the root of each position
        // that can be </null> if unavailable
        Map<Position, Pair<Long, Position>> tentativeDistances = new HashMap<Position, Pair<Long, Position>>();
        for (Position p : graph.keySet()) {
            tentativeDistances.put(p, new Pair<Long, Position>(Long.MAX_VALUE, null));
        }
        tentativeDistances.put(start, new Pair<Long, Position>(startTime.asMillis(), null));
        tentativeDistances.put(end, new Pair<Long, Position>(Long.MAX_VALUE, null));

        // create set of unvisited nodes
        List<Position> unvisited = new ArrayList<Position>(graph.keySet());
        unvisited.add(end);

        // set the initial node as current
        Position currentPosition = start;
        TimePoint currentTime = startTime;
        Bearing previousBearing = null;

        // search loop
        // ends when the end is visited
        while (currentPosition != end) {
            // set the polar diagram to the wind at the current position and time
            TimedPosition currentTimedPosition = new TimedPositionImpl(currentTime, currentPosition);
            SpeedWithBearing currentWind = windField.getWind(currentTimedPosition);
            polarDiagram.setWind(currentWind);

            // compute the tentative distance to all the unvisited neighbours of the current node
            // and replace it in the matrix if is smaller than the previous one
            List<Position> unvisitedNeighbours = new LinkedList<Position>(graph.get(currentPosition));
            unvisitedNeighbours.retainAll(unvisited);
            for (Position p : unvisitedNeighbours) {
                Bearing bearingToP = currentPosition.getBearingGreatCircle(p);
                Distance distanceToP = currentPosition.getDistance(p);
                Speed speedToP = polarDiagram.getSpeedAtBearing(bearingToP);
                // multiplied by 1000 to have milliseconds
                Long timeToP = (long) (1000 * (distanceToP.getMeters() / speedToP.getMetersPerSecond()));
                /*
                 * if (previousBearing != null) { Bearing windBearingFrom = currentWind.getBearing().reverse(); if(
                 * (PolarDiagram49.bearingComparator.compare(bearingToP, windBearingFrom) > 0) &&
                 * (PolarDiagram49.bearingComparator.compare(previousBearing, windBearingFrom) < 0) ) timeToP = timeToP
                 * + 4000; if( (PolarDiagram49.bearingComparator.compare(bearingToP, windBearingFrom) < 0) &&
                 * (PolarDiagram49.bearingComparator.compare(previousBearing, windBearingFrom) > 0) ) timeToP = timeToP
                 * + 4000; }
                 */

                Long tentativeDistanceToP = currentTime.asMillis() + timeToP;
                if (tentativeDistanceToP < tentativeDistances.get(p).getA()) {
                    tentativeDistances.put(p, new Pair<Long, Position>(tentativeDistanceToP, currentPosition));
                }
            }

            // mark current node as visited
            unvisited.remove(currentPosition);

            // select the unvisited node with the smallest tentative distance
            // and set it as current
            Long minTentativeDistance = Long.MAX_VALUE;
            for (Position p : unvisited) {
                if (tentativeDistances.get(p).getA() < minTentativeDistance) {
                    currentPosition = p;
                    minTentativeDistance = tentativeDistances.get(p).getA();
                    previousBearing = tentativeDistances.get(p).getB().getBearingGreatCircle(currentPosition);
                    currentTime = new MillisecondsTimePoint(minTentativeDistance);
                }

            }
        }
        // I need to add the end point to the distances matrix
        // tentativeDistances.put(end,currentTime.asMillis());

        // at this point currentPosition = end
        // currentTime = total duration of the course

        // reconstruct the optimal path by going from start to end
        /*
         * while(currentPosition != start) { TimedPositionWithSpeed currentTimedPositionWithSpeed = new
         * TimedPositionWithSpeedImpl(currentTime, currentPosition, null ); lst.addFirst(currentTimedPositionWithSpeed);
         * System.out.println(boundary.getGridIndex(currentTimedPositionWithSpeed.getPosition())); List<Position>
         * currentPredecessors = backGraph.get(currentPosition); Long minTime = Long.MAX_VALUE; for(Position p :
         * currentPredecessors) { if(tentativeDistances.get(p) < minTime) { minTime = tentativeDistances.get(p);
         * currentPosition = p; currentTime = new MillisecondsTimePoint(minTime); } } } //I need to add the first point
         * to the path lst.addFirst(new TimedPositionWithSpeedImpl(startTime, start, null));
         */
        while (currentPosition != null) {
            currentTime = new MillisecondsTimePoint(tentativeDistances.get(currentPosition).getA());
            SpeedWithBearing windAtPoint = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
            TimedPositionWithSpeed current = new TimedPositionWithSpeedImpl(currentTime, currentPosition, windAtPoint);
            lst.addFirst(current);
            currentPosition = tentativeDistances.get(currentPosition).getB();
        }

        return new PathImpl(lst, windField);
    }

    private Path createOneTurnLeft() {
        // retrieve simulation parameters
        Boundary boundary = new RectangularBoundary(simulationParameters.getCourse().get(0), simulationParameters
                .getCourse().get(1));// simulationParameters.getBoundaries();
        WindFieldGenerator windField = simulationParameters.getWindField();
        PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = windField.getStartTime();// new MillisecondsTimePoint(0);

        Distance courseLength = start.getDistance(end);

        // the solution path
        LinkedList<TimedPositionWithSpeed> lst = null;
        // the minimal one-turn time

        Long timeResolution = 120000L;
        boolean turned = true;
        boolean outOfBounds = false;
        Long minTurn = Long.MAX_VALUE;
        int turningStep = 0;

        while (turned) {
            LinkedList<TimedPositionWithSpeed> tempLst = new LinkedList<TimedPositionWithSpeed>();
            Position currentPosition = start;
            TimePoint currentTime = startTime;
            turned = false;
            outOfBounds = false;
            turningStep++;
            int currentStep = 0;

            while (true) {

                SpeedWithBearing currWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currWind);
                TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeResolution);
                tempLst.add(new TimedPositionWithSpeedImpl(currentTime, currentPosition, currWind));

                if (currentStep >= turningStep) {
                    turned = true;
                    nextTime = new MillisecondsTimePoint(nextTime.asMillis() + polarDiagram.getTurnLoss());
                }

                if (!turned) {
                    Bearing direction = polarDiagram.optimalDirectionsUpwind()[0];
                    // for(Bearing b: polarDiagram.optimalDirectionsUpwind())
                    // if(polarDiagram.getWindSide(b) == PolarDiagram.WindSide.LEFT)
                    // direction = b;
                    SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
                    currentPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                }
                if (turned) {
                    Bearing direction1 = currentPosition.getBearingGreatCircle(end);
                    Bearing direction2 = polarDiagram.optimalDirectionsUpwind()[1];
                    // for(Bearing b: polarDiagram.optimalDirectionsUpwind())
                    // if(polarDiagram.getWindSide(b) == PolarDiagram.WindSide.RIGHT)
                    // direction2 = b;
                    SpeedWithBearing currSpeed1 = polarDiagram.getSpeedAtBearing(direction1);
                    SpeedWithBearing currSpeed2 = polarDiagram.getSpeedAtBearing(direction2);
                    Position nextPosition1 = currSpeed1.travelTo(currentPosition, currentTime, nextTime);
                    Position nextPosition2 = currSpeed2.travelTo(currentPosition, currentTime, nextTime);
                    // nextPosition2.
                    if (nextPosition1.getDistance(end).compareTo(nextPosition2.getDistance(end)) < 0
                            && Math.abs(direction1.getDifferenceTo(direction2).getDegrees()) < 45.0)
                        currentPosition = nextPosition1;
                    else
                        currentPosition = nextPosition2;
                }

                currentStep++;
                // System.out.println(currentStep + "/" + turningStep + "/" + turned);
                currentTime = nextTime;

                if (currentTime.asMillis() > minTurn) {
                    // System.out.println("out of time");
                    break;
                }
                if (!boundary.isWithinBoundaries(currentPosition)) {
                    outOfBounds = true;
                    // System.out.println("out of bounds");
                    break;
                }
                if (currentPosition.getDistance(end).compareTo(courseLength.scale(0.005)) < 0) {
                    minTurn = currentTime.asMillis();
                    lst = new LinkedList<TimedPositionWithSpeed>(tempLst);
                    Bearing directionToEnd = currentPosition.getBearingGreatCircle(end);
                    SpeedWithBearing crtWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                    polarDiagram.setWind(crtWind);
                    Speed speedToEnd = polarDiagram.getSpeedAtBearing(directionToEnd);
                    Distance distanceToEnd = currentPosition.getDistance(end);
                    Long timeToEnd = (long) (1000.0 * distanceToEnd.getMeters() / speedToEnd.getMetersPerSecond());
                    TimePoint endTime = new MillisecondsTimePoint(currentTime.asMillis() + timeToEnd);
                    lst.addLast(new TimedPositionWithSpeedImpl(endTime, end, crtWind));
                    // System.out.println("end reached!!!");
                    break;
                }

            }

        }

        if (lst != null) {
            // lst.addLast(new TimedPositionWithSpeedImpl(new
            // MillisecondsTimePoint(lst.getLast().getTimePoint().asMillis() + timeResolution), end,
            // lst.getLast().getSpeed()));
            return new PathImpl(lst, windField);
        } else
            return null;

    }

    private Path createOneTurnRight() {
        // retrieve simulation parameters
        Boundary boundary = new RectangularBoundary(simulationParameters.getCourse().get(0), simulationParameters
                .getCourse().get(1));// simulationParameters.getBoundaries();
        WindFieldGenerator windField = simulationParameters.getWindField();
        PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = windField.getStartTime();// new MillisecondsTimePoint(0);

        Distance courseLength = start.getDistance(end);

        // the solution path
        LinkedList<TimedPositionWithSpeed> lst = null;
        // the minimal one-turn time

        Long timeResolution = 120000L;
        boolean turned = true;
        boolean outOfBounds = false;
        Long minTurn = Long.MAX_VALUE;
        int turningStep = 0;

        while (turned) {
            LinkedList<TimedPositionWithSpeed> tempLst = new LinkedList<TimedPositionWithSpeed>();
            Position currentPosition = start;
            TimePoint currentTime = startTime;
            turned = false;
            outOfBounds = false;
            turningStep++;
            int currentStep = 0;

            while (true) {

                SpeedWithBearing currWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currWind);
                TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + timeResolution);
                tempLst.add(new TimedPositionWithSpeedImpl(currentTime, currentPosition, currWind));

                if (currentStep >= turningStep) {
                    turned = true;
                    nextTime = new MillisecondsTimePoint(nextTime.asMillis() + polarDiagram.getTurnLoss());
                }

                if (!turned) {
                    Bearing direction = polarDiagram.optimalDirectionsUpwind()[1];
                    // for(Bearing b: polarDiagram.optimalDirectionsUpwind())
                    // if(polarDiagram.getWindSide(b) == PolarDiagram.WindSide.LEFT)
                    // direction = b;
                    SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
                    currentPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                }
                if (turned) {
                    Bearing direction1 = currentPosition.getBearingGreatCircle(end);
                    Bearing direction2 = polarDiagram.optimalDirectionsUpwind()[0];
                    // for(Bearing b: polarDiagram.optimalDirectionsUpwind())
                    // if(polarDiagram.getWindSide(b) == PolarDiagram.WindSide.RIGHT)
                    // direction2 = b;
                    SpeedWithBearing currSpeed1 = polarDiagram.getSpeedAtBearing(direction1);
                    SpeedWithBearing currSpeed2 = polarDiagram.getSpeedAtBearing(direction2);
                    Position nextPosition1 = currSpeed1.travelTo(currentPosition, currentTime, nextTime);
                    Position nextPosition2 = currSpeed2.travelTo(currentPosition, currentTime, nextTime);
                    // nextPosition2.
                    if (nextPosition1.getDistance(end).compareTo(nextPosition2.getDistance(end)) < 0
                            && Math.abs(direction1.getDifferenceTo(direction2).getDegrees()) < 45.0)
                        currentPosition = nextPosition1;
                    else
                        currentPosition = nextPosition2;
                }

                currentStep++;
                // System.out.println(currentStep + "/" + turningStep + "/" + turned);
                currentTime = nextTime;

                if (currentTime.asMillis() > minTurn) {
                    // System.out.println("out of time");
                    break;
                }
                if (!boundary.isWithinBoundaries(currentPosition)) {
                    outOfBounds = true;
                    // System.out.println("out of bounds");
                    break;
                }
                if (currentPosition.getDistance(end).compareTo(courseLength.scale(0.005)) < 0) {
                    minTurn = currentTime.asMillis();
                    lst = new LinkedList<TimedPositionWithSpeed>(tempLst);
                    Bearing directionToEnd = currentPosition.getBearingGreatCircle(end);
                    SpeedWithBearing crtWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                    polarDiagram.setWind(crtWind);
                    Speed speedToEnd = polarDiagram.getSpeedAtBearing(directionToEnd);
                    Distance distanceToEnd = currentPosition.getDistance(end);
                    Long timeToEnd = (long) (1000.0 * distanceToEnd.getMeters() / speedToEnd.getMetersPerSecond());
                    TimePoint endTime = new MillisecondsTimePoint(currentTime.asMillis() + timeToEnd);
                    lst.addLast(new TimedPositionWithSpeedImpl(endTime, end, crtWind));
                    // System.out.println("end reached!!!");
                    break;
                }

            }

        }

        if (lst != null) {
            // lst.addLast(new TimedPositionWithSpeedImpl(new
            // MillisecondsTimePoint(lst.getLast().getTimePoint().asMillis() + timeResolution), end,
            // lst.getLast().getSpeed()));
            return new PathImpl(lst, windField);
        } else
            return null;

    }

    private Path create1Turn(boolean tleft) {

        WindFieldGenerator windField = simulationParameters.getWindField();
        PolarDiagram polarDiagram = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
        TimePoint startTime = windField.getStartTime();// new MillisecondsTimePoint(0);

        Distance courseLength = start.getDistance(end);
        Position currentPosition = start;
        TimePoint currentTime = startTime;
        TimePoint nextTime;

        double reachingTolerance = 0.03;
        int smax = 400;
        double[] reachTime = new double[smax];
        boolean tfound;
        long tdelta = windField.getTimeStep().asMillis() / 5;
        Bearing direction;

        double newdist;
        double mindist = courseLength.getMeters();
        double allmindist = courseLength.getMeters();
        //int allmins = smax;
        LinkedList<TimedPositionWithSpeed> path = null;
        LinkedList<TimedPositionWithSpeed> allminpath = null;

        for (int s = 0; s < smax; s++) {

            currentPosition = start;
            currentTime = startTime;
            reachTime[s] = courseLength.getMeters();
            tfound = false;
            mindist = courseLength.getMeters();
            path = new LinkedList<TimedPositionWithSpeed>();
            path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null)); // currSpeed));

            int l = 0;
            while ((l < s) && (!tfound)) {

                SpeedWithBearing currWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currWind);
                if (tleft) {
                    direction = polarDiagram.optimalDirectionsUpwind()[0];
                } else {
                    direction = polarDiagram.optimalDirectionsUpwind()[1];
                }
                SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
                nextTime = new MillisecondsTimePoint(currentTime.asMillis() + tdelta);
                Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                newdist = nextPosition.getDistance(end).getMeters();
                if (newdist < mindist) {
                    mindist = newdist;
                }
                currentPosition = nextPosition;
                currentTime = nextTime;
                path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null)); // currSpeed));

                if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
                    reachTime[s] = mindist;
                    tfound = true;
                    if (mindist < allmindist) {
                        allmindist = mindist;
                        //allmins = s;
                        allminpath = path;
                    }
                }
                l++;
            }

            currentTime = new MillisecondsTimePoint(currentTime.asMillis() + 4000);

            int r = 0;
            while ((r < (smax - s)) && (!tfound)) {

                SpeedWithBearing currWind = windField.getWind(new TimedPositionImpl(currentTime, currentPosition));
                polarDiagram.setWind(currWind);
                if (tleft) {
                    direction = polarDiagram.optimalDirectionsUpwind()[1];
                } else {
                    direction = polarDiagram.optimalDirectionsUpwind()[0];
                }
                SpeedWithBearing currSpeed = polarDiagram.getSpeedAtBearing(direction);
                nextTime = new MillisecondsTimePoint(currentTime.asMillis() + tdelta);
                Position nextPosition = currSpeed.travelTo(currentPosition, currentTime, nextTime);
                newdist = nextPosition.getDistance(end).getMeters();
                if (newdist < mindist) {
                    mindist = newdist;
                }
                currentPosition = nextPosition;
                currentTime = nextTime;
                path.addLast(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null)); // currSpeed));

                if (currentPosition.getDistance(end).getMeters() < reachingTolerance * courseLength.getMeters()) {
                    // System.out.println(""+s+":"+path.size()+" dist:"+mindist);
                    reachTime[s] = mindist;
                    tfound = true;
                    if (mindist < allmindist) {
                        allmindist = mindist;
                        //allmins = s;
                        allminpath = path;
                    }
                }
                r++;
            }
        }

        // for (int i=0; i<reachTime.length; i++) { System.out.println(""+i+": "+reachTime[i]); } 

        return new PathImpl(allminpath, windField);

    }

    @Override
    public Map<String, Path> getAllPaths() {
        Map<String, Path> allPaths = new HashMap<String, Path>();
        // allPaths.put("Dummy", createDummy());
        // allPaths.put("1-Turner Right", createOneTurnRight());
        // allPaths.put("1-Turner Left", createOneTurnLeft());
        allPaths.put("Opportunistic", createHeuristic());
        allPaths.put("Omniscient", createDjikstra());
        // allPaths.put("Omniscient", createDP());
        return allPaths;
    }

    public Map<String, List<TimedPositionWithSpeed>> getAllPathsEvenTimed(long millisecondsStep) {

        Map<String, List<TimedPositionWithSpeed>> allPaths = new HashMap<String, List<TimedPositionWithSpeed>>();
        // allPaths.put("Dummy", createDummy().getEvenTimedPoints(millisecondsStep));
        // allPaths.put("1-Turner Right", createOneTurnRight().getEvenTimedPoints(millisecondsStep));
        /*
         * Path oneLeft = createOneTurnLeft(); Path oneRight = createOneTurnRight(); if (oneLeft != null)
         * allPaths.put("1-Turner Left", oneLeft.getEvenTimedPoints(millisecondsStep)); if (oneRight != null)
         * allPaths.put("1-Turner Right", oneRight.getEvenTimedPoints(millisecondsStep)); allPaths.put("Opportunistic",
         * createHeuristic().getEvenTimedPoints(millisecondsStep)); allPaths.put("Omniscient",
         * createDjikstra().getEvenTimedPoints(millisecondsStep)); //allPaths.put("Omniscient",
         * createDP().getEvenTimedPoints(millisecondsStep));
         */

        Path leftPath = create1Turn(true);
        Path rightPath = create1Turn(false);
        Path oppPath = createHeuristic();
        Path optPath = createDP();

        if (false) {

            if (leftPath != null) {
                allPaths.put("1-Turner Left", leftPath.getEvenTimedPoints(millisecondsStep));
            }

            if (rightPath != null) {
                allPaths.put("1-Turner Right", rightPath.getEvenTimedPoints(millisecondsStep));
            }

            if (oppPath != null) {
                allPaths.put("Opportunistic", oppPath.getEvenTimedPoints(millisecondsStep));
            }

        } else {

            if (leftPath != null) {
                if (leftPath.getPathPoints().get(leftPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath
                        .getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                    optPath = leftPath;
                }
                allPaths.put("1-Turner Left", leftPath.getEvenTimedPoints(millisecondsStep));
            }

            if (rightPath != null) {
                if (rightPath.getPathPoints().get(rightPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath
                        .getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                    optPath = rightPath;
                }
                allPaths.put("1-Turner Right", rightPath.getEvenTimedPoints(millisecondsStep));
            }

            if (oppPath != null) {
                if (oppPath.getPathPoints().get(oppPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath
                        .getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                    optPath = oppPath;
                }
                allPaths.put("Opportunistic", oppPath.getEvenTimedPoints(millisecondsStep));
            }

        }

        // allPaths.put(, createHeuristic().getEvenTimedPoints(millisecondsStep));
        allPaths.put("Omniscient", optPath.getEvenTimedPoints(millisecondsStep));

        return allPaths;
    }

}
