package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Quadruple;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.util.SerializationUtils;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorMeasured;

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

        this.initializePathGenerator(parameters, 0);
    }

    private void initializePathGenerator(SimulationParameters parameters, int selectedRaceIndex) {

        // System.out.println("initializing PathGeneratorTracTrac");

        this.pathGenerator = new PathGeneratorTracTrac(parameters);
        this.pathGenerator.setEvaluationParameters(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex), LIVE_URI, STORED_URI, WIND_SCALE);
    }

    @Override
    public void setSimulationParameters(SimulationParameters parameters, int selectedRaceIndex) {
        this.simulationParameters = parameters;
        this.initializePathGenerator(parameters, selectedRaceIndex);
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

    private Path getFromResourcesOrDownload(String pathName, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) throws Exception {

        String fileName = SerializationUtils.getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, pathName);

        Path path = (Path) SerializationUtils.readObjectFromResources(fileName);
        if (path == null) {
            if (pathName.equals("6#GPS Poly")) {
                path = this.pathGenerator.getPathPolyline(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, new MeterDistance(4.88));
            } else if (pathName.equals("7#GPS Track")) {
                path = this.pathGenerator.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
            } else if (pathName.equals("raceCourse")) {
                path = this.pathGenerator.getRaceCourse();
            } else {
                throw new Exception("Unknown path name!");
            }
            SerializationUtils.saveToFile(path, SerializationUtils.getPathPrefix() + "\\src\\resources\\" + fileName);
        }

        return path;
    }

    @Override
    public Map<String, Path> getAllPathsForLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        this.setSimulationParameters(this.simulationParameters, selectedRaceIndex);

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {

            Pair<Map<String, Path>, Path> result = SerializationUtils.readLegPathsFromResources(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
            allPaths = result.getA();
            this.raceCourse = result.getB();

            if (allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6) {
                return allPaths;
            }

            try {
                gpsPath = this.getFromResourcesOrDownload("7#GPS Track", selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
                gpsPathPoly = this.getFromResourcesOrDownload("6#GPS Poly", selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
                this.raceCourse = this.getFromResourcesOrDownload("raceCourse", selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
            }

            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
        }

        //
        // Initialize WindFields boundary
        //
        WindFieldGenerator wf = this.simulationParameters.getWindField();
        int[] gridRes = wf.getGridResolution();
        Position[] gridArea = wf.getGridAreaGps();
        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            ((WindFieldGeneratorMeasured) wf).setGPSWind(gpsPath);
            gridArea = new Position[2];

            List<TimedPositionWithSpeed> pathPoints = this.raceCourse.getPathPoints();

            gridArea[0] = pathPoints.get(0).getPosition();
            gridArea[1] = pathPoints.get(1).getPosition();
            List<Position> course = new ArrayList<Position>();
            course.add(gridArea[0]);
            course.add(gridArea[1]);
            this.simulationParameters.setCourse(course);
        }

        if (gridArea != null) {
            Boundary bd = new RectangularBoundary(gridArea[0], gridArea[1], 0.1);

            // set base wind bearing
            wf.getWindParameters().baseWindBearing += bd.getSouth().getDegrees();
            //System.out.println("baseWindBearing: " + wf.getWindParameters().baseWindBearing);
            LOGGER.info("base wind: " + this.simulationParameters.getBoatPolarDiagram().getWind().getKnots() + " kn, "
                    + ((wf.getWindParameters().baseWindBearing) % 360.0) + "°");

            // initialize interpolation table for getSpeedAtBearingOverGround, e.g. for what-if or for optimization on overground-grids
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(null); // initialize

            // set water current
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(0.0,new DegreeBearingImpl((wf.getWindParameters().baseWindBearing+90.0)%360.0)));
            this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(wf.getWindParameters().curSpeed, new DegreeBearingImpl(wf.getWindParameters().curBearing)));
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(2.0,new DegreeBearingImpl((270.0)%360.0)));
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
        Path leftPath = genTreeGrow.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        PathCandidate leftBestCand = genTreeGrow.getBestCand();
        int left1TurnMiddle = 1000;
        if (leftBestCand != null) {
            left1TurnMiddle = leftBestCand.path.indexOf("LR");
        }

        // search best right-starting 1-turner
        genTreeGrow.setEvaluationParameters("R", 1, null);
        Path rightPath = genTreeGrow.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        PathCandidate rightBestCand = genTreeGrow.getBestCand();
        int right1TurnMiddle = 1000;
        if (rightBestCand != null) {
            right1TurnMiddle = rightBestCand.path.indexOf("RL");
        }

        // search best multi-turn course
        genTreeGrow.setEvaluationParameters(null, 0, null);
        Path optPath = genTreeGrow.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);


        // evaluate opportunistic heuristic
        PathGeneratorOpportunistEuclidian genOpportunistic = new PathGeneratorOpportunistEuclidian(this.simulationParameters);
        // PathGeneratorOpportunistVMG genOpportunistic = new PathGeneratorOpportunistVMG(simulationParameters);

        // left-starting opportunist
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, true);
        Path oppPathL = genOpportunistic.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        // right-starting opportunist
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, false);
        Path oppPathR = genOpportunistic.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);

        // compare left- & right-starting opportunists
        Path oppPath = null;
        if (oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis() <= oppPathR
                .getPathPoints().get(oppPathR.getPathPoints().size() - 1).getTimePoint().asMillis()) {
            oppPath = oppPathL;
        } else {
            oppPath = oppPathR;
        }

        //
        // NOTE: pathName convention is: sort-digit + "#" + path-name
        // The sort-digit defines the sorting of paths in webbrowser
        //

        boolean plausCheck = false;
        // ensure omniscient is best avoiding artifactual results due to coarse-grainedness (finite timesteps) of course generation
        if (plausCheck) {
            if (leftPath.getPathPoints() != null) {
                if (leftPath.getPathPoints().get(leftPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint()
                        .asMillis()) {
                    optPath = leftPath;
                }
            }

            if (rightPath.getPathPoints() != null) {
                if (rightPath.getPathPoints().get(rightPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint()
                        .asMillis()) {
                    optPath = rightPath;
                }
            }

            if (oppPath != null) {
                if (oppPath.getPathPoints().get(oppPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= optPath.getPathPoints().get(optPath.getPathPoints().size() - 1).getTimePoint()
                        .asMillis()) {
                    optPath = oppPath;
                }
            }
        }

        allPaths.put("4#1-Turner Right", rightPath);
        allPaths.put("3#1-Turner Left", leftPath);
        allPaths.put("2#Opportunistic", oppPath);
        allPaths.put("1#Omniscient", optPath);

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            SerializationUtils.saveLegPathsToFiles(allPaths, this.raceCourse, selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        }

        return allPaths;
    }

    @Override
    public Path getRaceCourse() {
        return this.raceCourse;
    }

    @Override
    public Map<String, Path> getAllLegPathsEvenTimed(long millisecondsStep, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        Map<String, Path> allTimedPaths = new TreeMap<String, Path>();
        Map<String, Path> allPaths = this.getAllPathsForLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);

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
    public Path getLegGPSTrack(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        String fileName = SerializationUtils.getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, "7#GPS Track");

        Path path = (Path) SerializationUtils.readObjectFromResources("resources/" + fileName);
        if (path == null) {
            System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");
            LOGGER.warning("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");

            path = this.pathGenerator.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        }

        return path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getLegsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, selectedRaceIndex);

        List<String> legsNames = null;
        String fileName = SerializationUtils.getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + SerializationUtils.LEGSNAMES_DAT;

        legsNames = (List<String>) SerializationUtils.readObjectFromResources("resources/" + fileName);
        if (legsNames != null && legsNames.isEmpty() == false) {
            return legsNames;
        }

        legsNames = this.pathGenerator.getLegsNames();

        SerializationUtils.saveStringListToFiles(legsNames, fileName);

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
    public Path getLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        return this.pathGenerator.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getComeptitorsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, selectedRaceIndex);

        List<String> competitorsNames = null;
        String fileName = SerializationUtils.getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_"
                + SerializationUtils.COMPETITORSNAMES_DAT;

        competitorsNames = (List<String>) SerializationUtils.readObjectFromResources("resources/" + fileName);
        if (competitorsNames != null && competitorsNames.isEmpty() == false) {
            return competitorsNames;
        }

        competitorsNames = this.pathGenerator.getComeptitorsNames();

        SerializationUtils.saveStringListToFiles(competitorsNames, fileName);

        return competitorsNames;
    }
}
