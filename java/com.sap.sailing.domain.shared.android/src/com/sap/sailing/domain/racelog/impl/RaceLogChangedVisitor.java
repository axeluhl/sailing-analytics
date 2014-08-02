package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.tracking.FixedMarkPassingEvent;
import com.sap.sailing.domain.racelog.tracking.SuppressedMarkPassingsEvent;

public class RaceLogChangedVisitor extends AbstractRaceLogChangedVisitor {
    private final RaceLogChangedListener listener;

    public RaceLogChangedVisitor(RaceLogChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void notifyListenerAboutEventAdded(RaceLogEvent event) {
        listener.eventAdded(event);
    }

    @Override
    public void visit(FixedMarkPassingEvent event) {
        listener.eventAdded(event);
    }

    @Override
    public void visit(SuppressedMarkPassingsEvent event) {
    }
}
