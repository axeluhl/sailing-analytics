package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface RegattaIdentifier extends Serializable {
    Object getRegatta(RegattaFetcher regattaFetcher);
}
