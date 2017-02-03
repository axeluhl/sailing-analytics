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
    private static final int U_COMPONENT_OF_WIND_GRIB2_DISCIPLINE = 0;
    private static final int U_COMPONENT_OF_WIND_GRIB2_PARAMETER_CATEGORY = 2;
    private static final int U_COMPONENT_OF_WIND_GRIB2_PARAMETER_NUMBER = 2;

    private static final int V_COMPONENT_OF_WIND_PARAMETER_ID = 34;
    private static final int V_COMPONENT_OF_WIND_GRIB2_DISCIPLINE = 0;
    private static final int V_COMPONENT_OF_WIND_GRIB2_PARAMETER_CATEGORY = 2;
    private static final int V_COMPONENT_OF_WIND_GRIB2_PARAMETER_NUMBER = 3;
    
    private static final VariableSpecification uComponentOfWindVariableSpecification =
            new CompositeVariableSpecification(new Grib1VariableSpecification(U_COMPONENT_OF_WIND_PARAMETER_ID),
                                               new Grib2VariableSpecification(new int[] { U_COMPONENT_OF_WIND_GRIB2_DISCIPLINE, U_COMPONENT_OF_WIND_GRIB2_PARAMETER_CATEGORY, U_COMPONENT_OF_WIND_GRIB2_PARAMETER_NUMBER }));

    private static final VariableSpecification vComponentOfWindVariableSpecification =
            new CompositeVariableSpecification(new Grib1VariableSpecification(V_COMPONENT_OF_WIND_PARAMETER_ID),
                                               new Grib2VariableSpecification(new int[] { V_COMPONENT_OF_WIND_GRIB2_DISCIPLINE, V_COMPONENT_OF_WIND_GRIB2_PARAMETER_CATEGORY, V_COMPONENT_OF_WIND_GRIB2_PARAMETER_NUMBER }));

    public UVWindField(FeatureDataset... dataSets) {
        super(/* baseConfidence */ 0.5, dataSets);
    }

    @Override
    public WindWithConfidence<TimePoint> getWind(TimePoint timePoint, Position position) throws IOException {
        final Wind wind;
        final double confidence;
        Triple<Double, TimePoint, Position> uComponentInMetersPerSecond = null;
        Triple<Double, TimePoint, Position> vComponentInMetersPerSecond = null;
        for (final FeatureDataset dataSet : getDataSets()) {
            if (dataSet instanceof GridDataset) {
                for (final GridDatatype grid : ((GridDataset) dataSet).getGrids()) {
                    if (uComponentOfWindVariableSpecification.matches(grid.getVariable())) {
                        assert isMetersPerSecond(getUnit(grid.getVariable()).get());
                        uComponentInMetersPerSecond = getValue(grid, timePoint, position);
                    } else if (vComponentOfWindVariableSpecification.matches(grid.getVariable())) {
                        assert isMetersPerSecond(getUnit(grid.getVariable()).get());
                        vComponentInMetersPerSecond = getValue(grid, timePoint, position);
                    }
                    if (uComponentInMetersPerSecond != null && vComponentInMetersPerSecond != null) {
                        break;
                    }
                }
            }
        }
        if (uComponentInMetersPerSecond != null && vComponentInMetersPerSecond != null) {
            confidence = getTimeConfidence(timePoint, uComponentInMetersPerSecond.getB());
            final double atan2 = Math.atan2(uComponentInMetersPerSecond.getA(), vComponentInMetersPerSecond.getA());
            wind = new WindImpl(uComponentInMetersPerSecond.getC(), uComponentInMetersPerSecond.getB(),
                    new MeterPerSecondSpeedWithDegreeBearingImpl(Math.sqrt(uComponentInMetersPerSecond.getA()*uComponentInMetersPerSecond.getA()+
                            vComponentInMetersPerSecond.getA()*vComponentInMetersPerSecond.getA()),
                            new RadianBearingImpl(atan2>0 ? atan2 : 2*Math.PI+atan2)));
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
    public static boolean handles(FeatureDataset... dataSets) {
        return hasVariable(uComponentOfWindVariableSpecification, dataSets) && hasVariable(vComponentOfWindVariableSpecification, dataSets);
    }
}
