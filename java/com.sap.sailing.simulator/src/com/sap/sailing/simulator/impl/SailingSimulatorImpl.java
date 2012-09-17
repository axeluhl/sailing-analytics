package com.sap.sailing.simulator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

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

    // private static Logger logger = Logger.getLogger("com.sap.sailing");

    @Override
    public Map<String, Path> getAllPaths() {

        Map<String, Path> allPaths = new HashMap<String, Path>();

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
        //PathGeneratorOpportunistVMG genOpportunistic = new PathGeneratorOpportunistVMG(simulationParameters);
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, true);
        Path oppPathL = genOpportunistic.getPath();
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, false);
        Path oppPathR = genOpportunistic.getPath();

        Path oppPath = null;
        //System.out.println("left -going: "+oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis());
        //System.out.println("right-going: "+oppPathR.getPathPoints().get(oppPathR.getPathPoints().size() - 1).getTimePoint().asMillis());
        if (oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis() <= oppPathR
                .getPathPoints().get(oppPathR.getPathPoints().size() - 1).getTimePoint().asMillis()) {
            oppPath = oppPathL;
        } else {
            oppPath = oppPathR;
        }

        // get optimal path from dynamic programming with forward iteration
        PathGenerator genDynProgForward = new PathGeneratorDynProgForward(simulationParameters);
        Path optPath = genDynProgForward.getPath();

        //
        // NOTE: pathName convention is: sort-digit + "#" + path-name
        //       The sort-digit defines the sorting of paths in webbrowser
        //
        
        // compare paths to avoid misleading display due to artifactual results from optimization (caused by finite
        // resolution of optimization grid)
        if (leftPath.getPathPoints() != null) {
            if (leftPath.getPathPoints().get(leftPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath
                    .getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                optPath = leftPath;
            }
            allPaths.put("3#1-Turner Left", leftPath);
        }

        if (rightPath.getPathPoints() != null) {
            if (rightPath.getPathPoints().get(rightPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath
                    .getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                optPath = rightPath;
            }
            allPaths.put("4#1-Turner Right", rightPath);
        }

        if (oppPath != null) {
            if (oppPath.getPathPoints().get(oppPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath
                    .getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                optPath = oppPath;
            }
            allPaths.put("2#Opportunistic", oppPath);
            //allPaths.put("5#Opportunist Left", oppPathL);
            //allPaths.put("6#Opportunist Right", oppPathR);
        }

        allPaths.put("1#Omniscient", optPath);

        return allPaths;
    }

    public Map<String, List<TimedPositionWithSpeed>> getAllPathsEvenTimed(long millisecondsStep) {

        Map<String, List<TimedPositionWithSpeed>> allTimedPaths = new TreeMap<String, List<TimedPositionWithSpeed>>();

        Map<String, Path> allPaths = this.getAllPaths();
        String[] allKeys = allPaths.keySet().toArray(new String[0]);
        for (String currentKey : allKeys) {
            allTimedPaths.put(currentKey, allPaths.get(currentKey).getEvenTimedPath(millisecondsStep));
            //allTimedPaths.put(currentKey, allPaths.get(currentKey).getEvenTimedPoints(millisecondsStep));
            //allTimedPaths.put(currentKey, allPaths.get(currentKey).getPathPoints());
        }

        return allTimedPaths;
    }

}
