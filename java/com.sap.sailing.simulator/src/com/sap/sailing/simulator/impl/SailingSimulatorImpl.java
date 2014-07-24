package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.simulator.Grid;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.RaceProperties;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulatorUISelection;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sse.common.Util;

public class SailingSimulatorImpl implements SailingSimulator {

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing.simulator");

    private SimulationParameters simulationParameters = null;
    private Path raceCourse = null;
    private PathGeneratorTracTrac pathGenerator = null;
    
    private static final double WIND_SCALE = 4.5;

    // proxy configuration
    private static final String LIVE_URI = "tcp://10.18.22.156:1520";

    // no-proxy configuration
    // private static final String LIVE_URI = "tcp://germanmaster.traclive.dk:4400";

    // proxy configuration
    private static final String STORED_URI = "tcp://10.18.22.156:1521";

    // no-proxy configuration
    // private static final String STORED_URI = "tcp://germanmaster.traclive.dk:4401";

    public SailingSimulatorImpl(SimulationParameters parameters) {
        this.simulationParameters = parameters;
        if (this.simulationParameters.getMode() == SailingSimulatorConstants.ModeMeasured) {
            this.initializePathGenerator(parameters, 0, 0, 0);
        }
    }

    @Override
    public void setSimulationParameters(SimulationParameters parameters, SimulatorUISelection selection) {

        this.simulationParameters = parameters;
        this.initializePathGenerator(parameters, selection.getRaceIndex(), selection.getLegIndex(), selection.getCompetitorIndex());
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

    @Override
    public Map<String, Path> getAllPaths() {

        Map<String, Path> allPaths = new HashMap<String, Path>();

        //
        // Initialize WindFields boundary
        //
        WindFieldGenerator wf = this.simulationParameters.getWindField();
        int[] gridRes = wf.getGridResolution();
        Position[] gridArea = wf.getGridAreaGps();
        
        LOGGER.info("showOmniscient : "+this.simulationParameters.showOmniscient());
        LOGGER.info("showOpportunist: "+this.simulationParameters.showOpportunist());        

        if (gridArea != null) {
            Grid bd = new CurvedGrid(gridArea[0], gridArea[1]);

            // set base wind bearing
            wf.getWindParameters().baseWindBearing += bd.getSouth().getDegrees();
            // System.out.println("baseWindBearing: " + wf.getWindParameters().baseWindBearing);
            LOGGER.info("base wind: " + this.simulationParameters.getBoatPolarDiagram().getWind().getKnots() + " kn, "
                    + ((wf.getWindParameters().baseWindBearing) % 360.0) + "�");

            // initialize interpolation table for getSpeedAtBearingOverGround, e.g. for what-if or for optimization on overground-grids
            // this.simulationParameters.getBoatPolarDiagram().setCurrent(null); // initialize

            // set water current
            // this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(0.0,new
            // DegreeBearingImpl((wf.getWindParameters().baseWindBearing+90.0)%360.0)));
            this.simulationParameters.getBoatPolarDiagram().setCurrent(
                    new KnotSpeedWithBearingImpl(wf.getWindParameters().curSpeed, new DegreeBearingImpl(wf.getWindParameters().curBearing)));
            // this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(2.0,new
            // DegreeBearingImpl((270.0)%360.0)));
            if (this.simulationParameters.getBoatPolarDiagram().getCurrent() != null) {
                LOGGER.info("water current: " + this.simulationParameters.getBoatPolarDiagram().getCurrent().getKnots() + " kn, "
                        + this.simulationParameters.getBoatPolarDiagram().getCurrent().getBearing().getDegrees() + "�");
            }

            wf.setBoundary(bd);
            Position[][] positionGrid = bd.generatePositions(gridRes[0], gridRes[1], gridRes[2], gridRes[3]);
            wf.setPositionGrid(positionGrid);
            wf.generate(wf.getStartTime(), wf.getEndTime(), wf.getTimeStep());
        }

        //
        // Start Simulation
        //

        // get instance of heuristic searcher
        PathGeneratorTreeGrowWind genTreeGrow = new PathGeneratorTreeGrowWind(this.simulationParameters);

        // search best left-starting 1-turner
        genTreeGrow.setEvaluationParameters("L", 1, null);
        Path leftPath = genTreeGrow.getPath();
        PathCandidate leftBestCand = genTreeGrow.getBestCand();
        int left1TurnMiddle = 1000;
        long left1TurnMidtime = 1000000000;
        long left1TurnTimestep = genTreeGrow.getUsedTimeStep(); 
        if (leftBestCand != null) {
            left1TurnMiddle = leftBestCand.path.indexOf("LR");
            left1TurnMidtime = left1TurnMiddle * left1TurnTimestep; 
        }

        // search best right-starting 1-turner
        genTreeGrow.setEvaluationParameters("R", 1, null);
        Path rightPath = genTreeGrow.getPath();
        PathCandidate rightBestCand = genTreeGrow.getBestCand();
        int right1TurnMiddle = 1000;
        long right1TurnMidtime = 1000000000;
        long right1TurnTimestep = genTreeGrow.getUsedTimeStep(); 
        if (rightBestCand != null) {
            right1TurnMiddle = rightBestCand.path.indexOf("RL");
            right1TurnMidtime = right1TurnMiddle * right1TurnTimestep; 
        }

        Path optPath = null;
        if (this.simulationParameters.showOmniscient()) {
        	// search best multi-turn course
        	genTreeGrow.setEvaluationParameters(null, 0, null);
        	optPath = genTreeGrow.getPath();
        }
        
    	Path oppPath = null;
    	Path oppPathL = null;
    	Path oppPathR = null;
        if (this.simulationParameters.showOpportunist()) {
        	// evaluate opportunistic heuristic
        	PathGeneratorOpportunistEuclidian genOpportunistic = new PathGeneratorOpportunistEuclidian(this.simulationParameters);
        	// PathGeneratorOpportunistVMG genOpportunistic = new PathGeneratorOpportunistVMG(simulationParameters);

        	// left-starting opportunist
        	genOpportunistic.setEvaluationParameters(left1TurnMidtime, right1TurnMidtime, true);
        	oppPathL = genOpportunistic.getPath();
        	if (genOpportunistic.getTurns() == 1) {
        		oppPathL = leftPath;
        	}
        	
        	// right-starting opportunist
        	genOpportunistic.setEvaluationParameters(left1TurnMidtime, right1TurnMidtime, false);
        	oppPathR = genOpportunistic.getPath();
        	if (genOpportunistic.getTurns() == 1) {
        		oppPathR = rightPath;
        	}

        	// compare left- & right-starting opportunists
        	if (oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis() <= oppPathR.getPathPoints()
        			.get(oppPathR.getPathPoints().size() - 1).getTimePoint().asMillis()) {
        		oppPath = oppPathL;
        	} else {
        		oppPath = oppPathR;
        	}
        }

        //
        // NOTE: pathName convention is: sort-digit + "#" + path-name
        // The sort-digit defines the sorting of paths in webbrowser
        //

        boolean plausCheck = false;
        // ensure omniscient is best avoiding artifactual results due to coarse-grainedness (finite timesteps) of course
        // generation
        if ((plausCheck)&&(this.simulationParameters.showOmniscient())) {
            if (leftPath.getPathPoints() != null) {
                if (leftPath.getPathPoints().get(leftPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints()
                        .get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                    optPath = leftPath;
                }
            }

            if (rightPath.getPathPoints() != null) {
                if (rightPath.getPathPoints().get(rightPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints()
                        .get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                    optPath = rightPath;
                }
            }

            if (oppPath != null) {
                if (oppPath.getPathPoints().get(oppPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints()
                        .get(optPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                    optPath = oppPath;
                }
            }
        }

        allPaths.put("5#1-Turner Right", rightPath);
        allPaths.put("4#1-Turner Left", leftPath);
        if (this.simulationParameters.showOpportunist()) {
        	allPaths.put("3#Opportunist Right", oppPathR);
        	allPaths.put("2#Opportunist Left", oppPathL);
        }        
        if (this.simulationParameters.showOmniscient()) {
        	allPaths.put("1#Omniscient", optPath);
        }

        return allPaths;
    }


    @Override
    public Map<String, Path> getAllPathsForLeg(SimulatorUISelection selection) {

        this.setSimulationParameters(this.simulationParameters, selection);

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        Util.Pair<Map<String, Path>, Path> result = SimulatorUtils.readLegPathsFromResources(selection.getRaceIndex(), selection.getCompetitorIndex(),
                selection.getLegIndex());
        allPaths = result.getA();
        this.raceCourse = result.getB();

        if (allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6) {
            return allPaths;
        }

        try {
            gpsPath = this.getFromResourcesOrDownload("7#GPS Track", selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex());
            gpsPathPoly = this.getFromResourcesOrDownload("6#GPS Poly", selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex());
            this.raceCourse = this.getFromResourcesOrDownload("raceCourse", selection.getRaceIndex(), selection.getCompetitorIndex(),
                    selection.getLegIndex());
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        allPaths.put("6#GPS Poly", gpsPathPoly);
        allPaths.put("7#GPS Track", gpsPath);

        allPaths.putAll(SimulatorUtils.getSimulationPaths(this.simulationParameters, gpsPath, this.raceCourse));

        if (this.simulationParameters.getMode() == SailingSimulatorConstants.ModeMeasured) {
            SimulatorUtils.saveLegPathsToFiles(allPaths, this.raceCourse, selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex());
        }

        return allPaths;
    }

    @Override
    public Path getRaceCourse() {
        return this.raceCourse;
    }

    @Override
    public Map<String, Path> getAllPathsEvenTimed(long millisecondsStep, SimulatorUISelection selection) {

        Map<String, Path> allTimedPaths = new TreeMap<String, Path>();

        Map<String, Path> allPaths = selection == null ? this.getAllPaths() : this.getAllPathsForLeg(selection);

        for (Entry<String, Path> entry : allPaths.entrySet()) {

            String pathName = entry.getKey();
            Path value = entry.getValue();

            if (pathName.equals("7#GPS Track")) {
                allTimedPaths.put(pathName, value);
            } else {
                allTimedPaths.put(pathName, value.getEvenTimedPath(millisecondsStep));
            }
        }

        return allTimedPaths;
    }

    @Override
    public Path getLegGPSTrack(SimulatorUISelection selection) {
        //
        String fileName = SimulatorUtils.getFileName(selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex(), "7#GPS Track");

        Path path = (Path) SimulatorUtils.readObjectFromResources("resources/" + fileName);
        if (path == null) {
            System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");
            LOGGER.warning("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");

            this.pathGenerator.setSelectionParameters(selection.getLegIndex(), selection.getCompetitorIndex());
            path = this.pathGenerator.getPath();
        }

        return path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getLegsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, new SimulatorUISelectionImpl(0, selectedRaceIndex, 0, 0));

        List<String> legsNames = null;
        String fileName = SimulatorUtils.getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + SimulatorUtils.LEGSNAMES_DAT;

        legsNames = (List<String>) SimulatorUtils.readObjectFromResources("resources/" + fileName);
        if (legsNames != null && legsNames.isEmpty() == false) {
            return legsNames;
        }

        legsNames = this.pathGenerator.getLegsNames();

        SimulatorUtils.saveStringListToFiles(legsNames, fileName);

        return legsNames;
    }

    @Override
    public List<String> getRacesNames() {

        List<String> racesNames = new ArrayList<String>();

        for (RaceProperties raceInfo : ConfigurationManager.INSTANCE.getRacesInfo()) {
            racesNames.add(raceInfo.getName());
        }

        return racesNames;
    }

    @Override
    public Path getLeg(int selectedCompetitorIndex, int selectedLegIndex) {

        this.pathGenerator.setSelectionParameters(selectedLegIndex, selectedCompetitorIndex);
        return this.pathGenerator.getPath();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getComeptitorsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, new SimulatorUISelectionImpl(0, selectedRaceIndex, 0, 0));

        List<String> competitorsNames = null;
        String fileName = SimulatorUtils.getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + SimulatorUtils.COMPETITORSNAMES_DAT;

        competitorsNames = (List<String>) SimulatorUtils.readObjectFromResources("resources/" + fileName);
        if (competitorsNames != null && competitorsNames.isEmpty() == false) {
            return competitorsNames;
        }

        competitorsNames = this.pathGenerator.getComeptitorsNames();

        SimulatorUtils.saveStringListToFiles(competitorsNames, fileName);

        return competitorsNames;
    }

    private void initializePathGenerator(SimulationParameters parameters, int selectedRaceIndex, int selectedLegIndex, int selectedCompetitorIndex) {

        this.pathGenerator = new PathGeneratorTracTrac(parameters);
        this.pathGenerator.setEvaluationParameters(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex), LIVE_URI, STORED_URI, WIND_SCALE);
        this.pathGenerator.setSelectionParameters(selectedLegIndex, selectedCompetitorIndex);
    }

    private Path getFromResourcesOrDownload(String pathName, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) throws Exception {

        String fileName = SimulatorUtils.getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, pathName);

        Path path = (Path) SimulatorUtils.readObjectFromResources(fileName);
        if (path == null) {
            if (pathName.equals("6#GPS Poly")) {
                path = this.pathGenerator.getPathPolyline(new MeterDistance(4.88));
            } else if (pathName.equals("7#GPS Track")) {
                path = this.pathGenerator.getPath();
            } else if (pathName.equals("raceCourse")) {
                path = this.pathGenerator.getRaceCourse();
            } else {
                throw new Exception("Unknown path name!");
            }
            SimulatorUtils.saveToFile(path, SimulatorUtils.getPathPrefix() + "\\resources\\" + fileName);
        }

        return path;
    }
}
