package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;

public class MetadataUpdate<FixT extends Timed> {
    private final DeviceIdentifier device;
    private final Object dbDeviceId;
    private final int nrOfTotalFixes;
    private final TimeRange fixesTimeRange;
    private final FixT latestFix;

    public MetadataUpdate(DeviceIdentifier device, Object dbDeviceId, int nrOfTotalFixes, TimeRange fixesTimeRange,
            FixT latestFix) {
        super();
        this.device = device;
        this.dbDeviceId = dbDeviceId;
        this.nrOfTotalFixes = nrOfTotalFixes;
        this.fixesTimeRange = fixesTimeRange;
        this.latestFix = latestFix;
    }

    public DeviceIdentifier getDevice() {
        return device;
    }

    public Object getDbDeviceId() {
        return dbDeviceId;
    }

    public int getNrOfTotalFixes() {
        return nrOfTotalFixes;
    }

    public TimeRange getFixesTimeRange() {
        return fixesTimeRange;
    }

    public FixT getLatestFix() {
        return latestFix;
    }
    
    public MetadataUpdate<FixT> merge(MetadataUpdate<FixT> other) {
        final MetadataUpdate<FixT> result;
        if (other == null) {
            result = this;
        } else {
            if (!other.getDevice().equals(getDevice())) {
                throw new IllegalArgumentException("Can only merge metadata updates for the same device: "+getDevice()+" vs. "+other.getDevice());
            }
            result = new MetadataUpdate<FixT>(getDevice(), getDbDeviceId(),
                    getNrOfTotalFixes() + other.getNrOfTotalFixes(),
                    getFixesTimeRange().extend(other.getFixesTimeRange()),
                    other.getLatestFix());
        }
        return result;
    }

    @Override
    public String toString() {
        return "MetadataUpdate [device=" + device + ", dbDeviceId=" + dbDeviceId + ", nrOfTotalFixes=" + nrOfTotalFixes
                + ", fixesTimeRange=" + fixesTimeRange + ", latestFix=" + latestFix + "]";
    }
}
