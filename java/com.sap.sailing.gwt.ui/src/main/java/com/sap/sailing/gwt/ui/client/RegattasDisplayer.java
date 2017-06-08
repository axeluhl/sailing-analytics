package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public interface RegattasDisplayer {
    void fillRegattas(Iterable<RegattaDTO> result);
}
