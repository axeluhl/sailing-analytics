package com.sap.sailing.gwt.ui.raceboard;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.QuickFlagDataProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

public abstract class AbstractQuickFlagDataProvider implements QuickFlagDataProvider {
    private final Set<QuickFlagDataListener> listeners;

    public AbstractQuickFlagDataProvider() {
        listeners = new HashSet<>();
    }

    @Override
    public void addQuickFlagDataListener(QuickFlagDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeQuickFlagDataListener(QuickFlagDataListener listener) {
        listeners.remove(listener);
    }

    public void moveListernersTo(QuickFlagDataProvider newProvider) {
        for (QuickFlagDataListener quickRanksListener : listeners) {
            newProvider.addQuickFlagDataListener(quickRanksListener);
        }
        listeners.clear();
    }

    protected void notifyListenersRankChanged(String competitorIdAsString, QuickRankDTO oldQuickRank,
            QuickRankDTO newQuickRank) {
        for (QuickFlagDataListener listener : listeners) {
            listener.rankChanged(competitorIdAsString, oldQuickRank, newQuickRank);
        }
    }

    protected void notifyListenersSpeedChanged(CompetitorDTO competitor, Double newSpeed) {
        for (QuickFlagDataListener listener : listeners) {
            listener.speedChanged(competitor, newSpeed);
        }
    }
}
