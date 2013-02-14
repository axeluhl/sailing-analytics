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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.osgi.framework.FrameworkUtil;

import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.impl.PathGeneratorTreeGrowWind2.PathCand;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorMeasured;

public class SailingSimulatorImpl implements SailingSimulator {

    private SimulationParameters simulationParameters;
    private Path racecourse;

    private static double windScale = 4.5;

    // proxy configuration
    private static String liveURI = "tcp://10.18.22.156:1520";

    // no-proxy configuration
    // private static String liveURI = "tcp://germanmaster.traclive.dk:4400";

    // proxy configuration
    private static String storedURI = "tcp://10.18.22.156:1521";

    // no-proxy configuration
    // private static String storedURI = "tcp://germanmaster.traclive.dk:4401";

    private static String raceURL = "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=d1f521fa-ec52-11e0-a523-406186cbf87c";
    // private static String raceURL =
    // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=eb06795a-ec52-11e0-a523-406186cbf87c";
    // private static String raceURL =
    // "http://germanmaster.traclive.dk/events/event_20120615_KielerWoch/clientparams.php?event=event_20120615_KielerWoch&race=0b5969cc-b789-11e1-a845-406186cbf87c";
    // private static String raceURL =
    // "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=6bb0829e-ec44-11e0-a523-406186cbf87c";

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    public SailingSimulatorImpl(SimulationParameters params) {
        this.simulationParameters = params;
    }

    @Override
    public void setSimulationParameters(SimulationParameters params) {
        this.simulationParameters = params;
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

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

            PathGeneratorTracTrac genTrac = new PathGeneratorTracTrac(this.simulationParameters);
            genTrac.setEvaluationParameters(raceURL, liveURI, storedURI, windScale);

            gpsPath = genTrac.getPath();
            gpsPathPoly = genTrac.getPathPolyline(new MeterDistance(4.88));
            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
            this.racecourse = genTrac.getRaceCourse();

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
            gridArea[0] = this.racecourse.getPathPoints().get(0).getPosition();
            gridArea[1] = this.racecourse.getPathPoints().get(1).getPosition();
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
            logger.info("base wind: "+this.simulationParameters.getBoatPolarDiagram().getWind().getKnots()+" kn, "+((wf.getWindParameters().baseWindBearing)%360.0)+"°");

            // initialize interpolation table for getSpeedAtBearingOverGround, e.g. for what-if or for optimization on overground-grids
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(null); // initialize

            // set water current
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(0.0,new DegreeBearingImpl((wf.getWindParameters().baseWindBearing+90.0)%360.0)));
            this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(wf.getWindParameters().curSpeed, new DegreeBearingImpl(wf.getWindParameters().curBearing)));
            //this.simulationParameters.getBoatPolarDiagram().setCurrent(new KnotSpeedWithBearingImpl(2.0,new DegreeBearingImpl((270.0)%360.0)));
            if (this.simulationParameters.getBoatPolarDiagram().getCurrent() != null) {
                logger.info("water current: "+this.simulationParameters.getBoatPolarDiagram().getCurrent().getKnots()+" kn, "+this.simulationParameters.getBoatPolarDiagram().getCurrent().getBearing().getDegrees()+"°");
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
        PathGeneratorTreeGrowWind2 genTreeGrow = new PathGeneratorTreeGrowWind2(this.simulationParameters);

        // search best left-starting 1-turner
        genTreeGrow.setEvaluationParameters("L", 1);
        Path leftPath = genTreeGrow.getPath();
        PathCand leftBestCand = genTreeGrow.getBestCand();
        int left1TurnMiddle = 1000;
        if (leftBestCand != null) {
            left1TurnMiddle = leftBestCand.path.indexOf("LR");
        }

        // search best right-starting 1-turner
        genTreeGrow.setEvaluationParameters("R", 1);
        Path rightPath = genTreeGrow.getPath();
        PathCand rightBestCand = genTreeGrow.getBestCand();
        int right1TurnMiddle = 1000;
        if (rightBestCand != null) {
            right1TurnMiddle = rightBestCand.path.indexOf("RL");
        }

        // search best multi-turn course
        genTreeGrow.setEvaluationParameters(null, 0);
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
            this.savePathsToFiles(allPaths);
        }

        return allPaths;
    }

    @Override
    public Path getRaceCourse() {
        return this.racecourse;
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

    private static String[] PATH_NAMES = new String[] { "1#Omniscient", "2#Opportunistic", "3#1-Turner Left",
        "4#1-Turner Right", "6#GPS Poly", "7#GPS Track" };
    private static String PATH_PREFIX = "";

    private boolean savePathsToFiles(Map<String, Path> paths) {
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
        } catch (ClassNotFoundException exception) {
            return false;
        }

        String filePath = "";
        boolean result = true;

        for (String name : SailingSimulatorImpl.PATH_NAMES) {
            filePath = pathPrefix + "\\src\\resources\\" + name + ".dat";
            result &= SailingSimulatorImpl.saveToFile(paths.get(name), filePath);
        }

        filePath = pathPrefix + "\\src\\resources\\racecourse.dat";
        result &= SailingSimulatorImpl.saveToFile(this.racecourse, filePath);

        return result;
    }

    public static String getPathPrefix() throws ClassNotFoundException {
        if (SailingSimulatorImpl.PATH_PREFIX == null || SailingSimulatorImpl.PATH_PREFIX.length() == 0
                || SailingSimulatorImpl.PATH_PREFIX.equals("")) {
            SailingSimulatorImpl.PATH_PREFIX = SailingSimulatorImpl.computePathPrefix();
        }

        return SailingSimulatorImpl.PATH_PREFIX;
    }

    private static String computePathPrefix() throws ClassNotFoundException {
        String bundleName = FrameworkUtil.getBundle(
                Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl")).getSymbolicName();
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

    private Map<String, Path> readPathsFromResources() {
        HashMap<String, Path> paths = new HashMap<String, Path>();
        Path path = null;
        String filePath = "";

        for (String pathName : PATH_NAMES) {
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
    private static Path readFromExternalFile(String fileName) {
        Path result = null;
        try {
            InputStream file = new FileInputStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            try {
                result = (Path) input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromExternalFile][ClassNotFoundException] "
                    + ex.getMessage());
            result = null;
        } catch (IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromExternalFile][IOException]  " + ex.getMessage());
            result = null;
        }

        return result;
    }

    private static Path readFromResourcesFile(String fileName) {
        Path result = null;
        try {
            ClassLoader classLoader = Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl")
                    .getClassLoader();
            InputStream file = classLoader.getResourceAsStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            try {
                result = (Path) input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromResourcesFile][ClassNotFoundException] "
                    + ex.getMessage());
            result = null;
        } catch (IOException ex) {
            System.err.println("[ERROR][SailingSimulatorImpl][readFromResourcesFile][IOException]  " + ex.getMessage());
            result = null;
        }

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
            result = false;
        }

        return result;
    }

    @Override
    public Path getGPSTrack() {

        Path path = readFromResourcesFile("resources/7#GPS Track.dat");
        if (path == null) {
            System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");

            PathGeneratorTracTrac genTrac = new PathGeneratorTracTrac(this.simulationParameters);
            genTrac.setEvaluationParameters(raceURL, liveURI, storedURI, windScale);
            path = genTrac.getPath();
        }

        return path;
    }

    @Override
    public List<String> getLegsNames(int boatClassIndex) {

        List<String> result = new ArrayList<String>();
        
        URI liveURIr = null;
        URI storedURIr = null;
        URL paramURLr = null;
        RacesHandle raceHandle = null;
        
        RacingEventServiceImpl service = new RacingEventServiceImpl(); 
        try {
			liveURIr = new URI(liveURI);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			storedURIr = new URI(storedURI);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			paramURLr = new URL(raceURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			raceHandle = service.addTracTracRace(paramURLr, liveURIr, storedURIr, EmptyWindStore.INSTANCE, 60000, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		
		if( raceHandle != null) {
			Set<RaceDefinition> races = raceHandle.getRaces();
			for( RaceDefinition race : races) {
				List<Leg> legs = race.getCourse().getLegs();
				for( Leg leg : legs) {
					result.add(leg.toString());
				}
			}
		}

        return result;
    }
}
