package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class SeriesWithFleetsDefaultCreateDialog extends
                               SeriesWithFleetsCreateDialog {

                public SeriesWithFleetsDefaultCreateDialog(
                                               Collection<SeriesDTO> existingSeries,
                                               StringMessages stringMessages,
                                               com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<SeriesDTO> callback) {
                               super(existingSeries, stringMessages, callback);
                               nameEntryField.setText("default");//Otherwise an errorMessage will pop up
                }

                protected void addFleetListComposite(final VerticalPanel panel,
                                               TabPanel tabPanel) {
                               //don't add the tabPanel with the FleetListComposite to the panel
                }
                
                @Override
                protected Widget getAdditionalWidget() {
                	VerticalPanel panel = (VerticalPanel) super.getAdditionalWidget();
                	Grid formGrid = (Grid) panel.getWidget(0);
                	formGrid.removeRow(0);
                	formGrid.removeRow(0);
                	return panel;
                }

}

