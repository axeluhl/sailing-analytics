package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.impl.CentralAngleDistance;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.impl.RadianPosition;

public class AbstractPosition implements Position {

    private static final double TRESHOLD_DEGREES = 15;
    private static final long serialVersionUID = -3057027562787541064L;

    @Override
    public int hashCode() {
        return (int) (4711. * getLngRad() * getLatRad());
    }

    @Override
    public boolean equals(final Object o) {
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
    public double getCentralAngleRad(final Position p) {
        // Sinnott:
        final double dLat = p.getLatRad()-getLatRad();
        final double dLon = p.getLngRad()-getLngRad();
        final double a = Math.sin(dLat/2.) * Math.sin(dLat/2.) +
                Math.cos(getLatRad()) * Math.cos(p.getLatRad()) *
                Math.sin(dLon/2.) * Math.sin(dLon/2.);
        return 2. * Math.atan2(Math.sqrt(a), Math.sqrt(1.-a));
        // Spherical Law of Cosines; simpler formula, but doesn't work well for very small distances
        //        return Math.acos(Math.sin(getLatRad()) * Math.sin(p.getLatRad())
        //                + Math.cos(getLatRad()) * Math.cos(p.getLatRad())
        //                * Math.cos(p.getLngRad() - getLngRad()));
    }

    @Override
    public Distance getDistance(final Position p) {
        return new CentralAngleDistance(getCentralAngleRad(p));
    }

    @Override
    public Bearing getBearingGreatCircle(final Position p) {
        double result = Math.atan2(Math.sin(p.getLngRad()-getLngRad()) * Math.cos(p.getLatRad()),
                Math.cos(getLatRad())*Math.sin(p.getLatRad()) -
                Math.sin(getLatRad())*Math.cos(p.getLatRad())*Math.cos(p.getLngRad()-getLngRad())) ;
        if (result < 0) {
            result = result + 2*Math.PI;
        }
        return new RadianBearingImpl(result);
    }

    @Override
    public Position translateRhumb(final Bearing bearing, final Distance distance) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Position translateGreatCircle(final Bearing bearing, final Distance distance) {
        final double lat = Math.asin(Math.sin(getLatRad())
                * Math.cos(distance.getCentralAngleRad())
                + Math.cos(getLatRad())
                * Math.sin(distance.getCentralAngleRad())
                * Math.cos(bearing.getRadians()));
        final double lng = getLngRad()
                + Math.atan2(
                        Math.sin(bearing.getRadians())
                        * Math.sin(distance.getCentralAngleRad())
                        * Math.cos(getLatRad()),
                        Math.cos(distance.getCentralAngleRad())
                        - Math.sin(getLatRad()) * Math.sin(lat));
        return new RadianPosition(lat, lng);
    }

    @Override
    public Distance crossTrackError(final Position pos2, final Bearing bearing) {
        return new CentralAngleDistance(Math.abs(Math.asin(Math.sin(pos2.getCentralAngleRad(this))
                * Math.sin(pos2.getBearingGreatCircle(this).getRadians() - bearing.getRadians()))));
    }

    @Override
    public Position projectToLineThrough(final Position pos, final Bearing bearing) {
        return pos.translateGreatCircle(bearing, this.alongTrackDistance(pos, bearing));
    }

    @Override
    public Distance alongTrackDistance(final Position pos2, final Bearing bearing) {
        final double direction = Math.signum(Math.cos(pos2.getBearingGreatCircle(this).getRadians() - bearing.getRadians()));
        // Test if denominator gets ridiculously small; if so, the cross-track error is about 90° central angle.
        // This means that the cross-track error is maximized, and that there is no way to determine how far along
        // the great circle described by pos2 and bearing we should travel. This is an exception which will
        // surface as a division-by-zero exception or a NaN result
        return new CentralAngleDistance(direction * Math.acos(Math.cos(pos2.getCentralAngleRad(this))
                / Math.cos(crossTrackError(pos2, bearing).getCentralAngleRad())));
    }

    @Override
    public String toString() {
        return "("+getLatDeg()+","+getLngDeg()+")";
    }

    @Override
    public boolean isTurn(final Position previousPoint, final Position nextPoint) {
        final Bearing b1 = previousPoint.getBearingGreatCircle(this);
        final Bearing b2 = this.getBearingGreatCircle(nextPoint);
        double diff = b1.getDifferenceTo(b2).getDegrees();

        if (diff < 0) {
            diff = 360 + diff;
        }

        return !((diff >= 0 && diff <= TRESHOLD_DEGREES) || (diff >= (180 - TRESHOLD_DEGREES) && diff <= (180 + TRESHOLD_DEGREES)) || (diff >= (360 - TRESHOLD_DEGREES) && diff <= 360));
    }
}
