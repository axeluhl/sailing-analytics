package com.sap.sailing.gwt.ui.raceboard;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.gwt.ui.client.shared.racemap.QuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

public abstract class AbstractQuickRanksDTOProvider implements QuickRanksDTOProvider {
    private final Set<QuickRanksListener> listeners;
    
    public AbstractQuickRanksDTOProvider() {
        listeners = new HashSet<>(); 
    }

    @Override
    public void addQuickRanksListener(QuickRanksListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeQuickRanksListener(QuickRanksListener listener) {
        listeners.remove(listener);
    }

    public void moveListernersTo(QuickRanksDTOProvider newProvider) {
        for (QuickRanksListener quickRanksListener : listeners) {
            newProvider.addQuickRanksListener(quickRanksListener);
        }
        listeners.clear();
    }

    protected void notifyListeners(String competitorIdAsString, QuickRankDTO oldQuickRank, QuickRankDTO newQuickRank) {
        for (QuickRanksListener listener : listeners) {
            listener.rankChanged(competitorIdAsString, oldQuickRank, newQuickRank);
        }
    }
}
