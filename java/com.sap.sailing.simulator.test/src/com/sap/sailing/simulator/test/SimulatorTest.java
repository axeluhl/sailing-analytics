package com.sap.sailing.simulator.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindField;
import com.sap.sailing.simulator.impl.PolarDiagramImpl;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedSimple;
import com.sap.sailing.simulator.impl.WindFieldImpl;

public class SimulatorTest {
    private static Logger logger = Logger.getLogger("com.sap.sailing");

    @Test
    public void testSailingSimulator() {

        Position start = new DegreePosition(48.401856, -140.001526);
        Position end = new DegreePosition(49.143987, -139.987783);

        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);
        PolarDiagram pd = new PolarDiagramImpl(0);
        RectangularBoundary bd = new RectangularBoundary(start, end, 0.1);
        KilometersPerHourSpeedImpl kmhrSpeed = new KilometersPerHourSpeedImpl(7.2);
        WindControlParameters windParameters = new WindControlParameters(kmhrSpeed.getKnots(), 45);
        WindField wf = new WindFieldImpl(bd, windParameters);
        SimulationParameters param = new SimulationParametersImpl(course, pd, wf);

        SailingSimulatorImpl sailingSim = new SailingSimulatorImpl(param);
        Path path = sailingSim.getOptimumPath();
        logger.info("Path with " + path.getPathPoints().size() + "points");
        assertEquals("Number of path points", 2612, path.getPathPoints().size());
        /*
         * for(TimedPositionWithSpeed p : path.getPathPoints()) { logger.info("Position: " + p.getPosition() + " Wind: "
         * + wf.getWind(new TimedPositionWithSpeedSimple(p.getPosition()))); }
         */

    }

    @Test
    public void testSailingSimulator2() {
        Position p1 = new DegreePosition(25.661333, -90.752563);
        Position p2 = new DegreePosition(24.522137, -90.774536);

        Boundary b = new RectangularBoundary(p1, p2, 0.1);

        Distance dist = p1.getDistance(p2);
        // the Speed required to go from p1 to p2 in 10 minutes
        Speed requiredSpeed10 = dist.inTime(600000);

        // I am creating the WindField such as the course goes mainly against the wind (as it should)
        // and the speed of the wind would go over the course in 10 minutes (for the sake of the running time)
        WindControlParameters windParameters = new WindControlParameters(requiredSpeed10.getKnots(), b.getSouth().getDegrees());
        WindField wf = new WindFieldImpl(b, windParameters);
        PolarDiagram pd = new PolarDiagramImpl(1);
        List<Position> course = new ArrayList<Position>();
        course.add(p1);
        course.add(p2);
        SimulationParameters sp = new SimulationParametersImpl(course, pd, wf);
        SailingSimulator solver = new SailingSimulatorImpl(sp);

        Path pth = solver.getOptimumPath();
        logger.info("Path with " + pth.getPathPoints().size() + "points");
        for (TimedPositionWithSpeed p : pth.getPathPoints()) {
            // the null in the Wind output is the timestamp - this Wind is time-invariant!
            System.out.println("Position: " + p.getPosition() + " Wind: "
                    + wf.getWind(new TimedPositionWithSpeedSimple(p.getPosition())));
            System.out.println("Position: " + p.getPosition() + " Wind: "
                    + wf.getWind(pth.getPositionAtTime(p.getTimePoint())));
            // Wind wind = wf.getWind(pth.getPositionAtTime(p.getTimePoint()));
        }

        assertEquals("Number of path points", 37, pth.getPathPoints().size());
    }
    
}
