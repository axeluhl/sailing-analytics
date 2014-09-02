package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class SeriesWithFleetsDefaultCreateDialog extends
                               SeriesWithFleetsCreateDialog {

                public SeriesWithFleetsDefaultCreateDialog(
                                               Collection<SeriesDTO> existingSeries,
                                               StringMessages stringMessages,
                                               com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<SeriesDTO> callback) {
                               super(existingSeries, stringMessages, callback);
                               // TODO Auto-generated constructor stub
                }
                
                protected void setNameEntryField() {
                               super.setNameEntryField();
                               nameEntryField.setReadOnly(true);
                               nameEntryField.setText("Default");
                }

                protected void addFleetListComposite(final VerticalPanel panel,
                                               TabPanel tabPanel) {
                               //don't add the tabPanel with the FleetListComposite to the panel
                }

}

