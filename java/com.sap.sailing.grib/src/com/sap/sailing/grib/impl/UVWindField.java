package com.sap.sailing.grib.impl;

import java.io.IOException;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
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

public class UVWindField extends AbstractGribWindFieldImpl {
    private static final int U_COMPONENT_OF_WIND_PARAMETER_ID = 33;
    private static final int V_COMPONENT_OF_WIND_PARAMETER_ID = 34;
    
    public UVWindField(FeatureDataset dataSet) {
        super(dataSet, /* baseConfidence */ 0.5);
    }

    @Override
    public WindWithConfidence<TimePoint> getWind(TimePoint timePoint, Position position) throws IOException {
        FeatureDataset dataSet = getDataSet();
        final Wind wind;
        final double confidence;
        if (dataSet instanceof GridDataset) {
            Triple<Double, TimePoint, Position> uComponentInMetersPerSecond = null;
            Triple<Double, TimePoint, Position> vComponentInMetersPerSecond = null;
            for (final GridDatatype grid : ((GridDataset) dataSet).getGrids()) {
                final Integer variableId = getVariableId(grid.getVariable()).orElse(-1);
                if (variableId == U_COMPONENT_OF_WIND_PARAMETER_ID) {
                    assert getUnit(grid.getVariable()).get().equals("m/s");
                    uComponentInMetersPerSecond = getValue(grid, timePoint, position);
                } else if (variableId == V_COMPONENT_OF_WIND_PARAMETER_ID) {
                    assert getUnit(grid.getVariable()).get().equals("m/s");
                    vComponentInMetersPerSecond = getValue(grid, timePoint, position);
                }
                if (uComponentInMetersPerSecond != null && vComponentInMetersPerSecond != null) {
                    break;
                }
            }
            confidence = getTimeConfidence(timePoint, uComponentInMetersPerSecond.getB());
            final double atan2 = Math.atan2(uComponentInMetersPerSecond.getA(), vComponentInMetersPerSecond.getA());
            wind = new WindImpl(uComponentInMetersPerSecond.getC(), uComponentInMetersPerSecond.getB(),
                    new MeterPerSecondSpeedWithDegreeBearingImpl(Math.sqrt(uComponentInMetersPerSecond.getA()*uComponentInMetersPerSecond.getA()+
                            vComponentInMetersPerSecond.getA()*vComponentInMetersPerSecond.getA()),
                            new RadianBearingImpl(atan2>0 ? atan2 : 2*Math.PI+atan2).reverse()));
        } else {
            wind = null;
            confidence = 0;
        }
        return new WindWithConfidenceImpl<TimePoint>(wind, confidence*getBaseConfidence(), timePoint, /* useSpeed */ true);
    }

    /**
     * Checks whether the data set has a u-component of wind (GRIB parameter #33) and a v-component of wind
     * (GRIB parameter #34).
     */
    public static boolean handles(FeatureDataset dataSet) {
        return hasVariable(dataSet, U_COMPONENT_OF_WIND_PARAMETER_ID) && hasVariable(dataSet, V_COMPONENT_OF_WIND_PARAMETER_ID);
    }
}
