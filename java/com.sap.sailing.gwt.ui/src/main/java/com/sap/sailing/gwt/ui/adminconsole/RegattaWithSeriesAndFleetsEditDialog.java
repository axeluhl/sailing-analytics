package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaWithSeriesAndFleetsEditDialog extends RegattaWithSeriesAndFleetsDialog {

    public RegattaWithSeriesAndFleetsEditDialog(RegattaDTO regatta, Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, StringMessages stringConstants, DialogCallback<RegattaDTO> callback) {
        super(regatta, existingEvents, stringConstants.editRegatta(), stringConstants.ok(), stringConstants,
                null, callback);
        nameEntryField.setEnabled(false);
        boatClassEntryField.setEnabled(false);
        scoringSchemeListBox.setEnabled(false);
        
        sailingEventsListBox.setEnabled(true);
        courseAreaListBox.setEnabled(true);
    }

    @Override
    public void show() {
        super.show();
        courseAreaListBox.setFocus(true);
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(VerticalPanel panel) {
        // nothing yet
    }

}
