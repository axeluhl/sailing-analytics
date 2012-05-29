/**
 * 
 */
package com.sap.sailing.simulator.test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.impl.RectangularBoundary;
import com.sap.sailing.simulator.impl.WindFieldGenerator;

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
        RectangularBoundary bd = new RectangularBoundary(start, end);
        WindFieldGenerator wf = new WindFieldGenerator(bd, windParameters);
        int hSteps = 10;
        int vSteps = 5;
        List<Position> positionList = wf.extractLattice(hSteps,vSteps);
        assert(positionList.size() == hSteps*vSteps);
        int index = 0;
        for(Position p : positionList) {
            logger.info("P" + ++index + ":" + p);
        }
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
}
