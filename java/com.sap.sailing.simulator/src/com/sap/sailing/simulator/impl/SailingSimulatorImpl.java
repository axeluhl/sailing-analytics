package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorMeasured;

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

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;
        
        if (simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            
            //remove this into release mode
            allPaths = readPathsFromFiles();
            if(allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6)
            {
                System.out.println("Readding of all paths from external files succeded!");
                return allPaths;
            }
            
            //
            // load examplary GPS-path
            //
            String raceURL = "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=d1f521fa-ec52-11e0-a523-406186cbf87c";
            //String raceURL = "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=eb06795a-ec52-11e0-a523-406186cbf87c";
            //String raceURL = "http://germanmaster.traclive.dk/events/event_20120615_KielerWoch/clientparams.php?event=event_20120615_KielerWoch&race=0b5969cc-b789-11e1-a845-406186cbf87c";
            // String raceURL =
            // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=6bb0829e-ec44-11e0-a523-406186cbf87c";
            PathGeneratorTracTrac genTrac = new PathGeneratorTracTrac(simulationParameters);

            // proxy configuration
            //genTrac.setEvaluationParameters(raceURL, "tcp://10.18.22.156:1520", "tcp://10.18.22.156:1521", 4.5); // new tunnel-ip
            // no-proxy configuration
            genTrac.setEvaluationParameters(raceURL, "tcp://germanmaster.traclive.dk:4400", "tcp://germanmaster.traclive.dk:4401", 4.5); // new tunnel-ip
            
            gpsPath = genTrac.getPath();
            gpsPathPoly = genTrac.getPathPolyline(new MeterDistance(4.88));
            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
            racecourse = genTrac.getRaceCourse();

        }

        //
        // Initialize WindFields boundary
        //
        WindFieldGenerator wf = simulationParameters.getWindField();
        int[] gridRes = wf.getGridResolution();
        Position[] gridArea = wf.getGridAreaGps();
        if (simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            ((WindFieldGeneratorMeasured)wf).setGPSWind(gpsPath);
            gridArea = new Position[2];
            gridArea[0] = racecourse.getPathPoints().get(0).getPosition();
            gridArea[1] = racecourse.getPathPoints().get(1).getPosition();
            List<Position> course = new ArrayList<Position>();
            course.add(gridArea[0]);
            course.add(gridArea[1]);
            simulationParameters.setCourse(course);
        }
        
        if (gridArea != null) {
            Boundary bd = new RectangularBoundary(gridArea[0],gridArea[1], 0.1);
            wf.getWindParameters().baseWindBearing += bd.getSouth().getDegrees();
            //System.out.println("baseWindBearing: " + wf.getWindParameters().baseWindBearing);
            wf.setBoundary(bd);
            Position[][] positionGrid = bd.extractGrid(gridRes[0],gridRes[1]);
            wf.setPositionGrid(positionGrid);
            wf.generate(wf.getStartTime(), wf.getEndTime(), wf.getTimeStep());
        }
        
        //
        // Start Simulation
        //

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
        if (oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis() <= oppPathR.getPathPoints().get(oppPathR.getPathPoints().size() - 1).getTimePoint().asMillis()) {
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
            if (oppPath.getPathPoints().get(oppPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                optPath = oppPath;
            }
            allPaths.put("2#Opportunistic", oppPath);
        }

        allPaths.put("1#Omniscient", optPath);
        
        if (simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            //remove this into release mode        
            savePathsToFiles(allPaths);
        }
        
        return allPaths;
    }

    public Map<String, List<TimedPositionWithSpeed>> getAllPathsEvenTimed(long millisecondsStep) {

        Map<String, List<TimedPositionWithSpeed>> allTimedPaths = new TreeMap<String, List<TimedPositionWithSpeed>>();

        Map<String, Path> allPaths = this.getAllPaths();
        for (Entry<String, Path> entry : allPaths.entrySet()) {
            String key = entry.getKey();
            Path value = entry.getValue();
            allTimedPaths.put(key, value.getEvenTimedPath(millisecondsStep));
        }

        return allTimedPaths;
    }

    public Path getRaceCourse() {
        return racecourse;
    }

    @Override
    public Map<String, Path> getAllPathsEvenTimed2(long millisecondsStep) {
        Map<String, Path> allTimedPaths = new TreeMap<String, Path>();
        Map<String, Path> allPaths = this.getAllPaths();
        
        for(Entry<String, Path> entry : allPaths.entrySet()) {
                String key = entry.getKey();
                Path value = entry.getValue();
                allTimedPaths.put(key, value.getEvenTimedPath2(millisecondsStep));
        }
        
        return allTimedPaths;
    }

    //I077899 - Mihai Bogdan Eugen
    private boolean savePathsToFiles(Map<String, Path> paths) {
        
        if(paths == null)
                return false;
        
        if(paths.isEmpty())
                return true;
        
        boolean result = true;
        
        result &= SimulatorUtils.saveToFile((Path)paths.get("1#Omniscient"), "C:\\1#Omniscient.dat");
        result &= SimulatorUtils.saveToFile((Path)paths.get("2#Opportunistic"), "C:\\2#Opportunistic.dat");
        result &= SimulatorUtils.saveToFile((Path)paths.get("3#1-Turner Left"), "C:\\3#1-Turner Left.dat");
        
        result &= SimulatorUtils.saveToFile((Path)this.racecourse, "C:\\racecourse.dat");
        
        result &= SimulatorUtils.saveToFile((Path)paths.get("4#1-Turner Right"), "C:\\4#1-Turner Right.dat");
        result &= SimulatorUtils.saveToFile((Path)paths.get("6#GPS Poly"), "C:\\6#GPS Poly.dat");
        result &= SimulatorUtils.saveToFile((Path)paths.get("7#GPS Track"), "C:\\7#GPS Track.dat");
        
        return result;
    }
    
    //I077899 - Mihai Bogdan Eugen
    private Map<String, Path> readPathsFromFiles() {
        
        HashMap<String, Path> paths = new HashMap<String, Path>();
        
        Path path = null;
        
        path = SimulatorUtils.readFromExternalFile("C:\\1#Omniscient.dat");
        if(path != null) {
                paths.put("1#Omniscient", path);
        }
        
        path = SimulatorUtils.readFromExternalFile("C:\\2#Opportunistic.dat");
        if(path != null) {
                paths.put("2#Opportunistic", path);
        }
        
        path = SimulatorUtils.readFromExternalFile("C:\\3#1-Turner Left.dat");
        if(path != null) {
                paths.put("3#1-Turner Left", path);
        }
        
        this.racecourse = SimulatorUtils.readFromExternalFile("C:\\racecourse.dat");
        
        path = SimulatorUtils.readFromExternalFile("C:\\4#1-Turner Right.dat");
        if(path != null) {
                paths.put("4#1-Turner Right", path);
        }
        
        path = SimulatorUtils.readFromExternalFile("C:\\6#GPS Poly.dat");
        if(path != null) {
                paths.put("6#GPS Poly", path);
        }
        
        path = SimulatorUtils.readFromExternalFile("C:\\7#GPS Track.dat");
        if(path != null) {
                paths.put("7#GPS Track", path);
        }
        
        return paths;
    }
}
