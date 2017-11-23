package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class PairingListCreationSetupDialog extends PairingListCreationDialog {
    
    private final TextBox competitorCountTextBox;
    
    public PairingListCreationSetupDialog(RegattaIdentifier regattaIdentifier, StringMessages stringMessages, 
            DialogCallback<RegattaDTO> callback) {
        super(regattaIdentifier, stringMessages, callback);        // TODO initial value
        this.competitorCountTextBox = createTextBox("0");
        this.ensureDebugId("CompetitorCountTextBox");
        
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        
        Grid formGrid = new Grid(1, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0, 0, new Label("Please set the competitors count:"));
        formGrid.setWidget(0, 1, this.competitorCountTextBox);
                
        return panel;
    }

    @Override
    protected RegattaDTO getResult() {
        return null;
    }
}
