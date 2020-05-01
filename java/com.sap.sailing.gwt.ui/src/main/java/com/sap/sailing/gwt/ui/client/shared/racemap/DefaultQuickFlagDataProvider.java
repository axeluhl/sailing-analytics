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
    private Map<CompetitorDTO, Double> currentQuickSpeedsInKnotsFromServer = Collections.emptyMap();

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
    public void quickSpeedsInKnotsReceivedFromServer(Map<CompetitorDTO, Double> quickSpeedsFromServerInKnots) {
        final Map<CompetitorDTO, Double> oldQuickSpeedsFromServerInKnots = this.currentQuickSpeedsInKnotsFromServer;
        this.currentQuickSpeedsInKnotsFromServer = Util.nullToEmptyMap(quickSpeedsFromServerInKnots);
        for (final Entry<CompetitorDTO, Double> e : currentQuickSpeedsInKnotsFromServer.entrySet()) {
            final Double oldQuickSpeedInKnots = oldQuickSpeedsFromServerInKnots.get(e.getKey());
            if (Util.equalsWithNull(oldQuickSpeedInKnots, e.getValue())) {
                notifyListenersSpeedInKnotsChanged(e.getKey(), e.getValue());
            }
        }

    }

    @Override
    public Double getQuickSpeedsInKnots(CompetitorDTO competitor) {
        return currentQuickSpeedsInKnotsFromServer.get(competitor);
    }

}
