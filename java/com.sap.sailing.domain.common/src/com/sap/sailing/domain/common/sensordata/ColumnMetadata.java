package com.sap.sailing.domain.common.sensordata;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.SensorFix;

/**
 * Definition of column metadata used by {@link SensorFix} implementations to resolve columns in underlying
 * {@link DoubleVectorFix DoubleVectorFixes}. Once data has been stored in a sensor store according to such a
 * specification, the order of literals in implementing {@code enum} types must remain constant, and no literals must be
 * deleted anymore because the ordinal is used to determine the index into the {@link DoubleVectorFix}.
 */
public interface ColumnMetadata {
    int getColumnIndex();
}
