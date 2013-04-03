package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Quadruple;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulatorUISelection;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

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
        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
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
        // Path gpsPath = null;
        // Path gpsPathPoly = null;

        /*
         * if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
         * 
         * allPaths = this.readPathsFromResources(); if (allPaths != null && allPaths.isEmpty() == false &&
         * allPaths.size() == 6) { return allPaths; }
         * 
         * PathGeneratorTracTrac genTrac = new PathGeneratorTracTrac(this.simulationParameters);
         * genTrac.setEvaluationParameters(raceURL, liveURI, storedURI, windScale);
         * 
         * gpsPath = genTrac.getPath(); gpsPathPoly = genTrac.getPathPolyline(new MeterDistance(4.88));
         * allPaths.put("6#GPS Poly", gpsPathPoly); allPaths.put("7#GPS Track", gpsPath); this.racecourse =
         * genTrac.getRaceCourse();
         * 
         * }
         */

        //
        // Initialize WindFields boundary
        //
        WindFieldGenerator wf = this.simulationParameters.getWindField();
        int[] gridRes = wf.getGridResolution();
        Position[] gridArea = wf.getGridAreaGps();
        /*
         * if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) { ((WindFieldGeneratorMeasured)
         * wf).setGPSWind(gpsPath); gridArea = new Position[2]; gridArea[0] =
         * this.racecourse.getPathPoints().get(0).getPosition(); gridArea[1] =
         * this.racecourse.getPathPoints().get(1).getPosition(); List<Position> course = new ArrayList<Position>();
         * course.add(gridArea[0]); course.add(gridArea[1]); this.simulationParameters.setCourse(course); }
         */

        if (gridArea != null) {
            Boundary bd = new RectangularBoundary(gridArea[0], gridArea[1], 0.1);

            // set base wind bearing
            wf.getWindParameters().baseWindBearing += bd.getSouth().getDegrees();
            // System.out.println("baseWindBearing: " + wf.getWindParameters().baseWindBearing);
            LOGGER.info("base wind: " + this.simulationParameters.getBoatPolarDiagram().getWind().getKnots() + " kn, "
                    + ((wf.getWindParameters().baseWindBearing) % 360.0) + "°");

            // initialize interpolation table for getSpeedAtBearingOverGround, e.g. for what-if or for optimization on
            // overground-grids
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
                        + this.simulationParameters.getBoatPolarDiagram().getCurrent().getBearing().getDegrees() + "°");
            }

            wf.setBoundary(bd);
            Position[][] positionGrid = bd.extractGrid(gridRes[0], gridRes[1]);
            wf.setPositionGrid(positionGrid);
            wf.generate(wf.getStartTime(), wf.getEndTime(), wf.getTimeStep());
        }

        //
        // Start Simulation
        //

        // get instance of heuristic searcher
        PathGeneratorTreeGrowWind3 genTreeGrow = new PathGeneratorTreeGrowWind3(this.simulationParameters);

        // search best left-starting 1-turner
        genTreeGrow.setEvaluationParameters("L", 1, null);
        Path leftPath = genTreeGrow.getPath();
        PathCandidate leftBestCand = genTreeGrow.getBestCand();
        int left1TurnMiddle = 1000;
        if (leftBestCand != null) {
            left1TurnMiddle = leftBestCand.path.indexOf("LR");
        }

        // search best right-starting 1-turner
        genTreeGrow.setEvaluationParameters("R", 1, null);
        Path rightPath = genTreeGrow.getPath();
        PathCandidate rightBestCand = genTreeGrow.getBestCand();
        int right1TurnMiddle = 1000;
        if (rightBestCand != null) {
            right1TurnMiddle = rightBestCand.path.indexOf("RL");
        }

        // search best multi-turn course
        genTreeGrow.setEvaluationParameters(null, 0, null);
        Path optPath = genTreeGrow.getPath();

        // evaluate opportunistic heuristic
        PathGeneratorOpportunistEuclidian genOpportunistic = new PathGeneratorOpportunistEuclidian(this.simulationParameters);
        // PathGeneratorOpportunistVMG genOpportunistic = new PathGeneratorOpportunistVMG(simulationParameters);

        // left-starting opportunist
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, true);
        Path oppPathL = genOpportunistic.getPath();
        // right-starting opportunist
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, false);
        Path oppPathR = genOpportunistic.getPath();

        // compare left- & right-starting opportunists
        Path oppPath = null;
        if (oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis() <= oppPathR.getPathPoints()
                .get(oppPathR.getPathPoints().size() - 1).getTimePoint().asMillis()) {
            oppPath = oppPathL;
        } else {
            oppPath = oppPathR;
        }

        //
        // NOTE: pathName convention is: sort-digit + "#" + path-name
        // The sort-digit defines the sorting of paths in webbrowser
        //

        boolean plausCheck = false;
        // ensure omniscient is best avoiding artifactual results due to coarse-grainedness (finite timesteps) of course
        // generation
        if (plausCheck) {
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

        allPaths.put("4#1-Turner Right", rightPath);
        allPaths.put("3#1-Turner Left", leftPath);
        allPaths.put("2#Opportunistic", oppPath);
        allPaths.put("1#Omniscient", optPath);

        /*
         * if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) { this.savePathsToFiles(allPaths);
         * }
         */

        return allPaths;
    }


    @Override
    public Map<String, Path> getAllPathsForLeg(SimulatorUISelection selection) {

        this.setSimulationParameters(this.simulationParameters, selection);

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        Pair<Map<String, Path>, Path> result = SimulatorUtils.readLegPathsFromResources(selection.getRaceIndex(), selection.getCompetitorIndex(),
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

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
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

        for (Quadruple<String, String, String, Integer> raceInfo : ConfigurationManager.INSTANCE.getRacesInfo()) {
            racesNames.add(raceInfo.getA());
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
            SimulatorUtils.saveToFile(path, SimulatorUtils.getPathPrefix() + "\\src\\resources\\" + fileName);
        }

        return path;
    }
}
