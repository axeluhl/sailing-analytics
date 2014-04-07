package com.sap.sailing.gwt.ui.client.shared.charts;

import org.moxieapps.gwt.highcharts.client.Point;

public final class WindChartPointRecalculator {
    
    private WindChartPointRecalculator() { }
    
    public static Point stayClosestToPreviousPoint(Point previousPoint, Point newPoint) {
        return newPoint;
    }
    
    public static Point recalculateDirectionPoint(Double yMin, Double yMax, Point directionPoint) {
        double y = directionPoint.getY().doubleValue();

        if (yMax != null && yMin != null && (y < yMin || y > yMax)) {
            double deltaMin = Math.abs(yMin - y);
            double deltaMax = Math.abs(yMax - y);

            double yDown = y - 360;
            double deltaMinDown = Math.abs(yMin - yDown);

            double yUp = y + 360;
            double deltaMaxUp = Math.abs(yMax - yUp);

            if (isRecalculationNeeded(deltaMin, deltaMax, deltaMinDown, deltaMaxUp)) {
                y = deltaMaxUp <= deltaMinDown ? yUp : yDown;
                return new Point(directionPoint.getX(), y);
            }
        }
        
        return directionPoint;
    }

    private static boolean isRecalculationNeeded(double deltaMin, double deltaMax, double deltaMinDown, double deltaMaxUp) {
        return (deltaMinDown < deltaMin || deltaMinDown < deltaMax) || (deltaMaxUp < deltaMin || deltaMaxUp < deltaMax);
    }

}
