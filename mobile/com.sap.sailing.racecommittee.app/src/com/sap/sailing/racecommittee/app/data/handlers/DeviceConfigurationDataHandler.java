package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.configuration.StoredDeviceConfiguration;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;

public class DeviceConfigurationDataHandler extends DataHandler<StoredDeviceConfiguration> {

    public DeviceConfigurationDataHandler(OnlineDataManager manager) {
        super(manager);
    }
    
    @Override
    public boolean hasCachedResults() {
        return manager.getDataStore().getTabletConfiguration() != null;
    }
    
    @Override
    public StoredDeviceConfiguration getCachedResults() {
        return manager.getDataStore().getTabletConfiguration();
    }

    @Override
    public void onResult(StoredDeviceConfiguration data) {
        manager.getDataStore().setTabletConfiguration(data);
    }

}
