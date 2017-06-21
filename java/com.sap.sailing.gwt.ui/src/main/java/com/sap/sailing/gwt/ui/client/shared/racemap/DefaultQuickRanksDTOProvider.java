package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.gwt.ui.raceboard.AbstractQuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

public class DefaultQuickRanksDTOProvider extends AbstractQuickRanksDTOProvider {
    private Map<String, QuickRankDTO> quickRanksFromServer;
    
    @Override
    public void quickRanksReceivedFromServer(Map<String, QuickRankDTO> quickRanksFromServer) {
        this.quickRanksFromServer = quickRanksFromServer;
        for (final Entry<String, QuickRankDTO> e : quickRanksFromServer.entrySet()) {
            notifyListeners(e.getKey(), e.getValue());
        }
    }

    @Override
    public Map<String, QuickRankDTO> getQuickRanks() {
        return quickRanksFromServer;
    }

}
