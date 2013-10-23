package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;

public class DeviceConfigurationDataHandler extends DataHandler<DeviceConfiguration> {

    public DeviceConfigurationDataHandler(OnlineDataManager manager) {
        super(manager);
    }
    
    @Override
    public boolean hasCachedResults() {
        return manager.getDataStore().getTabletConfiguration() != null;
    }
    
    @Override
    public DeviceConfiguration getCachedResults() {
        return manager.getDataStore().getTabletConfiguration();
    }

    @Override
    public void onResult(DeviceConfiguration data) {
        manager.getDataStore().setTabletConfiguration(data);
    }

}
