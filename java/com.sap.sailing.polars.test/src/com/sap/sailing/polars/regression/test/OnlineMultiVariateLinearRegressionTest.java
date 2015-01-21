package com.sap.sailing.polars.regression.test;

import static org.junit.Assert.assertThat;

import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.OnlineMultiVariateRegression;
import com.sap.sailing.polars.regression.impl.OnlineMultiVariateRegressionImpl;

public class OnlineMultiVariateLinearRegressionTest {

    private static double EPSILON = 1E-3;
    
    @Test
    public void testRegressionWithABigAmountOfDataPointsAndOneDimension() throws NotEnoughDataHasBeenAddedException {
        OnlineMultiVariateRegression regression = new OnlineMultiVariateRegressionImpl(1);

        for (int i = -1000; i < 1000; i++) {
            // For every even number + 1, else - 1
            double y = i % 2 == 0 ? i + 1 : i - 1;
            double[] x = { i };
            regression.addData(y, x);
        }

        double[] x1 = { -2 };
        double y = regression.estimateY(x1);
        assertThat(y, new IsCloseTo(-2, EPSILON));

        double[] x2 = { 20 };
        y = regression.estimateY(x2);
        assertThat(y, new IsCloseTo(20, EPSILON));
    }

}
