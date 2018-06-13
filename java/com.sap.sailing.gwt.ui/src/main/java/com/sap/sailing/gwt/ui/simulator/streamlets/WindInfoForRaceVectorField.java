package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.confidence.BearingWithConfidenceCluster;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.common.confidence.impl.BearingWithConfidenceImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.simulator.streamlets.PositionDTOWeigher.AverageLatitudeProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * Implements the {@link VectorField} interface by providing real wind data from a <code>TrackedRace</code> which has
 * been received in the form of one or more {@link WindInfoForRaceDTO} objects. The field uses time and space distances
 * to weigh the wind measurements provided, leading to a spatially-resolved wind field that can be visualized. Its
 * bounds are infinite as time/space-weighed averages can generally be computed anywhere.
 * <p>
 * 
 * The vectors produced by this field are sized such that their x/y values represent 1/60th of a longitude/latitude
 * degree per hour which resembles knots (nautical miles per hour).
 * <p>
 * 
 * The maximum wind speed assumed by this wind field is 40kts which is used to calculate line widths and particle
 * speeds.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class WindInfoForRaceVectorField implements VectorField, AverageLatitudeProvider {
    private static final double MAX_WIND_SPEED_IN_KNOTS = 40;
    private final LatLngBounds infiniteBounds = LatLngBounds.newInstance(LatLng.newInstance(-90, -180), LatLng.newInstance(90, 180));
    private final Weigher<Position> weigher;
    private double averageLatitudeDeg;
    private double averageLatitudeCosine;
    private double knotsInDegreePerFrame;
    private final CoordinateSystem coordinateSystem;
    private boolean colored = false;
    
    private final Comparator<WindDTO> windByRequestTimePointComparator = new Comparator<WindDTO>() {
        @Override
        public int compare(WindDTO o1, WindDTO o2) {
            return o1.requestTimepoint > o2.requestTimepoint ? 1 : o1.requestTimepoint == o2.requestTimepoint ? 0 : -1;
        }
    };
    private final WindInfoForRaceDTO windInfoForRace;
    
    public WindInfoForRaceVectorField(WindInfoForRaceDTO windInfoForRace, double framesPerSecond, CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        this.windInfoForRace = windInfoForRace;
        this.knotsInDegreePerFrame = 1.0 / (60*3600) / framesPerSecond; // 1kn = 1/60 deg/h = 1/(60*3600) deg/s
        weigher = new PositionDTOWeigher(/* halfConfidenceDistance */new MeterDistance(100), this);
    }
    
    /**
     * Sets the average latitude used for the simplified approximating distance calculation. Until called, 0.0 is
     * assumed.
     */
    public void setAverageLatitudeDeg(double averageLatitudeDeg) {
        this.averageLatitudeDeg = averageLatitudeDeg;
        this.averageLatitudeCosine = Math.cos(averageLatitudeDeg/180.0*Math.PI);
    }
    
    @Override
    public double getAverageLatitudeDeg() {
        return averageLatitudeDeg;
    }
    
    @Override
    public double getCosineOfAverageLatitude() {
        return averageLatitudeCosine;
    }

    @Override
    public boolean inBounds(Position p) {
        // all positions are always considered in bounds as we'll always try to interpolate/extrapolate
        return true;
    }

    @Override
    public boolean inBounds(LatLng p) {
        // all positions are always considered in bounds as we'll always try to interpolate/extrapolate
        return true;
    }

    @Override
    public Vector getVector(final LatLng mappedPosition, final Date at) {
        final Position p = coordinateSystem.getPosition(mappedPosition);
        double speedConfidenceSum = 0;
        double knotSpeedSumScaledByConfidence = 0;
        final BearingWithConfidenceCluster<Position> bearingCluster = new BearingWithConfidenceCluster<>(weigher);
        for (final Entry<WindSource, WindTrackInfoDTO> windSourceAndWindTrack : windInfoForRace.windTrackInfoByWindSource.entrySet()) {
            /*if ((windSourceAndWindTrack.getKey().name().equals("RACECOMMITTEE"))||(windSourceAndWindTrack.getKey().name().equals("COURSE_BASED"))) {
                continue;
            }*/
           if (!Util.contains(windInfoForRace.windSourcesToExclude, windSourceAndWindTrack.getKey())) {
                WindDTO timewiseClosestFixForWindSource = getTimewiseClosestFix(windSourceAndWindTrack.getValue(), at);
                if (timewiseClosestFixForWindSource != null) {
                    final double confidence = (timewiseClosestFixForWindSource.confidence == null ? 1 : timewiseClosestFixForWindSource.confidence) *
                            weigher.getConfidence(timewiseClosestFixForWindSource.position, p);
                    if (windSourceAndWindTrack.getKey().getType().useSpeed()) {
                        speedConfidenceSum += confidence;
                        knotSpeedSumScaledByConfidence += confidence * timewiseClosestFixForWindSource.dampenedTrueWindSpeedInKnots;
                    }
                    bearingCluster.add(new BearingWithConfidenceImpl<Position>(
                            new DegreeBearingImpl(timewiseClosestFixForWindSource.dampenedTrueWindBearingDeg), confidence, timewiseClosestFixForWindSource.position));
                }
            }
        }
        final BearingWithConfidence<Position> bearing = bearingCluster.getAverage(p);
        final Vector result;
        if (bearing != null && bearing.getObject() != null) {
            final double mappedBearingRad = coordinateSystem.map(bearing.getObject()).getRadians();
            final double speedInKnots = knotSpeedSumScaledByConfidence / speedConfidenceSum;
            result = new Vector(speedInKnots * Math.sin(mappedBearingRad), speedInKnots * Math.cos(mappedBearingRad));
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Regular server-side wind tracks will deliver fixes for queries with a time that is far from any
     * of the fixes' time point. A respectively reduced confidence will be the result. However, some wind
     * track types such as that for the wind estimation may not deliver a fix, not even with low confidence,
     * if at the requested time point (give or take some rounding to a time tick resolution) no fix can be
     * provided. In this case, the server will not use any value from such a track when computing an average
     * including fixes from several tracks.<p>
     * 
     * To ensure consistent behavior on the client, this method will check if the wind track is marked
     * accordingly and will return <code>null</code> if the track cannot provide a fix within its resolution
     * around the requested time point.
     */
    private WindDTO getTimewiseClosestFix(WindTrackInfoDTO windTrackInfo, Date at) {
        List<WindDTO> windFixes = windTrackInfo.windFixes;
        final WindDTO preResult;
        if (windFixes == null || windFixes.isEmpty()) {
            preResult = null;
        } else {
            final WindDTO atDummy = new WindDTO();
            atDummy.requestTimepoint = at.getTime();
            int pos = Collections.binarySearch(windFixes, atDummy, windByRequestTimePointComparator);
            if (pos < 0) {
                pos = (-pos) - 1; // now pos points at the insertion point
                if (pos == 0
                        || (pos < windFixes.size() &&
                            Math.abs(windFixes.get(pos).requestTimepoint - at.getTime()) < Math.abs(windFixes
                                .get(pos - 1).requestTimepoint - at.getTime()))) {
                    preResult = windFixes.get(pos);
                } else {
                    // pos doesn't point to the first element, nor is the element at pos time-wise closer than the element at pos-1
                    // or pos points beyond the end of the list
                    preResult = windFixes.get(pos-1);
                }
            } else {
                preResult = windFixes.get(pos);
            }
        }
        final WindDTO result;
        // don't return wind fixes if according to the wind track there is a resolution to which times are rounded
        // and where null is returned when there is no fix at the rounded time point; mimic this behavior here. See
        // also bug 2689 comment #13.
        if (windTrackInfo.resolutionOutsideOfWhichNoFixWillBeReturned != null) {
            if (preResult == null || Math.abs(preResult.requestTimepoint-at.getTime()) > windTrackInfo.resolutionOutsideOfWhichNoFixWillBeReturned.asMillis()) {
                result = null;
            } else {
                result = preResult;
            }
        } else {
            result = preResult;
        }
        return result;
    }

    @Override
    public double getMotionScale(int zoomLevel) {
        // This implementation is copied from SimulatorField, hoping it does something useful in combination with
        // the Swarm implementation.
        return 2.0 * knotsInDegreePerFrame * Math.pow(1.8, Math.min(15.0, 20.0 - zoomLevel)); 
    }
    
    @Override
    public double getParticleWeight(LatLng p, Vector v) {
        return v == null ? 0 : (v.length() / MAX_WIND_SPEED_IN_KNOTS);
    }

    /**
     * The implementation uses 3 coordinate space units (usually mapping to pixels if no zoom or other transformation is
     * in place for the Context2d canvas) for {@link #MAX_WIND_SPEED_IN_KNOTS}, decreasing linearly to 0 for zero speeds.
     */
    @Override
    public double getLineWidth(double speed) {
        return 1.2;
    }

    @Override
    public LatLngBounds getFieldCorners() {
        return infiniteBounds;
    }

    @Override
    public double getParticleFactor() {
        return 0.5;
    }

    public void setColors(boolean isColored) {
        this.colored = isColored;
    }

    public boolean getColors() {
        return this.colored;
    }

    /**
     * {@link #MAX_WIND_SPEED_IN_KNOTS maximum wind speed} is fully opaque; zero speed is fully transparent
     */
    @Override
    public String getColor(double speed) {
        if (this.colored) {
            double h;
            if (speed <= 4.0) {
                h = 240.0;
            } else if (speed <= 12.0) {
                h = 120.0 + (12.0 - speed) / (12.0 - 4.0) * (240.0 - 120.0);
            } else if (speed <= 20.0) {
                h = 0.0 + (20.0 - speed) / (20.0 - 12.0) * (120.0 - 0.0);
            } else {
                h = 0.0;
            }
            return "hsl(" + Math.round(h) + ", 100%, 50%)";
        } else {
            return "rgba(255,255,255," + Math.min(1.0, 0.5 + 0.6 * speed / MAX_WIND_SPEED_IN_KNOTS) + ")";
        }
    }

}
