package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class DefaultRegattaCreateDialog extends
                               RegattaWithSeriesAndFleetsCreateDialog {

                public DefaultRegattaCreateDialog(
                                               Collection<RegattaDTO> existingRegattas,
                                               List<EventDTO> existingEvents,
                                               StringMessages stringConstants,
                                               com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<RegattaDTO> callback) {
                               super(existingRegattas, existingEvents, stringConstants, callback);
                               
                               nameEntryField.setText("Default");
                               nameEntryField.setReadOnly(true);
                               boatClassEntryField.setText("Default");
                               boatClassEntryField.setReadOnly(true);
                               
                               sailingEventsListBox.setItemSelected(1, true);
                               setCourseAreaSelection();
                               sailingEventsListBox.setEnabled(false);
                               
                               
                }
                protected void setSeriesEditor(){
                               this.seriesEditor = new SeriesWithFleetsDefaultListEditor(Collections.<SeriesDTO>emptyList(), stringMessages, resources.removeIcon(), /*enableFleetRemoval*/true);
                }

}

