package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;

public class RacingProcedureChangedListeners<T extends RacingProcedureChangedListener> implements RacingProcedureChangedListener {
    
    private Set<T> specificListeners;
    private Set<RacingProcedureChangedListener> allListeners;
    
    public RacingProcedureChangedListeners() {
        this.specificListeners = new HashSet<T>();
        this.allListeners = new HashSet<RacingProcedureChangedListener>();
    }
    
    protected Set<T> getListeners() {
        return specificListeners;
    }
    
    public boolean addBaseListener(RacingProcedureChangedListener listener) {
        return allListeners.add(listener);
    }
    
    public boolean add(T specificListener) {
        return specificListeners.add(specificListener) && allListeners.add(specificListener);
    }

    public void remove(RacingProcedureChangedListener listener) {
        specificListeners.remove(listener);
        allListeners.remove(listener);
    }

    @Override
    public void onActiveFlagsChanged(RacingProcedure racingProcedure) {
        for (RacingProcedureChangedListener listener : allListeners) {
            listener.onActiveFlagsChanged(racingProcedure);
        }
    }
    
    @Override
    public void onIndividualRecallDisplayed(RacingProcedure racingProcedure) {
        for (RacingProcedureChangedListener listener : allListeners) {
            listener.onIndividualRecallDisplayed(racingProcedure);
        }
    }

    @Override
    public void onIndividualRecallRemoved(RacingProcedure racingProcedure) {
        for (RacingProcedureChangedListener listener : allListeners) {
            listener.onIndividualRecallRemoved(racingProcedure);
        }
    }

    public void removeAll() {
        allListeners.clear();
        specificListeners.clear();
    }

}
