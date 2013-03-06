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

    /**
     * Internationale Deutche Meisterschaft, 49er Race4
     */
    private static final String RACE_URL = "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=d1f521fa-ec52-11e0-a523-406186cbf87c";

    /**
     * Internationale Deutche Meisterschaft, 49er Race5
     */
    // private static final String RACE_URL =
    // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=eb06795a-ec52-11e0-a523-406186cbf87c";

    /**
     * Internationale Deutche Meisterschaft, Star Race4
     */
    // private static final String RACE_URL =
    // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=6bb0829e-ec44-11e0-a523-406186cbf87c";

    /**
     * Kieler Woche 2012, 49er Yellow - Race 1
     */
    // private static final String RACE_URL =
    // "http://germanmaster.traclive.dk/events/event_20120615_KielerWoch/clientparams.php?event=event_20120615_KielerWoch&race=0b5969cc-b789-11e1-a845-406186cbf87c";



    public SailingSimulatorImpl(SimulationParameters parameters) {
        this.simulationParameters = parameters;

        this.initializePathGenerator(parameters);
    }

    private void initializePathGenerator(SimulationParameters parameters) {

        System.out.println("initializing PathGeneratorTracTrac");

        this.pathGenerator = new PathGeneratorTracTrac(parameters);
        this.pathGenerator.setEvaluationParameters(RACE_URL, LIVE_URI, STORED_URI, WIND_SCALE);
    }

    @Override
    public void setSimulationParameters(SimulationParameters parameters) {
        this.simulationParameters = parameters;
        this.initializePathGenerator(parameters);
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

    private Path getFromResourcesOrDownload(int legIndex, int competitorIndex, String pathName) throws Exception {

        Path path = (Path) SerializationUtils.readObjectFromResources("resources/" + pathName + "_" + competitorIndex + "_" + legIndex + ".dat");
        if (path == null) {
            if (pathName.equals("6#GPS Poly")) {
                path = this.pathGenerator.getLegPolyline(legIndex, competitorIndex, new MeterDistance(4.88));
            } else if (pathName.equals("7#GPS Track")) {
                path = this.pathGenerator.getLeg(legIndex, competitorIndex);
            } else if (pathName.equals("raceCourse")) {
                path = this.pathGenerator.getRaceCourse();
            } else {
                throw new Exception("Unknown path name!");
            }
            SerializationUtils.saveToFile(path, SerializationUtils.getPathPrefix() + "\\src\\resources\\" + pathName + "_" + competitorIndex + "_" + legIndex
                    + ".dat");
        }

        return path;
    }

    @Override
    public Map<String, Path> getAllPathsForLeg(int legIndex, int competitorIndex) {

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {

            Pair<Map<String, Path>, Path> result = SerializationUtils.readLegPathsFromResources(legIndex, competitorIndex);
            allPaths = result.getA();
            this.raceCourse = result.getB();

            if (allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6) {
                return allPaths;
            }

            try {
                gpsPath = this.getFromResourcesOrDownload(legIndex, competitorIndex, "7#GPS Track");
                gpsPathPoly = this.getFromResourcesOrDownload(legIndex, competitorIndex, "6#GPS Poly");
                this.raceCourse = this.getFromResourcesOrDownload(legIndex, competitorIndex, "raceCourse");
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
            SerializationUtils.saveLegPathsToFiles(allPaths, this.raceCourse, legIndex, competitorIndex);
        }

        return allPaths;
    }

    @Override
    public Map<String, Path> getAllPaths() {

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {

            Pair<Map<String, Path>, Path> result = SerializationUtils.readPathsFromResources();
            allPaths = result.getA();
            this.raceCourse = result.getB();

            if (allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6) {
                return allPaths;
            }

            gpsPath = this.pathGenerator.getPath();
            gpsPathPoly = this.pathGenerator.getPathPolyline(new MeterDistance(4.88));
            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
            this.raceCourse = this.pathGenerator.getRaceCourse();

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
            gridArea[0] = this.raceCourse.getPathPoints().get(0).getPosition();
            gridArea[1] = this.raceCourse.getPathPoints().get(1).getPosition();
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
            SerializationUtils.savePathsToFiles(allPaths, this.raceCourse);
        }

        return allPaths;
    }

    @Override
    public Path getRaceCourse() {
        return this.raceCourse;
    }

    @Override
    public Map<String, Path> getAllLegPathsEvenTimed(long millisecondsStep, int legIndex, int competitorIndex) {

        Map<String, Path> allTimedPaths = new TreeMap<String, Path>();
        Map<String, Path> allPaths = this.getAllPathsForLeg(legIndex, competitorIndex);

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
    public Map<String, Path> getAllPathsEvenTimed(long millisecondsStep) {

        Map<String, Path> allTimedPaths = new TreeMap<String, Path>();
        Map<String, Path> allPaths = this.getAllPaths();

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
    public Path getGPSTrack() {

        Path path = (Path) SerializationUtils.readObjectFromResources("resources/7#GPS Track.dat");
        if (path == null) {
            System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");
            LOGGER.warning("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");

            path = this.pathGenerator.getPath();
        }

        return path;
    }

    @Override
    public Path getLegGPSTrack(int legIndex, int competitorIndex) {

        Path path = (Path) SerializationUtils.readObjectFromResources("resources/7#GPS Track_" + competitorIndex + "_" + legIndex + ".dat");
        if (path == null) {
            System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");
            LOGGER.warning("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");

            path = this.pathGenerator.getPath();
        }

        return path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getLegsNames() {

        List<String> legsNames = null;

        legsNames = (List<String>) SerializationUtils.readObjectFromResources("resources/" + SerializationUtils.LEGSNAMES_DAT);
        if (legsNames != null && legsNames.isEmpty() == false && legsNames.size() == 4) {
            return legsNames;
        }

        legsNames = this.pathGenerator.getLegsNames();

        SerializationUtils.saveLegsNamesToFiles(legsNames);

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
    public Path getLeg(int legIndex, int competitorIndex) {

        return this.pathGenerator.getLeg(legIndex, competitorIndex);
    }
}
