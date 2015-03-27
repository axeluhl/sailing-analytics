package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaLogDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogEventDTO;

public class RegattaLogDialog extends AbstractLogDialog<RegattaLogDTO, RegattaLogEventDTO> {
    public RegattaLogDialog(final RegattaLogDTO regattaLogDTO, final StringMessages stringMessages, DialogCallback<RegattaLogDTO> callback) {
        super(regattaLogDTO, stringMessages, stringMessages.raceLog(), callback);
    }
}
