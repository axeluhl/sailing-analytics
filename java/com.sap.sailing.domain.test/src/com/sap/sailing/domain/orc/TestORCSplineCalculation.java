package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sse.common.TimePoint;

/**
 * This test class has the goal to show the accuracy of the used splining method. The spline is calculated when creating
 * a {@link ORCPerformanceCurve} for a {@link Competitor} for a given {@link TimePoint} on the {@link Course}. <br>
 * <br>
 * The used course in here consists out of the following legs:<br>
 * [No. | TWA | length] <br>
 * - 1. | 010° | 2.23nm <br>
 * - 2. | 170° | 2.00nm <br>
 * - 3. | 000° | 0.97nm <br>
 * - 4. | 015° | 1.03nm <br>
 * - 5. | 165° | 1.03nm <br>
 * - 6. | 180° | 1.17nm <br>
 * -------------------- <br>
 * Total Length: 8.43nm <br>
 * <br>
 * 
 * The calculated velocity predictions for the total course used in the interpolation are calculated correctly. These
 * velocitys are checked with the values from Manage2Sail/SwissTiming and the used ORC certificates are from the 15th
 * July 2019.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class TestORCSplineCalculation {

    private static final double[] xn = { 0, 6, 8, 10, 12, 14, 16, 20, 1000 };
    private static final double totalLength = 8.43;

    @Test
    public void testMoana() {
        assertEquals(1.5, interpolate(new double[] {0, 4.641211342235869, 5.736695797981516, 6.546702285181061, 7.02579946374626, 7.298101614710148, 7.608915286969485, 8.275181564996913, 8.275181564996913}, 7.6218), 0.00001);

    }

    // for a given implied wind, we expect to get back the velocity prediction for the whole course. The total Course
    // Length can be divided with the prediction to get the Allowance in hours.
    private double interpolate(double[] yn, double input) {
        AkimaSplineInterpolator interpolator = new AkimaSplineInterpolator();
        PolynomialSplineFunction function = interpolator.interpolate(xn, yn);
        
        return totalLength / function.value(input);
    }

}
