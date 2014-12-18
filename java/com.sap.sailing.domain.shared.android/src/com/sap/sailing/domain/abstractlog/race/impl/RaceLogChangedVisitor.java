package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

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
