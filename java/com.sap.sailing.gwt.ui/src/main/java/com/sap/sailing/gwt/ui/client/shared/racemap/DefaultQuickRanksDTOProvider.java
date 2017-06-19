package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.LinkedHashMap;

import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

public class DefaultQuickRanksDTOProvider implements QuickRanksDTOProvider {
    private LinkedHashMap<String, QuickRankDTO> quickRanksFromServer;
    
    @Override
    public void quickRanksReceivedFromServer(LinkedHashMap<String, QuickRankDTO> quickRanksFromServer) {
        this.quickRanksFromServer = quickRanksFromServer;
    }

    @Override
    public LinkedHashMap<String, QuickRankDTO> getQuickRanks() {
        return quickRanksFromServer;
    }

}
