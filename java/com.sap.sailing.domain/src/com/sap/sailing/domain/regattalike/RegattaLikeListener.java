package com.sap.sailing.domain.regattalike;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;

public interface RegattaLikeListener extends Serializable {
    void onRegattaLogEvent(RegattaLikeIdentifier identifier, RegattaLogEvent event);
}
