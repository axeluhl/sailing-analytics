package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MarkPassingsPanel extends AbstractRaceManagementPanel {

    MarkPassingManagementWidget markPassingManagementWidget;

    public MarkPassingsPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, RegattaRefresher regattaRefresher,
            StringMessages stringMessages) {
        super(sailingService, errorReporter, regattaRefresher, stringMessages);
        markPassingManagementWidget = new MarkPassingManagementWidget(stringMessages) {
            
            @Override
            void save() {
                // TODO Auto-generated method stub
            }
            
            @Override
            void refresh() {
                // TODO Auto-generated method stub
            }
        };
        this.selectedRaceContentPanel.add(markPassingManagementWidget);
    }

    @Override
    void refreshSelectedRaceData() {
        markPassingManagementWidget.refresh();
    }
}
