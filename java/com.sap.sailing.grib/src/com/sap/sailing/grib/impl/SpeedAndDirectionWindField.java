package com.sap.sailing.grib.impl;

import java.io.IOException;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedWithDegreeBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;

public class SpeedAndDirectionWindField extends AbstractGribWindFieldImpl {
    private static final int WIND_DIRECTION_PARAMETER_ID = 31;
    private static final int WIND_DIRECTION_GRIB2_DISCIPLINE = 0;
    private static final int WIND_DIRECTION_GRIB2_PARAMETER_CATEGORY = 2;
    private static final int WIND_DIRECTION_GRIB2_PARAMETER_NUMBER = 0;

    private static final int WIND_SPEED_PARAMETER_ID = 32;
    private static final int WIND_SPEED_GRIB2_DISCIPLINE = 0;
    private static final int WIND_SPEED_GRIB2_PARAMETER_CATEGORY = 2;
    private static final int WIND_SPEED_GRIB2_PARAMETER_NUMBER = 1;
    
    private static final VariableSpecification windDirectionVariableSpecification =
            new CompositeVariableSpecification(new Grib1VariableSpecification(WIND_DIRECTION_PARAMETER_ID),
                                               new Grib2VariableSpecification(new int[] { WIND_DIRECTION_GRIB2_DISCIPLINE, WIND_DIRECTION_GRIB2_PARAMETER_CATEGORY, WIND_DIRECTION_GRIB2_PARAMETER_NUMBER }));

    private static final VariableSpecification windSpeedVariableSpecification =
            new CompositeVariableSpecification(new Grib1VariableSpecification(WIND_SPEED_PARAMETER_ID),
                                               new Grib2VariableSpecification(new int[] { WIND_SPEED_GRIB2_DISCIPLINE, WIND_SPEED_GRIB2_PARAMETER_CATEGORY, WIND_SPEED_GRIB2_PARAMETER_NUMBER }));

    public SpeedAndDirectionWindField(FeatureDataset... dataSets) {
        super(/* baseConfidence */ 0.5, dataSets);
    }

    @Override
    public WindWithConfidence<TimePoint> getWind(TimePoint timePoint, Position position) throws IOException {
        Triple<Double, TimePoint, Position> directionComponentInDegreesTrue = null;
        Triple<Double, TimePoint, Position> speedComponentInMetersPerSecond = null;
        for (final FeatureDataset dataSet : getDataSets()) {
            if (dataSet instanceof GridDataset) {
                for (final GridDatatype grid : ((GridDataset) dataSet).getGrids()) {
                    if (windDirectionVariableSpecification.matches(grid.getVariable())) {
                        assert isDegreesTrue(getUnit(grid.getVariable()).get());
                        directionComponentInDegreesTrue = getValue(grid, timePoint, position);
                    } else if (windSpeedVariableSpecification.matches(grid.getVariable())) {
                        assert isMetersPerSecond(getUnit(grid.getVariable()).get());
                        speedComponentInMetersPerSecond = getValue(grid, timePoint, position);
                    }
                    if (directionComponentInDegreesTrue != null && speedComponentInMetersPerSecond != null) {
                        break;
                    }
                }
            }
        }
        final Wind wind;
        final double confidence;
        if (directionComponentInDegreesTrue != null && speedComponentInMetersPerSecond != null) {
            confidence = getTimeConfidence(timePoint, directionComponentInDegreesTrue.getB());
            wind = new WindImpl(directionComponentInDegreesTrue.getC(), directionComponentInDegreesTrue.getB(),
                    new MeterPerSecondSpeedWithDegreeBearingImpl(speedComponentInMetersPerSecond.getA(),
                            // we're getting the "from" direction from the GRIB file and need to convert to "to" here
                            new DegreeBearingImpl(directionComponentInDegreesTrue.getA()).reverse()));
        } else {
            wind = null;
            confidence = 0;
        }
        return new WindWithConfidenceImpl<TimePoint>(wind, confidence*getBaseConfidence(), timePoint, /* useSpeed */ true);
    }

    /**
     * Checks whether the data set has a wind speed variable (GRIB parameter #32) and a wind direction variable
     * (GRIB parameter #31).
     */
    public static boolean handles(FeatureDataset... dataSets) {
        return hasVariable(windDirectionVariableSpecification, dataSets) && hasVariable(windSpeedVariableSpecification, dataSets);
    }

}
