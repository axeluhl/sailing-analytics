package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.TabletConfiguration;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;

public class TabletConfigurationDataHandler extends DataHandler<TabletConfiguration> {

    public TabletConfigurationDataHandler(OnlineDataManager manager) {
        super(manager);
    }
    
    @Override
    public boolean hasCachedResults() {
        return manager.getDataStore().getTabletConfiguration() != null;
    }
    
    @Override
    public TabletConfiguration getCachedResults() {
        return manager.getDataStore().getTabletConfiguration();
    }

    @Override
    public void onResult(TabletConfiguration data) {
        manager.getDataStore().setTabletConfiguration(data);
    }

}
