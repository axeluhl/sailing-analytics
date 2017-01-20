package com.sap.sailing.grib.impl;

import java.io.IOException;
import java.util.Optional;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.grib.GribWindField;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.time.CalendarDate;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Wraps a {@link GridDataset GRIB data set} that either has the "Wind direction" parameter #31 and the
 * "Wind speed" parameter #32, or the "u-component of wind" parameter #33 and the "v-component of wind"
 * parameter #34. If the GRIB source has wind for more than one vertical layer/level (z-axis), only the
 * first one is used.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractGribWindFieldImpl implements GribWindField {
    private final FeatureDataset dataSet;
    
    private final Weigher<TimePoint> timeConfidenceWeigher;
    
    /**
     * The base confidence of the wind data coming from the underlying {@link #dataSet}, between 0 (not confident at
     * all) and 1 (really confident observation at this point in time)
     */
    private final double baseConfidence;

    public AbstractGribWindFieldImpl(FeatureDataset dataSet, double baseConfidence) {
        this.dataSet = dataSet;
        this.baseConfidence = baseConfidence;
        timeConfidenceWeigher = ConfidenceFactory.INSTANCE.createExponentialTimeDifferenceWeigher(
                // use a minimum confidence to avoid the bearing to flip to 270deg in case all is zero
                Duration.ONE_SECOND.times(30).asMillis(), /* minimum confidence */0.0000000001);
    }
    
    protected static boolean hasVariable(FeatureDataset dataSet, int variableId) {
        for (VariableSimpleIF variable : dataSet.getDataVariables()) {
            Optional<Integer> id = getVariableId(variable);
            if (id.isPresent() && id.get() == variableId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtains the {@code Grib1_Parameter} value which gives the ID of the variable, such as 33 for the
     * "u-component of wind" variable
     * 
     * @return an optional {@link Integer} that, if present, represents the {@code Grib1_Parameter} value for the
     *         variable
     */
    protected static Optional<Integer> getVariableId(VariableSimpleIF variable) {
        final Optional<Integer> result;
        final Attribute idVariable = variable.findAttributeIgnoreCase("Grib1_Parameter");
        if (idVariable != null) {
            result = Optional.of(idVariable.getNumericValue().intValue());
        } else {
            result = Optional.empty();
        }
        return result;
    }
    
    protected double getBaseConfidence() {
        return baseConfidence;
    }

    @Override
    public Bounds getBounds() {
        return toBounds(getDataSet().getBoundingBox());
    }
    
    protected TimeRange getTimeRange() {
        return new TimeRangeImpl(getStartTime(), getEndTime());
    }

    /**
     * Based on the time difference to the {@link #getTimeRange() time range} of this GRIB data set computes
     * a confidence value. If within the time range, the confidence value is 1. Outside the time range a
     * weigher is used that reduces the confidence accordingly, like for other wind sources.
     */
    protected double getTimeConfidence(TimePoint timePoint) {
        final double result;
        final TimeRange timeRange = getTimeRange();
        if (timeRange.includes(timePoint)) {
            result = 1.0;
        } else if (timeRange.startsAfter(timePoint)) {
            result = timeConfidenceWeigher.getConfidence(timeRange.from(), timePoint);
        } else {
            assert timeRange.endsBefore(timePoint);
            result = timeConfidenceWeigher.getConfidence(timeRange.to(), timePoint);
        }
        return result;
    }
    
    protected double getTimeConfidence(TimePoint request, TimePoint fact) {
        return timeConfidenceWeigher.getConfidence(request, fact);
    }
    
    protected TimePoint toTimePoint(CalendarDate calendarDate) {
        return new MillisecondsTimePoint(calendarDate.getMillis());
    }

    protected TimePoint getStartTime() {
        return toTimePoint(getDataSet().getCalendarDateStart());
    }

    private TimePoint getEndTime() {
        return toTimePoint(getDataSet().getCalendarDateEnd());
    }

    private Bounds toBounds(LatLonRect boundingBox) {
        return new BoundsImpl(toPosition(boundingBox.getLowerLeftPoint()), toPosition(boundingBox.getUpperRightPoint()));
    }

    private Position toPosition(LatLonPoint point) {
        return new DegreePosition(point.getLatitude(), point.getLongitude());
    }

    protected FeatureDataset getDataSet() {
        return dataSet;
    }
    
    /**
     * Looks up a value in {@code grid} for the given time point and position.
     * 
     * @param timePoint
     *            may be outside of {@link #getTimeRange()}; in any case, the value time-wise closes to the time point
     *            requested is chosen
     * @param position
     *            may be outside of {@link #getBounds()}; in any case, the value at the position closest to
     *            {@code position} will be chosen
     * @return a non-interpolated, non-extrapolated value taken at the grid point that is time-wise and position-wise
     *         closes to those co-ordinates requested; as a triple whose first component is the value itself, the second
     *         component is the grid time point at which the value was taken, and the third component is the grid
     *         position where the value was obtained
     */
    protected Triple<Double, TimePoint, Position> getValue(GridDatatype grid, TimePoint timePoint, Position position) throws IOException {
        GridCoordSystem coordinateSystem = grid.getCoordinateSystem();
        int[] xy = coordinateSystem.findXYindexFromLatLon(position.getLatDeg(), position.getLngDeg(), new int[2]);
        Dimension timeDimension = grid.getTimeDimension();
        final int timeIndex;
        final TimePoint responseTimePoint;
        if (timeDimension != null) {
            final int numberOfTimepointsInGrid = timeDimension.getLength();
            if (numberOfTimepointsInGrid == 1) {
                timeIndex = 0;
                responseTimePoint = getStartTime();
            } else {
                final Duration timeBetweenSamples = getTimeRange().getDuration().divide(numberOfTimepointsInGrid);
                final Duration requestedDurationAfterStart = getStartTime().until(timePoint);
                final double steps = requestedDurationAfterStart.divide(timeBetweenSamples);
                if (steps < 0) {
                    timeIndex = 0;
                    responseTimePoint = getStartTime();
                } else if (steps > numberOfTimepointsInGrid-1) {
                    timeIndex = numberOfTimepointsInGrid-1;
                    responseTimePoint = getEndTime();
                } else {
                    timeIndex = (int) Math.round(steps);
                    responseTimePoint = getStartTime().plus(timeBetweenSamples.times(timeIndex));
                }
            }
        } else {
            timeIndex = -1; // time index doesn't matter if no time dimension exists for the grid
            responseTimePoint = getStartTime();
        }
        final Array arrayForTimePointBefore = grid.readDataSlice(timeIndex, /* z_index */ 0, xy[1], xy[0]);
        final Position responsePosition = toPosition(coordinateSystem.getLatLon(xy[0], xy[1]));
        return new Triple<Double, TimePoint, Position>((double) arrayForTimePointBefore.getFloat(0), responseTimePoint, responsePosition);
    }

    protected Optional<String> getUnit(VariableDS variable) {
        final Optional<String> result;
        final ucar.nc2.Attribute attribute = variable.findAttribute("units");
        if (attribute != null) {
            result = Optional.of(attribute.getStringValue());
        } else {
            result = Optional.empty();
        }
        return result;
    }

}
