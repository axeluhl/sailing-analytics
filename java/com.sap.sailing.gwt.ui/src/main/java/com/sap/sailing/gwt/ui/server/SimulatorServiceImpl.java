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
import com.sap.sailing.gwt.ui.shared.Request1TurnerDTO;
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.Response1TurnerDTO;
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
import com.sap.sailing.simulator.impl.PathGenerator1Turner;
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
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorMeasured;
public class SimulatorServiceImpl extends RemoteServiceServlet implements SimulatorService {

    private static final long serialVersionUID = 4445427185387524086L;

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing");
    private static final WindFieldGeneratorFactory wfGenFactory = WindFieldGeneratorFactory.INSTANCE;
    private static final WindPatternDisplayManager wpDisplayManager = WindPatternDisplayManager.INSTANCE;
    private static final String POLYLINE_PATH_NAME = "Polyline";

    private static final double TOTAL_TIME_SCALE_FACTOR = 0.9;
    private static final int DEFAULT_STEP_MAX = 800;
    private static final long DEFAULT_TIMESTEP = 6666;

    private WindControlParameters controlParameters = new WindControlParameters(0, 0);

    /* PUBLIC MEMBERS */

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

    @Override
    public WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params) {
        Bearing north = new DegreeBearingImpl(0);
        Bearing east = new DegreeBearingImpl(90);
        Bearing south = new DegreeBearingImpl(180);
        Bearing west = new DegreeBearingImpl(270);

        double xSize = params.getxSize();
        double ySize = params.getySize();
        int gridsizeX = params.getGridsizeX();
        int gridsizeY = params.getGridsizeY();

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

                PositionDTO pdto = new PositionDTO(crt.getLatDeg(), crt.getLngDeg());
                matrix[i][j] = pdto;
            }

        }

        wl.setMatrix(matrix);

        return wl;
    }

    @Override
    public WindFieldDTO getWindField(WindFieldGenParamsDTO params, WindPatternDisplay pattern)
            throws WindPatternNotFoundException {
        LOGGER.info("Entering getWindField");
        Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);
        // List<Position> lattice = bd.extractLattice(params.getxRes(),
        // params.getyRes());

        controlParameters.resetBlastRandomStream = params.isKeepState();
        retreiveWindControlParameters(pattern);
        LOGGER.info("Boundary south direction " + bd.getSouth());
        controlParameters.baseWindBearing += bd.getSouth().getDegrees();

        WindFieldGenerator wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), bd,
                controlParameters);

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

        if (params.getMode() != SailingSimulatorUtil.measured) {
            Position[] gridAreaGps = new Position[2];
            gridAreaGps = course.toArray(gridAreaGps);
            wf.setGridAreaGps(gridAreaGps);
        }

        WindFieldDTO wfDTO = createWindFieldDTO(wf, startTime, endTime, timeStep, params.isShowLines(), params.getSeedLines());
        LOGGER.info("Exiting getWindField");
        return wfDTO;

    }

    @Override
    public List<WindPatternDTO> getWindPatterns() {
        return wpDisplayManager.getWindPatterns();
    }

    @Override
    public WindPatternDisplay getWindPatternDisplay(WindPatternDTO pattern) {
        return wpDisplayManager.getDisplay(WindPattern.valueOf(pattern.name));
    }

    // @Override
    // public SimulatorResultsDTO getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay
    // pattern, boolean withWindField,
    // int boatClassIndex) throws WindPatternNotFoundException, ConfigurationException {
    //
    // WindFieldGenerator wf = null;
    // List<Position> course = null;
    // TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());
    // TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());
    //
    // controlParameters.resetBlastRandomStream = params.isKeepState();
    // this.retreiveWindControlParameters(pattern);
    //
    // wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), null, controlParameters);
    //
    // if (wf == null) {
    // throw new WindPatternNotFoundException("Please select a valid wind pattern.");
    // }
    //
    // if (mode != SailingSimulatorUtil.measured) {
    // Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
    // Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
    // course = new ArrayList<Position>();
    // course.add(nw);
    // course.add(se);
    // Position[] gridAreaGps = new Position[2];
    // gridAreaGps = course.toArray(gridAreaGps);
    // wf.setGridAreaGps(gridAreaGps);
    // }
    //
    // int[] gridRes = new int[2];
    // gridRes[0] = params.getxRes();
    // gridRes[1] = params.getyRes();
    // wf.setGridResolution(gridRes);
    //
    // wf.generate(startTime, null, timeStep);
    // Long longestPathTime = 0L;
    //
    // SimulatedPathsEvenTimedResultDTO simulatedPaths = this.getSimulatedPathsEvenTimed(course, wf, mode,
    // boatClassIndex);
    // PathDTO[] pathDTOs = simulatedPaths.pathDTOs;
    // RaceMapDataDTO rcDTO = simulatedPaths.raceMapDataDTO;
    //
    // for (PathDTO path : pathDTOs) {
    // if (path.name.equals(POLYLINE_PATH_NAME)) {
    // continue;
    // }
    //
    // List<SimulatorWindDTO> points = path.getPoints();
    // long pathTime = points.get(points.size() - 1).timepoint - points.get(0).timepoint;
    // longestPathTime = Math.max(longestPathTime, pathTime);
    // }
    //
    // TimePoint endTime = new MillisecondsTimePoint(startTime.asMillis() + longestPathTime);
    //
    // WindFieldDTO windFieldDTO = null;
    // if (pattern != null) {
    // windFieldDTO = this.createWindFieldDTO(wf, startTime, endTime, timeStep, params.isShowLines(),
    // params.getSeedLines());
    // }
    //
    // return new SimulatorResultsDTO(rcDTO, pathDTOs, windFieldDTO, simulatedPaths.notificationMessage);
    // }

    @Override
    public SimulatorResultsDTO getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay pattern, boolean withWindField,
            int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) throws WindPatternNotFoundException,
            ConfigurationException {
        WindFieldGenerator wf = null;
        List<Position> course = null;
        TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());
        TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());

        this.controlParameters.resetBlastRandomStream = params.isKeepState();
        this.retreiveWindControlParameters(pattern);

        wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), null, this.controlParameters);

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

        SimulatedPathsEvenTimedResultDTO simulatedPaths = this.getSimulatedPathsEvenTimed(course, wf, mode, selectedBoatClassIndex, selectedRaceIndex,
                selectedCompetitorIndex, selectedLegIndex);
        PathDTO[] pathDTOs = simulatedPaths.pathDTOs;
        RaceMapDataDTO rcDTO = simulatedPaths.raceMapDataDTO;

        for (PathDTO path : pathDTOs) {
            if (path.name.equals(POLYLINE_PATH_NAME)) {
                continue;
            }

            List<SimulatorWindDTO> points = path.getPoints();
            long pathTime = points.get(points.size() - 1).timepoint - points.get(0).timepoint;
            longestPathTime = Math.max(longestPathTime, pathTime);
        }

        TimePoint endTime = new MillisecondsTimePoint(startTime.asMillis() + longestPathTime);

        WindFieldDTO windFieldDTO = null;
        if (pattern != null) {
            windFieldDTO = this.createWindFieldDTO(wf, startTime, endTime, timeStep, params.isShowLines(), params.getSeedLines());
        }

        return new SimulatorResultsDTO(rcDTO, pathDTOs, windFieldDTO, simulatedPaths.notificationMessage);
    }

    @Override
    public BoatClassDTOsAndNotificationMessage getBoatClasses() throws ConfigurationException {

        List<BoatClassDTO> boatClassesDTOs = new ArrayList<BoatClassDTO>();

        BoatClassDTOsAndNotificationMessage result = new BoatClassDTOsAndNotificationMessage();

        ConfigurationManager config = ConfigurationManager.INSTANCE;
        if (config.getStatus() == ReadingConfigurationFileStatus.IO_ERROR) {
            throw new ConfigurationException(config.getErrorMessage());
        } else if (config.getStatus() == ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE
                || config.getStatus() == ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE) {
            result.setNotificationMessage(config.getErrorMessage());
        }

        for (Quadruple<String, Double, String, Integer> tuple : ConfigurationManager.INSTANCE.getBoatClassesInfo()) {
            boatClassesDTOs.add(new BoatClassDTO(tuple.getA(), tuple.getB()));
        }

        result.setBoatClassDTOs(boatClassesDTOs.toArray(new BoatClassDTO[boatClassesDTOs.size()]));

        return result;
    }

    @Override
    public PolarDiagramDTOAndNotificationMessage getPolarDiagram(Double bearingStep, int boatClassIndex)
            throws ConfigurationException {

        Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);

        NavigableMap<Speed, NavigableMap<Bearing, Speed>> navMap = polarDiagramAndNotificationMessage.getA()
                .polarDiagramPlot(bearingStep);

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
        result.setNotificationMessage(polarDiagramAndNotificationMessage.getB());

        return result;
    }

    @Override
    public ResponseTotalTimeDTO getTotalTime_old(RequestTotalTimeDTO requestData) throws ConfigurationException {

        this.averageWind = requestData.useRealAverageWindSpeed ? SimulatorServiceUtils.getAverage(requestData.allPoints) : DEFAULT_AVERAGE_WIND;

        this.stepSizeMeters = SimulatorServiceUtils.knotsToMetersPerSecond(this.averageWind.getKnots()) * (requestData.stepDurationMilliseconds / 1000);

        List<Position> points = SimulatorServiceUtils.getIntermediatePoints2(requestData.turnPoints, this.stepSizeMeters);
        int noOfPointsMinus1 = points.size() - 1;

        Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(requestData.selectedBoatClassIndex);
        PolarDiagram polarDiagram = polarDiagramAndNotificationMessage.getA();
        String notificationMessage = polarDiagramAndNotificationMessage.getB();

        List<Pair<Position, Double>> speeds = new ArrayList<Pair<Position, Double>>();

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
        int noOfSpeedsMinus1 = speeds.size() - 1;
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
    public ResponseTotalTimeDTO getTotalTime_new(RequestTotalTimeDTO requestData) throws ConfigurationException {

        this.averageWind = requestData.useRealAverageWindSpeed ? SimulatorServiceUtils.getAverage(requestData.allPoints) : DEFAULT_AVERAGE_WIND;

        this.stepSizeMeters = SimulatorServiceUtils.knotsToMetersPerSecond(this.averageWind.getKnots()) * (requestData.stepDurationMilliseconds / 1000.);

        List<Position> points = SimulatorServiceUtils.getIntermediatePoints2(requestData.turnPoints, this.stepSizeMeters);
        int noOfPointsMinus1 = points.size() - 1;

        Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(requestData.selectedBoatClassIndex);
        PolarDiagram polarDiagram = polarDiagramAndNotificationMessage.getA();
        String notificationMessage = polarDiagramAndNotificationMessage.getB();

        SailingSimulator simulator = new SailingSimulatorImpl(new SimulationParametersImpl(null, polarDiagram, null, SailingSimulatorUtil.measured));
        Path gpsTrack = simulator.getLegGPSTrack(requestData.selectedRaceIndex, requestData.selectedCompetitorIndex, requestData.selectedLegIndex);

        Position startPoint = null;
        Position endPoint = null;
        double boatBearingDeg = 0.;
        double boatSpeedMetersPerSecond = 0.;
        double distanceMeters = 0.;

        SimulatorWindDTO courseStartPoint = requestData.allPoints.get(0);
        long timepointAsMillis = courseStartPoint.timepoint;
        SpeedWithBearing windAtTimePoint = null;

        long stepTimeMilliseconds = 0L;

        // start of [used only for debug mode]
        List<Quadruple<PositionDTO, PositionDTO, Double, Double>> segments = new ArrayList<Quadruple<PositionDTO, PositionDTO, Double, Double>>();
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
            //problem right here
            //boatSpeed might be 0 for very small distances
            //this is a rough fix
            if(boatSpeedMetersPerSecond == 0.0) {
                stepTimeMilliseconds = 1000;
            }

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

        double totalTimeSeconds = (timepointAsMillis - requestData.allPoints.get(0).timepoint) / 1000;

        double totalTimeGPSTrackSeconds = (gpsTrack.getPathPoints().get(gpsTrack.getPathPoints().size() - 1).getTimePoint().asMillis() - gpsTrack
                .getPathPoints().get(0).getTimePoint().asMillis()) / 1000;
        // System.out.println("totalTimeSeconds = " + totalTimeSeconds);
        // System.out.println("totalTimeGPSTrackSeconds = " + totalTimeGPSTrackSeconds);

        while (totalTimeSeconds > totalTimeGPSTrackSeconds) {
            totalTimeSeconds *= TOTAL_TIME_SCALE_FACTOR;
        }

        return new ResponseTotalTimeDTO((long) totalTimeSeconds, notificationMessage, (requestData.debugMode ? segments : null));
    }

    @Override
    public Response1TurnerDTO get1Turner(Request1TurnerDTO requestData) throws ConfigurationException {

        Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(requestData.selectedBoatClassIndex);
        PolarDiagram polarDiagram = polarDiagramAndNotificationMessage.getA();
        String notificationMessage = polarDiagramAndNotificationMessage.getB();

        Position startPosition = toPosition(requestData.firstPoint);
        Position endPosition = toPosition(requestData.secondPoint);
        RectangularBoundary rectangularBoundry = new RectangularBoundary(startPosition, endPosition, 0.1);

        SimulationParameters simulationParameters = new SimulationParametersImpl(null, polarDiagram, null, SailingSimulatorUtil.measured);
        SailingSimulator sailingSimulator = new SailingSimulatorImpl(simulationParameters);
        Path gpsWind = sailingSimulator.getLegGPSTrack(requestData.selectedRaceIndex, requestData.selectedCompetitorIndex, requestData.selectedLegIndex);

        this.controlParameters.baseWindBearing += rectangularBoundry.getSouth().getDegrees();
        WindFieldGeneratorMeasured windFieldGenerator = new WindFieldGeneratorMeasured(rectangularBoundry, this.controlParameters);
        windFieldGenerator.setGPSWind(gpsWind);

        TimePoint startTime = new MillisecondsTimePoint(requestData.firstPointTimepoint);

        PathGenerator1Turner pathGenerator = new PathGenerator1Turner(null);

        TimedPositionWithSpeed oneTurner = null;
        try {
            oneTurner = pathGenerator.get1Turner(windFieldGenerator, polarDiagram, startPosition, endPosition, startTime,
                    requestData.leftSide,
                    DEFAULT_STEP_MAX, DEFAULT_TIMESTEP);
        } catch (Exception e) {
            oneTurner = pathGenerator.get1Turner(windFieldGenerator, polarDiagram, endPosition, startPosition, startTime, requestData.leftSide,
                    DEFAULT_STEP_MAX, DEFAULT_TIMESTEP);
        }

        Position oneTurnerPosition = oneTurner.getPosition();
        SpeedWithBearing oneTurnerWindSpeed = oneTurner.getSpeed();
        TimePoint oneTurnerTimePoint = oneTurner.getTimePoint();

        return new Response1TurnerDTO(new SimulatorWindDTO(oneTurnerPosition.getLatDeg(), oneTurnerPosition.getLngDeg(), oneTurnerWindSpeed.getKnots(),
                oneTurnerWindSpeed.getBearing().getDegrees(), oneTurnerTimePoint.asMillis()), notificationMessage);
    }

    private void retreiveWindControlParameters(WindPatternDisplay pattern) {

        controlParameters.setDefaults();

        for (WindPatternSetting<?> s : pattern.getSettings()) {
            Field f;
            try {
                f = controlParameters.getClass().getField(s.getName());
                try {

                    LOGGER.info("Setting " + f.getName() + " to " + s.getName() + " value : " + s.getValue());
                    f.set(controlParameters, s.getValue());
                    // f.setDouble(controlParameters, (Double) s.getValue());

                } catch (IllegalArgumentException e) {
                    LOGGER.warning("SimulatorServiceImpl => IllegalArgumentException with message " + e.getMessage());
                } catch (IllegalAccessException e) {
                    LOGGER.warning("SimulatorServiceImpl => IllegalAccessException with message " + e.getMessage());
                }
            } catch (SecurityException e) {
                LOGGER.warning("SimulatorServiceImpl => SecurityException with message " + e.getMessage());
            } catch (NoSuchFieldException e) {
                LOGGER.warning("SimulatorServiceImpl => NoSuchFieldException with message " + e.getMessage());
            }

        }
    }

    private static PositionDTO toPositionDTO(Position position) {
        return new PositionDTO(position.getLatDeg(), position.getLngDeg());
    }

    private static Position toPosition(PositionDTO positionDTO) {
        return new DegreePosition(positionDTO.latDeg, positionDTO.lngDeg);
    }

    private SimulatorWindDTO createSimulatorWindDTO(Wind wind) {

        Position position = wind.getPosition();
        TimePoint timePoint = wind.getTimePoint();

        SimulatorWindDTO result = new SimulatorWindDTO();
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

    private SimulatorWindDTO createSimulatorWindDTO(TimedPositionWithSpeed timedPositionWithSpeed) {

        Position position = timedPositionWithSpeed.getPosition();
        SpeedWithBearing speedWithBearing = timedPositionWithSpeed.getSpeed();
        TimePoint timePoint = timedPositionWithSpeed.getTimePoint();

        SimulatorWindDTO result = new SimulatorWindDTO();
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

    private WindFieldDTO createWindFieldDTO(WindFieldGenerator wf, TimePoint startTime, TimePoint endTime, TimePoint timeStep, boolean isShowLines,
            char seedLines) {

        WindFieldDTO windFieldDTO = new WindFieldDTO();
        List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
        Position[][] positionGrid = wf.getPositionGrid();

        if (positionGrid != null && positionGrid.length > 0) {
            TimePoint t = startTime;
            while (t.compareTo(endTime) <= 0) {
                for (int i = 0; i < positionGrid.length; ++i) {
                    for (int j = 0; j < positionGrid[i].length; ++j) {
                        Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(t, positionGrid[i][j], null));
                        LOGGER.finer(localWind.toString());
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

    private void getWindLinesFromStartLine(WindFieldGenerator wf, WindFieldDTO windFieldDTO, TimePoint startTime, TimePoint endTime, TimePoint timeStep) {

        Position[][] positionGrid = wf.getPositionGrid();
        WindLinesDTO windLinesDTO = windFieldDTO.getWindLinesDTO();
        if (windLinesDTO == null) {
            windLinesDTO = new WindLinesDTO();
            windFieldDTO.setWindLinesDTO(windLinesDTO);
        }
        if (positionGrid != null && positionGrid.length > 0 && positionGrid[0].length > 2) {
            for (int j = 1; j < positionGrid[0].length - 1; ++j) {
                // for (int j = 0; j < positionGrid[0].length; ++j) {
                TimePoint t = startTime;
                Position p0 = positionGrid[0][j];
                Position p1 = positionGrid[1][j];
                Position seed = new DegreePosition(p0.getLatDeg() + 0.5 * (p0.getLatDeg() - p1.getLatDeg()), p0.getLngDeg() + 0.5
                        * (p0.getLngDeg() - p1.getLngDeg()));
                PositionDTO startPosition = new PositionDTO(seed.getLatDeg(), seed.getLngDeg());
                while (t.compareTo(endTime) <= 0) {
                    TimedPosition tp = new TimedPositionImpl(t, seed);
                    Path p = wf.getLine(tp, false /* forward */);
                    if (p != null) {
                        List<PositionDTO> positions = new ArrayList<PositionDTO>();
                        for (TimedPositionWithSpeed pathPoint : p.getPathPoints()) {
                            Position position = pathPoint.getPosition();
                            PositionDTO positionDTO = new PositionDTO(position.getLatDeg(), position.getLngDeg());
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

    private void getWindLinesFromEndLine(WindFieldGenerator wf, WindFieldDTO windFieldDTO, TimePoint startTime, TimePoint endTime, TimePoint timeStep) {

        Position[][] positionGrid = wf.getPositionGrid();
        WindLinesDTO windLinesDTO = windFieldDTO.getWindLinesDTO();
        if (windLinesDTO == null) {
            windLinesDTO = new WindLinesDTO();
            windFieldDTO.setWindLinesDTO(windLinesDTO);
        }
        if (positionGrid != null && positionGrid.length > 1 && positionGrid[0].length > 2) {
            int lastRowIndex = positionGrid.length - 1;
            for (int j = 1; j < positionGrid[lastRowIndex].length - 1; ++j) {

                TimePoint t = startTime;
                Position p0 = positionGrid[lastRowIndex][j];
                Position p1 = positionGrid[lastRowIndex - 1][j];
                Position seed = new DegreePosition(p0.getLatDeg() + 0.5 * (p0.getLatDeg() - p1.getLatDeg()), p0.getLngDeg() + 0.5
                        * (p0.getLngDeg() - p1.getLngDeg()));
                PositionDTO startPosition = new PositionDTO(seed.getLatDeg(), seed.getLngDeg());
                while (t.compareTo(endTime) <= 0) {
                    TimedPosition tp = new TimedPositionImpl(t, seed);
                    Path p = wf.getLine(tp, true /* forward */);
                    if (p != null) {
                        List<PositionDTO> positions = new ArrayList<PositionDTO>();
                        for (TimedPositionWithSpeed pathPoint : p.getPathPoints()) {
                            Position position = pathPoint.getPosition();
                            PositionDTO positionDTO = new PositionDTO(position.getLatDeg(), position.getLngDeg());
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
    private void getWindLines(WindFieldGenerator wf, WindFieldDTO windFieldDTO) {
        Position[] course = wf.getGridAreaGps();
        /**
         * TODO Check this works for the measured case
         */
        if (course != null && course.length > 1) {
            /**
             * Currently create only a single line from the start position at the start time.
             */
            TimedPosition tp = new TimedPositionImpl(wf.getStartTime(), course[0]);
            PositionDTO startPosition = new PositionDTO(course[0].getLatDeg(), course[0].getLngDeg());

            Path p = wf.getLine(tp, false);
            if (p != null) {
                List<PositionDTO> positions = new ArrayList<PositionDTO>();
                for (TimedPositionWithSpeed pathPoint : p.getPathPoints()) {
                    Position position = pathPoint.getPosition();
                    PositionDTO positionDTO = new PositionDTO(position.getLatDeg(), position.getLngDeg());
                    positions.add(positionDTO);
                }
                WindLinesDTO windLinesDTO = new WindLinesDTO();
                windLinesDTO.addWindLine(startPosition, tp.getTimePoint().asMillis(), positions);
                windFieldDTO.setWindLinesDTO(windLinesDTO);
                LOGGER.info("Added : " + windFieldDTO.getWindLinesDTO().getWindLinesMap().size() + " wind lines");
            }
        }
    }

    private SimulatedPathsEvenTimedResultDTO getSimulatedPathsEvenTimed(List<Position> course, WindFieldGenerator wf, char mode, int selectedBoatClassIndex,
            int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) throws ConfigurationException {

        LOGGER.info("Retrieving simulated paths");

        Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(selectedBoatClassIndex);
        PolarDiagram pd = polarDiagramAndNotificationMessage.getA();

        SimulationParameters sp = new SimulationParametersImpl(course, pd, wf, mode);
        SailingSimulator simulator = new SailingSimulatorImpl(sp);
        Map<String, Path> pathsAndNames = simulator.getAllLegPathsEvenTimed(wf.getTimeStep().asMillis(), selectedRaceIndex, selectedCompetitorIndex,
                selectedLegIndex);

        int noOfPaths = pathsAndNames.size();
        if (mode == SailingSimulatorUtil.measured) {
            noOfPaths++; // the last path is the polyline
        }
        PathDTO[] pathDTOs = new PathDTO[noOfPaths];
        int index = noOfPaths - 1;

        if (mode == SailingSimulatorUtil.measured) {
            // Adding the polyline
            pathDTOs[0] = this.getPolylinePathDTO(pathsAndNames.get("6#GPS Poly"), pathsAndNames.get("7#GPS Track"));
        }

        for (Entry<String, Path> entry : pathsAndNames.entrySet()) {
            LOGGER.info("Path " + entry.getKey());

            // NOTE: pathName convention is: sort-digit + "#" + path-name
            pathDTOs[index] = new PathDTO(entry.getKey().split("#")[1]);

            // fill pathDTO with path points where speed is true wind speed
            List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
            for (TimedPositionWithSpeed p : entry.getValue().getPathPoints()) {
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

            Path rc = simulator.getRaceCourse();
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

    // private SimulatedPathsEvenTimedResultDTO getSimulatedPathsEvenTimed(List<Position> course, WindFieldGenerator wf,
    // char mode, int boatClassIndex)
    // throws ConfigurationException {
    //
    // LOGGER.info("Retrieving simulated paths");
    //
    // Pair<PolarDiagram, String> polarDiagramAndNotificationMessage = this.getPolarDiagram(boatClassIndex);
    // PolarDiagram pd = polarDiagramAndNotificationMessage.getA();
    //
    // SimulationParameters sp = new SimulationParametersImpl(course, pd, wf, mode);
    // SailingSimulator simulator = new SailingSimulatorImpl(sp);
    // Map<String, Path> pathsAndNames = simulator.getAllPathsEvenTimed(wf.getTimeStep().asMillis());
    //
    // int noOfPaths = pathsAndNames.size();
    // if (mode == SailingSimulatorUtil.measured) {
    // noOfPaths++; // the last path is the polyline
    // }
    // PathDTO[] pathDTOs = new PathDTO[noOfPaths];
    // int index = noOfPaths - 1;
    //
    // if (mode == SailingSimulatorUtil.measured) {
    // // Adding the polyline
    // pathDTOs[0] = this.getPolylinePathDTO(pathsAndNames.get("6#GPS Poly"), pathsAndNames.get("7#GPS Track"));
    // }
    //
    // for (Entry<String, Path> entry : pathsAndNames.entrySet()) {
    // LOGGER.info("Path " + entry.getKey());
    //
    // // NOTE: pathName convention is: sort-digit + "#" + path-name
    // pathDTOs[index] = new PathDTO(entry.getKey().split("#")[1]);
    //
    // // fill pathDTO with path points where speed is true wind speed
    // List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
    // for (TimedPositionWithSpeed p : entry.getValue().getPathPoints()) {
    // wList.add(createSimulatorWindDTO(p));
    // }
    //
    // pathDTOs[index].setPoints(wList);
    //
    // index--;
    // }
    //
    //
    // RaceMapDataDTO rcDTO;
    // if (mode == SailingSimulatorUtil.measured) {
    // rcDTO = new RaceMapDataDTO();
    // rcDTO.coursePositions = new CourseDTO();
    // rcDTO.coursePositions.waypointPositions = new ArrayList<PositionDTO>();
    //
    // Path rc = simulator.getRaceCourse();
    // PositionDTO posDTO;
    // posDTO = toPositionDTO(rc.getPathPoints().get(0).getPosition());
    //
    // rcDTO.coursePositions.waypointPositions.add(posDTO);
    // posDTO = toPositionDTO(rc.getPathPoints().get(1).getPosition());
    // rcDTO.coursePositions.waypointPositions.add(posDTO);
    // } else {
    // rcDTO = null;
    // }
    //
    // return new SimulatedPathsEvenTimedResultDTO(pathDTOs, rcDTO, null, polarDiagramAndNotificationMessage.getB());
    // }

    private Pair<PolarDiagram, String> getPolarDiagram(int boatClassIndex) throws ConfigurationException {

        ConfigurationManager config = ConfigurationManager.INSTANCE;
        PolarDiagram polarDiagram = null;
        String notificationMessage = "";

        if (config.getStatus() == ReadingConfigurationFileStatus.IO_ERROR) {
            throw new ConfigurationException(config.getErrorMessage());
        } else if (config.getStatus() == ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE
                || config.getStatus() == ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE) {
            notificationMessage = config.getErrorMessage();
        }

        String csvFilePath = config.getPolarDiagramFileLocation(boatClassIndex);

        try {
            polarDiagram = new PolarDiagramCSV(csvFilePath);
        } catch (IOException exception) {
            throw new ConfigurationException(
                    "An IO error occured when parsing the CSV file! The original error message is "
                            + exception.getMessage());
        }

        return new Pair<PolarDiagram, String>(polarDiagram, notificationMessage);
    }

    private PathDTO getPolylinePathDTO(Path gpsPoly, Path gpsTrack) {

        List<TimedPositionWithSpeed> gpsTrackPoints = gpsTrack.getPathPoints();
        List<TimedPositionWithSpeed> gpsPolyPoints = gpsPoly.getPathPoints();

        int noOfGpsTrackPoints = gpsTrackPoints.size();
        int noOfGpsPolyPoints = gpsPolyPoints.size();

        if (noOfGpsTrackPoints == 0 || noOfGpsTrackPoints == 1 || noOfGpsPolyPoints == 0 || noOfGpsPolyPoints == 1) {
            return null;
        }

        TimedPositionWithSpeed startPoint = gpsPolyPoints.get(0);
        TimedPositionWithSpeed endPoint = gpsPolyPoints.get(noOfGpsPolyPoints - 1);

        // System.out.println("gpsTrackPoints.size() = " + gpsTrackPoints.size());
        int startPointIndex = getIndexOfClosest(gpsTrackPoints, startPoint);
        // System.out.println("startPointIndex = " + startPointIndex);
        int endPointIndex = getIndexOfClosest(gpsTrackPoints, endPoint);
        // System.out.println("endPointIndex = " + endPointIndex);

        List<TimedPositionWithSpeed> polylinePoints = gpsTrackPoints.subList(startPointIndex, endPointIndex + 1);

        List<TimedPositionWithSpeed> turns = (new PathImpl(polylinePoints, null)).getTurns();
        // PathImpl.saveToGpxFile(new PathImpl(turns, null), "C:\\gps_path_turns_20deg_not_even_timed.gpx");

        List<SimulatorWindDTO> points = new ArrayList<SimulatorWindDTO>();

        boolean isTurn = false;
        SpeedWithBearing speedWithBearing = null;
        Position position = null;

        for (TimedPositionWithSpeed point : polylinePoints) {

            isTurn = false;

            for (TimedPositionWithSpeed turn : turns) {
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

        PathDTO result = new PathDTO(POLYLINE_PATH_NAME);
        result.setPoints(points);

        return result;
    }

    private static int getIndexOfClosest(List<TimedPositionWithSpeed> items, TimedPositionWithSpeed item) {
        int count = items.size();

        List<Double> diff_lat = new ArrayList<Double>();
        List<Double> diff_lng = new ArrayList<Double>();
        List<Long> diff_timepoint = new ArrayList<Long>();

        for (int index = 0; index < count; index++) {
            diff_lat.add(Math.abs(items.get(index).getPosition().getLatDeg() - item.getPosition().getLatDeg()));
            diff_lng.add(Math.abs(items.get(index).getPosition().getLngDeg() - item.getPosition().getLngDeg()));
            diff_timepoint.add(Math.abs(items.get(index).getTimePoint().asMillis() - item.getTimePoint().asMillis()));
        }

        double min_diff_lat = Collections.min(diff_lat);
        double min_max_diff_lat = min_diff_lat + Collections.max(diff_lat);

        double min_diff_lng = Collections.min(diff_lng);
        double min_max_diff_lng = min_diff_lng + Collections.max(diff_lng);

        long min_diff_timepoint = Collections.min(diff_timepoint);
        double min_max_diff_timepoint = min_diff_timepoint + Collections.max(diff_timepoint);

        List<Double> norm_diff_lat = new ArrayList<Double>();
        List<Double> norm_diff_lng = new ArrayList<Double>();
        List<Double> norm_diff_timepoint = new ArrayList<Double>();

        for (int index = 0; index < count; index++) {
            norm_diff_lat.add((diff_lat.get(index) - min_diff_lat) / min_max_diff_lat);
            norm_diff_lng.add((diff_lng.get(index) - min_diff_lng) / min_max_diff_lng);
            norm_diff_timepoint.add((diff_timepoint.get(index) - min_diff_timepoint) / min_max_diff_timepoint);
        }

        List<Double> deltas = new ArrayList<Double>();

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

    private static SpeedWithBearing getWindAtTimepoint(long timepointAsMillis, Path gpsTrack) {
        List<TimedPositionWithSpeed> pathPoints = gpsTrack.getPathPoints();
        int noOfPathPoints = pathPoints.size();
        List<Double> diffs = new ArrayList<Double>();

        for (int index = 0; index < noOfPathPoints; index++) {
            diffs.add(new Double(Math.abs(pathPoints.get(index).getTimePoint().asMillis() - timepointAsMillis)));
        }

        int indexOfMinDiff = diffs.indexOf(Collections.min(diffs));

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

    public static boolean equals(Position position, PositionDTO positonDTO) {
        return (position.getLatDeg() == positonDTO.latDeg && position.getLngDeg() == positonDTO.lngDeg);
    }

    @Override
    public List<String> getLegsNames(int selectedRaceIndex) {

        if (selectedRaceIndex < 0) {
            selectedRaceIndex = 0;
        }

        SailingSimulator simulator = new SailingSimulatorImpl(new SimulationParametersImpl(null, null, null, SailingSimulatorUtil.measured));

        return simulator.getLegsNames(selectedRaceIndex);
    }

    @Override
    public List<String> getRacesNames() {

        SailingSimulator simulator = new SailingSimulatorImpl(new SimulationParametersImpl(null, null, null, SailingSimulatorUtil.measured));

        return simulator.getRacesNames();
    }

    @Override
    public List<String> getCompetitorsNames(int selectedRaceIndex) {

        if (selectedRaceIndex < 0) {
            selectedRaceIndex = 0;
        }

        SailingSimulator simulator = new SailingSimulatorImpl(new SimulationParametersImpl(null, null, null, SailingSimulatorUtil.measured));

        return simulator.getComeptitorsNames(selectedRaceIndex);
    }
}
