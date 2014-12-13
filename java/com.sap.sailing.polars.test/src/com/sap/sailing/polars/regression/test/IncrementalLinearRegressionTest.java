package com.sap.sailing.polars.regression.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import com.sap.sailing.polars.regression.IncrementalLinearRegressionProcessor;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.impl.IncrementalLeastSquaresProcessor;

public class IncrementalLinearRegressionTest {

    private static double EPSILON = 1E-12;

    private static double VAGUE_EPSILON = 0.1;

    private IncrementalLinearRegressionProcessor createRegressionProcessor() {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();
        return processor;
    }

    @Test
    public void testRegressionWithTwoDataPoints() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();

        double x1 = 3;
        double y1 = 7;
        processor.addMeasuredPoint(x1, y1);

        double x2 = 6;
        double y2 = 14;
        processor.addMeasuredPoint(x2, y2);

        double y = processor.getEstimatedY(x1);
        assertThat(y, new IsCloseTo(y1, EPSILON));

        y = processor.getEstimatedY(x2);
        assertThat(y, new IsCloseTo(y2, EPSILON));

        y = processor.getEstimatedY(4.5);
        assertThat(y, new IsCloseTo(10.5, EPSILON));
    }

    @Test
    public void testRegressionWithMultipleDataPoints() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();

        for (int i = -50; i < 51; i++) {
            processor.addMeasuredPoint(i, i);
        }

        double y = processor.getEstimatedY(-2);
        assertThat(y, new IsCloseTo(-2, EPSILON));

        // 2 Points off the line
        processor.addMeasuredPoint(-8, -3);
        processor.addMeasuredPoint(8, -3);


        y = processor.getEstimatedY(-2);
        assertThat(y, new IsCloseTo(-2, VAGUE_EPSILON));
        y = processor.getEstimatedY(20);
        assertThat(y, new IsCloseTo(20, VAGUE_EPSILON));
    }

    @Test
    public void testRegressionWithABigAmountOfDataPoints() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();

        for (int i = -1000; i < 1000; i++) {
            // For every even number + 1, else - 1
            double y = i % 2 == 0 ? i + 1 : i - 1;
            processor.addMeasuredPoint(i, y);
        }

        double y = processor.getEstimatedY(-2);
        assertThat(y, new IsCloseTo(-2, VAGUE_EPSILON));
        y = processor.getEstimatedY(20);
        assertThat(y, new IsCloseTo(20, VAGUE_EPSILON));
    }

    @Test
    public void testRegressionWithZeroValueData() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();

        double x1 = 0;
        double y1 = 0;
        processor.addMeasuredPoint(x1, y1);

        double x2 = 5;
        double y2 = 0;
        processor.addMeasuredPoint(x2, y2);

        double y = processor.getEstimatedY(x1);
        assertThat(y, new IsCloseTo(y1, EPSILON));

        y = processor.getEstimatedY(x2);
        assertThat(y, new IsCloseTo(y2, EPSILON));
    }

    @Test
    public void testRegressionWithNegativeData() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();

        double x1 = -5;
        double y1 = -5;
        processor.addMeasuredPoint(x1, y1);

        double x2 = 5;
        double y2 = 5;
        processor.addMeasuredPoint(x2, y2);

        double y = processor.getEstimatedY(x1);
        assertThat(y, new IsCloseTo(y1, EPSILON));

        y = processor.getEstimatedY(x2);
        assertThat(y, new IsCloseTo(y2, EPSILON));

        y = processor.getEstimatedY(0);
        assertThat(y, new IsCloseTo(0, EPSILON));
    }

    @Test
    public void testRegressionWithSmallValuedData() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();

        double x1 = 5E-9;
        double y1 = 5E-9;
        for (int i = 0; i < 1000; i++) {
            double yTimesI = y1 * i;
            double xTimesI = x1 * i;
            processor.addMeasuredPoint(xTimesI, yTimesI);
            if (i > 0) {
                double y = processor.getEstimatedY(xTimesI);
                assertThat(y, new IsCloseTo(yTimesI, EPSILON));
            }
        }
        // Add data that is off the line but offsetting each other
        processor.addMeasuredPoint(-x1, y1);
        processor.addMeasuredPoint(x1, -y1);

        double y = processor.getEstimatedY(0);
        assertThat(y, new IsCloseTo(0, EPSILON));
    }

    @Test(expected = NotEnoughDataHasBeenAddedException.class)
    public void testNoDataExceptionThrowing() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();
        processor.getEstimatedY(15);
    }

    /**
     * Assert that y is constant for only one added data point
     * 
     * @throws NotEnoughDataHasBeenAddedException
     */
    @Test
    public void testRegressionWithOneDataPoint() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = createRegressionProcessor();

        double x1 = 3;
        double y1 = 5;
        processor.addMeasuredPoint(x1, y1);

        double y = processor.getEstimatedY(20);
        assertThat(y, is(y1));
    }
    
    /*
     * The following tests and the data are taken from: org.apache.commons.math3.stat.regression.SimpleRegressionTest
     * 
     * http://svn.apache.org/viewvc/commons/proper/math/trunk/src/test/java/org/apache/commons/math3/stat/regression/
     * SimpleRegressionTest.java?view=markup
     * 
     * They are adapted to use the IncrementalLinearRegressionProcessor, but the data and the asserts were kept as is
     */
    
    /*
     * NIST "Norris" refernce data set from
     * http://www.itl.nist.gov/div898/strd/lls/data/LINKS/DATA/Norris.dat
     * Strangely, order is {y,x}
     */
    private double[][] data = { { 0.1, 0.2 }, {338.8, 337.4 }, {118.1, 118.2 },
            {888.0, 884.6 }, {9.2, 10.1 }, {228.1, 226.5 }, {668.5, 666.3 }, {998.5, 996.3 },
            {449.1, 448.6 }, {778.9, 777.0 }, {559.2, 558.2 }, {0.3, 0.4 }, {0.1, 0.6 }, {778.1, 775.5 },
            {668.8, 666.9 }, {339.3, 338.0 }, {448.9, 447.5 }, {10.8, 11.6 }, {557.7, 556.0 },
            {228.3, 228.1 }, {998.0, 995.8 }, {888.8, 887.6 }, {119.6, 120.2 }, {0.3, 0.3 },
            {0.6, 0.3 }, {557.6, 556.8 }, {339.3, 339.1 }, {888.0, 887.2 }, {998.5, 999.0 },
            {778.9, 779.0 }, {10.2, 11.1 }, {117.6, 118.3 }, {228.9, 229.2 }, {668.4, 669.1 },
            {449.2, 448.9 }, {0.2, 0.5 }
    };


    @Test
    public void testNorris() throws NotEnoughDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor regression = createRegressionProcessor();
        for (int i = 0; i < data.length; i++) {
            regression.addMeasuredPoint(data[i][1], data[i][0]);
        }
        // Tests against certified values from
        // http://www.itl.nist.gov/div898/strd/lls/data/LINKS/DATA/Norris.dat
        assertThat(regression.getSlope(), new IsCloseTo(1.00211681802045, EPSILON));
        assertThat(regression.getIntercept(), new IsCloseTo(-0.262323073774029, EPSILON));
        // ------------ End certified data tests
        assertThat(regression.getEstimatedY(0), new IsCloseTo(-0.262323073774029, EPSILON));
        assertThat(regression.getEstimatedY(1), new IsCloseTo(1.00211681802045 - 0.26232307377402, EPSILON));
    }

    @Test
    public void testPerfect() {
        IncrementalLinearRegressionProcessor regression = createRegressionProcessor();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addMeasuredPoint(((double) i) / (n - 1), i);
        }
        assertTrue(regression.getSlope() > 0);
    }

    @Test
    public void testPerfectNegative() {
        IncrementalLinearRegressionProcessor regression = createRegressionProcessor();
        int n = 100;
        for (int i = 0; i < n; i++) {
            regression.addMeasuredPoint(-((double) i) / (n - 1), i);
        }
        assertTrue(regression.getSlope() < 0);
    }


}
