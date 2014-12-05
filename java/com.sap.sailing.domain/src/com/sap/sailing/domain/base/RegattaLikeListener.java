package com.sap.sailing.domain.base;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;

/**
 * Listener on {@link RegattaLikeIdentifier regatta-like} objects.
 * @author Fredrik Teschke
 *
 */
public interface RegattaLikeListener {
    void regattaLogEventAdded(RegattaLikeIdentifier identifier, RegattaLogEvent event);
}
