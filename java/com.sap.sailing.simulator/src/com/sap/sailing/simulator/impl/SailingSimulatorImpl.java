package com.sap.sailing.simulator.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
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

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    public SailingSimulatorImpl(final SimulationParameters params) {
        this.simulationParameters = params;
    }

    @Override
    public void setSimulationParameters(final SimulationParameters params) {
        this.simulationParameters = params;
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

    // private static Logger logger = Logger.getLogger("com.sap.sailing");

    @Override
    public Map<String, Path> getAllPaths() {

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {

            allPaths = this.readPathsFromResources();
            if (allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6) {
                return allPaths;
            }

            //
            // load examplary GPS-path
            //
            final String raceURL = "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=d1f521fa-ec52-11e0-a523-406186cbf87c";
            // String raceURL =
            // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=eb06795a-ec52-11e0-a523-406186cbf87c";
            // String raceURL =
            // "http://germanmaster.traclive.dk/events/event_20120615_KielerWoch/clientparams.php?event=event_20120615_KielerWoch&race=0b5969cc-b789-11e1-a845-406186cbf87c";
            // String raceURL =
            // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=6bb0829e-ec44-11e0-a523-406186cbf87c";
            final PathGeneratorTracTrac genTrac = new PathGeneratorTracTrac(this.simulationParameters);

            // proxy configuration
            genTrac.setEvaluationParameters(raceURL, "tcp://10.18.22.156:1520", "tcp://10.18.22.156:1521", 4.5); // new

            // no-proxy configuration
            //genTrac.setEvaluationParameters(raceURL, "tcp://germanmaster.traclive.dk:4400", "tcp://germanmaster.traclive.dk:4401", 4.5);

            gpsPath = genTrac.getPath();
            gpsPathPoly = genTrac.getPathPolyline(new MeterDistance(4.88));
            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
            this.racecourse = genTrac.getRaceCourse();

        }

        //
        // Initialize WindFields boundary
        //
        final WindFieldGenerator wf = this.simulationParameters.getWindField();
        final int[] gridRes = wf.getGridResolution();
        Position[] gridArea = wf.getGridAreaGps();
        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            ((WindFieldGeneratorMeasured) wf).setGPSWind(gpsPath);
            gridArea = new Position[2];
            gridArea[0] = this.racecourse.getPathPoints().get(0).getPosition();
            gridArea[1] = this.racecourse.getPathPoints().get(1).getPosition();
            final List<Position> course = new ArrayList<Position>();
            course.add(gridArea[0]);
            course.add(gridArea[1]);
            this.simulationParameters.setCourse(course);
        }

        if (gridArea != null) {
            final Boundary bd = new RectangularBoundary(gridArea[0], gridArea[1], 0.1);
            
            // set base wind bearing
            wf.getWindParameters().baseWindBearing += bd.getSouth().getDegrees();
            //System.out.println("baseWindBearing: " + wf.getWindParameters().baseWindBearing);
            logger.info("base wind: "+this.simulationParameters.getBoatPolarDiagram().getWind().getKnots()+" kn, "+((wf.getWindParameters().baseWindBearing)%360.0)+"°");
            
            // set water current
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(2.0,new DegreeBearingImpl((wf.getWindParameters().baseWindBearing+90.0)%360.0)));
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(2.0,new DegreeBearingImpl((270.0)%360.0)));
            if (this.simulationParameters.getBoatPolarDiagram().getCurrent() != null) {
                logger.info("water current: "+this.simulationParameters.getBoatPolarDiagram().getCurrent().getKnots()+" kn, "+this.simulationParameters.getBoatPolarDiagram().getCurrent().getBearing().getDegrees()+"°");
            }
            
            wf.setBoundary(bd);
            final Position[][] positionGrid = bd.extractGrid(gridRes[0], gridRes[1]);
            wf.setPositionGrid(positionGrid);
            wf.generate(wf.getStartTime(), wf.getEndTime(), wf.getTimeStep());
        }

        //
        // Start Simulation
        //

        // get 1-turners
        final PathGenerator1Turner gen1Turner = new PathGenerator1Turner(this.simulationParameters);
        gen1Turner.setEvaluationParameters(true, null, null, 0, 0, 0);
        final Path leftPath = gen1Turner.getPath();
        final int left1TurnMiddle = gen1Turner.getMiddle();
        gen1Turner.setEvaluationParameters(false, null, null, 0, 0, 0);
        final Path rightPath = gen1Turner.getPath();
        final int right1TurnMiddle = gen1Turner.getMiddle();

        // get left- and right-going heuristic based on 1-turner
        final PathGeneratorOpportunistEuclidian genOpportunistic = new PathGeneratorOpportunistEuclidian(
                this.simulationParameters);
        // PathGeneratorOpportunistVMG genOpportunistic = new PathGeneratorOpportunistVMG(simulationParameters);
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, true);
        final Path oppPathL = genOpportunistic.getPath();
        genOpportunistic.setEvaluationParameters(left1TurnMiddle, right1TurnMiddle, false);
        final Path oppPathR = genOpportunistic.getPath();

        Path oppPath = null;
        // System.out.println("left -going: "+oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() -
        // 1).getTimePoint().asMillis());
        // System.out.println("right-going: "+oppPathR.getPathPoints().get(oppPathR.getPathPoints().size() -
        // 1).getTimePoint().asMillis());
        if (oppPathL.getPathPoints().get(oppPathL.getPathPoints().size() - 1).getTimePoint().asMillis() <= oppPathR
                .getPathPoints().get(oppPathR.getPathPoints().size() - 1).getTimePoint().asMillis()) {
            oppPath = oppPathL;
        } else {
            oppPath = oppPathR;
        }

        // get optimal path from dynamic programming with forward iteration
        final PathGenerator genDynProgForward = new PathGeneratorDynProgForward(this.simulationParameters);
        Path optPath = genDynProgForward.getPath();

        //
        // NOTE: pathName convention is: sort-digit + "#" + path-name
        // The sort-digit defines the sorting of paths in webbrowser
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
        }

        allPaths.put("1#Omniscient", optPath);

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            this.savePathsToFiles(allPaths);
        }

        return allPaths;
    }

    @Override
    public Map<String, List<TimedPositionWithSpeed>> getAllPathsEvenTimed(final long millisecondsStep) {

        final Map<String, List<TimedPositionWithSpeed>> allTimedPaths = new TreeMap<String, List<TimedPositionWithSpeed>>();

        final Map<String, Path> allPaths = this.getAllPaths();
        for (final Entry<String, Path> entry : allPaths.entrySet()) {
            final String key = entry.getKey();
            final Path value = entry.getValue();
            allTimedPaths.put(key, value.getEvenTimedPath(millisecondsStep));
        }

        return allTimedPaths;
    }

    @Override
    public Path getRaceCourse() {
        return this.racecourse;
    }

    @Override
    public Map<String, Path> getAllPathsEvenTimed2(final long millisecondsStep) {
        final Map<String, Path> allTimedPaths = new TreeMap<String, Path>();
        final Map<String, Path> allPaths = this.getAllPaths();

        for (final Entry<String, Path> entry : allPaths.entrySet()) {
            final String key = entry.getKey();
            final Path value = entry.getValue();
            allTimedPaths.put(key, value.getEvenTimedPath2(millisecondsStep));
        }

        return allTimedPaths;
    }

    private static String[] PATH_NAMES = new String[] { "1#Omniscient", "2#Opportunistic", "3#1-Turner Left",
        "4#1-Turner Right", "6#GPS Poly", "7#GPS Track" };
    private static String PATH_PREFIX = "";

    private boolean savePathsToFiles(final Map<String, Path> paths) {
        if (paths == null) {
            return false;
        }

        if (paths.isEmpty()) {
            return true;
        }

        String pathPrefix = "";

        try {
            pathPrefix = SailingSimulatorImpl.getPathPrefix();
            logger.info("pathPrefix=" + pathPrefix);
        } catch (final ClassNotFoundException exception) {
            return false;
        }

        String filePath = "";
        boolean result = true;

        for (final String name : SailingSimulatorImpl.PATH_NAMES) {
            filePath = pathPrefix + "\\src\\resources\\" + name + ".dat";
            result &= SailingSimulatorImpl.saveToFile(paths.get(name), filePath);
        }

        filePath = pathPrefix + "\\src\\resources\\racecourse.dat";
        result &= SailingSimulatorImpl.saveToFile(this.racecourse, filePath);

        return result;
    }

    private static String getPathPrefix() throws ClassNotFoundException {
        if (SailingSimulatorImpl.PATH_PREFIX == null || SailingSimulatorImpl.PATH_PREFIX.length() == 0
                || SailingSimulatorImpl.PATH_PREFIX.equals("")) {
            SailingSimulatorImpl.PATH_PREFIX = SailingSimulatorImpl.computePathPrefix();
        }

        return SailingSimulatorImpl.PATH_PREFIX;
    }

    private static String computePathPrefix() throws ClassNotFoundException {
        final String bundleName = FrameworkUtil.getBundle(
                Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl")).getSymbolicName();
        final String bundlesProperty = System.getProperty("osgi.bundles");

        final int bundleNameStart = bundlesProperty.indexOf(bundleName);
        final int bundleNameEnd = bundleNameStart + bundleName.length();

        String prependedBundlePath = bundlesProperty.substring(0, bundleNameEnd);

        final int prefixPos = prependedBundlePath.lastIndexOf("reference:file:");

        if (prefixPos >= 0) {
            prependedBundlePath = prependedBundlePath.substring(prefixPos + 15, prependedBundlePath.length());
        }

        return prependedBundlePath;
    }

    private Map<String, Path> readPathsFromResources() {
        final HashMap<String, Path> paths = new HashMap<String, Path>();
        Path path = null;
        String filePath = "";

        for (final String pathName : PATH_NAMES) {
            filePath = "resources/" + pathName + ".dat";
            path = readFromResourcesFile(filePath);
            if (path == null) {
                System.err
                .println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from"
                        + pathName);
            } else {
                paths.put(pathName, path);
            }
        }

        this.racecourse = readFromResourcesFile("resources/racecourse.dat");

        return paths;
    }

    @SuppressWarnings("unused")
    private static Path readFromExternalFile(final String fileName) {
        Path result = null;
        try {
            final InputStream file = new FileInputStream(fileName);
            final InputStream buffer = new BufferedInputStream(file);
            final ObjectInput input = new ObjectInputStream(buffer);

            try {
                result = (Path) input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (final ClassNotFoundException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromExternalFile][ClassNotFoundException] "
                    + ex.getMessage());
            result = null;
        } catch (final IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromExternalFile][IOException]  " + ex.getMessage());
            result = null;
        }

        return result;
    }

    private static Path readFromResourcesFile(final String fileName) {
        Path result = null;
        try {
            final ClassLoader classLoader = Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl")
                    .getClassLoader();
            final InputStream file = classLoader.getResourceAsStream(fileName);
            final InputStream buffer = new BufferedInputStream(file);
            final ObjectInput input = new ObjectInputStream(buffer);

            try {
                result = (Path) input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (final ClassNotFoundException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromResourcesFile][ClassNotFoundException] "
                    + ex.getMessage());
            result = null;
        } catch (final IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromResourcesFile][IOException]  " + ex.getMessage());
            result = null;
        }

        return result;
    }

    private static boolean saveToFile(final Path path, final String fileName) {
        boolean result = true;
        try {
            final OutputStream file = new FileOutputStream(fileName);
            final OutputStream buffer = new BufferedOutputStream(file);
            final ObjectOutput output = new ObjectOutputStream(buffer);

            try {
                output.writeObject(path);
            } finally {
                output.close();
                buffer.close();
                file.close();
            }
        } catch (final IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][saveToFile][IOException]  " + ex.getMessage());
            result = false;
        }

        return result;
    }
}
