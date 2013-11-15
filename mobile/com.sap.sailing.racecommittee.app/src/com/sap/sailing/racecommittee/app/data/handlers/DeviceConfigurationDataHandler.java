package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.configuration.ApplyableDeviceConfiguration;

public class DeviceConfigurationDataHandler extends DataHandler<ApplyableDeviceConfiguration> {

    public DeviceConfigurationDataHandler(OnlineDataManager manager) {
        super(manager);
    }
    
    @Override
    public boolean hasCachedResults() {
        return manager.getDataStore().getTabletConfiguration() != null;
    }
    
    @Override
    public ApplyableDeviceConfiguration getCachedResults() {
        return manager.getDataStore().getTabletConfiguration();
    }

    @Override
    public void onResult(ApplyableDeviceConfiguration data) {
        manager.getDataStore().setTabletConfiguration(data);
    }

}
