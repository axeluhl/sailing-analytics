package com.sap.sailing.domain.abstractlog.race.impl;

import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.base.Competitor;

public class RaceLogEventDataImpl implements RaceLogEventData {
    private static final long serialVersionUID = 61677173867866892L;
    private final List<Competitor> involvedCompetitors;
    private final int passId;
    
    public RaceLogEventDataImpl(List<Competitor> involvedCompetitors, int passId) {
        if (involvedCompetitors == null) {
            this.involvedCompetitors = Collections.emptyList();
        } else {
            this.involvedCompetitors = involvedCompetitors;
        }
        this.passId = passId;
    }

    @Override
    public List<Competitor> getInvolvedCompetitors() {
        return involvedCompetitors;
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
