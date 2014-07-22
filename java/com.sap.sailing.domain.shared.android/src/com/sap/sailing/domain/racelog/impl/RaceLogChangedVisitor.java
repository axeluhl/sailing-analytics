package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public class RaceLogChangedVisitor extends AbstractRaceLogChangedVisitor {
    private final RaceLogChangedListener listener;

    public RaceLogChangedVisitor(RaceLogChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void notifyListenerAboutEventAdded(RaceLogEvent event) {
        listener.eventAdded(event);
    }
}
