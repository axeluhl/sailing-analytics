package com.sap.sailing.domain.racelog.tracking;

/**
 * This interface is used to report progress from the SensorStorages. The SensorStorage is responsible for proper throttling of events. 
 */
public interface ProgressCallback {
    public void progressChange(double progress);
}
