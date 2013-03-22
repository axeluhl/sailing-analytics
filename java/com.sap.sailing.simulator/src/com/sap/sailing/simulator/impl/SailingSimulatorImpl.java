package com.sap.sailing.simulator.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.osgi.framework.FrameworkUtil;

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
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
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
        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            this.initializePathGenerator(parameters, 0);
        }
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

        String fileName = getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, pathName);

        Path path = (Path) readObjectFromResources(fileName);
        if (path == null) {
            if (pathName.equals("6#GPS Poly")) {
                path = this.pathGenerator.getPathPolyline(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, new MeterDistance(4.88));
            } else if (pathName.equals("7#GPS Track")) {
                path = this.pathGenerator.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
            } else if (pathName.equals("raceCourse")) {
                path = this.pathGenerator.getRaceCourse();
            } else {
                throw new Exception("Unknown path name!");
            }
            saveToFile(path, getPathPrefix() + "\\src\\resources\\" + fileName);
        }

        return path;
    }

    @Override
    public Map<String, Path> getAllPaths() {

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

/*        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {

            allPaths = this.readPathsFromResources();
            if (allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6) {
                return allPaths;
            }

            PathGeneratorTracTrac genTrac = new PathGeneratorTracTrac(this.simulationParameters);
            genTrac.setEvaluationParameters(raceURL, liveURI, storedURI, windScale);

            gpsPath = genTrac.getPath();
            gpsPathPoly = genTrac.getPathPolyline(new MeterDistance(4.88));
            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
            this.racecourse = genTrac.getRaceCourse();

        }*/

        //
        // Initialize WindFields boundary
        //
        WindFieldGenerator wf = this.simulationParameters.getWindField();
        int[] gridRes = wf.getGridResolution();
        Position[] gridArea = wf.getGridAreaGps();
        /*if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            ((WindFieldGeneratorMeasured) wf).setGPSWind(gpsPath);
            gridArea = new Position[2];
            gridArea[0] = this.racecourse.getPathPoints().get(0).getPosition();
            gridArea[1] = this.racecourse.getPathPoints().get(1).getPosition();
            List<Position> course = new ArrayList<Position>();
            course.add(gridArea[0]);
            course.add(gridArea[1]);
            this.simulationParameters.setCourse(course);
        }*/

        if (gridArea != null) {
            Boundary bd = new RectangularBoundary(gridArea[0], gridArea[1], 0.1);

            // set base wind bearing
            wf.getWindParameters().baseWindBearing += bd.getSouth().getDegrees();
            //System.out.println("baseWindBearing: " + wf.getWindParameters().baseWindBearing);
            LOGGER.info("base wind: "+this.simulationParameters.getBoatPolarDiagram().getWind().getKnots()+" kn, "+((wf.getWindParameters().baseWindBearing)%360.0)+"°");

            // initialize interpolation table for getSpeedAtBearingOverGround, e.g. for what-if or for optimization on overground-grids
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(null); // initialize

            // set water current
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(0.0,new DegreeBearingImpl((wf.getWindParameters().baseWindBearing+90.0)%360.0)));
            this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(wf.getWindParameters().curSpeed, new DegreeBearingImpl(wf.getWindParameters().curBearing)));
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(2.0,new DegreeBearingImpl((270.0)%360.0)));
            if (this.simulationParameters.getBoatPolarDiagram().getCurrent() != null) {
                LOGGER.info("water current: "+this.simulationParameters.getBoatPolarDiagram().getCurrent().getKnots()+" kn, "+this.simulationParameters.getBoatPolarDiagram().getCurrent().getBearing().getDegrees()+"°");
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

        /*if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            this.savePathsToFiles(allPaths);
        }*/

        return allPaths;
    }

    //TODO: To be cleaned up based on getAllPaths()
    @Override
    public Map<String, Path> getAllPathsForLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        this.setSimulationParameters(this.simulationParameters, selectedRaceIndex);

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {

            Pair<Map<String, Path>, Path> result = readLegPathsFromResources(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
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
        Path leftPath = genTreeGrow.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        PathCandidate leftBestCand = genTreeGrow.getBestCand();
        int left1TurnMiddle = 1000;
        if (leftBestCand != null) {
            left1TurnMiddle = leftBestCand.path.indexOf("LR");
        }

        // search best right-starting 1-turner
        genTreeGrow.setEvaluationParameters("R", 1, null);
        Path rightPath = genTreeGrow.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        PathCandidate rightBestCand = genTreeGrow.getBestCand();
        int right1TurnMiddle = 1000;
        if (rightBestCand != null) {
            right1TurnMiddle = rightBestCand.path.indexOf("RL");
        }

        // search best multi-turn course
        genTreeGrow.setEvaluationParameters(null, 0, null);
        Path optPath = genTreeGrow.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);


        // evaluate opportunistic heuristic
        PathGeneratorOpportunistEuclidian genOpportunistic = new PathGeneratorOpportunistEuclidian(this.simulationParameters);
        // PathGeneratorOpportunistVMG genOpportunistic = new PathGeneratorOpportunistVMG(simulationParameters);

        // left-starting opportunist
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, true);
        Path oppPathL = genOpportunistic.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        // right-starting opportunist
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, false);
        Path oppPathR = genOpportunistic.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);

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
            saveLegPathsToFiles(allPaths, this.raceCourse, selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        }

        return allPaths;
    }

    @Override
    public Path getRaceCourse() {
        return this.raceCourse;
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

    //TODO: To be cleaned up based on getAllPathsEvenTimed()
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

        String fileName = getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, "7#GPS Track");

        Path path = (Path) readObjectFromResources("resources/" + fileName);
        if (path == null) {
            System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");
            LOGGER.warning("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");

            path = this.pathGenerator.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
        }

        return path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getLegsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, selectedRaceIndex);

        List<String> legsNames = null;
        String fileName = getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + LEGSNAMES_DAT;

        legsNames = (List<String>) readObjectFromResources("resources/" + fileName);
        if (legsNames != null && legsNames.isEmpty() == false) {
            return legsNames;
        }

        legsNames = this.pathGenerator.getLegsNames();

        saveStringListToFiles(legsNames, fileName);

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

        return this.pathGenerator.getPathLeg(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getComeptitorsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, selectedRaceIndex);

        List<String> competitorsNames = null;
        String fileName = getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + COMPETITORSNAMES_DAT;

        competitorsNames = (List<String>) readObjectFromResources("resources/" + fileName);
        if (competitorsNames != null && competitorsNames.isEmpty() == false) {
            return competitorsNames;
        }

        competitorsNames = this.pathGenerator.getComeptitorsNames();

        saveStringListToFiles(competitorsNames, fileName);

        return competitorsNames;
    }

    // private static final Logger LOGGER = Logger.getLogger("com.sap.sailing.simulator");

    public static final String LEGSNAMES_DAT = "legsNames.dat";
    public static final String RACECOURSE_DAT = "racecourse.dat";
    public static final String COMPETITORSNAMES_DAT = "competitorsNames.dat";

    private static final String[] PATH_NAMES = new String[] { "1#Omniscient", "2#Opportunistic", "3#1-Turner Left", "4#1-Turner Right", "6#GPS Poly",
    "7#GPS Track" };

    private static String pathPrefix = null;

    private static String getPathPrefix() {
        String bundleName = null;
        try {
            bundleName = FrameworkUtil.getBundle(Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl")).getSymbolicName();
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR][SailingSimulatorImpl][getPathPrefix][ClassNotFoundException]  " + e.getMessage());
            LOGGER.severe("[ERROR][SailingSimulatorImpl][getPathPrefix][ClassNotFoundException]  " + e.getMessage());
            return null;
        }
        String bundlesProperty = System.getProperty("osgi.bundles");

        int bundleNameStart = bundlesProperty.indexOf(bundleName);
        int bundleNameEnd = bundleNameStart + bundleName.length();

        String prependedBundlePath = bundlesProperty.substring(0, bundleNameEnd);

        int prefixPos = prependedBundlePath.lastIndexOf("reference:file:");

        if (prefixPos >= 0) {
            prependedBundlePath = prependedBundlePath.substring(prefixPos + 15, prependedBundlePath.length());
        }

        return prependedBundlePath;
    }

    // private static boolean savePathsToFiles(Map<String, Path> paths, Path raceCourse) {
    // if (paths == null) {
    // return false;
    // }
    //
    // if (paths.isEmpty()) {
    // return true;
    // }
    //
    // if (pathPrefix == null) {
    // pathPrefix = getPathPrefix();
    // }
    //
    // String filePath = "";
    // boolean result = true;
    //
    // for (String name : PATH_NAMES) {
    // filePath = pathPrefix + "\\src\\resources\\" + name + ".dat";
    // result &= saveToFile(paths.get(name), filePath);
    // }
    //
    // filePath = pathPrefix + "\\src\\resources\\" + RACECOURSE_DAT;
    // result &= saveToFile(raceCourse, filePath);
    //
    // return result;
    // }

    private static boolean saveLegPathsToFiles(Map<String, Path> paths, Path raceCourse, int selectedRaceIndex, int selectedCompetitorIndex,
            int selectedLegIndex) {
        if (paths == null) {
            return false;
        }

        if (paths.isEmpty()) {
            return true;
        }

        if (pathPrefix == null) {
            pathPrefix = getPathPrefix();
        }

        String filePath = "";
        String fileName = "";
        boolean result = true;

        for (String name : PATH_NAMES) {

            fileName = getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, name);
            filePath = pathPrefix + "\\src\\resources\\" + fileName;
            result &= saveToFile(paths.get(name), filePath);
        }

        fileName = getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, "racecourse");
        filePath = pathPrefix + "\\src\\resources\\" + fileName;
        result &= saveToFile(raceCourse, filePath);

        return result;
    }

    private static boolean saveToFile(Path path, String fileName) {
        boolean result = true;
        try {
            OutputStream file = new FileOutputStream(fileName);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            try {
                output.writeObject(path);
            } finally {
                output.close();
                buffer.close();
                file.close();
            }
        } catch (IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][saveToFile][IOException]  " + ex.getMessage());
            LOGGER.severe("[ERROR][SailingSimulatorImpl][saveToFile][IOException]  " + ex.getMessage());
            result = false;
        }

        return result;
    }

    private static Pair<Map<String, Path>, Path> readLegPathsFromResources(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {
        HashMap<String, Path> paths = new HashMap<String, Path>();

        Path path = null;
        String filePath = "";
        String fileName = "";
        for (String pathName : PATH_NAMES) {

            fileName = getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, pathName);
            filePath = "resources/" + fileName;

            path = (Path) readObjectFromResources(filePath);
            if (path == null) {
                System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from" + pathName);
                LOGGER.severe("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from" + pathName);
            } else {
                paths.put(pathName, path);
            }
        }

        Path raceCourse = (Path) readObjectFromResources("resources/" + getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, "racecourse"));

        return new Pair<Map<String, Path>, Path>(paths, raceCourse);
    }

    // private static Pair<Map<String, Path>, Path> readPathsFromResources() {
    // HashMap<String, Path> paths = new HashMap<String, Path>();
    // Path path = null;
    // String filePath = "";
    //
    // for (String pathName : PATH_NAMES) {
    // filePath = "resources/" + pathName + ".dat";
    // path = (Path) readObjectFromResources(filePath);
    // if (path == null) {
    // System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from" +
    // pathName);
    // LOGGER.severe("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from" + pathName);
    // } else {
    // paths.put(pathName, path);
    // }
    // }
    //
    // Path raceCourse = (Path) readObjectFromResources("resources/" + RACECOURSE_DAT);
    //
    // return new Pair<Map<String, Path>, Path>(paths, raceCourse);
    // }

    // private static Object readObjectExternalFile(String fileName) {
    // Object result = null;
    // try {
    // InputStream file = new FileInputStream(fileName);
    // InputStream buffer = new BufferedInputStream(file);
    // ObjectInput input = new ObjectInputStream(buffer);
    //
    // try {
    // result = input.readObject();
    // } finally {
    // input.close();
    // buffer.close();
    // file.close();
    // }
    // } catch (ClassNotFoundException ex) {
    // System.err.println("[ERROR][SailingSimulatorImpl][readFromExternalFile][ClassNotFoundException] " +
    // ex.getMessage());
    // LOGGER.severe("[ERROR][SailingSimulatorImpl][readFromExternalFile][ClassNotFoundException] " + ex.getMessage());
    // result = null;
    // } catch (IOException ex) {
    // System.err.println("[ERROR][SailingSimulatorImpl][readFromExternalFile][IOException]  " + ex.getMessage());
    // LOGGER.severe("[ERROR][SailingSimulatorImpl][readFromExternalFile][IOException]  " + ex.getMessage());
    // result = null;
    // }
    //
    // return result;
    // }

    private static Object readObjectFromResources(String fileName) {
        Object result = null;

        try {
            ClassLoader classLoader = Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl").getClassLoader();
            InputStream file = classLoader.getResourceAsStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            try {
                result = input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromResourcesFile][ClassNotFoundException] " + ex.getMessage());
            LOGGER.severe("[ERROR][SailingSimulatorImpl][readFromResourcesFile][ClassNotFoundException] " + ex.getMessage());
            result = null;
        } catch (IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromResourcesFile][IOException]  " + ex.getMessage());
            LOGGER.severe("[ERROR][SailingSimulatorImpl][readFromResourcesFile][IOException]  " + ex.getMessage());
            result = null;
        }

        return result;
    }

    private static boolean saveStringListToFiles(List<String> legsNames, String fileName) {
        if (legsNames == null) {
            return false;
        }

        if (legsNames.isEmpty()) {
            return true;
        }

        if (pathPrefix == null) {
            pathPrefix = getPathPrefix();
        }

        String filePath = pathPrefix + "\\src\\resources\\" + fileName;

        boolean result = true;

        try {
            OutputStream file = new FileOutputStream(filePath);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            try {
                output.writeObject(legsNames);
            } finally {
                output.close();
                buffer.close();
                file.close();
            }
        } catch (IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][saveLegsNamesToFiles][IOException]  " + ex.getMessage());
            LOGGER.severe("[ERROR][SailingSimulatorImpl][saveLegsNamesToFiles][IOException]  " + ex.getMessage());
            result = false;
        }

        return result;
    }

    private static String getRaceID(String raceURL) {

        String result = null;

        if (raceURL.contains("&race=")) {
            String[] parts = raceURL.split("&");
            for (String part : parts) {
                if (part.startsWith("race=")) {
                    result = part.replace("race=", "");
                }
            }
        } else if (raceURL.contains("?race=")) {
            String[] parts = raceURL.split("?");
            for (String part : parts) {
                if (part.startsWith("race=")) {
                    result = part.replace("race=", "");
                }
            }
        }

        return result;
    }

    private static String getFileName(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex, String pathName) {
        return getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + selectedCompetitorIndex + "_" + selectedLegIndex + "_" + pathName
                + ".dat";
    }
}
