package com.sap.sailing.polars.regression.test;

import static org.junit.Assert.assertThat;

import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import com.sap.sailing.polars.regression.IncrementalLinearRegressionProcessor;
import com.sap.sailing.polars.regression.NoDataHasBeenAddedException;
import com.sap.sailing.polars.regression.impl.IncrementalLeastSquaresProcessor;

public class IncrementalLinearRegressionTest {

    private static double EPSILON = 1E-12;

    @Test
    public void testRegressionWithOneDataPoint() throws NoDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();

        double x1 = 3;
        double y1 = 5;
        processor.addMeasuredPoint(x1, y1);

        double y = processor.getY(x1);
        assertThat(y, new IsCloseTo(y1, EPSILON));
    }

    @Test
    public void testRegressionWithTwoDataPoints() throws NoDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();

        double x1 = 3;
        double y1 = 7;
        processor.addMeasuredPoint(x1, y1);

        double x2 = 6;
        double y2 = 14;
        processor.addMeasuredPoint(x2, y2);

        double y = processor.getY(x1);
        assertThat(y, new IsCloseTo(y1, EPSILON));

        y = processor.getY(x2);
        assertThat(y, new IsCloseTo(y2, EPSILON));

        y = processor.getY(4.5);
        assertThat(y, new IsCloseTo(10.5, EPSILON));
    }

    @Test
    public void testRegressionWithEightDataPoints() throws NoDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();

        // 6 Points on the line
        for (int i = -500000; i < 500000; i++) {
            processor.addMeasuredPoint(i, i);
        }

        double y = processor.getY(-2);
        assertThat(y, new IsCloseTo(-2, EPSILON));

        // 2 Points off the line
        processor.addMeasuredPoint(-30, 30);
        processor.addMeasuredPoint(30, -30);

        y = processor.getY(-2);
        assertThat(y, new IsCloseTo(-2, EPSILON));
        y = processor.getY(20);
        assertThat(y, new IsCloseTo(20, EPSILON));
    }

    @Test
    public void testRegressionWithZeroValueData() throws NoDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();

        double x1 = 0;
        double y1 = 0;
        processor.addMeasuredPoint(x1, y1);

        double x2 = 5;
        double y2 = 0;
        processor.addMeasuredPoint(x2, y2);

        double y = processor.getY(x1);
        assertThat(y, new IsCloseTo(y1, EPSILON));

        y = processor.getY(x2);
        assertThat(y, new IsCloseTo(y2, EPSILON));
    }

    @Test
    public void testRegressionWithNegativeData() throws NoDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();

        double x1 = -5;
        double y1 = -5;
        processor.addMeasuredPoint(x1, y1);

        double x2 = 5;
        double y2 = 5;
        processor.addMeasuredPoint(x2, y2);

        double y = processor.getY(x1);
        assertThat(y, new IsCloseTo(y1, EPSILON));

        y = processor.getY(x2);
        assertThat(y, new IsCloseTo(y2, EPSILON));

        y = processor.getY(0);
        assertThat(y, new IsCloseTo(0, EPSILON));
    }

    @Test
    public void testRegressionWithSmallValuedData() throws NoDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();

        double x1 = 5E-9;
        double y1 = 5E-9;
        for (int i = 0; i < 1000; i++) {
            double yTimesI = y1 * i;
            double xTimesI = x1 * i;
            processor.addMeasuredPoint(xTimesI, yTimesI);
            double y = processor.getY(xTimesI);
            assertThat(y, new IsCloseTo(yTimesI, EPSILON));
        }
        // Add data that is off the line but offsetting each other
        processor.addMeasuredPoint(-x1, y1);
        processor.addMeasuredPoint(x1, -y1);

        double y = processor.getY(0);
        assertThat(y, new IsCloseTo(0, EPSILON));
    }

    @Test(expected = NoDataHasBeenAddedException.class)
    public void testNoDataExceptionThrowing() throws NoDataHasBeenAddedException {
        IncrementalLinearRegressionProcessor processor = new IncrementalLeastSquaresProcessor();
        processor.getY(15);
    }

}
