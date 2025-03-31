package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeListener;
import com.sap.sailing.util.impl.RaceColumnListeners;

/**
 * Can {@link IsRegattaLike#addListener(RegattaLikeListener) listen} on an {@link IsRegattaLike}'s {@link RegattaLog}
 * and forwards each event that was added to the log to the {@link RaceColumnListeners} object passed to this object's
 * constructor.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RegattaLogEventAdditionForwarder implements RegattaLikeListener {
    private static final long serialVersionUID = -8112813552939605252L;
    private final RaceColumnListeners raceColumnListeners;

    public RegattaLogEventAdditionForwarder(RaceColumnListeners raceColumnListeners) {
        super();
        this.raceColumnListeners = raceColumnListeners;
    }

    @Override
    public void onRegattaLogEvent(RegattaLikeIdentifier identifier, RegattaLogEvent event) {
        raceColumnListeners.notifyListenersAboutRegattaLogEventAdded(event);
    }
}