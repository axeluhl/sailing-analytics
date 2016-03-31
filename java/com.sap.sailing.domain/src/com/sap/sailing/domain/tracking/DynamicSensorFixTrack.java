package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.tracking.SensorFix;

public interface DynamicSensorFixTrack<FixT extends SensorFix> extends SensorFixTrack<FixT>, DynamicTrack<FixT> {

}
