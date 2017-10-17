package com.sap.sailing.domain.common.sensordata;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.SensorFix;

/**
 * Definition of column metadata used by {@link SensorFix} implementations to resolve columns in underlying
 * {@link DoubleVectorFix DoubleVectorFixes}.
 */
public interface ColumnMetadata {
    int getColumnIndex();
}
