package com.sap.sailing.dashboards.gwt.client;

import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;

public interface WindBotDataRetrieverProvider {

    public void addWindBotDataRetrieverListener(WindBotDataRetrieverListener windBotDataRetrieverListener);
    public void removeWindBotDataRetrieverListener(WindBotDataRetrieverListener windBotDataRetrieverListener);
    public void notifyWindBotDataRetrieverListeners(WindInfoForRaceDTO windInfoForRaceDTO);
}