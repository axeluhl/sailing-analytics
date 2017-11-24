package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.controls.IntegerBox;

public class PairingListCreationSetupDialog extends PairingListCreationDialog {
    
    private final IntegerBox competitorCountTextBox;
    
    public PairingListCreationSetupDialog(RegattaIdentifier regattaIdentifier, StringMessages stringMessages, 
            DialogCallback<PairingListTemplateDTO> callback) {
        super(regattaIdentifier, stringMessages, callback);        // TODO initial value
        this.competitorCountTextBox = createIntegerBox(0, 2);
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
    protected PairingListTemplateDTO getResult() {
        return new PairingListTemplateDTO(this.competitorCountTextBox.getValue(), null, 0.0);
    }
}
