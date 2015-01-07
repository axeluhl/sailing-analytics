package com.sap.sailing.polars.test;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.sap.sailing.polars.impl.CubicEquation;

public class CubicEquationTest {
    
    
    private static final double ERROR = 0.00001;

    @Test
    public void testForOneRealRoot() {
        CubicEquation eq = new CubicEquation(3, -10, 14, 27);
        double[] result = eq.solve();
        assertThat(result.length, is(1));
        
        double x1 = result[0];
        assertThat(x1, closeTo(-1, ERROR));
        
    }
    
    @Test
    public void testThreeRealAndEqualRoots() {
        CubicEquation eq = new CubicEquation(1, 6, 12, 8);
        double[] result = eq.solve();
        assertThat(result.length, is(1));
        
        double x1 = result[0];
        assertThat(x1, closeTo(-2, ERROR));
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testThreeRealRoots() {
        CubicEquation eq = new CubicEquation(2, -4, -22, 24);
        double[] result = eq.solve();
        assertThat(result.length, is(3));
        
        double x1 = result[0];
        assertThat(x1, anyOf(closeTo(4, ERROR),closeTo(-3, ERROR),closeTo(1, ERROR)));
        
        double x2 = result[1];
        assertThat(x2, anyOf(closeTo(4, ERROR),closeTo(-3, ERROR),closeTo(1, ERROR)));
        
        double x3 = result[2];
        assertThat(x3, anyOf(closeTo(4, ERROR),closeTo(-3, ERROR),closeTo(1, ERROR)));
        
        
        
    }

}
