package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.gwt.ui.raceboard.AbstractQuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sse.common.Util;

public class DefaultQuickRanksDTOProvider extends AbstractQuickRanksDTOProvider {
    private Map<String, QuickRankDTO> quickRanksFromServer;
    
    @Override
    public void quickRanksReceivedFromServer(Map<String, QuickRankDTO> quickRanksFromServer) {
        final Map<String, QuickRankDTO> oldQuickRanksFromServer = quickRanksFromServer;
        this.quickRanksFromServer = quickRanksFromServer;
        for (final Entry<String, QuickRankDTO> e : quickRanksFromServer.entrySet()) {
            final QuickRankDTO oldQuickRank = oldQuickRanksFromServer.get(e.getKey());
            if (Util.equalsWithNull(oldQuickRank, e.getValue())) {
                notifyListeners(e.getKey(), oldQuickRank, e.getValue());
            }
        }
    }

    @Override
    public Map<String, QuickRankDTO> getQuickRanks() {
        return quickRanksFromServer;
    }

}
