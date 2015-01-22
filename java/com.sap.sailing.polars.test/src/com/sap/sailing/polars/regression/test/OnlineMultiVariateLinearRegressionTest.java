package com.sap.sailing.polars.regression.test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Test;

import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.OnlineMultiVariateRegression;
import com.sap.sailing.polars.regression.impl.OnlineMultiVariateRegressionImpl;

public class OnlineMultiVariateLinearRegressionTest {

    private static double EPSILON = 0.5;
    
    @Test
    public void testRegressionWithPolarData() throws NotEnoughDataHasBeenAddedException {
        OnlineMultiVariateRegression regression = new OnlineMultiVariateRegressionImpl(3);
        Random random = new Random(9999);
        
        for (int i = 0; i < 1000; i++) {
            double x = random.nextDouble() * 20 - 10;
            double[] xArray  = {x, x*x, x*x*x};
            regression.addData(function(x, random), xArray);
        }
        
        double[] xArray  = {0, 0, 0};
        double estimated = regression.estimateY(xArray);
        
        assertThat(estimated, closeTo(function(0, random), EPSILON));
        
        

    }
    
    private double function(double x) {
        return 1 + x*0.2 - x*x + 0.1*x*x*x;
    }

    private double function(double x, Random random) {
        return function(x) + random.nextGaussian() * 5;
    }

}
