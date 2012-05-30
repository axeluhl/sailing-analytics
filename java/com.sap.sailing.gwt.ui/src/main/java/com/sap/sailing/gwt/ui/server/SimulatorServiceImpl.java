package com.sap.sailing.gwt.ui.server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager.WindPattern;
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
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedSimple;

public class SimulatorServiceImpl extends RemoteServiceServlet implements SimulatorService {

    /**
     * Generated uid serial version
     */
    private static final long serialVersionUID = 4445427185387524086L;

    private static Logger logger = Logger.getLogger("com.sap.sailing");

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
        for (WindPatternSetting<?> s : pattern.getSettings()) {
            Field f;
            try {
                f = controlParameters.getClass().getField(s.getName());
                try {

                    logger.info("Setting " + f.getName() + " to " + s.getName());
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
    public WindFieldDTO getWindField(WindFieldGenParamsDTO params, WindPatternDisplay pattern) {

        Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);
        // List<Position> lattice = bd.extractLattice(params.getxRes(), params.getyRes());

        retreiveWindControlParameters(pattern);

        controlParameters.baseWindBearing = bd.getSouth().getDegrees();
        WindFieldGenerator wf = WindFieldGeneratorFactory.INSTANCE.createWindFieldGenerator(pattern.getWindPattern()
                .name(), bd, controlParameters);

        List<Position> lattice = wf.extractLattice(params.getxRes(), params.getyRes());

        List<WindDTO> wList = new ArrayList<WindDTO>();

        if (lattice != null) {
            for (Position p : lattice) {
                Wind localWind = wf.getWind(new TimedPositionWithSpeedSimple(p));
                logger.fine(localWind.toString());
                WindDTO w = createWindDTO(localWind);
                wList.add(w);
            }
        }

        WindFieldDTO wfDTO = new WindFieldDTO();

        wfDTO.setMatrix(wList);
        return wfDTO;

    }

    /**
     * Currently the path is a list of WindDTO objects at the path points and only a single optimal path is returned
     * 
     * */
    public PathDTO[] getPaths(WindFieldGenParamsDTO params, WindPatternDisplay pattern) {

        Position nw = new DegreePosition(params.getNorthWest().latDeg, params.getNorthWest().lngDeg);
        Position se = new DegreePosition(params.getSouthEast().latDeg, params.getSouthEast().lngDeg);
        List<Position> course = new ArrayList<Position>();
        course.add(nw);
        course.add(se);

        RectangularBoundary bd = new RectangularBoundary(nw, se, 0.1);
        retreiveWindControlParameters(pattern);

        controlParameters.baseWindBearing = bd.getSouth().getDegrees();
        WindFieldGenerator wf = WindFieldGeneratorFactory.INSTANCE.createWindFieldGenerator(pattern.getWindPattern()
                .name(), bd, controlParameters);
        
        // TODO Get all the paths that need to be displayed
        List<WindDTO> path = getOptimumPath(course, wf);
        PathDTO[] pathDTO = new PathDTO[1];
        pathDTO[0] = new PathDTO("Path 1");
        pathDTO[0].setMatrix(path);
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
            logger.fine(localWind.toString());
            WindDTO w = createWindDTO(localWind);
            // w.trueWindBearingDeg = 10.0*i;
            ++i;
            wList.add(w);
        }
        return wList;

    }

    public List<WindPatternDTO> getWindPatterns() {
        return WindPatternDisplayManager.INSTANCE.getWindPatterns();
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
        return WindPatternDisplayManager.INSTANCE.getDisplay(WindPattern.valueOf(pattern.name));
    }

}
