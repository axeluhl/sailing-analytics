package com.sap.sailing.domain.abstractlog.race.impl;

import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.base.Competitor;

public class RaceLogEventDataImpl implements RaceLogEventData {
    private static final long serialVersionUID = 61677173867866892L;
    private final List<Competitor> involvedBoats;
    private final int passId;
    
    public RaceLogEventDataImpl(List<Competitor> involvedBoats, int passId) {
        if (involvedBoats == null) {
            this.involvedBoats = Collections.emptyList();
        } else {
            this.involvedBoats = involvedBoats;
        }
        this.passId = passId;
    }

    @Override
    public List<Competitor> getInvolvedCompetitors() {
        return involvedBoats;
    }

    @Override
    public int getPassId() {
        return passId;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", involvedBoats: " + getInvolvedCompetitors() + ", passId: " + getPassId();
    }
}
