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

    protected void notifyListeners(String competitorIdAsString, QuickRankDTO quickRank) {
        for (QuickRanksListener listener : listeners) {
            listener.rankChanged(competitorIdAsString, quickRank);
        }
    }
}
