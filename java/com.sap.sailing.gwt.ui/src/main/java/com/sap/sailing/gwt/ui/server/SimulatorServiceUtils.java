package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.RadianPosition;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;

public class SimulatorServiceUtils {

    public static final double EARTH_RADIUS_METERS = 6378137;
    public static final double FACTOR_DEG2RAD = 0.0174532925;
    public static final double FACTOR_RAD2DEG = 57.2957795;
    public static final double FACTOR_KN2MPS = 0.514444;
    public static final double FACTOR_MPS2KN = 1.94384;

    /**
     * Converts degress to radians
     */
    public static double degreesToRadians(final double degrees) {
        return (degrees * FACTOR_DEG2RAD);
    }

    /**
     * Converts radians to degrees
     */
    public static double radiansToDegrees(final double radians) {
        return (radians * FACTOR_RAD2DEG);
    }

    /**
     * Converts knots to meters per second
     */
    public static double knotsToMetersPerSecond(final double knots) {
        return knots * FACTOR_KN2MPS;
    }

    /**
     * Converts meters per second to knots
     */
    public static double metersPerSecondToKnots(final double metersPerSecond) {
        return metersPerSecond * FACTOR_MPS2KN;
    }

    /**
     * Computes the average value from the given list of SpeedWithBearing objects.
     */
    public static SpeedWithBearing getAverage(final SimulatorWindDTO windDTO1, final SimulatorWindDTO windDTO2) {
        final List<SimulatorWindDTO> windDTOs = new ArrayList<SimulatorWindDTO>();
        windDTOs.add(windDTO1);
        windDTOs.add(windDTO2);
        return SimulatorServiceUtils.getAverage(windDTOs);
    }

    /**
     * Computes the average value from the given list of SpeedWithBearing objects.
     */
    public static SpeedWithBearing getAverage(final List<SimulatorWindDTO> windDTOs) {

        double sumOfProductOfSpeedAndCosBearing = 0.0;
        double sumOfProductOfSpeedAndSinBearing = 0.0;
        double windBearingRadians = 0.0;

        for (final SimulatorWindDTO windDTO : windDTOs) {
            windBearingRadians = degreesToRadians(windDTO.trueWindBearingDeg);
            sumOfProductOfSpeedAndSinBearing += (windDTO.trueWindSpeedInKnots * Math.sin(windBearingRadians));
            sumOfProductOfSpeedAndCosBearing += (windDTO.trueWindSpeedInKnots * Math.cos(windBearingRadians));
        }
        final int count = windDTOs.size();
        final double a = sumOfProductOfSpeedAndSinBearing / count;
        final double b = sumOfProductOfSpeedAndCosBearing / count;
        final double c = radiansToDegrees(Math.atan(a / b));

        double averageBearingDegrees = 0.0;

        if (a > 0 && b >= 0) {
            averageBearingDegrees = c;
        } else if (a < 0 && b >= 0) {
            averageBearingDegrees = 360 + c;
        } else if (a < 0 && b < 0) {
            averageBearingDegrees = 180 + c;
        } else if (a > 0 && b < 0) {
            averageBearingDegrees = 180 - c;
        }

        final double averageSpeedKnots = Math.sqrt(a * a + b * b);

        return new KnotSpeedWithBearingImpl(averageSpeedKnots, new DegreeBearingImpl(averageBearingDegrees));
    }

    /**
     * Gets an array of points from the start to the end with a certain step.
     */
    public static List<Position> getIntermediatePoints(final Position startPoint, final Position endPoint, final double stepSizeMeters) {
        final List<Position> result = new ArrayList<Position>();

        final double distance = getDistanceBetween(startPoint, endPoint);
        final int noOfSteps = (int) (distance / stepSizeMeters);
        double bearing = getInitialBearing(startPoint, endPoint);

        Position temp = null;

        result.add(startPoint);
        for (int stepIndex = 1; stepIndex < noOfSteps; stepIndex++) {
            temp = getDestinationPoint(startPoint, bearing, stepSizeMeters * stepIndex);
            result.add(temp);
            bearing = getInitialBearing(startPoint, temp);
        }

        return result;
    }

    public static List<Position> getIntermediatePoints2(final List<PositionDTO> points, final double stepSizeMeters) {
        final List<Position> newPoints = new ArrayList<Position>();

        for (final PositionDTO point : points) {
            newPoints.add(new DegreePosition(point.latDeg, point.lngDeg));
        }

        return SimulatorServiceUtils.getIntermediatePoints(newPoints, stepSizeMeters);
    }

    /**
     * For every segment of two points in the given array, it computes the intermediate points given the certain step
     * size.
     */
    public static List<Position> getIntermediatePoints(final List<Position> points, final double stepSizeMeters) {

        final int noOfPoints = points.size();
        if (noOfPoints == 0) {
            return new ArrayList<Position>();
        } else if (noOfPoints == 1) {
            return points;
        } else if (noOfPoints == 2) {

            final Position startPoint = points.get(0);
            final Position endPoint = points.get(1);

            final double distance = getDistanceBetween(startPoint, endPoint);
            if (distance <= stepSizeMeters) {
                return points;
            } else {
                final List<Position> newPoints = getIntermediatePoints(startPoint, endPoint, stepSizeMeters);
                newPoints.add(endPoint);
                return newPoints;
            }
        }

        final List<Position> result = new ArrayList<Position>();

        for (int index = 0; index < noOfPoints; index++) {
            if (index == noOfPoints - 1) {
                break;
            }

            result.addAll(getIntermediatePoints(points.get(index), points.get(index + 1), stepSizeMeters));
        }

        result.add(points.get(noOfPoints - 1));

        return result;

    }

    /**
     * Computes the time required to travel from the start point to the end point with the specified speed.
     */
    public static double getTimeSeconds(final Position startPoint, final Position endPoint, final double endSpeedMetersPerSecond) {

        final double distance = getDistanceBetween(startPoint, endPoint);

        if (distance == 0.0) {
            return 0.0;
        }

        return distance / endSpeedMetersPerSecond;
    }

    /**
     * Returns the total distance between the path composed of the given points
     */
    public static double getTotalDistanceMeters(final Position[] points) {

        final int noOfPositions = points.length;

        if (noOfPositions == 0 || noOfPositions == 1) {
            return 0.0;
        }

        double result = 0.0;

        for (int index = 0; index < noOfPositions; index++) {

            if (index == noOfPositions - 1) {
                break;
            }

            result += getDistanceBetween(points[index], points[index + 1]);
        }

        return result;
    }

    /**
     * Returns the distance from this point to the supplied point, in meters (using Haversine formula).
     */
    public static double getDistanceBetween(final Position startPoint, final Position endPoint) {

        final double lat1 = startPoint.getLatRad();
        final double lon1 = startPoint.getLngRad();

        final double lat2 = endPoint.getLatRad();
        final double lon2 = endPoint.getLngRad();

        final double dLat = lat2 - lat1;
        final double dLon = lon2 - lon1;

        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double d = EARTH_RADIUS_METERS * c;

        return d;
    }

    /**
     * Returns the initial bearing from the start point to the end point in degrees
     */
    public static double getInitialBearing(final Position startPoint, final Position endPoint) {

        final double lat1 = startPoint.getLatRad();
        final double lat2 = endPoint.getLatRad();

        final double dLon = endPoint.getLngRad() - startPoint.getLngRad();

        final double y = Math.sin(dLon) * Math.cos(lat2);
        final double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        final double bearing = Math.atan2(y, x);

        return (radiansToDegrees(bearing) + 360) % 360;
    }

    /**
     * Returns final bearing arriving at the end point from the start point; The final bearing will differ from the
     * initial bearing by varying degrees according to distance and latitude
     */
    public static double getFinalBearing(final Position startPoint, final Position endPoint) {

        final double lat1 = startPoint.getLatRad();
        final double lat2 = endPoint.getLatRad();

        final double dLon = endPoint.getLngRad() - startPoint.getLngRad();

        final double y = Math.sin(dLon) * Math.cos(lat2);
        final double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        final double bearing = Math.atan2(y, x);

        return (radiansToDegrees(bearing) + 180) % 360;
    }

    /**
     * Returns the midpoint between this point and the supplied point.
     */
    public static Position getMidpointBetween(final Position startPoint, final Position endPoint) {

        final double lat1 = startPoint.getLatRad();
        final double lon1 = startPoint.getLngRad();
        final double lat2 = endPoint.getLatRad();

        final double dLon = endPoint.getLngRad() - lon1;

        final double Bx = Math.cos(lat2) * Math.cos(dLon);
        final double By = Math.cos(lat2) * Math.sin(dLon);

        final double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
        lon3 = (lon3 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return new RadianPosition(lat3, lon3);
    }

    /**
     * Returns the destination point from this point having travelled the given distance, in meters on the given initial
     * bearing (bearing may vary before destination is reached)
     */
    public static Position getDestinationPoint(final Position startPoint, final double bearingDegrees, final double distanceMeters) {

        final double distance = distanceMeters / EARTH_RADIUS_METERS;
        final double bearing = degreesToRadians(bearingDegrees);
        final double lat1 = startPoint.getLatRad();
        final double lon1 = startPoint.getLngRad();

        final double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance) + Math.cos(lat1) * Math.sin(distance) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance) * Math.cos(lat1), Math.cos(distance) - Math.sin(lat1) * Math.sin(lat2));
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return new RadianPosition(lat2, lon2);
    }
}