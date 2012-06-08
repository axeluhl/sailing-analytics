package com.sap.sailing.gwt.ui.server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
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
import com.sap.sailing.simulator.WindField;
import com.sap.sailing.simulator.WindFieldGenerator;
import com.sap.sailing.simulator.WindFieldGeneratorFactory;
import com.sap.sailing.simulator.impl.PolarDiagramImpl;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedImpl;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedSimple;

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

        return new PositionDTO[] { kiel, lakeGeneva, lakeGarda };
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
    public WindFieldDTO getWindField(WindFieldGenParamsDTO params, WindPatternDisplay pattern)
            throws WindPatternNotFoundException {
        logger.info("Entering getWindField");
        Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);
        // List<Position> lattice = bd.extractLattice(params.getxRes(), params.getyRes());

        retreiveWindControlParameters(pattern);
        logger.info("Boundary south direction " + bd.getSouth());
        controlParameters.baseWindBearing += bd.getSouth().getDegrees();

        WindFieldGenerator wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), bd,
                controlParameters);

        if (wf == null) {
            throw new WindPatternNotFoundException("Please select a valid wind pattern.");
        }

        List<Position> lattice = bd.extractLattice(params.getxRes(), params.getyRes());// wf.extractLattice(params.getxRes(),
                                                                                       // params.getyRes());
        wf.setPositionGrid(bd.extractGrid(params.getxRes(), params.getyRes()));

        TimePoint startTime = new MillisecondsTimePoint(params.getStartTime().getTime());// new
                                                                                         // MillisecondsTimePoint(0);
        TimePoint timeStep = new MillisecondsTimePoint(params.getTimeStep().getTime());// new
                                                                                       // MillisecondsTimePoint(30*1000);
        TimePoint endTime = new MillisecondsTimePoint(params.getEndTime().getTime());// new MillisecondsTimePoint(10 *
                                                                                     // 60 * 1000);

        wf.generate(startTime, null, timeStep);
        List<WindDTO> wList = new ArrayList<WindDTO>();

        if (lattice != null) {
            TimePoint t = startTime;
            while (t.compareTo(endTime) <= 0) {
                for (Position p : lattice) {
                    Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(t, p, null));
                    logger.finer(localWind.toString());
                    WindDTO w = createWindDTO(localWind);
                    wList.add(w);
                }
                t = new MillisecondsTimePoint(t.asMillis() + timeStep.asMillis());
            }
        }

        WindFieldDTO wfDTO = new WindFieldDTO();

        wfDTO.setMatrix(wList);
        logger.info("Exiting getWindField");
        return wfDTO;

    }

    /**
     * Currently the path is a list of WindDTO objects at the path points and only a single optimal path is returned
     * 
     * */
    public PathDTO[] getPaths(WindFieldGenParamsDTO params, WindPatternDisplay pattern)
            throws WindPatternNotFoundException {

        Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);

        retreiveWindControlParameters(pattern);
        controlParameters.baseWindBearing += bd.getSouth().getDegrees();

        WindFieldGenerator wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), bd,
                controlParameters);

        if (wf == null) {
            throw new WindPatternNotFoundException("Please select a valid wind pattern.");
        }
        // List<Position> lattice = bd.extractLattice(params.getxRes(), params.getyRes());//
        // wf.extractLattice(params.getxRes(),
        // params.getyRes());
        wf.setPositionGrid(bd.extractGrid(params.getxRes(), params.getyRes()));
        TimePoint start = new MillisecondsTimePoint(0);
        TimePoint timeStep = new MillisecondsTimePoint(30 * 1000);
        wf.generate(start, null, timeStep);

        // TODO Get all the paths that need to be displayed
        List<WindDTO> path = getOppurtunisticPath(course, wf);
        PathDTO[] pathDTO = new PathDTO[2];
        pathDTO[0] = new PathDTO("Path 1");
        pathDTO[0].setMatrix(path);
        pathDTO[1] = new PathDTO("Path 2");
        List<WindDTO> path1 = getOptimumPath(course, wf);
        pathDTO[1].setMatrix(path1);
        return pathDTO;
    }

    private List<WindDTO> getOptimumPath(List<Position> course, WindField wf) {

        PolarDiagram pd = new PolarDiagramImpl(1);
        SimulationParameters sp = new SimulationParametersImpl(course, pd, wf);
        SailingSimulator solver = new SailingSimulatorImpl(sp);

        Path pth = solver.getOptimumPath();
        int i = 0;
        List<WindDTO> wList = new ArrayList<WindDTO>();
        for (TimedPositionWithSpeed p : pth.getPathPoints()) {
            // the null in the Wind output is the timestamp - this Wind is time-invariant!
            // System.out.println("Position: " + p.getPosition() + " Wind: "
            // + wf.getWind(new TimedPositionWithSpeedSimple(p.getPosition())));
            // System.out.println("Position: " + p.getPosition() + " Wind: " +
            // wf.getWind(pth.getPositionAtTime(p.getTimePoint())));
            Wind localWind = wf.getWind(pth.getPositionAtTime(p.getTimePoint()));
            logger.finer(localWind.toString());
            WindDTO w = createWindDTO(localWind);
            // w.trueWindBearingDeg = 10.0*i;
            ++i;
            wList.add(w);
        }
        return wList;

    }

    private List<WindDTO> getOppurtunisticPath(List<Position> course, WindField wf) {

        PolarDiagram pd = new PolarDiagramImpl(1);
        SimulationParameters sp = new SimulationParametersImpl(course, pd, wf);
        SailingSimulator solver = new SailingSimulatorImpl(sp);

        Path pth = solver.getOpputunisticPath();
        int i = 0;
        List<WindDTO> wList = new ArrayList<WindDTO>();
        for (TimedPositionWithSpeed p : pth.getPathPoints()) {

            Wind localWind = wf.getWind(pth.getPositionAtTime(p.getTimePoint()));
            logger.finer(localWind.toString());
            WindDTO w = createWindDTO(localWind);
            // w.trueWindBearingDeg = 10.0*i;
            ++i;
            wList.add(w);
        }
        return wList;

    }

    public List<WindPatternDTO> getWindPatterns() {
        return wpDisplayManager.getWindPatterns();
    }

    public BoatClassDTO[] getBoatClasses() {
        BoatClassDTO boatClassDTO = new BoatClassDTO("49er");

        return new BoatClassDTO[] { boatClassDTO };

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

    @Override
    public WindPatternDisplay getWindPatternDisplay(WindPatternDTO pattern) {
        return wpDisplayManager.getDisplay(WindPattern.valueOf(pattern.name));
    }

    @Override
    public SimulatorResultsDTO getSimulatorResults(WindFieldGenParamsDTO params, WindPatternDisplay pattern,
            boolean withWindField) throws WindPatternNotFoundException {
        Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);

        retreiveWindControlParameters(pattern);
        controlParameters.baseWindBearing += bd.getSouth().getDegrees();

        WindFieldGenerator wf = wfGenFactory.createWindFieldGenerator(pattern.getWindPattern().name(), bd,
                controlParameters);

        if (wf == null) {
            throw new WindPatternNotFoundException("Please select a valid wind pattern.");
        }
        Position[][] positionGrid = bd.extractGrid(params.getxRes(), params.getyRes());
        wf.setPositionGrid(positionGrid);

        TimePoint startTime = new MillisecondsTimePoint(0);
        TimePoint timeStep = new MillisecondsTimePoint(30 * 1000);
        wf.generate(startTime, null, timeStep);

        // TODO Get all the paths that need to be displayed
        List<WindDTO> path = getOppurtunisticPath(course, wf);
        Long pathTime = path.get(path.size() - 1).timepoint - path.get(0).timepoint;
        PathDTO[] pathDTO = new PathDTO[2];
        pathDTO[0] = new PathDTO("Path 1");
        pathDTO[0].setMatrix(path);
        pathDTO[1] = new PathDTO("Path 2");
        List<WindDTO> path1 = getOptimumPath(course, wf);
        Long pathTime1 = path1.get(path1.size() - 1).timepoint - path1.get(0).timepoint;
        pathDTO[1].setMatrix(path1);

        TimePoint endTime = new MillisecondsTimePoint(startTime.asMillis() + Math.max(pathTime, pathTime1));
        WindFieldDTO windFieldDTO = createWindFieldDTO(wf, startTime, endTime, timeStep);
        SimulatorResultsDTO simulatorResults = new SimulatorResultsDTO(pathDTO, windFieldDTO);

        return simulatorResults;

    }

    private WindFieldDTO createWindFieldDTO(WindFieldGenerator wf, TimePoint startTime, TimePoint endTime,
            TimePoint timeStep) {

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
}
