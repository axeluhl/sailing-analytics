package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.raceboard.AbstractQuickFlagDataProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sse.common.Util;

public class DefaultQuickFlagDataProvider extends AbstractQuickFlagDataProvider {
    private Map<String, QuickRankDTO> currentQuickRanksFromServer = Collections.emptyMap();
    private Map<CompetitorDTO, Double> currentQuickSpeedsFromServer = Collections.emptyMap();

    @Override
    public void quickRanksReceivedFromServer(Map<String, QuickRankDTO> receivedQuickRanksFromServer) {
        final Map<String, QuickRankDTO> oldQuickRanksFromServer = this.currentQuickRanksFromServer;
        this.currentQuickRanksFromServer = Util.nullToEmptyMap(receivedQuickRanksFromServer);
        for (final Entry<String, QuickRankDTO> e : currentQuickRanksFromServer.entrySet()) {
            final QuickRankDTO oldQuickRank = oldQuickRanksFromServer.get(e.getKey());
            if (Util.equalsWithNull(oldQuickRank, e.getValue())) {
                notifyListenersRankChanged(e.getKey(), oldQuickRank, e.getValue());
            }
        }
    }

    @Override
    public Map<String, QuickRankDTO> getQuickRanks() {
        return currentQuickRanksFromServer;
    }

    @Override
    public void quickSpeedsReceivedFromServer(Map<CompetitorDTO, Double> quickSpeedsFromServer) {
        final Map<CompetitorDTO, Double> oldQuickSpeedsFromServer = this.currentQuickSpeedsFromServer;
        this.currentQuickSpeedsFromServer = Util.nullToEmptyMap(quickSpeedsFromServer);
        for (final Entry<CompetitorDTO, Double> e : currentQuickSpeedsFromServer.entrySet()) {
            final Double oldQuickSpeed = oldQuickSpeedsFromServer.get(e.getKey());
            if (Util.equalsWithNull(oldQuickSpeed, e.getValue())) {
                notifyListenersSpeedChanged(e.getKey(), e.getValue());
            }
        }

    }

    @Override
    public Map<CompetitorDTO, Double> getQuickSpeeds() {
        return currentQuickSpeedsFromServer;
    }

}
