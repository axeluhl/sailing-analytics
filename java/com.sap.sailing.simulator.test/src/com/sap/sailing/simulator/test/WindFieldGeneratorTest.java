/**
 * 
 */
package com.sap.sailing.simulator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.impl.RectangularGrid;
import com.sap.sailing.simulator.impl.TimedPositionWithSpeedImpl;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorBlastImpl;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorCombined;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorImpl;
import com.sap.sailing.simulator.windfield.impl.WindFieldGeneratorOscillationImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Test for @WindFieldGenerator
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindFieldGeneratorTest {

    private static Logger logger = Logger.getLogger(WindFieldGeneratorTest.class.getName());

    @Test
    public void testWindFieldGeneratorBasic() {
        Position start = new DegreePosition(54.32447456461419, 10.15613079071045);
        Position end = new DegreePosition(54.32877915239163, 10.156173706054688);
        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);

        WindControlParameters windParameters = new WindControlParameters(3, 180);
        RectangularGrid bd = new RectangularGrid(start, end);
        WindFieldGeneratorImpl wf = new WindFieldGeneratorBlastImpl(bd, windParameters);
        int hSteps = 10;
        int vSteps = 5;
        Position[][] positions = bd.generatePositions(hSteps, vSteps, 0, 0);
        assert (positions.length*positions[0].length == hSteps * vSteps);
        int index = 0;
        for (int i = 0; i < positions.length; ++i) {
            for (int j = 0; j < positions[0].length; ++j) {
                logger.info("P" + ++index + ":" + positions[i][j]);
            }
        }
        wf.setPositionGrid(bd.generatePositions(hSteps, vSteps, 0, 0));
        Position[][] positionGrid = wf.getPositionGrid();
        assertNotNull("Position Grid is not null", positionGrid);
        assertEquals("Position Grid Number of Rows", vSteps, positionGrid.length);
        assertEquals("Position Grid Number of Columns", hSteps, positionGrid[0].length);
        for (int i = 0; i < vSteps; ++i) {
            for (int j = 0; j < hSteps; ++j) {
                logger.info("P[" + i + "][" + j + "]:" + positionGrid[i][j]);
                assertEquals("Map index check", positionGrid[i][j], wf.getPosition(i, j));
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

        WindControlParameters windParameters = new WindControlParameters(10,0);
        windParameters.leftWindSpeed = 70.0;
        windParameters.middleWindSpeed = 80.0;
        windParameters.rightWindSpeed = 90.0;
        windParameters.frequency = 0.375;
        windParameters.amplitude = 20.0;

        RectangularGrid bd = new RectangularGrid(start, end);
        WindFieldGeneratorOscillationImpl wf = new WindFieldGeneratorOscillationImpl(bd, windParameters);
      
        int hSteps = 30;
        int vSteps = 15;

        wf.setPositionGrid(bd.generatePositions(hSteps, vSteps, 0, 0));
        Position[][] positionGrid = wf.getPositionGrid();
        TimePoint startTime = new MillisecondsTimePoint(0);
        Duration timeStep = new MillisecondsDurationImpl(30 * 1000);
        wf.generate(startTime, null, timeStep);
        wf.setTimeScale(1.0);
        SpeedWithBearing speed = new KilometersPerHourSpeedWithBearingImpl(0, new DegreeBearingImpl(0));

        /*
         * Check the speed & angle at the start time
         */
        List<Wind> windList = new ArrayList<Wind>();
        for (int i = 0; i < vSteps; ++i) {
            for (int j = 0; j < hSteps; ++j) {
                Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(startTime, positionGrid[i][j], speed));
                logger.info("Wind[" + i + "][" + j + "]" + localWind.toString());
                windList.add(localWind);
            }
        }
        assertEquals("Size of windList ", hSteps * vSteps, windList.size());
        double epsilon = 1e-6;
        // Check the speed
        assertEquals("StartTime First Wind Speed ", 7, windList.get(0).getKnots(), 0);
        assertEquals("StartTime Last Wind Speed in first row ", 9, windList.get(hSteps - 1).getKnots(), 0);

        assertEquals("StartTime One before Middle Wind Speed ", 7.896551, windList.get(windList.size() / 2 - 2)
                .getKnots(), epsilon);
        assertEquals("StartTime Middle Wind Speed ", 7.965517, windList.get(windList.size() / 2 - 1).getKnots(), epsilon);
        assertEquals("StartTime Last Wind Speed ", 9, windList.get(windList.size() - 1).getKnots(), 0);
        // Check the angle
        assertEquals("StartTime First Wind Angle ", 0, windList.get(0).getBearing().getRadians(), 0);
        Util.Pair<Integer, Integer> pairIndex = getIndex(windList.size(), hSteps);
        logger.info(pairIndex.toString());
        assertEquals("StartTime One before Middle Wind Angle ", 0.3224948, windList.get(windList.size() / 2 - 2).getBearing()
                .getRadians(), epsilon);
        assertEquals("StartTime Middle Wind Angle ", 0.3224948, windList.get(windList.size() / 2 - 1).getBearing().getRadians(),
                epsilon);
        assertEquals("StartTime Last Wind Angle ", 0.2468268, windList.get(windList.size() - 1).getBearing().getRadians(), epsilon);

        /**
         * Check the speed at the next time
         */
        /*
         * Check the speed & angle at the next time point
         */
        windList.clear();
        for (int i = 0; i < vSteps; ++i) {
            for (int j = 0; j < hSteps; ++j) {
                Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(startTime
                        .asMillis() + timeStep.asMillis()), positionGrid[i][j], speed));
                logger.info("Wind[" + i + "][" + j + "]" + localWind.toString());
                windList.add(localWind);
            }
        }
        assertEquals("Size of windList ", hSteps * vSteps, windList.size());

        // Check the speed
        assertEquals("One Time Unit First Wind Speed ", 7, windList.get(0).getKnots(), 0);
        assertEquals("One Time Unit One before Middle Wind Speed ", 7.896551, windList.get(windList.size() / 2 - 2).getKnots(),
                epsilon);
        assertEquals("One Time Unit Middle Wind Speed ", 7.965517, windList.get(windList.size() / 2 - 1).getKnots(), epsilon);
        assertEquals("One Time Unit Last Wind Speed ", 9, windList.get(windList.size() - 1).getKnots(), 0);
        // Check the angle
        assertEquals("One Time Unit First Wind Angle ",0.0068534, windList.get(0).getBearing().getRadians(), epsilon);
        assertEquals("One Time Unit One before Middle Wind Angle ",  0.3250553, windList.get(windList.size() / 2 - 2).getBearing()
                .getRadians(), epsilon);
        assertEquals("One Time Unit Middle Wind Angle ", 0.3250553, windList.get(windList.size() / 2 - 1).getBearing()
               .getRadians(), epsilon);
        assertEquals("One Time Unit Last Wind Angle ",  0.241933, windList.get(windList.size() - 1).getBearing().getRadians(),
                epsilon);

    }

    //@Test
    /*public void testWindFieldGeneratorBlast() {
        Position start = new DegreePosition(54.32447456461419, 10.15613079071045);
        Position end = new DegreePosition(54.32877915239163, 10.156173706054688);
        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);

        WindControlParameters windParameters = new WindControlParameters(10, 180);
        windParameters.blastProbability = 15.0;
        windParameters.maxBlastSize = 4.0;
        windParameters.blastWindSpeed = 15.0;
        windParameters.blastWindSpeedVar = 15.0;

        RectangularBoundary bd = new RectangularBoundary(start, end, 0.1);
        WindFieldGeneratorBlastImpl wf = new WindFieldGeneratorBlastImpl(bd, windParameters);
        int hSteps = 10;
        int vSteps = 5;

        wf.setPositionGrid(bd.extractGrid(hSteps, vSteps));
        Position[][] positionGrid = wf.getPositionGrid();
        TimePoint startTime = new MillisecondsTimePoint(0);
        TimePoint timeStep = new MillisecondsTimePoint(30 * 1000);
        wf.generate(startTime, null, timeStep);

        SpeedWithBearing speed = new KilometersPerHourSpeedWithBearingImpl(0, new DegreeBearingImpl(0));

        //
        // Check the speed & angle at the start time
        //
        List<Wind> windList = new ArrayList<Wind>();
        for (int i = 0; i < vSteps; ++i) {
            for (int j = 0; j < hSteps; ++j) {
                Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(startTime, positionGrid[i][j], speed));
                logger.info("Wind[" + i + "][" + j + "]" + localWind.toString());
                windList.add(localWind);
            }
        }
        assertEquals("Size of windList ", hSteps * vSteps, windList.size());
        //double epsilon = 1e-6;
        // Check the speed
        assertEquals("StartTime First Wind Speed ", 10, windList.get(0).getKnots(), 0);
    }*/
    
    @Test
    public void testWindFieldGeneratorCombinedNoBlast() {
        Position start = new DegreePosition(54.32447456461419, 10.15613079071045);
        Position end = new DegreePosition(54.32877915239163, 10.156173706054688);
        List<Position> course = new LinkedList<Position>();
        course.add(start);
        course.add(end);

        WindControlParameters windParameters = new WindControlParameters(10, 0);
        //Set Oscillation from testWindFieldGeneratorTest
        windParameters.leftWindSpeed = 70.0;
        windParameters.middleWindSpeed = 80.0;
        windParameters.rightWindSpeed = 90.0;
        windParameters.frequency = 0.375;
        windParameters.amplitude = 20.0;
        //No Blast
        windParameters.blastProbability = 0.0;
        windParameters.maxBlastSize = 1.0;
        windParameters.blastWindSpeed = 0.0;
        windParameters.blastWindSpeedVar = 0.0;

        RectangularGrid bd = new RectangularGrid(start, end);
        WindFieldGeneratorCombined wf = new WindFieldGeneratorCombined(bd, windParameters);
        int hSteps = 30;
        int vSteps = 15;

        wf.setPositionGrid(bd.generatePositions(hSteps, vSteps, 0, 0));
        Position[][] positionGrid = wf.getPositionGrid();
        TimePoint startTime = new MillisecondsTimePoint(0);
        Duration timeStep = new MillisecondsDurationImpl(30 * 1000);
        wf.generate(startTime, null, timeStep);

        SpeedWithBearing speed = new KilometersPerHourSpeedWithBearingImpl(0, new DegreeBearingImpl(0));

        /*
         * Check the speed & angle at the start time
         */
        List<Wind> windList = new ArrayList<Wind>();
        for (int i = 0; i < vSteps; ++i) {
            for (int j = 0; j < hSteps; ++j) {
                Wind localWind = wf.getWind(new TimedPositionWithSpeedImpl(startTime, positionGrid[i][j], speed));
                //logger.info("Wind[" + i + "][" + j + "]" + localWind.toString());
                windList.add(localWind);
            }
        }
        assertEquals("Size of windList ", hSteps * vSteps, windList.size());
        double epsilon = 1e-6;
        // Check the speed
        assertEquals("StartTime First Wind Speed ", 7, windList.get(0).getKnots(), 0);
        assertEquals("StartTime Last Wind Speed in first row ", 9, windList.get(hSteps - 1).getKnots(), 0);

        assertEquals("StartTime One before Middle Wind Speed ", 7.896551, windList.get(windList.size() / 2 - 2)
                .getKnots(), epsilon);
        assertEquals("StartTime Middle Wind Speed ", 7.965517, windList.get(windList.size() / 2 - 1).getKnots(), epsilon);
        assertEquals("StartTime Last Wind Speed ", 9, windList.get(windList.size() - 1).getKnots(), 0);
        // Check the angle
        assertEquals("StartTime First Wind Angle ", 0, windList.get(0).getBearing().getRadians(), 0);
       
    }
    
    private Util.Pair<Integer, Integer> getIndex(int listIndex, int numCols) {
        Util.Pair<Integer, Integer> indexPair = new Util.Pair<Integer, Integer>(1 + (listIndex - 1) / numCols, 1
                + (listIndex - 1) % numCols);
        return indexPair;
    }

    @Test
    public void testIndex() {
        Util.Pair<Integer, Integer> pairIndex = getIndex(1, 30);
        assertEquals("Index 1 RowIndex ", 1, (int) pairIndex.getA());
        assertEquals("Index 1 ColIndex ", 1, (int) pairIndex.getB());
        pairIndex = getIndex(30, 30);
        assertEquals("Index 30 RowIndex ", 1, (int) pairIndex.getA());
        assertEquals("Index 30 ColIndex ", 30, (int) pairIndex.getB());
        pairIndex = getIndex(31, 30);
        assertEquals("Index 31 RowIndex ", 2, (int) pairIndex.getA());
        assertEquals("Index 31 ColIndex ", 1, (int) pairIndex.getB());
        pairIndex = getIndex(450, 30);
        assertEquals("Index 450 RowIndex ", 15, (int) pairIndex.getA());
        assertEquals("Index 450 ColIndex ", 30, (int) pairIndex.getB());
    }

}
