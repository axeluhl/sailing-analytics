package com.sap.sailing.racecommittee.app.domain.configuration;

import android.content.Context;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;

public interface ApplyableDeviceConfiguration extends DeviceConfiguration {
    void apply(Context context);
}
