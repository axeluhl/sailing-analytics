package com.sap.sailing.simulator.test;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.impl.PathGeneratorTreeGrow;
import com.sap.sailing.simulator.impl.PolarDiagram49STG;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorOscillationImpl;

public class TreeGrowTest {
    
    @Test
    public void testSailingSimulatorALL() {
        
        Position start = new DegreePosition(54.001917,10.82222);
        //Position end = new DegreePosition(54.023806,10.822048);
        SpeedWithBearing bearNorth =  new KnotSpeedWithBearingImpl(6.0, new DegreeBearingImpl(33.0));
        Position end = bearNorth.travelTo(start, new MillisecondsTimePoint(0), new MillisecondsTimePoint(10*60*1000));
        //System.out.println(start.getDistance(end).getKilometers());

        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);
        PolarDiagram pd = new PolarDiagram49STG();//PolarDiagram49.CreateStandard49();
        RectangularBoundary bd = new RectangularBoundary(start, end, 0.1);
        Position[][] positions = bd.extractGrid(10, 10);
        //RectangularBoundary new_bd = new RectangularBoundary(start, end, 0.1);
        //Speed knotSpeed = new KnotSpeedImpl(8);
        WindControlParameters windParameters = new WindControlParameters(12, start.getBearingGreatCircle(end).reverse().getDegrees());
        WindFieldGenerator wf = new WindFieldGeneratorOscillationImpl(bd, windParameters);
        wf.setPositionGrid(positions);
        Date startDate = new Date(0);
        TimePoint startTime = new MillisecondsTimePoint(startDate.getTime());
        TimePoint timeStep = new MillisecondsTimePoint(20000);
        wf.generate(startTime, null, timeStep);
        SimulationParameters param = new SimulationParametersImpl(course, pd, wf, SailingSimulatorUtil.freestyle);        
        
        /*param.setProperty("Heuristic.targetTolerance[double]", 0.05);
        param.setProperty("Heuristic.timeResolution[long]", 30000.0);
        param.setProperty("Djikstra.gridv[int]", 10.0);
        param.setProperty("Djikstra.gridh[int]", 100.0);*/

        PathGeneratorTreeGrow treeGrow = new PathGeneratorTreeGrow(param);        
        Path path = treeGrow.getPath();
        	
        for(TimedPositionWithSpeed pos : path.getPathPoints()) {
            
            System.out.println(""+pos.getPosition().getLatDeg()+", "+pos.getPosition().getLngDeg());
            
        }
    }
    
}
