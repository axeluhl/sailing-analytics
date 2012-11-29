package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.maps.client.geom.LatLng;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;

public class PathPolylineUtils {

    public static final double EARTH_RADIUS_METERS = 6378137;

    public static double convertFromDegToRad(final double noOfDegrees) {
        return (noOfDegrees * 0.0174532925);
    }

    public static double convertFromRadToDeg(final double noOfRadians) {
        return (noOfRadians * 57.2957795);
    }

    public static double convertFromKnotsToMPS(final double knots) {
        return knots * 0.51444;
    }

    public static SpeedWithBearingDTO computeAverageWindSpeed(final List<WindDTO> windDTOs, final boolean useDampenedValues) {

        final int count = windDTOs.size();

        double sumOfProductOfSpeedAndCosBearing = 0.0;
        double sumOfProductOfSpeedAndSinBearing = 0.0;
        double windSpeedKnots = 0.0;
        double windBearingDegrees = 0.0;
        double windBearingRadians = 0.0;

        for (final WindDTO windDTO : windDTOs) {

            windSpeedKnots = useDampenedValues ? windDTO.dampenedTrueWindSpeedInKnots : windDTO.trueWindSpeedInKnots;
            windBearingDegrees = useDampenedValues ? windDTO.dampenedTrueWindBearingDeg : windDTO.trueWindBearingDeg;
            windBearingRadians = PathPolylineUtils.convertFromDegToRad(windBearingDegrees);

            sumOfProductOfSpeedAndCosBearing += (windSpeedKnots * Math.cos(windBearingRadians));
            sumOfProductOfSpeedAndSinBearing += (windSpeedKnots * Math.sin(windBearingRadians));
        }

        final double a = sumOfProductOfSpeedAndCosBearing / count;
        final double b = sumOfProductOfSpeedAndSinBearing / count;
        final double c = Math.atan(b / a);

        double averageBearing = 0.0;

        if (a == 0) {
            averageBearing = (b >= 0) ? 90 : 270;
        } else if (a < 0) { // 2nd Q : 3rd Q
            averageBearing = 180 + c;
        } else if (a > 0) {
            averageBearing = (b >= 0) ? c : 360 + c; // 1st Q : 4th Q
        }

        final double averageSpeed = Math.sqrt(a * a + b * b);

        return new SpeedWithBearingDTO(averageSpeed, averageBearing);
    }

    public static SpeedWithBearingDTO computeMinMaxAverageWindSpeed(final List<WindDTO> windDTOs, final boolean useDampenedValues) {

        WindDTO minWindDTO = windDTOs.get(0);
        WindDTO maxWindDTO = windDTOs.get(0);
        WindDTO currentWindDTO = null;
        final int count = windDTOs.size();

        for (int index = 1; index < count; index++) {

            currentWindDTO = windDTOs.get(index);

            if (useDampenedValues) {
                if (currentWindDTO.dampenedTrueWindSpeedInKnots < minWindDTO.dampenedTrueWindSpeedInKnots) {
                    minWindDTO = currentWindDTO;
                }

                if (currentWindDTO.dampenedTrueWindSpeedInKnots > maxWindDTO.dampenedTrueWindSpeedInKnots) {
                    maxWindDTO = currentWindDTO;
                }
            } else {
                if (currentWindDTO.trueWindSpeedInKnots < minWindDTO.trueWindSpeedInKnots) {
                    minWindDTO = currentWindDTO;
                }

                if (currentWindDTO.trueWindSpeedInKnots > maxWindDTO.trueWindSpeedInKnots) {
                    maxWindDTO = currentWindDTO;
                }
            }
        }

        final double speedInKnots = useDampenedValues ? ((minWindDTO.dampenedTrueWindSpeedInKnots + maxWindDTO.dampenedTrueWindSpeedInKnots) / 2)
                : ((minWindDTO.trueWindSpeedInKnots + maxWindDTO.trueWindSpeedInKnots) / 2);
        final double bearingInDegrees = useDampenedValues ? ((minWindDTO.dampenedTrueWindBearingDeg + maxWindDTO.dampenedTrueWindBearingDeg) / 2)
                : ((minWindDTO.trueWindBearingDeg + maxWindDTO.trueWindBearingDeg) / 2);

        return new SpeedWithBearingDTO(speedInKnots, bearingInDegrees);
    }

    public static List<LatLng> getIntermediatePoints(final LatLng startPoint, final LatLng endPoint, final double stepSizeMeters) {
        final List<LatLng> result = new ArrayList<LatLng>();

        final double distance = getDistanceBetween(startPoint, endPoint);
        final int noOfSteps = (int) (distance / stepSizeMeters);
        double bearing = getInitialBearing(startPoint, endPoint);

        LatLng temp = null;

        result.add(startPoint);
        for (int stepIndex = 1; stepIndex < noOfSteps; stepIndex++) {
            temp = getDestinationPoint(startPoint, bearing, stepSizeMeters * stepIndex);
            result.add(temp);
            bearing = getInitialBearing(startPoint, temp);
        }

        return result;
    }

    public static LatLng[] getIntermediatePoints(final LatLng[] points, final double stepSizeMeters) {

        final int noOfPoints = points.length;
        if (noOfPoints == 0) {
            return new LatLng[0];
        }
        else if (noOfPoints == 1) {
            return points;
        }
        else if (noOfPoints == 2) {

            final LatLng startPoint = points[0];
            final LatLng endPoint = points[1];

            final double distance = getDistanceBetween(startPoint, endPoint);
            if (distance <= stepSizeMeters) {
                return points;
            }
            else {
                final List<LatLng> newPoints = getIntermediatePoints(startPoint, endPoint, stepSizeMeters);
                newPoints.add(endPoint);
                return newPoints.toArray(new LatLng[0]);
            }
        }

        final List<LatLng> result = new ArrayList<LatLng>();

        for (int index = 0; index < noOfPoints; index++) {
            if (index == noOfPoints - 1) {
                break;
            }

            result.addAll(getIntermediatePoints(points[index], points[index + 1], stepSizeMeters));
        }

        result.add(points[noOfPoints - 1]);

        return result.toArray(new LatLng[0]);

    }

    public static double getTime(final LatLng startPoint, final double startSpeedMetersPerSecond, final LatLng endPoint,
            final double endSpeedMetersPerSecond) {

        final double distance = getDistanceBetween(startPoint, endPoint);

        if (distance == 0.0) {
            return 0.0;
        }



        return 2 * distance / (startSpeedMetersPerSecond + endSpeedMetersPerSecond);
    }

    /**
     * Returns the total distance between the path composed of the given points
     */
    public static double getTotalDistanceMeters(final LatLng[] points) {

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
    public static double getDistanceBetween(final LatLng startPoint, final LatLng endPoint) {

        final double lat1 = startPoint.getLatitudeRadians();
        final double lon1 = startPoint.getLongitudeRadians();

        final double lat2 = endPoint.getLatitudeRadians();
        final double lon2 = endPoint.getLongitudeRadians();

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
    public static double getInitialBearing(final LatLng startPoint, final LatLng endPoint) {

        final double lat1 = startPoint.getLatitudeRadians();
        final double lat2 = endPoint.getLatitudeRadians();

        final double dLon = endPoint.getLongitudeRadians() - startPoint.getLongitudeRadians();

        final double y = Math.sin(dLon) * Math.cos(lat2);
        final double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        final double bearing = Math.atan2(y, x);

        return (convertFromRadToDeg(bearing) + 360) % 360;
    }

    /**
     * Returns final bearing arriving at the end point from the start point; The final bearing will differ from the
     * initial bearing by varying degrees according to distance and latitude
     */
    public static double getFinalBearing(final LatLng startPoint, final LatLng endPoint) {

        final double lat1 = startPoint.getLatitudeRadians();
        final double lat2 = endPoint.getLatitudeRadians();

        final double dLon = endPoint.getLongitudeRadians() - startPoint.getLongitudeRadians();

        final double y = Math.sin(dLon) * Math.cos(lat2);
        final double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        final double bearing = Math.atan2(y, x);

        return (convertFromRadToDeg(bearing) + 180) % 360;
    }

    /**
     * Returns the midpoint between this point and the supplied point.
     */
    public static LatLng getMidpointBetween(final LatLng startPoint, final LatLng endPoint) {

        final double lat1 = startPoint.getLatitudeRadians();
        final double lon1 = startPoint.getLongitudeRadians();
        final double lat2 = endPoint.getLatitudeRadians();

        final double dLon = endPoint.getLongitudeRadians() - startPoint.getLongitudeRadians();

        final double Bx = Math.cos(lat2) * Math.cos(dLon);
        final double By = Math.cos(lat2) * Math.sin(dLon);

        final double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
        lon3 = (lon3 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return LatLng.newInstance(lat3, lon3);
    }

    /**
     * Returns the destination point from this point having travelled the given distance, in meters on the given initial
     * bearing (bearing may vary before destination is reached)
     */
    public static LatLng getDestinationPoint(final LatLng startPoint, final double bearingDegrees, final double distanceMeters) {

        final double distance = distanceMeters / EARTH_RADIUS_METERS;
        final double bearing = convertFromDegToRad(bearingDegrees);
        final double lat1 = startPoint.getLatitudeRadians();
        final double lon1 = startPoint.getLongitudeRadians();

        final double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance) + Math.cos(lat1) * Math.sin(distance) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance) * Math.cos(lat1), Math.cos(distance) - Math.sin(lat1) * Math.sin(lat2));
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return LatLng.newInstance(convertFromRadToDeg(lat2), convertFromRadToDeg(lon2));
    }
}
