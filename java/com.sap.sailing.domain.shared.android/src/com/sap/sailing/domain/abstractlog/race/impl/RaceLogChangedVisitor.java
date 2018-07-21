package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

/**
 * Forwards events received by this visitor to the listener passed to the constructor.
 * 
 * @author Axel Uhl (D043530)
 *
 */
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
