package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.LinkedHashMap;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

public class DefaultQuickRanksDTOProvider implements QuickRanksDTOProvider {
    private LinkedHashMap<CompetitorDTO, QuickRankDTO> quickRanksFromServer;
    
    @Override
    public void quickRanksReceivedFromServer(LinkedHashMap<CompetitorDTO, QuickRankDTO> quickRanksFromServer) {
        this.quickRanksFromServer = quickRanksFromServer;
    }

    @Override
    public LinkedHashMap<CompetitorDTO, QuickRankDTO> getQuickRanks() {
        return quickRanksFromServer;
    }

}
