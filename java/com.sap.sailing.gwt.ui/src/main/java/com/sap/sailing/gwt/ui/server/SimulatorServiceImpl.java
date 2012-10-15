package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.BoatClassDTOsAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.ConfigurationException;
import com.sap.sailing.gwt.ui.shared.CourseDTO;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTO;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.SimulatedPathsEvenTimedResultDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager.WindPattern;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternNotFoundException;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindFieldGenerator;
import com.sap.sailing.simulator.WindFieldGeneratorFactory;
import com.sap.sailing.simulator.impl.ConfigurationManager;
import com.sap.sailing.simulator.impl.PolarDiagramCSV;
import com.sap.sailing.simulator.impl.ReadingConfigurationFileStatus;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedImpl;
import com.sap.sailing.simulator.impl.Tuple;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;

public class SimulatorServiceImpl extends RemoteServiceServlet implements SimulatorService {

    /**
     * Generated uid serial version
     */
    private static final long serialVersionUID = 4445427185387524086L;

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    private static final WindFieldGeneratorFactory wfGenFactory = WindFieldGeneratorFactory.INSTANCE;
    private static final WindPatternDisplayManager wpDisplayManager = WindPatternDisplayManager.INSTANCE;
    
    private WindControlParameters controlParameters = new WindControlParameters(0, 0);

    @Override
    public PositionDTO[] getRaceLocations() {
        PositionDTO lakeGarda = new PositionDTO();
        lakeGarda.latDeg = 45.57055337226086;
        lakeGarda.lngDeg = 10.693345069885254;

        PositionDTO lakeGeneva = new PositionDTO();
        lakeGeneva.latDeg = 46.23376539670794;
        lakeGeneva.lngDeg = 6.168651580810547;

        PositionDTO kiel = new PositionDTO();
        kiel.latDeg = 54.3232927;
        kiel.lngDeg = 10.122765200000003;

        PositionDTO travemuende = new PositionDTO();
        travemuende.latDeg = 53.978276;
        travemuende.lngDeg = 10.880156;

        return new PositionDTO[] { kiel, lakeGeneva, lakeGarda, travemuende };
    }

    public WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params) {
        final Bearing north = new DegreeBearingImpl(0);
        final Bearing east = new DegreeBearingImpl(90);
        final Bearing south = new DegreeBearingImpl(180);
        final Bearing west = new DegreeBearingImpl(270);

        final double xSize = params.getxSize();
        final double ySize = params.getySize();
        final int gridsizeX = params.getGridsizeX();
        final int gridsizeY = params.getGridsizeY();

        Position center = new DegreePosition(params.getCenter().latDeg, params.getCenter().lngDeg);

        WindLatticeDTO wl = new WindLatticeDTO();
        PositionDTO[][] matrix = new PositionDTO[gridsizeY][gridsizeX];

        Distance deastwest = new NauticalMileDistance((gridsizeX - 1.) / (2 * gridsizeX) * xSize);
        Distance dnorthsouth = new NauticalMileDistance((gridsizeY - 1.) / (2 * gridsizeY) * ySize);
        Position start = center.translateGreatCircle(south, dnorthsouth).translateGreatCircle(west, deastwest);

        deastwest = new NauticalMileDistance(xSize / gridsizeX);
        dnorthsouth = new NauticalMileDistance(ySize / gridsizeY);

        Position rowStart = null, crt = null;
        for (int i = 0; i < gridsizeY; i++) {
            if (i == 0) {
                rowStart = start;
            } else {
                rowStart = rowStart.translateGreatCircle(north, dnorthsouth);
            }

            for (int j = 0; j < gridsizeX; j++) {
                if (j == 0) {
                    crt = rowStart;
                } else {
                    crt = crt.translateGreatCircle(east, deastwest);
                    if ((i == 3) && (j == 5)) {
                        crt = crt.translateGreatCircle(north, new NauticalMileDistance(ySize / gridsizeY * Math.random()));
                        crt = crt.translateGreatCircle(east, new NauticalMileDistance(xSize / gridsizeX * Math.random()));
                        crt = crt.translateGreatCircle(south, new NauticalMileDistance(ySize / gridsizeY * Math.random()));
                        crt = crt.translateGreatCircle(west, new NauticalMileDistance(xSize / gridsizeX * Math.random()));
                    }
                }

                PositionDTO pdto = new PositionDTO(crt.getLatDeg(), crt.getLngDeg());
                matrix[i][j] = pdto;
            }

        }

        wl.setMatrix(matrix);

        return wl;
    }

    private void retreiveWindControlParameters(WindPatternDisplay pattern) {

        controlParameters.setDefaults();

        for (WindPatternSetting<?> s : pattern.getSettings()) {
            Field f;
            try {
                f = controlParameters.getClass().getField(s.getName());
                try {

                    logger.info("Setting " + f.getName() + " to " + s.getName() + " value : " + s.getValue());
                    f.set(controlParameters, s.getValue());
                    // f.setDouble(controlParameters, (Double) s.getValue());

                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                logger.info(e.getMessage());
            }

        }
    }

    @Override
    public WindFieldDTO getWindField(WindFieldGenParamsDTO params, WindPatternDisplay pattern) throws WindPatternNotFoundException {
        logger.info("Entering getWindField");
        Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);
        // List<Position> lattice = bd.extractLattice(params.getxRes(),
        // params.getyRes());

        retreiveWindControlParameters(pattern);
        logger.info("Boundary south direction " + bd.getSouth());
        controlParameters.baseWindBearing += bd.getSouth().getDegrees();

        WindFieldGenerator wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), bd, controlParameters);

        if (wf == null) {
            throw new WindPatternNotFoundException("Please select a valid wind pattern.");
        }

        Position[][] grid = bd.extractGrid(params.getxRes(), params.getyRes());
        wf.setPositionGrid(grid);

        TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());// new
                                                                                         // MillisecondsTimePoint(0);
        TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());// new
                                                                                       // MillisecondsTimePoint(30*1000);
        TimePoint endTime = new MillisecondsTimePoint(params.getEndTime().getTime());// new MillisecondsTimePoint(10 *
                                                                                     // 60 * 1000);

        wf.generate(startTime, null, timeStep);
        List<WindDTO> wList = new ArrayList<WindDTO>();

        if (grid != null) {
            TimePoint t = startTime;
            while (t.compareTo(endTime) <= 0) {
                for (int i = 0; i < grid.length; i++) {
                    for (int j = 0; j < grid[0].length; j++) {
                        Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(t, grid[i][j], null));
                        logger.finer(localWind.toString());
                        WindDTO w = createWindDTO(localWind);
                        wList.add(w);
                    }
                }
                t = new MillisecondsTimePoint(t.asMillis() + timeStep.asMillis());
            }
        }

        WindFieldDTO wfDTO = new WindFieldDTO();

        wfDTO.setMatrix(wList);
        logger.info("Exiting getWindField");
        return wfDTO;

    }

    public List<WindPatternDTO> getWindPatterns() {
        return wpDisplayManager.getWindPatterns();
    }

    private PositionDTO createPositionDTO(Position pos) {
        PositionDTO posDTO = new PositionDTO();
        posDTO.latDeg = pos.getLatDeg();
        posDTO.lngDeg = pos.getLngDeg();
        
        return posDTO;
    }
    
    private WindDTO createWindDTO(Wind wind) {
        WindDTO windDTO = new WindDTO();
        windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = wind.getKnots();
        windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        if (wind.getPosition() != null) {
            windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(), wind.getPosition().getLngDeg());
        }
        if (wind.getTimePoint() != null) {
            windDTO.timepoint = wind.getTimePoint().asMillis();
        }

        return windDTO;
    }

    private WindDTO createWindDTO2(TimedPositionWithSpeed tPos) {
        WindDTO windDTO = new WindDTO();
        windDTO.trueWindBearingDeg = tPos.getSpeed().getBearing().getDegrees();
        windDTO.trueWindFromDeg = tPos.getSpeed().getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = tPos.getSpeed().getKnots();
        windDTO.trueWindSpeedInMetersPerSecond = tPos.getSpeed().getMetersPerSecond();
        if (tPos.getPosition() != null) {
            windDTO.position = new PositionDTO(tPos.getPosition().getLatDeg(), tPos.getPosition().getLngDeg());
        }
        if (tPos.getTimePoint() != null) {
            windDTO.timepoint = tPos.getTimePoint().asMillis();
        }

        return windDTO;
    }

    @Override
    public WindPatternDisplay getWindPatternDisplay(WindPatternDTO pattern) {
        return wpDisplayManager.getDisplay(WindPattern.valueOf(pattern.name));
    }

    private WindFieldDTO createWindFieldDTO(WindFieldGenerator wf, TimePoint startTime, TimePoint endTime, TimePoint timeStep) {

        WindFieldDTO windFieldDTO = new WindFieldDTO();
        List<WindDTO> wList = new ArrayList<WindDTO>();
        Position[][] positionGrid = wf.getPositionGrid();

        if (positionGrid != null && positionGrid.length > 0) {
            TimePoint t = startTime;
            while (t.compareTo(endTime) <= 0) {
                for (int i = 0; i < positionGrid.length; ++i) {
                    for (int j = 0; j < positionGrid[i].length; ++j) {
                        Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(t, positionGrid[i][j], null));
                        logger.finer(localWind.toString());
                        WindDTO w = createWindDTO(localWind);
                        wList.add(w);
                    }
                }
                t = new MillisecondsTimePoint(t.asMillis() + timeStep.asMillis());
            }
        }

        windFieldDTO.setMatrix(wList);
        return windFieldDTO;
    }

    //I0077899 - Mihai Bogdan Eugen
    private SimulatedPathsEvenTimedResultDTO getSimulatedPathsEvenTimed(List<Position> course, WindFieldGenerator wf, char mode, int boatClassIndex) throws ConfigurationException {

    	logger.info("Retrieving simulated paths");

        PolarDiagramAndNotificationMessage polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);
        PolarDiagram pd = polarDiagramAndNotificationMessage.getPolarDiagram(); 
        
        SimulationParameters sp = new SimulationParametersImpl(course, pd, wf, mode);
        SailingSimulator simulator = new SailingSimulatorImpl(sp);

        Map<String, List<TimedPositionWithSpeed>> paths = simulator.getAllPathsEvenTimed(wf.getTimeStep().asMillis());
        PathDTO[] pathDTO = new PathDTO[paths.size()];
        int pathIndex = paths.keySet().size() - 1;
        for (String pathName : paths.keySet()) {

            logger.info("Path " + pathName);
            List<TimedPositionWithSpeed> path = paths.get(pathName);
            
            // NOTE: pathName convention is: sort-digit + "#" + path-name
            pathDTO[pathIndex] = new PathDTO(pathName.split("#")[1]);

            // fill pathDTO with path points where speed is true wind speed
            List<WindDTO> wList = new ArrayList<WindDTO>();
            for (TimedPositionWithSpeed p : path) {
                WindDTO w = createWindDTO2(p);
                wList.add(w);
            }
            pathDTO[pathIndex].setMatrix(wList);

            pathIndex--;
        }
        
        RaceMapDataDTO rcDTO;
        if (mode == SailingSimulatorUtil.measured) {
            rcDTO = new RaceMapDataDTO();
            rcDTO.coursePositions = new CourseDTO();
            rcDTO.coursePositions.waypointPositions = new ArrayList<PositionDTO>();

            Path rc = simulator.getRaceCourse();
            PositionDTO posDTO;
            posDTO = createPositionDTO(rc.getPathPoints().get(0).getPosition());
            
            rcDTO.coursePositions.waypointPositions.add(posDTO);
            posDTO = createPositionDTO(rc.getPathPoints().get(1).getPosition());
            rcDTO.coursePositions.waypointPositions.add(posDTO);
        } else {
            rcDTO = null;
        }
        
        WindFieldDTO wfDTO = null;
        
        SimulatedPathsEvenTimedResultDTO result = new SimulatedPathsEvenTimedResultDTO();
        result.setPathDTOs(pathDTO);
        result.setRaceMapDataDTO(rcDTO);
        result.setWindFieldDTO(wfDTO);
        result.setNotificationMessage(polarDiagramAndNotificationMessage.getNotificationMesssage());
        
        return result;
    }
    
    //I00788 - Mihai Bogdan Eugen       
    @Override
    public SimulatorResultsDTOAndNotificationMessage getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay pattern, boolean withWindField, int boatClassIndex) throws WindPatternNotFoundException, ConfigurationException {
        
        WindFieldGenerator wf = null;
        List<Position> course = null;
        TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());
        TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());
        retreiveWindControlParameters(pattern);
                
        wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), null, controlParameters);

        if (wf == null) {
            throw new WindPatternNotFoundException("Please select a valid wind pattern.");
        }

        if (mode != SailingSimulatorUtil.measured) {
            Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
            Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
            course = new ArrayList<Position>();
            course.add(nw);
            course.add(se);
            Position[] gridAreaGps = new Position[2];
            gridAreaGps = course.toArray(gridAreaGps);
            wf.setGridAreaGps(gridAreaGps);
        }

        int[] gridRes = new int[2];
        gridRes[0] = params.getxRes();
        gridRes[1] = params.getyRes();
        wf.setGridResolution(gridRes);
        
        wf.generate(startTime, null, timeStep);
        Long longestPathTime = 0L;
        
        SimulatedPathsEvenTimedResultDTO simulatedPathsEvenTimedResult = this.getSimulatedPathsEvenTimed(course, wf, mode, boatClassIndex);
        
        PathDTO[] pathDTO = simulatedPathsEvenTimedResult.getPathDTOs();
        RaceMapDataDTO rcDTO = simulatedPathsEvenTimedResult.getRaceMapDataDTO();
        
        for (int i = 0; i < pathDTO.length; ++i) {
            List<WindDTO> path = pathDTO[i].getMatrix();
            int pathLength = path.size();
            long pathTime = pathDTO[i].getMatrix().get(pathLength - 1).timepoint - path.get(0).timepoint;
            longestPathTime = Math.max(longestPathTime, pathTime);
        }

        TimePoint endTime = new MillisecondsTimePoint(startTime.asMillis() + longestPathTime);
        
        WindFieldDTO windFieldDTO = null;
        if (pattern != null) {
            windFieldDTO = createWindFieldDTO(wf, startTime, endTime, timeStep);
        }
        SimulatorResultsDTO simulatorResults = new SimulatorResultsDTO(rcDTO, pathDTO, windFieldDTO);
        
        SimulatorResultsDTOAndNotificationMessage result = new SimulatorResultsDTOAndNotificationMessage();
        result.setSimulatorResultsDTO(simulatorResults);
        result.setNotificationMessage(simulatedPathsEvenTimedResult.getNotificationMessage());
        
        return result;
    }
    
    //I00788 - Mihai Bogdan Eugen    
    @Override
    public BoatClassDTOsAndNotificationMessage getBoatClasses() throws ConfigurationException {
    	
        ArrayList<BoatClassDTO> boatClassesDTOs = new ArrayList<BoatClassDTO>();
        
        BoatClassDTOsAndNotificationMessage result = new BoatClassDTOsAndNotificationMessage();

        ConfigurationManager config = ConfigurationManager.getDefault();
        if(config.getStatus() == ReadingConfigurationFileStatus.IO_ERROR) {
        	throw new ConfigurationException(config.getErrorMessage());
        }
        else if(config.getStatus() == ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE || config.getStatus() == ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE) {
        	result.setNotificationMessage(config.getErrorMessage());
        }
        
        for (Tuple<String, Double, String> tuple : ConfigurationManager.getDefault().getBoatClassesInfo()) {
        	boatClassesDTOs.add(new BoatClassDTO(tuple.first, tuple.second));
        }

        result.setBoatClassDTOs(boatClassesDTOs.toArray(new BoatClassDTO[boatClassesDTOs.size()]));
        
        return result; 
    }
    
    //I00788 - Mihai Bogdan Eugen   
    @Override
    public PolarDiagramDTOAndNotificationMessage getPolarDiagramDTO(Double bearingStep, int boatClassIndex) throws ConfigurationException {

    	PolarDiagramAndNotificationMessage polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);

        NavigableMap<Speed, NavigableMap<Bearing, Speed>> navMap = polarDiagramAndNotificationMessage.getPolarDiagram().polarDiagramPlot(bearingStep);

        Set<Speed> validSpeeds = navMap.keySet();
        validSpeeds.remove(Speed.NULL);

        Number[][] series = new Number[validSpeeds.size()][];
        int i = 0;
        for (Speed s : validSpeeds) {
            Collection<Speed> boatSpeeds = navMap.get(s).values();
            series[i] = new Number[boatSpeeds.size()];
            int j = 0;
            for (Speed boatSpeed : boatSpeeds) {
                series[i][j++] = new Double(boatSpeed.getKnots());
            }
            i++;
        }
        PolarDiagramDTO dto = new PolarDiagramDTO();
    	dto.setNumberSeries(series);

    	PolarDiagramDTOAndNotificationMessage result = new PolarDiagramDTOAndNotificationMessage();
    	result.setPolarDiagramDTO(dto);
    	result.setNotificationMessage(polarDiagramAndNotificationMessage.getNotificationMesssage());
    	
    	return result;
    }
    
    //I00788 - Mihai Bogdan Eugen    
    private PolarDiagramAndNotificationMessage getPolarDiagram(int boatClassIndex) throws ConfigurationException {
    	
        ConfigurationManager config = ConfigurationManager.getDefault();
        PolarDiagramAndNotificationMessage result = new PolarDiagramAndNotificationMessage();
        
        if(config.getStatus() == ReadingConfigurationFileStatus.IO_ERROR) {
        	throw new ConfigurationException(config.getErrorMessage());
        }
        else if(config.getStatus() == ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE || config.getStatus() == ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE) {
        	result.setNotificationMessage(config.getErrorMessage());
        }
        	
        String csvFilePath = config.getPolarDiagramFileLocation(boatClassIndex);
        
        try {
	        result.setPolarDiagram(new PolarDiagramCSV(csvFilePath));
        }
        catch(IOException exception) {
        	throw new ConfigurationException("An IO error occured when parsing the CSV file! The original error message is " + exception.getMessage());
        }	        
        
        return result;
    }
    
    //I00788 - Mihai Bogdan Eugen
    private class PolarDiagramAndNotificationMessage {
    	private PolarDiagram polarDiagram = null;
    	private String notificationMessage = "";
    	
    	public void setPolarDiagram(PolarDiagram polarDiagram) {
    		this.polarDiagram = polarDiagram;
    	}
    	
    	public void setNotificationMessage(String notificationMessage) {
    		this.notificationMessage = notificationMessage;
    	}
    	
    	public PolarDiagram getPolarDiagram() {
    		return this.polarDiagram;
    	}
    	
    	public String getNotificationMesssage() {
    		return this.notificationMessage;
    	}
    }
}
