package com.sap.sailing.grib.impl;

import java.io.IOException;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedWithDegreeBearingImpl;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
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
    private static final int WIND_SPEED_PARAMETER_ID = 32;

    public SpeedAndDirectionWindField(FeatureDataset dataSet) {
        super(dataSet, /* baseConfidence */ 0.5);
    }

    @Override
    public WindWithConfidence<TimePoint> getWind(TimePoint timePoint, Position position) throws IOException {
        FeatureDataset dataSet = getDataSet();
        final Wind wind;
        final double confidence;
        if (dataSet instanceof GridDataset) {
            Triple<Double, TimePoint, Position> directionComponentInDegreesTrue = null;
            Triple<Double, TimePoint, Position> speedComponentInMetersPerSecond = null;
            for (final GridDatatype grid : ((GridDataset) dataSet).getGrids()) {
                final Integer variableId = getVariableId(grid.getVariable()).orElse(-1);
                if (variableId == WIND_DIRECTION_PARAMETER_ID) {
                    assert getUnit(grid.getVariable()).get().equals("m/s");
                    directionComponentInDegreesTrue = getValue(grid, timePoint, position);
                } else if (variableId == WIND_SPEED_PARAMETER_ID) {
                    assert getUnit(grid.getVariable()).get().equals("m/s");
                    speedComponentInMetersPerSecond = getValue(grid, timePoint, position);
                }
                if (directionComponentInDegreesTrue != null && speedComponentInMetersPerSecond != null) {
                    break;
                }
            }
            confidence = getTimeConfidence(timePoint, directionComponentInDegreesTrue.getB());
            wind = new WindImpl(directionComponentInDegreesTrue.getC(), directionComponentInDegreesTrue.getB(),
                    new MeterPerSecondSpeedWithDegreeBearingImpl(speedComponentInMetersPerSecond.getA(),
                            new DegreeBearingImpl(directionComponentInDegreesTrue.getA())));
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
    public static boolean handles(FeatureDataset dataSet) {
        return hasVariable(dataSet, WIND_DIRECTION_PARAMETER_ID) && hasVariable(dataSet, WIND_SPEED_PARAMETER_ID);
    }

}
