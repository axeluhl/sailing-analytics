package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
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
    private final IntegerBox flightMultiplierTextBox;
    
    private final CheckBox flightMultiplierCheckBox;
    
    public PairingListCreationSetupDialog(RegattaIdentifier regattaIdentifier, StringMessages stringMessages, 
            DialogCallback<PairingListTemplateDTO> callback) {
        super(regattaIdentifier, stringMessages, null, callback);        // TODO initial value
        this.competitorCountTextBox = createIntegerBox(0, 2);
        this.flightMultiplierTextBox = createIntegerBox(0, 2);
        this.flightMultiplierTextBox.setEnabled(false);
        this.flightMultiplierCheckBox = createCheckbox("Flight Multiplier");
        
        this.flightMultiplierCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                flightMultiplierTextBox.setEnabled(event.getValue());
            }
        });
        
        this.flightMultiplierCheckBox.ensureDebugId("CompetitorCountTextBox");
        
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        
        Grid formGrid = new Grid(3, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0, 0, new Label("Please set the competitors count:"));
        formGrid.setWidget(0, 1, this.competitorCountTextBox);
        formGrid.setWidget(1, 1, this.flightMultiplierCheckBox);
        formGrid.setWidget(2, 0, new Label("Flight Multiplier:"));
        formGrid.setWidget(2, 1, this.flightMultiplierTextBox);
                
        return panel;
    }

    @Override
    protected PairingListTemplateDTO getResult() {
        return new PairingListTemplateDTO(this.competitorCountTextBox.getValue(), this.flightMultiplierTextBox.getValue());
    }
}
