package com.sap.sailing.simulator.test;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.impl.PolarDiagram49STG;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorBlastImpl;

public class SimulatorTest {

    @Test
    public void testSailingSimulatorALL() {
        Position start = new DegreePosition(48.401856, -140.001526);
        Position end = new DegreePosition(49.143987, -139.987783);
        //System.out.println(start.getDistance(end).getKilometers());

        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);
        PolarDiagram pd = new PolarDiagram49STG();//PolarDiagram49.CreateStandard49();
        RectangularBoundary bd = new RectangularBoundary(start, end, 0.1);
        Position[][] positions = bd.extractGrid(10, 10);
        //RectangularBoundary new_bd = new RectangularBoundary(start, end, 0.1);
        //Speed knotSpeed = new KnotSpeedImpl(8);
        WindControlParameters windParameters = new WindControlParameters(10, 180);
        WindFieldGenerator wf = new WindFieldGeneratorBlastImpl(bd, windParameters);
        wf.setPositionGrid(positions);
        Date startDate = new Date(0);
        TimePoint startTime = new MillisecondsTimePoint(startDate.getTime());
        TimePoint timeStep = new MillisecondsTimePoint(30000);
        wf.generate(startTime, null, timeStep);
        SimulationParameters param = new SimulationParametersImpl(course, pd, wf, SailingSimulatorUtil.freestyle);
        param.setProperty("Heuristic.targetTolerance[double]", 0.05);
        param.setProperty("Heuristic.timeResolution[long]", 30000.0);
        param.setProperty("Djikstra.gridv[int]", 10.0);
        param.setProperty("Djikstra.gridh[int]", 100.0);


        SailingSimulatorImpl sailingSim = new SailingSimulatorImpl(param);

        Map<String, Path> paths = sailingSim.getAllPathsForLeg(0, 0, 0);
        System.out.println(paths.get("2#Opportunistic").getPathPoints().size());
        System.out.println(paths.get("1#Omniscient").getPathPoints().size());


    }

}
