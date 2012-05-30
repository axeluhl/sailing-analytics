/**
 * 
 */
package com.sap.sailing.simulator.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedImpl;
import com.sap.sailing.simulator.impl.WindFieldGeneratorBlastImpl;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.WindFieldGeneratorImpl;
import com.sap.sailing.simulator.impl.WindFieldGeneratorOscillationImpl;

/**
 * Test for @WindFieldGenerator
 * @author Nidhi Sawhney(D054070)
 *
 */
public class WindFieldGeneratorTest {
    
    private static Logger logger = Logger.getLogger("com.sap.sailing");
    
    @Test
    public void testWindFieldGeneratorBasic() {
        Position start = new DegreePosition(54.32447456461419, 10.15613079071045);
        Position end = new DegreePosition(54.32877915239163, 10.156173706054688);
        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);
       
        WindControlParameters windParameters = new WindControlParameters(3, 180);
        RectangularBoundary bd = new RectangularBoundary(start, end, 0.1);
        WindFieldGeneratorImpl wf = new WindFieldGeneratorBlastImpl(bd, windParameters);
        int hSteps = 10;
        int vSteps = 5;
        List<Position> positionList = bd.extractLattice(hSteps,vSteps);
        assert(positionList.size() == hSteps*vSteps);
        int index = 0;
        for(Position p : positionList) {
            logger.info("P" + ++index + ":" + p);
        }
        wf.setPositionGrid(bd.extractGrid(hSteps, vSteps));
        Position[][] positionGrid = wf.getPositionsGrid();
        assertNotNull("Position Grid is not null", positionGrid);
        assertEquals("Position Grid Number of Rows", vSteps, positionGrid.length);
        assertEquals("Position Grid Number of Columns", hSteps, positionGrid[0].length);
        for (int i = 0; i < vSteps; ++i) {
            for (int j = 0; j < hSteps; ++j) {
                logger.info("P["+i+"]["+j+"]:" + positionGrid[i][j]);
                assertEquals("Map index check",positionGrid[i][j],wf.getPosition(i, j));
            }
        }
    }
    
    @Test
    public void testWindFieldGeneratorOscillation() {
        Position start = new DegreePosition(54.32447456461419, 10.15613079071045);
        Position end = new DegreePosition(54.32877915239163, 10.156173706054688);
        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);
       
        WindControlParameters windParameters = new WindControlParameters(10, 180);
        windParameters.leftWindSpeed = 70.0;
        windParameters.middleWindSpeed = 80.0;
        windParameters.rightWindSpeed = 90.0;
        RectangularBoundary bd = new RectangularBoundary(start, end, 0.1);
        WindFieldGeneratorOscillationImpl wf = new WindFieldGeneratorOscillationImpl(bd, windParameters);
        int hSteps = 30;
        int vSteps = 15;
      
        wf.setPositionGrid(bd.extractGrid(hSteps, vSteps));
        Position[][] positionGrid = wf.getPositionsGrid();
        TimePoint startTime = new MillisecondsTimePoint(0);
        TimePoint timeStep = new MillisecondsTimePoint(30*1000);
        wf.generate(startTime,null,timeStep);
        
        SpeedWithBearing speed = new KilometersPerHourSpeedWithBearingImpl(0, new DegreeBearingImpl(0));
        
        for (int i = 0; i < vSteps; ++i) { 
            for (int j = 0; j < hSteps; ++j) {
                Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(startTime,positionGrid[i][j],speed));
                logger.info("Wind["+i+"]["+j+"]" + localWind.toString());
            }
        }
    }
}
