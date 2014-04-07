package com.sap.sailing.gwt.ui.client.shared.charts;

import org.moxieapps.gwt.highcharts.client.Point;

public final class WindChartPointRecalculator {
    
    private WindChartPointRecalculator() { }
    
    public static Point stayClosestToPreviousPoint(Point previousPoint, Point newPoint) {
        double previousY = previousPoint.getY().doubleValue();
        double newY = newPoint.getY().doubleValue();
        
        double deltaPreviousNew = Math.abs(previousY - newY);
        double deltaPreviosNewUp = Math.abs(previousY - (newY + 360));
        double deltaPreviosNewDown = Math.abs(previousY - (newY - 360));
        
        if (isStrictlyLessThan(deltaPreviosNewUp, deltaPreviousNew, deltaPreviosNewDown)) {
            return new Point(newPoint.getX(), newPoint.getY().doubleValue() + 360);
        }
        if (isStrictlyLessThan(deltaPreviosNewDown, deltaPreviousNew, deltaPreviosNewUp)) {
            return new Point(newPoint.getX(), newPoint.getY().doubleValue() - 360);
        }
        
        return newPoint;
    }
    
    private static boolean isStrictlyLessThan(double valueToCheck, double... valuesToCompare) {
        for (double value : valuesToCompare) {
            if (value <= valueToCheck) {
                return false;
            }
        }
        return true;
    }

    public static Point keepOverallDeltaMinimal(Double yMin, Double yMax, Point newPoint) {
        double y = newPoint.getY().doubleValue();

        if (yMax != null && yMin != null && (y < yMin || y > yMax)) {
            double deltaMin = Math.abs(yMin - y);
            double deltaMax = Math.abs(yMax - y);

            double yDown = y - 360;
            double deltaMinDown = Math.abs(yMin - yDown);

            double yUp = y + 360;
            double deltaMaxUp = Math.abs(yMax - yUp);

            if (isRecalculationNeeded(deltaMin, deltaMax, deltaMinDown, deltaMaxUp)) {
                y = deltaMaxUp <= deltaMinDown ? yUp : yDown;
                return new Point(newPoint.getX(), y);
            }
        }
        
        return newPoint;
    }

    private static boolean isRecalculationNeeded(double deltaMin, double deltaMax, double deltaMinDown, double deltaMaxUp) {
        return (deltaMinDown < deltaMin || deltaMinDown < deltaMax) || (deltaMaxUp < deltaMin || deltaMaxUp < deltaMax);
    }

}
