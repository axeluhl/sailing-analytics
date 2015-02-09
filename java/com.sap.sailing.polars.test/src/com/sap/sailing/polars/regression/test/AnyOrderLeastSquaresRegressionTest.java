package com.sap.sailing.polars.regression.test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Test;

import com.sap.sailing.polars.regression.impl.AnyOrderLeastSquaresRegression;

public class AnyOrderLeastSquaresRegressionTest {
    
    
    private static final double ERROR = 0.01;

    @Test
    public void testNonIncrementalRegression() {
        AnyOrderLeastSquaresRegression regression = new AnyOrderLeastSquaresRegression(3);
        Random random = new Random(1);
        for (int i = 0; i < 100000; i++) {
            double x = random.nextDouble() * 40;
            regression.addData(function(x) +  (random.nextDouble() - 0.5), x);
        }
        
        long startTime = System.nanoTime();
        double[] coeffs = regression.getCoefficiants();
        System.out.println("First calculation " + (System.nanoTime() - startTime) + "ns");
        
        startTime = System.nanoTime();
        coeffs = regression.getCoefficiants();
        System.out.println("No calculation " + (System.nanoTime() - startTime) + "ns");
        double x = random.nextDouble() * 40;
        regression.addData(function(x) +  (random.nextDouble() - 0.5), x);
        startTime = System.nanoTime() ;
        coeffs = regression.getCoefficiants();
        System.out.println("Second calculation " + (System.nanoTime()  - startTime) + "ns");
        
        assertThat(coeffs[0], closeTo(-2.1309, ERROR));
        assertThat(coeffs[1], closeTo(1.5213, ERROR));
        assertThat(coeffs[2], closeTo(-0.0373, ERROR));
        assertThat(coeffs[3], closeTo(0.0003, ERROR));
    }
    
    private double function(double x) {
        return 0.0003*x*x*x-0.0373*x*x+1.5213*x -2.1309;
    }
    



}
