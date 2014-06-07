package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.impl.CentralAngleDistance;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.impl.RadianPosition;

public class AbstractPosition implements Position {
    private static final long serialVersionUID = -3057027562787541064L;

    public int hashCode() {
        return (int) (4711. * getLngRad() * getLatRad());
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            return o instanceof Position && getLatRad() == ((Position) o).getLatRad()
                    && getLngRad() == ((Position) o).getLngRad();
        }
    }

    @Override
    public double getLngRad() {
        return getLngDeg() / 180. * Math.PI;
    }

    @Override
    public double getLatRad() {
        return getLatDeg() / 180. * Math.PI;
    }

    @Override
    public double getLatDeg() {
        return getLatRad() / Math.PI * 180.;
    }

    @Override
    public double getLngDeg() {
        return getLngRad() / Math.PI * 180.;
    }

    @Override
    public double getCentralAngleRad(Position p) {
        // Sinnott:
        double dLat = p.getLatRad() - getLatRad();
        double dLon = p.getLngRad() - getLngRad();
        double a = Math.sin(dLat / 2.) * Math.sin(dLat / 2.) + Math.cos(getLatRad()) * Math.cos(p.getLatRad())
                * Math.sin(dLon / 2.) * Math.sin(dLon / 2.);
        return 2. * Math.atan2(Math.sqrt(a), Math.sqrt(1. - a));
        // Spherical Law of Cosines; simpler formula, but doesn't work well for very small distances
        // return Math.acos(Math.sin(getLatRad()) * Math.sin(p.getLatRad())
        // + Math.cos(getLatRad()) * Math.cos(p.getLatRad())
        // * Math.cos(p.getLngRad() - getLngRad()));
    }

    @Override
    public Distance getDistance(Position p) {
        final Distance result;
        if (p == this || this.equals(p)) {
            result = Distance.NULL;
        } else {
            result = new CentralAngleDistance(getCentralAngleRad(p));
        }
        return result;
    }

    @Override
    public Bearing getBearingGreatCircle(Position p) {
        Bearing bearing = null;
        if (p != null) {
            double result = Math.atan2(Math.sin(p.getLngRad() - getLngRad()) * Math.cos(p.getLatRad()),
                    Math.cos(getLatRad()) * Math.sin(p.getLatRad()) - Math.sin(getLatRad()) * Math.cos(p.getLatRad())
                            * Math.cos(p.getLngRad() - getLngRad()));
            if (result < 0) {
                result = result + 2 * Math.PI;
            }
            bearing = new RadianBearingImpl(result);
        }
        return bearing;
    }

    @Override
    public Position translateRhumb(Bearing bearing, Distance distance) {
        /*
         * This algorithm is limited to distances such that dlon < pi/2, i.e those that extend around less than one
         * quarter of the circumference of the earth in longitude. A completely general, but more complicated algorithm
         * is necessary if greater distances are allowed.
         */
        double distanceRad = distance.getKilometers() / 6371.0; // r = 6371 means earth's radius in km
        double lat1 = getLatRad();
        double lon1 = getLngRad();
        double bearingRad = bearing.getRadians();

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distanceRad) + Math.cos(lat1) * Math.sin(distanceRad)
                * Math.cos(bearingRad));
        double lon2 = lon1
                + Math.atan2(Math.sin(bearingRad) * Math.sin(distanceRad) * Math.cos(lat1), Math.cos(distanceRad)
                        - Math.sin(lat1) * Math.sin(lat2));
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI; // normalize to -180..+180

        return new DegreePosition(lat2 / Math.PI * 180., lon2 / Math.PI * 180.);
    }

    @Override
    public Position translateGreatCircle(Bearing bearing, Distance distance) {
        double lat = Math.asin(Math.sin(getLatRad()) * Math.cos(distance.getCentralAngleRad()) + Math.cos(getLatRad())
                * Math.sin(distance.getCentralAngleRad()) * Math.cos(bearing.getRadians()));
        double lng = getLngRad()
                + Math.atan2(
                        Math.sin(bearing.getRadians()) * Math.sin(distance.getCentralAngleRad())
                                * Math.cos(getLatRad()),
                        Math.cos(distance.getCentralAngleRad()) - Math.sin(getLatRad()) * Math.sin(lat));
        return new RadianPosition(lat, lng);
    }

    @Override
    public Distance absoluteCrossTrackError(Position p, Bearing bearing) {
        return new CentralAngleDistance(Math.abs(crossTrackError(p, bearing).getCentralAngleRad()));
    }
    
    @Override
    public Distance crossTrackError(Position p, Bearing bearing) {
        return new CentralAngleDistance(Math.asin(Math.sin(p.getCentralAngleRad(this))
                * Math.sin(p.getBearingGreatCircle(this).getRadians() - bearing.getRadians())));
    }

    @Override
    public Position projectToLineThrough(Position pos, Bearing bearing) {
        return pos.translateGreatCircle(bearing, this.alongTrackDistance(pos, bearing));
    }

    @Override
    public Distance alongTrackDistance(Position from, Bearing bearing) {
        double direction = Math.signum(Math.cos(from.getBearingGreatCircle(this).getRadians() - bearing.getRadians()));
        // Test if denominator gets ridiculously small; if so, the cross-track error is about 90� central angle.
        // This means that the cross-track error is maximized, and that there is no way to determine how far along
        // the great circle described by pos2 and bearing we should travel. This is an exception which will
        // surface as a division-by-zero exception or a NaN result
        return new CentralAngleDistance(direction
                * Math.acos(Math.cos(from.getCentralAngleRad(this))
                        / Math.cos(crossTrackError(from, bearing).getCentralAngleRad())));
    }

    @Override
    public Distance getDistanceToLine(Position left, Position right) {
        final Distance result;
        final int factor = this.crossTrackError(left, left.getBearingGreatCircle(right)).getMeters()>0?1:-1;
        double toLeft = Math.abs(left.getBearingGreatCircle(this).getDifferenceTo(left.getBearingGreatCircle(right))
                .getDegrees());
        double toRight = Math.abs(right.getBearingGreatCircle(this).getDifferenceTo(right.getBearingGreatCircle(left))
                .getDegrees());
        if (toLeft > 90) {
            result = this.getDistance(left).scale(factor);
        } else if (toRight > 90) {
                result = this.getDistance(right).scale(factor);
            } else {
                result = this.crossTrackError(left, left.getBearingGreatCircle(right));
        }
        return result;
    }

    @Override
    public String toString() {
        return "(" + getLatDeg() + "," + getLngDeg() + ")";
    }

}
