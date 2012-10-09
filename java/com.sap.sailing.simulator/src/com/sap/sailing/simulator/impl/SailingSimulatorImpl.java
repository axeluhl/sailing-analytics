package com.sap.sailing.simulator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindFieldGenerator;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;

public class SailingSimulatorImpl implements SailingSimulator {

    private SimulationParameters simulationParameters;
    private Path racecourse;

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

    // private static Logger logger = Logger.getLogger("com.sap.sailing");

    @Override
    public Map<String, Path> getAllPaths() {

        //
        // Initialize WindFields boundary
        //
        WindFieldGenerator wf = simulationParameters.getWindField();
        int[] gridRes = wf.getGridResolution();
        Position[] gridArea = wf.getGridAreaGps();
        
        if (gridArea != null) {
            Boundary bd = new RectangularBoundary(gridArea[0],gridArea[1], 0.1);
            wf.getWindParameters().baseWindBearing += bd.getSouth().getDegrees();
            wf.setBoundary(bd);
            Position[][] positionGrid = bd.extractGrid(gridRes[0],gridRes[1]);
            wf.setPositionGrid(positionGrid);
            wf.generate(wf.getStartTime(), wf.getEndTime(), wf.getTimeStep());
        }
        
        //
        // Start Simulation
        //
        Map<String, Path> allPaths = new HashMap<String, Path>();

        if (simulationParameters.getMode() != SailingSimulatorUtil.measured) {

            // get 1-turners
            PathGenerator1Turner gen1Turner = new PathGenerator1Turner(simulationParameters);
            gen1Turner.setEvaluationParameters(true, null, null, 0, 0, 0);
            Path leftPath = gen1Turner.getPath();
            int left1TurnMiddle = gen1Turner.getMiddle();
            gen1Turner.setEvaluationParameters(false, null, null, 0, 0, 0);
            Path rightPath = gen1Turner.getPath();
            int right1TurnMiddle = gen1Turner.getMiddle();

            // get left- and right-going heuristic based on 1-turner
            PathGeneratorOpportunistEuclidian genOpportunistic = new PathGeneratorOpportunistEuclidian(simulationParameters);
            // PathGeneratorOpportunistVMG genOpportunistic = new PathGeneratorOpportunistVMG(simulationParameters);
            genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, true);
            Path oppPathL = genOpportunistic.getPath();
            genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, false);
            Path oppPathR = genOpportunistic.getPath();

            Path oppPath = null;
            // System.out.println("left -going: "+oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() -
            // 1).getTimePoint().asMillis());
            // System.out.println("right-going: "+oppPathR.getPathPoints().get(oppPathR.getPathPoints().size() -
            // 1).getTimePoint().asMillis());
            if (oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis() <= oppPathR.getPathPoints().get(oppPathR.getPathPoints().size() - 1).getTimePoint()
                    .asMillis()) {
                oppPath = oppPathL;
            } else {
                oppPath = oppPathR;
            }

            // get optimal path from dynamic programming with forward iteration
            PathGenerator genDynProgForward = new PathGeneratorDynProgForward(simulationParameters);
            Path optPath = genDynProgForward.getPath();

            //
            // NOTE: pathName convention is: sort-digit + "#" + path-name
            // The sort-digit defines the sorting of paths in webbrowser
            //

            // compare paths to avoid misleading display due to artifactual results from optimization (caused by finite
            // resolution of optimization grid)
            if (leftPath.getPathPoints() != null) {
                if (leftPath.getPathPoints().get(leftPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint()
                        .asMillis()) {
                    optPath = leftPath;
                }
                allPaths.put("3#1-Turner Left", leftPath);
            }

            if (rightPath.getPathPoints() != null) {
                if (rightPath.getPathPoints().get(rightPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint()
                        .asMillis()) {
                    optPath = rightPath;
                }
                allPaths.put("4#1-Turner Right", rightPath);
            }

            if (oppPath != null) {
                if (oppPath.getPathPoints().get(oppPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint()
                        .asMillis()) {
                    optPath = oppPath;
                }
                allPaths.put("2#Opportunistic", oppPath);
            }

            allPaths.put("1#Omniscient", optPath);

        } else {

            //
            // load examplary GPS-path
            //
            String raceURL = "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=d1f521fa-ec52-11e0-a523-406186cbf87c";
            // String raceURL =
            // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=eb06795a-ec52-11e0-a523-406186cbf87c";
            // String raceURL =
            // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=6bb0829e-ec44-11e0-a523-406186cbf87c";
            PathGeneratorTracTrac genTrac = new PathGeneratorTracTrac(simulationParameters);
            genTrac.setEvaluationParameters(raceURL, "tcp://10.18.206.73:1520", "tcp://10.18.206.73:1521");

            Path gpsPath = genTrac.getPath();
            Path gpsPathPoly = genTrac.getPathPolyline(new MeterDistance(4.88));
            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
            racecourse = genTrac.getRaceCourse();

        }

        return allPaths;
    }

    public Map<String, List<TimedPositionWithSpeed>> getAllPathsEvenTimed(long millisecondsStep) {

        Map<String, List<TimedPositionWithSpeed>> allTimedPaths = new TreeMap<String, List<TimedPositionWithSpeed>>();

        Map<String, Path> allPaths = this.getAllPaths();
        String[] allKeys = allPaths.keySet().toArray(new String[0]);
        for (String currentKey : allKeys) {
            allTimedPaths.put(currentKey, allPaths.get(currentKey).getEvenTimedPath(millisecondsStep));
        }

        return allTimedPaths;
    }

    public Path getRaceCourse() {
        return racecourse;
    }

}
