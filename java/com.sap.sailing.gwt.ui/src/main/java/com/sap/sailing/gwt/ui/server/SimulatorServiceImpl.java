package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Quadruple;
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
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatedPathsEvenTimedResultDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
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
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.WindFieldGeneratorFactory;

public class SimulatorServiceImpl extends RemoteServiceServlet implements SimulatorService {

    private static final long serialVersionUID = 4445427185387524086L;

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    private static final WindFieldGeneratorFactory wfGenFactory = WindFieldGeneratorFactory.INSTANCE;
    private static final WindPatternDisplayManager wpDisplayManager = WindPatternDisplayManager.INSTANCE;
    private static final String POLYLINE_PATH_NAME = "Polyline";

    private final WindControlParameters controlParameters = new WindControlParameters(0, 0);

    /* PUBLIC MEMBERS */

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

    @Override
    public WindPatternDisplay getWindPatternDisplay(final WindPatternDTO pattern) {
        return wpDisplayManager.getDisplay(WindPattern.valueOf(pattern.name));
    }

    @Override
    public SimulatorResultsDTO getSimulatorResults(final char mode, final WindFieldGenParamsDTO params, final WindPatternDisplay pattern,
            final boolean withWindField, final int boatClassIndex) throws WindPatternNotFoundException, ConfigurationException {

        WindFieldGenerator wf = null;
        List<Position> course = null;
        final TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());
        final TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());

        controlParameters.resetBlastRandomStream = params.isKeepState();
        this.retreiveWindControlParameters(pattern);

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

        final SimulatedPathsEvenTimedResultDTO simulatedPaths = this.getSimulatedPathsEvenTimed(course, wf, mode, boatClassIndex);
        final PathDTO[] pathDTOs = simulatedPaths.pathDTOs;
        final RaceMapDataDTO rcDTO = simulatedPaths.raceMapDataDTO;

        for (final PathDTO path : pathDTOs) {
            if (path.name.equals(POLYLINE_PATH_NAME)) {
                continue;
            }

            final List<SimulatorWindDTO> points = path.getPoints();
            final long pathTime = points.get(points.size() - 1).timepoint - points.get(0).timepoint;
            longestPathTime = Math.max(longestPathTime, pathTime);
        }

        final TimePoint endTime = new MillisecondsTimePoint(startTime.asMillis() + longestPathTime);

        WindFieldDTO windFieldDTO = null;
        if (pattern != null) {
            windFieldDTO = this.createWindFieldDTO(wf, startTime, endTime, timeStep, params.isShowLines(), params.getSeedLines());
        }

        return new SimulatorResultsDTO(rcDTO, pathDTOs, windFieldDTO, simulatedPaths.notificationMessage);
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

        for (final Quadruple<String, Double, String, Integer> tuple : ConfigurationManager.INSTANCE.getBoatClassesInfo()) {
            boatClassesDTOs.add(new BoatClassDTO(tuple.getA(), tuple.getB()));
        }

        result.setBoatClassDTOs(boatClassesDTOs.toArray(new BoatClassDTO[boatClassesDTOs.size()]));

        return result;
    }

    @Override
    public PolarDiagramDTOAndNotificationMessage getPolarDiagramDTO(final Double bearingStep, final int boatClassIndex)
            throws ConfigurationException {

        final Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);

        final NavigableMap<Speed, NavigableMap<Bearing, Speed>> navMap = polarDiagramAndNotificationMessage.getA()
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
        result.setNotificationMessage(polarDiagramAndNotificationMessage.getB());

        return result;
    }

    /* PRIVATE MEMBERS */

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

    private static PositionDTO toPositionDTO(final Position position) {
        return new PositionDTO(position.getLatDeg(), position.getLngDeg());
    }

    private SimulatorWindDTO createSimulatorWindDTO(final Wind wind) {

        final Position position = wind.getPosition();
        final TimePoint timePoint = wind.getTimePoint();

        final SimulatorWindDTO result = new SimulatorWindDTO();
        result.trueWindBearingDeg = wind.getBearing().getDegrees();
        result.trueWindSpeedInKnots = wind.getKnots();

        if (position != null) {
            result.position = toPositionDTO(position);
        }
        if (timePoint != null) {
            result.timepoint = timePoint.asMillis();
        }

        return result;
    }

    private SimulatorWindDTO createSimulatorWindDTO(final TimedPositionWithSpeed timedPositionWithSpeed) {

        final Position position = timedPositionWithSpeed.getPosition();
        final SpeedWithBearing speedWithBearing = timedPositionWithSpeed.getSpeed();
        final TimePoint timePoint = timedPositionWithSpeed.getTimePoint();

        final SimulatorWindDTO result = new SimulatorWindDTO();
        result.trueWindBearingDeg = speedWithBearing.getBearing().getDegrees();
        result.trueWindSpeedInKnots = speedWithBearing.getKnots();

        if (position != null) {
            result.position = toPositionDTO(position);
        }

        if (timePoint != null) {
            result.timepoint = timePoint.asMillis();
        }

        return result;
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
                        wList.add(createSimulatorWindDTO(localWind));
                    }
                }
                t = new MillisecondsTimePoint(t.asMillis() + timeStep.asMillis());
            }
        }

        windFieldDTO.setMatrix(wList);

        if (isShowLines && seedLines == 'f') {
            this.getWindLinesFromStartLine(wf, windFieldDTO, startTime, endTime, timeStep);
        }

        if (isShowLines && seedLines == 'b') {
            this.getWindLinesFromEndLine(wf, windFieldDTO, startTime, endTime, timeStep);
        }
        
        windFieldDTO.curBearing = wf.getWindParameters().curBearing;
        windFieldDTO.curSpeed = wf.getWindParameters().curSpeed;

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
                final Position p0 =  positionGrid[0][j];
                final Position p1 =  positionGrid[1][j];
                final Position seed = new DegreePosition(p0.getLatDeg() + 0.5*(p0.getLatDeg()-p1.getLatDeg()), p0.getLngDeg() + 0.5*(p0.getLngDeg()-p1.getLngDeg()));
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
                final Position p0 =  positionGrid[lastRowIndex][j];
                final Position p1 =  positionGrid[lastRowIndex-1][j];
                final Position seed = new DegreePosition(p0.getLatDeg() + 0.5*(p0.getLatDeg()-p1.getLatDeg()), p0.getLngDeg() + 0.5*(p0.getLngDeg()-p1.getLngDeg()));
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

    private SimulatedPathsEvenTimedResultDTO getSimulatedPathsEvenTimed(final List<Position> course, final WindFieldGenerator wf, final char mode,
            final int boatClassIndex) throws ConfigurationException {

        logger.info("Retrieving simulated paths");

        final Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);
        final PolarDiagram pd = polarDiagramAndNotificationMessage.getA();

        final SimulationParameters sp = new SimulationParametersImpl(course, pd, wf, mode);
        final SailingSimulator simulator = new SailingSimulatorImpl(sp);
        final Map<String, Path> pathsAndNames = simulator.getAllPathsEvenTimed(wf.getTimeStep().asMillis());

        int noOfPaths = pathsAndNames.size();
        if (mode == SailingSimulatorUtil.measured) {
            noOfPaths++; // the last path is the polyline
        }
        final PathDTO[] pathDTOs = new PathDTO[noOfPaths];
        int index = noOfPaths - 1;

        if (mode == SailingSimulatorUtil.measured) {
            // Adding the polyline
            pathDTOs[0] = this.getPolylinePathDTO(pathsAndNames.get("6#GPS Poly"), pathsAndNames.get("7#GPS Track"));
        }

        for (final Entry<String, Path> entry : pathsAndNames.entrySet()) {
            logger.info("Path " + entry.getKey());

            // NOTE: pathName convention is: sort-digit + "#" + path-name
            pathDTOs[index] = new PathDTO(entry.getKey().split("#")[1]);

            // fill pathDTO with path points where speed is true wind speed
            final List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
            for (final TimedPositionWithSpeed p : entry.getValue().getPathPoints()) {
                wList.add(createSimulatorWindDTO(p));
            }

            pathDTOs[index].setPoints(wList);

            index--;
        }


        RaceMapDataDTO rcDTO;
        if (mode == SailingSimulatorUtil.measured) {
            rcDTO = new RaceMapDataDTO();
            rcDTO.coursePositions = new CourseDTO();
            rcDTO.coursePositions.waypointPositions = new ArrayList<PositionDTO>();

            final Path rc = simulator.getRaceCourse();
            PositionDTO posDTO;
            posDTO = toPositionDTO(rc.getPathPoints().get(0).getPosition());

            rcDTO.coursePositions.waypointPositions.add(posDTO);
            posDTO = toPositionDTO(rc.getPathPoints().get(1).getPosition());
            rcDTO.coursePositions.waypointPositions.add(posDTO);
        } else {
            rcDTO = null;
        }

        return new SimulatedPathsEvenTimedResultDTO(pathDTOs, rcDTO, null, polarDiagramAndNotificationMessage.getB());
    }

    private Pair<PolarDiagram, String> getPolarDiagram(final int boatClassIndex) throws ConfigurationException {

        final ConfigurationManager config = ConfigurationManager.INSTANCE;
        PolarDiagram polarDiagram = null;
        String notificationMessage = "";

        if (config.getStatus() == ReadingConfigurationFileStatus.IO_ERROR) {
            throw new ConfigurationException(config.getErrorMessage());
        } else if (config.getStatus() == ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE
                || config.getStatus() == ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE) {
            notificationMessage = config.getErrorMessage();
        }

        final String csvFilePath = config.getPolarDiagramFileLocation(boatClassIndex);

        try {
            polarDiagram = new PolarDiagramCSV(csvFilePath);
        } catch (final IOException exception) {
            throw new ConfigurationException(
                    "An IO error occured when parsing the CSV file! The original error message is "
                            + exception.getMessage());
        }

        return new Pair<PolarDiagram, String>(polarDiagram, notificationMessage);
    }

    private PathDTO getPolylinePathDTO(final Path gpsPoly, final Path gpsTrack) {

        final List<TimedPositionWithSpeed> gpsTrackPoints = gpsTrack.getPathPoints();
        final List<TimedPositionWithSpeed> gpsPolyPoints = gpsPoly.getPathPoints();

        final int noOfGpsTrackPoints = gpsTrackPoints.size();
        final int noOfGpsPolyPoints = gpsPolyPoints.size();

        if (noOfGpsTrackPoints == 0 || noOfGpsTrackPoints == 1 || noOfGpsPolyPoints == 0 || noOfGpsPolyPoints == 1) {
            return null;
        }

        final TimedPositionWithSpeed startPoint = gpsPolyPoints.get(0);
        final TimedPositionWithSpeed endPoint = gpsPolyPoints.get(noOfGpsPolyPoints - 1);

        // System.out.println("gpsTrackPoints.size() = " + gpsTrackPoints.size());
        final int startPointIndex = getIndexOfClosest(gpsTrackPoints, startPoint);
        // System.out.println("startPointIndex = " + startPointIndex);
        final int endPointIndex = getIndexOfClosest(gpsTrackPoints, endPoint);
        // System.out.println("endPointIndex = " + endPointIndex);

        final List<TimedPositionWithSpeed> polylinePoints = gpsTrackPoints.subList(startPointIndex, endPointIndex + 1);

        final List<TimedPositionWithSpeed> turns = (new PathImpl(polylinePoints, null)).getTurns();
        // PathImpl.saveToGpxFile(new PathImpl(turns, null), "C:\\gps_path_turns_20deg_not_even_timed.gpx");

        final List<SimulatorWindDTO> points = new ArrayList<SimulatorWindDTO>();

        boolean isTurn = false;
        SpeedWithBearing speedWithBearing = null;
        Position position = null;

        for (final TimedPositionWithSpeed point : polylinePoints) {

            isTurn = false;

            for (final TimedPositionWithSpeed turn : turns) {
                if (turn.getPosition().getLatDeg() == point.getPosition().getLatDeg() && turn.getPosition().getLngDeg() == point.getPosition().getLngDeg()
                        && turn.getTimePoint().asMillis() == point.getTimePoint().asMillis() && turn.getSpeed().getKnots() == point.getSpeed().getKnots()
                        && turn.getSpeed().getBearing().getDegrees() == point.getSpeed().getBearing().getDegrees()) {
                    isTurn = true;
                    break;
                }
            }

            speedWithBearing = point.getSpeed();
            position = point.getPosition();

            points.add(new SimulatorWindDTO(position.getLatDeg(), position.getLngDeg(), speedWithBearing.getKnots(),
                    speedWithBearing.getBearing()
                    .getDegrees(), point.getTimePoint().asMillis(), isTurn));
        }

        final PathDTO result = new PathDTO(POLYLINE_PATH_NAME);
        result.setPoints(points);

        return result;
    }

    private static int getIndexOfClosest(final List<TimedPositionWithSpeed> items, final TimedPositionWithSpeed item) {
        final int count = items.size();

        final List<Double> diff_lat = new ArrayList<Double>();
        final List<Double> diff_lng = new ArrayList<Double>();
        final List<Long> diff_timepoint = new ArrayList<Long>();

        for (int index = 0; index < count; index++) {
            diff_lat.add(Math.abs(items.get(index).getPosition().getLatDeg() - item.getPosition().getLatDeg()));
            diff_lng.add(Math.abs(items.get(index).getPosition().getLngDeg() - item.getPosition().getLngDeg()));
            diff_timepoint.add(Math.abs(items.get(index).getTimePoint().asMillis() - item.getTimePoint().asMillis()));
        }

        final double min_diff_lat = Collections.min(diff_lat);
        final double min_max_diff_lat = min_diff_lat + Collections.max(diff_lat);

        final double min_diff_lng = Collections.min(diff_lng);
        final double min_max_diff_lng = min_diff_lng + Collections.max(diff_lng);

        final long min_diff_timepoint = Collections.min(diff_timepoint);
        final double min_max_diff_timepoint = min_diff_timepoint + Collections.max(diff_timepoint);

        final List<Double> norm_diff_lat = new ArrayList<Double>();
        final List<Double> norm_diff_lng = new ArrayList<Double>();
        final List<Double> norm_diff_timepoint = new ArrayList<Double>();

        for (int index = 0; index < count; index++) {
            norm_diff_lat.add((diff_lat.get(index) - min_diff_lat) / min_max_diff_lat);
            norm_diff_lng.add((diff_lng.get(index) - min_diff_lng) / min_max_diff_lng);
            norm_diff_timepoint.add((diff_timepoint.get(index) - min_diff_timepoint) / min_max_diff_timepoint);
        }

        final List<Double> deltas = new ArrayList<Double>();

        for (int index = 0; index < count; index++) {
            deltas.add(Math.sqrt(Math.pow(norm_diff_lat.get(index), 2) + Math.pow(norm_diff_lng.get(index), 2) + Math.pow(norm_diff_timepoint.get(index), 2)));
        }

        int result = 0;
        double min = deltas.get(0);

        for (int index = 0; index < count; index++) {
            if (deltas.get(index) < min) {
                result = index;
                min = deltas.get(index);
            }
        }

        return result;
    }


    @Override
    public ResponseTotalTimeDTO getTotalTime_old(final RequestTotalTimeDTO requestData) throws ConfigurationException {

        this.averageWind = requestData.useRealAverageWindSpeed ? SimulatorServiceUtils.getAverage(requestData.allPoints)
                : DEFAULT_AVERAGE_WIND;

        this.stepSizeMeters = SimulatorServiceUtils.knotsToMetersPerSecond(this.averageWind.getKnots()) * (requestData.stepDurationMilliseconds / 1000);

        final List<Position> points = SimulatorServiceUtils.getIntermediatePoints2(requestData.turnPoints, this.stepSizeMeters);
        final int noOfPointsMinus1 = points.size() - 1;

        final Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(requestData.boatClassID);
        final PolarDiagram polarDiagram = polarDiagramAndNotificationMessage.getA();
        final String notificationMessage = polarDiagramAndNotificationMessage.getB();

        final List<Pair<Position, Double>> speeds = new ArrayList<Pair<Position, Double>>();

        Position startPoint = null;
        Position endPoint = null;
        double bearingDeg = 0.;
        SpeedWithBearing speedWithBearing = null;

        speeds.add(new Pair<Position, Double>(points.get(0), 0.0));

        polarDiagram.setWind(averageWind);

        for (int index = 0; index < noOfPointsMinus1; index++) {

            startPoint = points.get(index);
            endPoint = points.get(index + 1);

            bearingDeg = SimulatorServiceUtils.getInitialBearing(startPoint, endPoint);

            speedWithBearing = polarDiagram.getSpeedAtBearing(new DegreeBearingImpl(bearingDeg));
            speeds.add(new Pair<Position, Double>(endPoint, speedWithBearing.getKnots()));
        }

        double totalTimeSeconds = 0.0;
        final int noOfSpeedsMinus1 = speeds.size() - 1;
        Pair<Position, Double> startSpeedAndPosition = null;
        Pair<Position, Double> endSpeedAndPosition = null;
        double endSpeed = 0.0;

        for (int index = 0; index < noOfSpeedsMinus1; index++) {

            startSpeedAndPosition = speeds.get(index);
            endSpeedAndPosition = speeds.get(index + 1);

            startPoint = startSpeedAndPosition.getA();
            endPoint = endSpeedAndPosition.getA();

            endSpeed = SimulatorServiceUtils.knotsToMetersPerSecond(endSpeedAndPosition.getB());

            totalTimeSeconds += SimulatorServiceUtils.getTimeSeconds(startPoint, endPoint, endSpeed);
        }

        return new ResponseTotalTimeDTO((long) totalTimeSeconds, notificationMessage, null);
    }

    @Override
    public ResponseTotalTimeDTO getTotalTime_new(final RequestTotalTimeDTO requestData) throws ConfigurationException {

        this.averageWind = requestData.useRealAverageWindSpeed ? SimulatorServiceUtils.getAverage(requestData.allPoints)
                : DEFAULT_AVERAGE_WIND;

        this.stepSizeMeters = SimulatorServiceUtils.knotsToMetersPerSecond(this.averageWind.getKnots()) * (requestData.stepDurationMilliseconds / 1000.);

        final List<Position> points = SimulatorServiceUtils.getIntermediatePoints2(requestData.turnPoints, this.stepSizeMeters);
        final int noOfPointsMinus1 = points.size() - 1;

        final Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(requestData.boatClassID);
        final PolarDiagram polarDiagram = polarDiagramAndNotificationMessage.getA();
        final String notificationMessage = polarDiagramAndNotificationMessage.getB();

        final SailingSimulator simulator = new SailingSimulatorImpl(new SimulationParametersImpl(null, polarDiagram, null, SailingSimulatorUtil.measured));
        final Path gpsTrack = simulator.getGPSTrack();

        Position startPoint = null;
        Position endPoint = null;
        double boatBearingDeg = 0.;
        double boatSpeedMetersPerSecond = 0.;
        double distanceMeters = 0.;

        final SimulatorWindDTO courseStartPoint = requestData.allPoints.get(0);
        long timepointAsMillis = courseStartPoint.timepoint;
        SpeedWithBearing windAtTimePoint = null;

        long stepTimeMilliseconds = 0L;

        // start of [used only for debug mode]
        final List<Quadruple<PositionDTO, PositionDTO, Double, Double>> segments = new ArrayList<Quadruple<PositionDTO, PositionDTO, Double, Double>>();
        Position segmentStart = points.get(0);
        Position segmentEnd = null;
        double segmentLength = 0.0;
        double segmentTime = 0.0;
        int turnIndex = 1;
        // end of [used only for debug mode]

        for (int index = 0; index < noOfPointsMinus1; index++) {

            startPoint = points.get(index);
            endPoint = points.get(index + 1);
            distanceMeters = SimulatorServiceUtils.getDistanceBetween(startPoint, endPoint);

            windAtTimePoint = requestData.useRealAverageWindSpeed ? getWindAtTimepoint(timepointAsMillis, gpsTrack) : DEFAULT_AVERAGE_WIND;

            boatBearingDeg = SimulatorServiceUtils.getInitialBearing(startPoint, endPoint);
            polarDiagram.setWind(windAtTimePoint);
            boatSpeedMetersPerSecond = polarDiagram.getSpeedAtBearing(new DegreeBearingImpl(boatBearingDeg)).getMetersPerSecond();
            stepTimeMilliseconds = (long) ((distanceMeters / boatSpeedMetersPerSecond) * 1000);

            if (requestData.debugMode) {

                segmentTime += stepTimeMilliseconds;
                if (equals(endPoint, requestData.turnPoints.get(turnIndex))) {

                    segmentEnd = endPoint;
                    segmentLength = SimulatorServiceUtils.getDistanceBetween(segmentStart, segmentEnd);
                    segments.add(new Quadruple<PositionDTO, PositionDTO, Double, Double>(toPositionDTO(segmentStart), toPositionDTO(segmentEnd), segmentLength,
                            segmentTime));

                    segmentTime = 0.0;
                    segmentStart = endPoint;
                    turnIndex++;
                }
            }

            timepointAsMillis += stepTimeMilliseconds;
        }

        final double totalTimeSeconds = (timepointAsMillis - requestData.allPoints.get(0).timepoint) / 1000;

        return new ResponseTotalTimeDTO((long) totalTimeSeconds, notificationMessage, (requestData.debugMode ? segments : null));
    }

    private static SpeedWithBearing getWindAtTimepoint(final long timepointAsMillis, final Path gpsTrack) {
        final List<TimedPositionWithSpeed> pathPoints = gpsTrack.getPathPoints();
        final int noOfPathPoints = pathPoints.size();
        final List<Double> diffs = new ArrayList<Double>();

        for (int index = 0; index < noOfPathPoints; index++) {
            diffs.add(new Double(Math.abs(pathPoints.get(index).getTimePoint().asMillis() - timepointAsMillis)));
        }

        final int indexOfMinDiff = diffs.indexOf(Collections.min(diffs));

        return pathPoints.get(indexOfMinDiff).getSpeed();
    }

    private static SpeedWithBearing DEFAULT_AVERAGE_WIND = new KnotSpeedWithBearingImpl(4.5, new DegreeBearingImpl(350));

    private SpeedWithBearing averageWind = null;

    public SpeedWithBearing getAverageWind() {
        return this.averageWind;
    }

    private double stepSizeMeters = 0.0;

    public double getStepSizeMeters() {
        return this.stepSizeMeters;
    }

    public static boolean equals(final Position position, final PositionDTO positonDTO) {
        return (position.getLatDeg() == positonDTO.latDeg && position.getLngDeg() == positonDTO.lngDeg);
    }
}
