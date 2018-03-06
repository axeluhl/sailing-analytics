package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.sensordata.ColumnMetadata;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sse.common.TimePoint;

/**
 * Abstract implementation of {@link SensorFix} that implements the generic value access through
 * {@link SensorFixImpl#get(String)} by resolving a {@link ColumnMetadata} instance for a given valueName. The data is
 * taken from the underlying {@link DoubleVectorFix} by using the column index taken from the resolved
 * {@link ColumnMetadata} instance.
 */
public abstract class SensorFixImpl implements SensorFix {
    private static final long serialVersionUID = -5815764620564953478L;
    protected final DoubleVectorFix fix;

    public SensorFixImpl(DoubleVectorFix fix) {
        super();
        assert fix != null;
        this.fix = fix;
    }

    @Override
    public TimePoint getTimePoint() {
        return fix.getTimePoint();
    }

    @Override
    public Double[] get() {
        Double[] result = new Double[fix.get().length];
        System.arraycopy(fix.get(), 0, result, 0, result.length);
        return result;
    }

    @Override
    public double get(String valueName) {
        final ColumnMetadata columnMetadata = resolveMetadataFromValueName(valueName);
        if (columnMetadata == null) {
            throw new IllegalArgumentException("Unknown value \"" + valueName + "\" for " + getClass().getSimpleName());
        }
        int index = columnMetadata.getColumnIndex();
        return fix.get(index);
    }

    /**
     * To be implemented by subclasses to resolve the associated {@link ColumnMetadata} for a given valueName. The
     * result may be null if the given valueName is unknown for the fix implementation.
     */
    protected abstract ColumnMetadata resolveMetadataFromValueName(String valueName);
}
