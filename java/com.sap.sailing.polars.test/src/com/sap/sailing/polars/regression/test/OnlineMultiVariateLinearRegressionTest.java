package com.sap.sailing.polars.regression.test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Test;

import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.OnlineMultiVariateRegression;
import com.sap.sailing.polars.regression.impl.OnlineMultiVariateRegressionImpl;

public class OnlineMultiVariateLinearRegressionTest {

    private static double EPSILON = 1;
    
    @Test
    public void testRegressionWithPolarData() throws NotEnoughDataHasBeenAddedException {
        OnlineMultiVariateRegression regression = new OnlineMultiVariateRegressionImpl(3);
        Random random = new Random(1);
        
        for (int i = 0; i < 10000; i++) {
            double x = random.nextDouble() * 80 - 40;
            double[] xArray  = {x, x*x, x*x*x};
            regression.addData(function(x, random), xArray);
        }
        
        double[] xArray  = {5, 25, 125};
        double estimated = regression.estimateY(xArray);
        
        assertThat(estimated, closeTo(function(5, random), EPSILON));
    }
    
    private double function(double x) {
        return 0.0003*x*x*x-0.0373*x*x+1.5213*x -2.1309;
    }

    private double function(double x, Random random) {
        return function(x) + random.nextGaussian() * 5;
    }

}
