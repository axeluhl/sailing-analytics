package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.controls.IntegerBox;

public class PairingListCreationSetupDialog extends AbstractPairingListCreationSetupDialog<PairingListTemplateDTO> {
    
    private final IntegerBox competitorCountTextBox;
    private final IntegerBox flightMultiplierTextBox;
    private final CheckBox flightMultiplierCheckBox;
    private final int groupCount;
    
    protected static class PairingListParameterValidator extends AbstractPairingListParameterValidator {
        public PairingListParameterValidator(StringMessages stringMessages) {
            super(stringMessages);
        }
    }
    
    public PairingListCreationSetupDialog(StrippedLeaderboardDTO leaderboardDTO, StringMessages stringMessages, 
            DialogCallback<PairingListTemplateDTO> callback) {
        
        super(leaderboardDTO, stringMessages.pairingLists(), stringMessages, new PairingListParameterValidator(stringMessages), 
                callback);
        this.groupCount = Util.size(leaderboardDTO.getRaceList().get(0).getFleets());
        this.competitorCountTextBox = createIntegerBox(leaderboardDTO.competitorsCount, 2);
        this.flightMultiplierTextBox = createIntegerBox(0, 2);
        this.flightMultiplierTextBox.setEnabled(false);
        this.flightMultiplierTextBox.setValue(1);
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
        
        Grid formGrid = new Grid(4, 2);
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
        PairingListTemplateDTO dto = new PairingListTemplateDTO(this.competitorCountTextBox.getValue(), 
                this.flightMultiplierTextBox.getValue());
        dto.setGroupCount(this.groupCount);
        
        int flightCount = 0;
        
        for (RaceColumnDTO raceColumn : leaderboardDTO.getRaceList()) {
            if (!raceColumn.isMedalRace()) {
                flightCount++;
            }
        }
        
        dto.setFlightCount(flightCount);
        dto.setFlightMultiplier(this.flightMultiplierTextBox.getValue());
        return dto; 
    }
    
    public void setDefaultCompetitorCount(int competitorCount) {
        if (this.competitorCountTextBox.getValue() == 0) {
            this.competitorCountTextBox.setValue(competitorCount);
            this.validateAndUpdate();
        }
    }
}
