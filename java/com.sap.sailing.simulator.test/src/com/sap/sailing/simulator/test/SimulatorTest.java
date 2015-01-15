package com.sap.sailing.simulator.test;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.impl.PolarDiagram49STG;
import com.sap.sailing.simulator.impl.RectangularGrid;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorBlastImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SimulatorTest {

    @Test
    public void testSailingSimulatorALL() {

        // race course: copacabana, rio de janeiro, brasil
        Position start = new DegreePosition(-22.975779, -43.17421);
        Position end = new DegreePosition(-22.99016, -43.156013);
        // System.out.println("race course size: "+start.getDistance(end).getKilometers());

        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);
        PolarDiagram pd = new PolarDiagram49STG();// PolarDiagram49.CreateStandard49();

        RectangularGrid bd = new RectangularGrid(start, end);
        Position[][] positions = bd.generatePositions(10, 10, 0, 0);
        Bearing windBear = end.getBearingGreatCircle(start);
        WindControlParameters windParameters = new WindControlParameters(12.0, windBear.getDegrees());
        WindFieldGenerator wf = new WindFieldGeneratorBlastImpl(bd, windParameters);
        wf.setPositionGrid(positions);
        Date startDate = new Date(0);
        TimePoint startTime = new MillisecondsTimePoint(startDate.getTime());
        Duration timeStep = new MillisecondsDurationImpl(30000);
        wf.generate(startTime, null, timeStep);

        SimulationParameters param = new SimulationParametersImpl(course, pd, wf, null,
                SailingSimulatorConstants.ModeFreestyle, true, true);
        SailingSimulatorImpl sailingSim = new SailingSimulatorImpl(param);

        // Map<String, Path> paths = sailingSim.getAllPathsForLeg(new SimulatorUISelectionImpl(0, 0, 0, 0));
        Map<String, Path> paths = sailingSim.getAllPaths();

        // System.out.println("opportunistic path points: "+paths.get("2#Opportunist Left").getPathPoints().size());
        Assert.assertNotNull(paths.get("2#Opportunist Left").getPathPoints());
        // System.out.println("omnciscient path points: "+paths.get("1#Omniscient").getPathPoints().size());
        Assert.assertNotNull(paths.get("1#Omniscient").getPathPoints());
    }

}
