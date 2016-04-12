package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sse.common.WithID;

public interface DynamicSensorFixTrack<ItemType extends WithID & Serializable, FixT extends SensorFix> extends
        SensorFixTrack<ItemType, FixT>, DynamicTrack<FixT> {

}
