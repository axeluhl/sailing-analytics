package com.sap.sailing.domain.base.configuration;


public interface StoredDeviceConfiguration extends DeviceConfiguration, StoreableConfiguration<StoredDeviceConfiguration> {
    StoredRacingProceduresConfiguration getRacingProceduresConfiguration();
}
