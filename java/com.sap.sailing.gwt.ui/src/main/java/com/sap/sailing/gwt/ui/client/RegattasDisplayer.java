package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public interface RegattasDisplayer extends Displayer {
    void fillRegattas(Iterable<RegattaDTO> result);
}
