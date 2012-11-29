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
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
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
import com.sap.sailing.gwt.ui.shared.ReceivePolarDiagramDataDTO;
import com.sap.sailing.gwt.ui.shared.RequestPolarDiagramDataDTO;
import com.sap.sailing.gwt.ui.shared.SimulatedPathsEvenTimedResultDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.SpeedBearingPositionDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLinesDTO;
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
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.impl.ConfigurationManager;
import com.sap.sailing.simulator.impl.PathImpl;
import com.sap.sailing.simulator.impl.PolarDiagramCSV;
import com.sap.sailing.simulator.impl.ReadingConfigurationFileStatus;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.TimedPositionImpl;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedImpl;
import com.sap.sailing.simulator.impl.Tuple;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.WindFieldGeneratorFactory;

public class SimulatorServiceImpl extends RemoteServiceServlet implements SimulatorService {

    /**
     * Generated uid serial version
     */
    private static final long serialVersionUID = 4445427185387524086L;

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    private static final WindFieldGeneratorFactory wfGenFactory = WindFieldGeneratorFactory.INSTANCE;
    private static final WindPatternDisplayManager wpDisplayManager = WindPatternDisplayManager.INSTANCE;

    private final WindControlParameters controlParameters = new WindControlParameters(0, 0);

    @Override
    public PositionDTO[] getRaceLocations() {
        final PositionDTO lakeGarda = new PositionDTO();
        lakeGarda.latDeg = 45.57055337226086;
        lakeGarda.lngDeg = 10.693345069885254;

        final PositionDTO lakeGeneva = new PositionDTO();
        lakeGeneva.latDeg = 46.23376539670794;
        lakeGeneva.lngDeg = 6.168651580810547;

        final PositionDTO kiel = new PositionDTO();
        kiel.latDeg = 54.3232927;
        kiel.lngDeg = 10.122765200000003;

        final PositionDTO travemuende = new PositionDTO();
        travemuende.latDeg = 53.978276;
        travemuende.lngDeg = 10.880156;

        return new PositionDTO[] { kiel, lakeGeneva, lakeGarda, travemuende };
    }

    @Override
    public WindLatticeDTO getWindLatice(final WindLatticeGenParamsDTO params) {
        final Bearing north = new DegreeBearingImpl(0);
        final Bearing east = new DegreeBearingImpl(90);
        final Bearing south = new DegreeBearingImpl(180);
        final Bearing west = new DegreeBearingImpl(270);

        final double xSize = params.getxSize();
        final double ySize = params.getySize();
        final int gridsizeX = params.getGridsizeX();
        final int gridsizeY = params.getGridsizeY();

        final Position center = new DegreePosition(params.getCenter().latDeg, params.getCenter().lngDeg);

        final WindLatticeDTO wl = new WindLatticeDTO();
        final PositionDTO[][] matrix = new PositionDTO[gridsizeY][gridsizeX];

        Distance deastwest = new NauticalMileDistance((gridsizeX - 1.) / (2 * gridsizeX) * xSize);
        Distance dnorthsouth = new NauticalMileDistance((gridsizeY - 1.) / (2 * gridsizeY) * ySize);
        final Position start = center.translateGreatCircle(south, dnorthsouth).translateGreatCircle(west, deastwest);

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
                        crt = crt.translateGreatCircle(north,
                                new NauticalMileDistance(ySize / gridsizeY * Math.random()));
                        crt = crt.translateGreatCircle(east,
                                new NauticalMileDistance(xSize / gridsizeX * Math.random()));
                        crt = crt.translateGreatCircle(south,
                                new NauticalMileDistance(ySize / gridsizeY * Math.random()));
                        crt = crt.translateGreatCircle(west,
                                new NauticalMileDistance(xSize / gridsizeX * Math.random()));
                    }
                }

                final PositionDTO pdto = new PositionDTO(crt.getLatDeg(), crt.getLngDeg());
                matrix[i][j] = pdto;
            }

        }

        wl.setMatrix(matrix);

        return wl;
    }

    private void retreiveWindControlParameters(final WindPatternDisplay pattern) {

        controlParameters.setDefaults();

        for (final WindPatternSetting<?> s : pattern.getSettings()) {
            Field f;
            try {
                f = controlParameters.getClass().getField(s.getName());
                try {

                    logger.info("Setting " + f.getName() + " to " + s.getName() + " value : " + s.getValue());
                    f.set(controlParameters, s.getValue());
                    // f.setDouble(controlParameters, (Double) s.getValue());

                } catch (final IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (final IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (final SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final NoSuchFieldException e) {
                logger.info(e.getMessage());
            }

        }
    }

    @Override
    public WindFieldDTO getWindField(final WindFieldGenParamsDTO params, final WindPatternDisplay pattern)
            throws WindPatternNotFoundException {
        logger.info("Entering getWindField");
        final Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        final Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        final List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        final RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);
        // List<Position> lattice = bd.extractLattice(params.getxRes(),
        // params.getyRes());

        controlParameters.resetBlastRandomStream = params.isKeepState();
        retreiveWindControlParameters(pattern);
        logger.info("Boundary south direction " + bd.getSouth());
        controlParameters.baseWindBearing += bd.getSouth().getDegrees();

        final WindFieldGenerator wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), bd,
                controlParameters);

        if (wf == null) {
            throw new WindPatternNotFoundException("Please select a valid wind pattern.");
        }

        final Position[][] grid = bd.extractGrid(params.getxRes(), params.getyRes());
        wf.setPositionGrid(grid);

        final TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());// new
        // MillisecondsTimePoint(0);
        final TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());// new
        // MillisecondsTimePoint(30*1000);
        final TimePoint endTime = new MillisecondsTimePoint(params.getEndTime().getTime());// new MillisecondsTimePoint(10 *
        // 60 * 1000);

        wf.generate(startTime, null, timeStep);

        if (params.getMode() != SailingSimulatorUtil.measured) {
            Position[] gridAreaGps = new Position[2];
            gridAreaGps = course.toArray(gridAreaGps);
            wf.setGridAreaGps(gridAreaGps);
        }

        final WindFieldDTO wfDTO = createWindFieldDTO(wf, startTime, endTime, timeStep, params.isShowLines(), params.getSeedLines());
        logger.info("Exiting getWindField");
        return wfDTO;

    }

    @Override
    public List<WindPatternDTO> getWindPatterns() {
        return wpDisplayManager.getWindPatterns();
    }

    private PositionDTO createPositionDTO(final Position pos) {
        final PositionDTO posDTO = new PositionDTO();
        posDTO.latDeg = pos.getLatDeg();
        posDTO.lngDeg = pos.getLngDeg();

        return posDTO;
    }

    private SimulatorWindDTO createWindDTO(final Wind wind) {
        final SimulatorWindDTO windDTO = new SimulatorWindDTO();

        windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
        //windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = wind.getKnots();
        //windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();

        if (wind.getPosition() != null) {
            windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(), wind.getPosition().getLngDeg());
        }
        if (wind.getTimePoint() != null) {
            windDTO.timepoint = wind.getTimePoint().asMillis();
        }

        return windDTO;
    }

    private SimulatorWindDTO createWindDTO2(final TimedPositionWithSpeed tPos) {

        final SimulatorWindDTO windDTO = new SimulatorWindDTO();
        windDTO.trueWindBearingDeg = tPos.getSpeed().getBearing().getDegrees();
        //windDTO.trueWindFromDeg = tPos.getSpeed().getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = tPos.getSpeed().getKnots();
        //windDTO.trueWindSpeedInMetersPerSecond = tPos.getSpeed().getMetersPerSecond();

        if (tPos.getPosition() != null) {
            windDTO.position = new PositionDTO(tPos.getPosition().getLatDeg(), tPos.getPosition().getLngDeg());
        }

        if (tPos.getTimePoint() != null) {
            windDTO.timepoint = tPos.getTimePoint().asMillis();
        }

        return windDTO;
    }

    @Override
    public WindPatternDisplay getWindPatternDisplay(final WindPatternDTO pattern) {
        return wpDisplayManager.getDisplay(WindPattern.valueOf(pattern.name));
    }

    private WindFieldDTO createWindFieldDTO(final WindFieldGenerator wf, final TimePoint startTime, final TimePoint endTime,
            final TimePoint timeStep, final boolean isShowLines, final char seedLines) {

        final WindFieldDTO windFieldDTO = new WindFieldDTO();
        final List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
        final Position[][] positionGrid = wf.getPositionGrid();

        if (positionGrid != null && positionGrid.length > 0) {
            TimePoint t = startTime;
            while (t.compareTo(endTime) <= 0) {
                for (int i = 0; i < positionGrid.length; ++i) {
                    for (int j = 0; j < positionGrid[i].length; ++j) {
                        final Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(t, positionGrid[i][j], null));
                        logger.finer(localWind.toString());
                        final SimulatorWindDTO w = createWindDTO(localWind);
                        wList.add(w);
                    }
                }
                t = new MillisecondsTimePoint(t.asMillis() + timeStep.asMillis());
            }
        }

        windFieldDTO.setMatrix(wList);
        if (isShowLines) {
            
            if (seedLines == 'f') {
                getWindLinesFromStartLine(wf, windFieldDTO, startTime, endTime, timeStep);
            }
            if (seedLines == 'b') {
                getWindLinesFromEndLine(wf, windFieldDTO, startTime, endTime, timeStep);
            }
            
        }
        return windFieldDTO;
    }

    private void getWindLinesFromStartLine(final WindFieldGenerator wf, final WindFieldDTO windFieldDTO, final TimePoint startTime,
            final TimePoint endTime, final TimePoint timeStep) {

        final Position[][] positionGrid = wf.getPositionGrid();
        WindLinesDTO windLinesDTO = windFieldDTO.getWindLinesDTO();
        if (windLinesDTO == null) {
            windLinesDTO = new WindLinesDTO();
            windFieldDTO.setWindLinesDTO(windLinesDTO);
        }
        if (positionGrid != null && positionGrid.length > 0 && positionGrid[0].length > 2) {
            for (int j = 1; j < positionGrid[0].length - 1; ++j) {
                // for (int j = 0; j < positionGrid[0].length; ++j) {
                TimePoint t = startTime;
                Position p0 =  positionGrid[0][j];
                Position p1 =  positionGrid[1][j];
                Position seed = new DegreePosition(p0.getLatDeg() + 0.5*(p0.getLatDeg()-p1.getLatDeg()), p0.getLngDeg() + 0.5*(p0.getLngDeg()-p1.getLngDeg()));
                final PositionDTO startPosition = new PositionDTO(seed.getLatDeg(), seed.getLngDeg());
                while (t.compareTo(endTime) <= 0) {
                    final TimedPosition tp = new TimedPositionImpl(t, seed);
                    final Path p = wf.getLine(tp, false /*forward*/);
                    if (p != null) {
                        final List<PositionDTO> positions = new ArrayList<PositionDTO>();
                        for (final TimedPositionWithSpeed pathPoint : p.getPathPoints()) {
                            final Position position = pathPoint.getPosition();
                            final PositionDTO positionDTO = new PositionDTO(position.getLatDeg(), position.getLngDeg());
                            positions.add(positionDTO);
                        }

                        windLinesDTO.addWindLine(startPosition, tp.getTimePoint().asMillis(), positions);
                    }
                    t = new MillisecondsTimePoint(t.asMillis() + timeStep.asMillis());
                }
            }
        }
        //TODO: throws null pointer exception for when reading serialized paths.
        //TODO: should windlines also be serialized?
        //logger.info("Added : " + windFieldDTO.getWindLinesDTO().getWindLinesMap().size() + " wind lines");
    }

    private void getWindLinesFromEndLine(final WindFieldGenerator wf, final WindFieldDTO windFieldDTO, final TimePoint startTime,
            final TimePoint endTime, final TimePoint timeStep) {

        final Position[][] positionGrid = wf.getPositionGrid();
        WindLinesDTO windLinesDTO = windFieldDTO.getWindLinesDTO();
        if (windLinesDTO == null) {
            windLinesDTO = new WindLinesDTO();
            windFieldDTO.setWindLinesDTO(windLinesDTO);
        }
        if (positionGrid != null && positionGrid.length > 1 && positionGrid[0].length > 2) {
            final int lastRowIndex = positionGrid.length - 1;
            for (int j = 1; j < positionGrid[lastRowIndex].length - 1; ++j) {

                TimePoint t = startTime;
                Position p0 =  positionGrid[lastRowIndex][j];
                Position p1 =  positionGrid[lastRowIndex-1][j];
                Position seed = new DegreePosition(p0.getLatDeg() + 0.5*(p0.getLatDeg()-p1.getLatDeg()), p0.getLngDeg() + 0.5*(p0.getLngDeg()-p1.getLngDeg()));
                final PositionDTO startPosition = new PositionDTO(seed.getLatDeg(), seed.getLngDeg());
                while (t.compareTo(endTime) <= 0) {
                    final TimedPosition tp = new TimedPositionImpl(t, seed);
                    final Path p = wf.getLine(tp, true /*forward*/);
                    if (p != null) {
                        final List<PositionDTO> positions = new ArrayList<PositionDTO>();
                        for (final TimedPositionWithSpeed pathPoint : p.getPathPoints()) {
                            final Position position = pathPoint.getPosition();
                            final PositionDTO positionDTO = new PositionDTO(position.getLatDeg(), position.getLngDeg());
                            positions.add(positionDTO);
                        }

                        windLinesDTO.addWindLine(startPosition, tp.getTimePoint().asMillis(), positions);
                    }
                    t = new MillisecondsTimePoint(t.asMillis() + timeStep.asMillis());
                }
            }
        }

    }

    @SuppressWarnings("unused")
    private void getWindLines(final WindFieldGenerator wf, final WindFieldDTO windFieldDTO) {
        final Position[] course = wf.getGridAreaGps();
        /**
         * TODO Check this works for the measured case
         */
        if (course != null && course.length > 1) {
            /**
             * Currently create only a single line from the start position at the start time.
             */
            final TimedPosition tp = new TimedPositionImpl(wf.getStartTime(), course[0]);
            final PositionDTO startPosition = new PositionDTO(course[0].getLatDeg(), course[0].getLngDeg());

            final Path p = wf.getLine(tp, false);
            if (p != null) {
                final List<PositionDTO> positions = new ArrayList<PositionDTO>();
                for (final TimedPositionWithSpeed pathPoint : p.getPathPoints()) {
                    final Position position = pathPoint.getPosition();
                    final PositionDTO positionDTO = new PositionDTO(position.getLatDeg(), position.getLngDeg());
                    positions.add(positionDTO);
                }
                final WindLinesDTO windLinesDTO = new WindLinesDTO();
                windLinesDTO.addWindLine(startPosition, tp.getTimePoint().asMillis(), positions);
                windFieldDTO.setWindLinesDTO(windLinesDTO);
                logger.info("Added : " + windFieldDTO.getWindLinesDTO().getWindLinesMap().size() + " wind lines");
            }
        }
    }

    private SimulatedPathsEvenTimedResultDTO getSimulatedPathsEvenTimed(final List<Position> course, final WindFieldGenerator wf, final char mode, final int boatClassIndex) throws ConfigurationException {

        logger.info("Retrieving simulated paths");

        final PolarDiagramAndNotificationMessage polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);
        final PolarDiagram pd = polarDiagramAndNotificationMessage.getPolarDiagram();

        final SimulationParameters sp = new SimulationParametersImpl(course, pd, wf, mode);
        final SailingSimulator simulator = new SailingSimulatorImpl(sp);

        // final Map<String, Path> paths2 = simulator.getAllPathsEvenTimed2(wf.getTimeStep().asMillis());

        final Map<String, List<TimedPositionWithSpeed>> paths = simulator.getAllPathsEvenTimed(wf.getTimeStep().asMillis());
        final PathDTO[] pathDTO = new PathDTO[paths.size()];
        int pathIndex = paths.keySet().size() - 1;
        for (final String pathName : paths.keySet()) {

            logger.info("Path " + pathName);
            final List<TimedPositionWithSpeed> path = paths.get(pathName);

            // TODO: cleanup getAllPathsEvenTimed to return Paths instead of PathPoints
            if (pathName.equals("6#GPS Poly")) {
                // special initialization for polyline
                pathDTO[pathIndex] = this.convertToPathDTOAndMarkTurns(new PathImpl(paths.get(pathName), null), pathName.split("#")[1]);

            } else {

                // NOTE: pathName convention is: sort-digit + "#" + path-name
                pathDTO[pathIndex] = new PathDTO(pathName.split("#")[1]);

                // fill pathDTO with path points where speed is true wind speed
                final List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
                for (final TimedPositionWithSpeed p : path) {
                    final SimulatorWindDTO w = createWindDTO2(p);
                    wList.add(w);
                }
                pathDTO[pathIndex].setMatrix(wList);

            }

            pathIndex--;
        }


        RaceMapDataDTO rcDTO;
        if (mode == SailingSimulatorUtil.measured) {
            rcDTO = new RaceMapDataDTO();
            rcDTO.coursePositions = new CourseDTO();
            rcDTO.coursePositions.waypointPositions = new ArrayList<PositionDTO>();

            final Path rc = simulator.getRaceCourse();
            PositionDTO posDTO;
            posDTO = createPositionDTO(rc.getPathPoints().get(0).getPosition());

            rcDTO.coursePositions.waypointPositions.add(posDTO);
            posDTO = createPositionDTO(rc.getPathPoints().get(1).getPosition());
            rcDTO.coursePositions.waypointPositions.add(posDTO);
        } else {
            rcDTO = null;
        }

        final WindFieldDTO wfDTO = null;

        final SimulatedPathsEvenTimedResultDTO result = new SimulatedPathsEvenTimedResultDTO();
        result.setPathDTOs(pathDTO);
        result.setRaceMapDataDTO(rcDTO);
        result.setWindFieldDTO(wfDTO);
        result.setNotificationMessage(polarDiagramAndNotificationMessage.getNotificationMesssage());

        return result;
    }

    @Override
    public SimulatorResultsDTOAndNotificationMessage getSimulatorResults(final char mode, final WindFieldGenParamsDTO params,
            final WindPatternDisplay pattern, final boolean withWindField, final int boatClassIndex) throws WindPatternNotFoundException,
            ConfigurationException {

        WindFieldGenerator wf = null;
        List<Position> course = null;
        final TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());
        final TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());

        controlParameters.resetBlastRandomStream = params.isKeepState();
        retreiveWindControlParameters(pattern);

        wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), null, controlParameters);

        if (wf == null) {
            throw new WindPatternNotFoundException("Please select a valid wind pattern.");
        }

        if (mode != SailingSimulatorUtil.measured) {
            final Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
            final Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
            course = new ArrayList<Position>();
            course.add(nw);
            course.add(se);
            Position[] gridAreaGps = new Position[2];
            gridAreaGps = course.toArray(gridAreaGps);
            wf.setGridAreaGps(gridAreaGps);
        }

        final int[] gridRes = new int[2];
        gridRes[0] = params.getxRes();
        gridRes[1] = params.getyRes();
        wf.setGridResolution(gridRes);

        wf.generate(startTime, null, timeStep);
        Long longestPathTime = 0L;

        final SimulatedPathsEvenTimedResultDTO simulatedPathsEvenTimedResult = this.getSimulatedPathsEvenTimed(course, wf,
                mode, boatClassIndex);

        final PathDTO[] pathDTO = simulatedPathsEvenTimedResult.getPathDTOs();
        final RaceMapDataDTO rcDTO = simulatedPathsEvenTimedResult.getRaceMapDataDTO();

        for (int i = 0; i < pathDTO.length; ++i) {
            final List<SimulatorWindDTO> path = pathDTO[i].getMatrix();
            final int pathLength = path.size();
            final long pathTime = pathDTO[i].getMatrix().get(pathLength - 1).timepoint - path.get(0).timepoint;
            longestPathTime = Math.max(longestPathTime, pathTime);
        }

        final TimePoint endTime = new MillisecondsTimePoint(startTime.asMillis() + longestPathTime);

        WindFieldDTO windFieldDTO = null;
        if (pattern != null) {
            windFieldDTO = createWindFieldDTO(wf, startTime, endTime, timeStep, params.isShowLines(), params.getSeedLines());
        }
        final SimulatorResultsDTO simulatorResults = new SimulatorResultsDTO(rcDTO, pathDTO, windFieldDTO);

        final SimulatorResultsDTOAndNotificationMessage result = new SimulatorResultsDTOAndNotificationMessage();
        result.setSimulatorResultsDTO(simulatorResults);
        result.setNotificationMessage(simulatedPathsEvenTimedResult.getNotificationMessage());

        return result;
    }

    @Override
    public BoatClassDTOsAndNotificationMessage getBoatClasses() throws ConfigurationException {

        final List<BoatClassDTO> boatClassesDTOs = new ArrayList<BoatClassDTO>();

        final BoatClassDTOsAndNotificationMessage result = new BoatClassDTOsAndNotificationMessage();

        final ConfigurationManager config = ConfigurationManager.INSTANCE;
        if (config.getStatus() == ReadingConfigurationFileStatus.IO_ERROR) {
            throw new ConfigurationException(config.getErrorMessage());
        } else if (config.getStatus() == ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE
                || config.getStatus() == ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE) {
            result.setNotificationMessage(config.getErrorMessage());
        }

        for (final Tuple<String, Double, String> tuple : ConfigurationManager.INSTANCE.getBoatClassesInfo()) {
            boatClassesDTOs.add(new BoatClassDTO(tuple.first, tuple.second));
        }

        result.setBoatClassDTOs(boatClassesDTOs.toArray(new BoatClassDTO[boatClassesDTOs.size()]));

        return result;
    }

    @Override
    public PolarDiagramDTOAndNotificationMessage getPolarDiagramDTO(final Double bearingStep, final int boatClassIndex)
            throws ConfigurationException {

        final PolarDiagramAndNotificationMessage polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);

        final NavigableMap<Speed, NavigableMap<Bearing, Speed>> navMap = polarDiagramAndNotificationMessage.getPolarDiagram()
                .polarDiagramPlot(bearingStep);

        final Set<Speed> validSpeeds = navMap.keySet();
        validSpeeds.remove(Speed.NULL);

        final Number[][] series = new Number[validSpeeds.size()][];
        int i = 0;
        for (final Speed s : validSpeeds) {
            final Collection<Speed> boatSpeeds = navMap.get(s).values();
            series[i] = new Number[boatSpeeds.size()];
            int j = 0;
            for (final Speed boatSpeed : boatSpeeds) {
                series[i][j++] = new Double(boatSpeed.getKnots());
            }
            i++;
        }
        final PolarDiagramDTO dto = new PolarDiagramDTO();
        dto.setNumberSeries(series);

        final PolarDiagramDTOAndNotificationMessage result = new PolarDiagramDTOAndNotificationMessage();
        result.setPolarDiagramDTO(dto);
        result.setNotificationMessage(polarDiagramAndNotificationMessage.getNotificationMesssage());

        return result;
    }

    private PolarDiagramAndNotificationMessage getPolarDiagram(final int boatClassIndex) throws ConfigurationException {

        final ConfigurationManager config = ConfigurationManager.INSTANCE;
        final PolarDiagramAndNotificationMessage result = new PolarDiagramAndNotificationMessage();

        if (config.getStatus() == ReadingConfigurationFileStatus.IO_ERROR) {
            throw new ConfigurationException(config.getErrorMessage());
        } else if (config.getStatus() == ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE
                || config.getStatus() == ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE) {
            result.setNotificationMessage(config.getErrorMessage());
        }

        final String csvFilePath = config.getPolarDiagramFileLocation(boatClassIndex);

        try {
            result.setPolarDiagram(new PolarDiagramCSV(csvFilePath));
        } catch (final IOException exception) {
            throw new ConfigurationException(
                    "An IO error occured when parsing the CSV file! The original error message is "
                            + exception.getMessage());
        }

        return result;
    }

    private class PolarDiagramAndNotificationMessage {
        private PolarDiagram polarDiagram = null;
        private String notificationMessage = "";

        public void setPolarDiagram(final PolarDiagram polarDiagram) {
            this.polarDiagram = polarDiagram;
        }

        public void setNotificationMessage(final String notificationMessage) {
            this.notificationMessage = notificationMessage;
        }

        public PolarDiagram getPolarDiagram() {
            return this.polarDiagram;
        }

        public String getNotificationMesssage() {
            return this.notificationMessage;
        }
    }

    private PathDTO convertToPathDTOAndMarkTurns(final Path path, final String name) {
        if (path == null) {
            return null;
        }

        final List<TimedPositionWithSpeed> pathPoints = path.getPathPoints();
        final List<SimulatorWindDTO> windDTOs = new ArrayList<SimulatorWindDTO>();

        SimulatorWindDTO windDTO = null;
        SpeedWithBearing speedWithBearing = null;
        Bearing bearing = null;
        Position position = null;

        final List<TimedPositionWithSpeed> turnPoints = path.getTurns();
        boolean isTurn = false;

        for (final TimedPositionWithSpeed point : pathPoints) {

            isTurn = false;

            for (final TimedPositionWithSpeed turn : turnPoints) {
                if (turn.getPosition().getLatDeg() == point.getPosition().getLatDeg()
                        && turn.getPosition().getLngDeg() == point.getPosition().getLngDeg()) {
                    isTurn = true;
                    break;
                }
            }

            windDTO = new SimulatorWindDTO();
            windDTO.isTurn = isTurn;

            speedWithBearing = point.getSpeed();
            bearing = speedWithBearing.getBearing();

            windDTO.trueWindBearingDeg = bearing.getDegrees();
            //windDTO.trueWindFromDeg = bearing.reverse().getDegrees();
            windDTO.trueWindSpeedInKnots = speedWithBearing.getKnots();
            //windDTO.trueWindSpeedInMetersPerSecond = speedWithBearing.getMetersPerSecond();

            position = point.getPosition();
            if (position != null) {
                windDTO.position = new PositionDTO(position.getLatDeg(), position.getLngDeg());
            }

            if (point.getTimePoint() != null) {
                windDTO.timepoint = point.getTimePoint().asMillis();
            }

            windDTOs.add(windDTO);
        }

        final PathDTO result = new PathDTO(name);
        result.setMatrix(windDTOs);

        return result;
    }

    @Override
    public ReceivePolarDiagramDataDTO getSpeedsFromPolarDiagram(final RequestPolarDiagramDataDTO requestData) throws ConfigurationException {

        final PolarDiagramAndNotificationMessage polarDiagramAndNotificationMessage = this.getPolarDiagram(requestData.getBoatClass());
        final PolarDiagram polarDiagram = polarDiagramAndNotificationMessage.getPolarDiagram();

        final SpeedWithBearingDTO windSpeedDTO = requestData.getWindSpeed();
        final SpeedWithBearing windSpeed = new KnotSpeedWithBearingImpl(windSpeedDTO.speedInKnots, new DegreeBearingImpl(windSpeedDTO.bearingInDegrees));
        polarDiagram.setWind(windSpeed);

        final List<SpeedBearingPositionDTO> speeds = new ArrayList<SpeedBearingPositionDTO>();

        final List<PositionDTO> positions = requestData.getPositions();
        final int noOfPositions = positions.size();

        DegreePosition degreePositionStart = null;
        DegreePosition degreePositionEnd = null;
        PositionDTO positionDTO = null;
        Bearing bearing = null;
        SpeedWithBearing speedWithBearing = null;

        speeds.add(new SpeedBearingPositionDTO(positions.get(0), new SpeedWithBearingDTO(0.0, 0.0)));

        for (int index = 0; index < noOfPositions; index++) {

            if (index == noOfPositions - 1) {
                break;
            }

            positionDTO = positions.get(index);
            degreePositionStart = new DegreePosition(positionDTO.latDeg, positionDTO.lngDeg);

            positionDTO = positions.get(index + 1);
            degreePositionEnd = new DegreePosition(positionDTO.latDeg, positionDTO.lngDeg);

            bearing = degreePositionStart.getBearingGreatCircle(degreePositionEnd);

            speedWithBearing = polarDiagram.getSpeedAtBearing(bearing);

            speeds.add(new SpeedBearingPositionDTO(positionDTO,
                    new SpeedWithBearingDTO(speedWithBearing.getKnots(),
                    speedWithBearing.getBearing().getDegrees())));
        }

        return new ReceivePolarDiagramDataDTO(speeds, polarDiagramAndNotificationMessage.getNotificationMesssage());
    }
}
