package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.raceboard.AbstractQuickFlagDataProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sse.common.Util;

public class DefaultQuickFlagDataProvider extends AbstractQuickFlagDataProvider {
    private Map<String, QuickRankDTO> currentQuickRanksFromServerByCompetitorIdAsString = Collections.emptyMap();
    private Map<String, Double> currentQuickSpeedsInKnotsFromServerByCompetitorIdAsString = Collections.emptyMap();

    @Override
    public void quickRanksReceivedFromServer(Map<String, QuickRankDTO> receivedQuickRanksFromServer) {
        final Map<String, QuickRankDTO> oldQuickRanksFromServer = this.currentQuickRanksFromServerByCompetitorIdAsString;
        this.currentQuickRanksFromServerByCompetitorIdAsString = Util.nullToEmptyMap(receivedQuickRanksFromServer);
        for (final Entry<String, QuickRankDTO> e : currentQuickRanksFromServerByCompetitorIdAsString.entrySet()) {
            final QuickRankDTO oldQuickRank = oldQuickRanksFromServer.get(e.getKey());
            if (Util.equalsWithNull(oldQuickRank, e.getValue())) {
                notifyListenersRankChanged(e.getKey(), oldQuickRank, e.getValue());
            }
        }
    }

    @Override
    public Map<String, QuickRankDTO> getQuickRanks() {
        return currentQuickRanksFromServerByCompetitorIdAsString;
    }

    @Override
    public void quickSpeedsInKnotsReceivedFromServer(Map<String, Double> quickSpeedsFromServerInKnotsByCompetitorIdAsString, Map<String, CompetitorDTO> competitorsByIdAsString) {
        final Map<String, Double> oldQuickSpeedsFromServerInKnots = this.currentQuickSpeedsInKnotsFromServerByCompetitorIdAsString;
        this.currentQuickSpeedsInKnotsFromServerByCompetitorIdAsString = Util.nullToEmptyMap(quickSpeedsFromServerInKnotsByCompetitorIdAsString);
        for (final Entry<String, Double> e : currentQuickSpeedsInKnotsFromServerByCompetitorIdAsString.entrySet()) {
            final Double oldQuickSpeedInKnots = oldQuickSpeedsFromServerInKnots.get(e.getKey());
            if (Util.equalsWithNull(oldQuickSpeedInKnots, e.getValue())) {
                notifyListenersSpeedInKnotsChanged(competitorsByIdAsString.get(e.getKey()), e.getValue());
            }
        }

    }

    @Override
    public Double getQuickSpeedsInKnots(CompetitorDTO competitor) {
        return currentQuickSpeedsInKnotsFromServerByCompetitorIdAsString.get(competitor.getIdAsString());
    }
}
