package com.sap.sailing.simulator.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
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
import com.sap.sailing.simulator.WindFieldGenerator;
import com.sap.sailing.simulator.impl.PolarDiagram49;
import com.sap.sailing.simulator.impl.PolarDiagramImpl;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedSimple;
import com.sap.sailing.simulator.impl.WindFieldGeneratorBlastImpl;
import com.sap.sailing.simulator.impl.WindFieldGeneratorOscillationImpl;
import com.sap.sailing.simulator.impl.WindFieldImpl;

public class SimulatorTest {
    private static Logger logger = Logger.getLogger("com.sap.sailing");
    
    @Test
    public void testSailingSImulatorALL() {
    	Position start = new DegreePosition(48.401856, -140.001526);
        Position end = new DegreePosition(49.143987, -139.987783);
        //System.out.println(start.getDistance(end).getKilometers());

        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);
        PolarDiagram pd = PolarDiagram49.CreateStandard49();
        RectangularBoundary bd = new RectangularBoundary(start, end, 0.1);
        RectangularBoundary new_bd = new RectangularBoundary(start, end, 0.1);
        Speed knotSpeed = new KnotSpeedImpl(8);
        WindControlParameters windParameters = new WindControlParameters(10, 180);
        WindField wf = new WindFieldImpl(bd, windParameters);
        SimulationParameters param = new SimulationParametersImpl(course, pd, wf);
        param.setProperty("Heuristic.targetTolerance[double]", 0.05);
        param.setProperty("Heuristic.timeResolution[long]", 30000.0);
        param.setProperty("Djikstra.gridv[int]", 10.0);
        param.setProperty("Djikstra.gridh[int]", 100.0);


        SailingSimulatorImpl sailingSim = new SailingSimulatorImpl(param);
        
        Map <String, Path> paths = sailingSim.getAllPaths();
        System.out.println(paths.get("Djikstra").getPathPoints().size());
        System.out.println(paths.get("Heuristic").getPathPoints().size());
        
        	
    }
    
}
