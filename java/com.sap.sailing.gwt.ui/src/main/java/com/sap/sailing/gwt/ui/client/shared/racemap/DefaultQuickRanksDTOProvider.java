package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.gwt.ui.raceboard.AbstractQuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sse.common.Util;

public class DefaultQuickRanksDTOProvider extends AbstractQuickRanksDTOProvider {
    private Map<String, QuickRankDTO> currentQuickRanksFromServer = Collections.emptyMap();
    
    @Override
    public void quickRanksReceivedFromServer(Map<String, QuickRankDTO> receivedQuickRanksFromServer) {
        final Map<String, QuickRankDTO> oldQuickRanksFromServer = this.currentQuickRanksFromServer;
        this.currentQuickRanksFromServer = Util.nullToEmptyMap(receivedQuickRanksFromServer);
        for (final Entry<String, QuickRankDTO> e : currentQuickRanksFromServer.entrySet()) {
            final QuickRankDTO oldQuickRank = oldQuickRanksFromServer.get(e.getKey());
            if (Util.equalsWithNull(oldQuickRank, e.getValue())) {
                notifyListeners(e.getKey(), oldQuickRank, e.getValue());
            }
        }
    }

    @Override
    public Map<String, QuickRankDTO> getQuickRanks() {
        return currentQuickRanksFromServer;
    }

}
