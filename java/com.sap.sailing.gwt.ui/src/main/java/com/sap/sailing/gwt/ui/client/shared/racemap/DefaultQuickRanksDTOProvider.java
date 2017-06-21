package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.sap.sailing.gwt.ui.raceboard.AbstractQuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

public class DefaultQuickRanksDTOProvider extends AbstractQuickRanksDTOProvider {
    private LinkedHashMap<String, QuickRankDTO> quickRanksFromServer;
    
    @Override
    public void quickRanksReceivedFromServer(LinkedHashMap<String, QuickRankDTO> quickRanksFromServer) {
        this.quickRanksFromServer = quickRanksFromServer;
        for (final Entry<String, QuickRankDTO> e : quickRanksFromServer.entrySet()) {
            notifyListeners(e.getKey(), e.getValue());
        }
    }

    @Override
    public LinkedHashMap<String, QuickRankDTO> getQuickRanks() {
        return quickRanksFromServer;
    }

}
