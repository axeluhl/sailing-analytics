package com.sap.sailing.gwt.ui.test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.moxieapps.gwt.highcharts.client.Point;

import com.sap.sailing.gwt.ui.client.shared.charts.ChartPointRecalculator;

public class TestWindChartPointRecalculation {
    
    private static final double POINT_Y_EQUALITY_EPSILON = 0.005;
    
    private static final List<Double> MULTIPLE_WRAP_AROUNDS = Arrays.asList(356.74, 10.54, 30.192, 20.625, 5.647, 350.526);
    private static final List<Double> EXPECTED_RECALCULATED_VALUES_FOR_MULTIPLE_WRAP_AROUNDS = Arrays.asList(356.74, 10.54 + 360, 30.192 + 360, 20.625 + 360, 5.647 + 360, 350.526);
    
    private static final List<Double> CONTINOUS_WRAP_AROUNDS = Arrays.asList(5.34, 358.97, 10.42, 50.0, 110.65, 140.54,
            210.85, 280.38, 330.5, 358.97, 7.24, 48.0, 112.65, 135.54, 215.85, 278.38, 333.5, 356.97, 8.24, 48.245,
            112.57, 133.54, 215.637, 278.38, 333.5, 356.97);
    private static final List<Double> EXPECTED_RECALCULATED_VALUES_FOR_CONTINOUS_WRAP_AROUNDS_OVERALL_DELTA_MINIMUM = Arrays.asList(
            5.34, 358.97 - 360, 10.42, 50.0, 110.65, 140.54, 210.85 - 360, 280.38 - 360, 330.5 - 360,
            358.97 - 360, 7.24, 48.0, 112.65, 135.54, 215.85 - 360, 278.38 - 360, 333.5 - 360, 356.97 - 360,
            8.24, 48.245, 112.57, 133.54, 215.637 - 360, 278.38 - 360, 333.5 - 360, 356.97 - 360);
    private static final List<Double> EXPECTED_RECALCULATED_VALUES_FOR_CONTINOUS_WRAP_AROUNDS_CLOSEST_TO_PREVIOUS_POINT = Arrays.asList(
            5.34, 358.97 - 360, 10.42, 50.0, 110.65, 140.54, 210.85, 280.38, 330.5, 358.97, 367.24, 408.0, 472.65,
            135.54 + 360, 575.85, 638.38, 693.5, 716.97, 728.24, 768.245, 112.57 + 720, 853.54, 935.637, 998.38, 1053.5,
            1076.97);
    
    private static void pointYEquals(Point actual, double expectedY) {
        assertThat(actual.getY().doubleValue(), closeTo(expectedY, POINT_Y_EQUALITY_EPSILON));
    }

    @Test
    public void testClosestToPreviousPointRecalculation() {
        Point previousPoint = new Point(9, 20);
        
        Point notToBeRecalculated = new Point(10, 100);
        pointYEquals(ChartPointRecalculator.stayClosestToPreviousPoint(previousPoint, notToBeRecalculated), notToBeRecalculated.getY().doubleValue());
        
        notToBeRecalculated = new Point(10, 5);
        pointYEquals(ChartPointRecalculator.stayClosestToPreviousPoint(previousPoint, notToBeRecalculated), notToBeRecalculated.getY().doubleValue());
        
        Point toBeMovedDown = new Point(10, 358);
        pointYEquals(ChartPointRecalculator.stayClosestToPreviousPoint(previousPoint, toBeMovedDown), -2);
        
        previousPoint = new Point(9, 350);
        Point toBeMovedUp = new Point(10, 2);
        pointYEquals(ChartPointRecalculator.stayClosestToPreviousPoint(previousPoint, toBeMovedUp), 362);
    }

    @Test
    public void testOverallDeltaMinimalRecalculaion() {
        Point notToBeRecalculated = new Point(10, 100);
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(90.0, 110.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(null, 110.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(90.0, null, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(100.27458, 110.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(90.0, 100.75639, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        
        //Test for special case, which caused a wrong recalculation, because of the wrong use of Math.abs()
        notToBeRecalculated = new Point(10, 179.8537);
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(180.12356, 190.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(170.0, 179.5372, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        
        Point toBeMovedDown = new Point(10, 356);
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(4.0, 23.0, toBeMovedDown).getY(), equalTo(new Point(10, 356.0 - 360.0).getY()));
        
        Point toBeMovedUp = new Point(10, 5);
        assertThat(ChartPointRecalculator.keepOverallDeltaMinimal(356.0, 359.83364, toBeMovedUp).getY(), equalTo(new Point(10, 5.0 + 360.0).getY()));
    }
    
    @Test
    public void testClosestToPreviousPointWithMultipleAndContiniusWrapArounds() {
        //Test multiple wrap around between 350 and 30
        assertThat(recalculatePointsClosestToPreviousPoint(MULTIPLE_WRAP_AROUNDS), equalTo(EXPECTED_RECALCULATED_VALUES_FOR_MULTIPLE_WRAP_AROUNDS));
        
        //Test multiple round trips from 0 to 360
        assertThat(recalculatePointsClosestToPreviousPoint(CONTINOUS_WRAP_AROUNDS), equalTo(EXPECTED_RECALCULATED_VALUES_FOR_CONTINOUS_WRAP_AROUNDS_CLOSEST_TO_PREVIOUS_POINT));
    }
    
    private List<Double> recalculatePointsClosestToPreviousPoint(List<Double> points) {
        List<Double> racalculatedPoints = new ArrayList<Double>();
        Point previousPoint = null;
        for (Double yValue : points) {
            Point newPoint = new Point(10, yValue);
            if (previousPoint != null) {
                newPoint = ChartPointRecalculator.stayClosestToPreviousPoint(previousPoint, newPoint);
            }
            racalculatedPoints.add(newPoint.getY().doubleValue());
            previousPoint = newPoint;
        }
        return racalculatedPoints;
    }

    @Test
    public void testOverallDeltaMinimalWithMultipleAndContinuousWrapArounds() {
        //Test multiple wrap around between 350 and 30
        assertThat(recalculatePointsWithOverallDeltaMinimal(MULTIPLE_WRAP_AROUNDS), equalTo(EXPECTED_RECALCULATED_VALUES_FOR_MULTIPLE_WRAP_AROUNDS));
        
        //Test multiple round trips from 0 to 360
        assertThat(recalculatePointsWithOverallDeltaMinimal(CONTINOUS_WRAP_AROUNDS), equalTo(EXPECTED_RECALCULATED_VALUES_FOR_CONTINOUS_WRAP_AROUNDS_OVERALL_DELTA_MINIMUM));
    }

    private List<Double> recalculatePointsWithOverallDeltaMinimal(List<Double> pointYValues) {
        List<Double> recalculatedPointYValues = new ArrayList<Double>();
        Double min = null;
        Double max = null;
        for (Double yValue : pointYValues) {
            Point p = new Point(10, yValue);
            p = ChartPointRecalculator.keepOverallDeltaMinimal(min, max, p);
            Double newYValue = p.getY().doubleValue();
            recalculatedPointYValues.add(newYValue);
            
            if (min == null || newYValue < min) {
                min = newYValue;
            }
            if (max == null || newYValue > max) {
                max = newYValue;
            }
        }
        return recalculatedPointYValues;
    }

}
