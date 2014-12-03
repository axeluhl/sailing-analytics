package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public abstract class RegattaWithSeriesAndFleetsDialog extends AbstractRegattaWithSeriesAndFleetsDialog<RegattaDTO> {
    protected BetterDateTimeBox startDateBox;
    protected BetterDateTimeBox endDateBox;
    protected TextBox nameEntryField;
    protected TextBox boatClassEntryField;

    public RegattaWithSeriesAndFleetsDialog(RegattaDTO regatta, List<EventDTO> existingEvents, String title,
            String okButton, StringMessages stringMessages, Validator<RegattaDTO> validator,
            DialogCallback<RegattaDTO> callback) {
        super(regatta, existingEvents, title, okButton, stringMessages, validator, callback);
        this.stringMessages = stringMessages;
        nameEntryField = createTextBox(null);
        nameEntryField.ensureDebugId("NameTextBox");
        nameEntryField.setVisibleLength(40);
        nameEntryField.setText(regatta.getName());
        startDateBox = createDateTimeBox(regatta.startDate);
        startDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        endDateBox = createDateTimeBox(regatta.endDate);
        endDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        boatClassEntryField = createTextBox(null);
        boatClassEntryField.ensureDebugId("BoatClassTextBox");
        boatClassEntryField.setVisibleLength(20);
        if (regatta.boatClass != null) {
            boatClassEntryField.setText(regatta.boatClass.getName());
        }
		setSeriesEditor();
        formGrid.setWidget(6, 0, new Label(stringMessages.courseArea() + ":"));
        formGrid.setWidget(6, 1, courseAreaListBox);
        formGrid.setWidget(7, 0, new Label(stringMessages.useStartTimeInference() + ":"));
        formGrid.setWidget(7, 1, useStartTimeInferenceCheckBox);
    }

    @Override
    protected RegattaDTO getResult() {
        regatta.setName(nameEntryField.getText());
        regatta.startDate = startDateBox.getValue();
        regatta.endDate = endDateBox.getValue();
        regatta.boatClass = new BoatClassDTO(boatClassEntryField.getText(), 0.0);
        regatta.scoringScheme = getSelectedScoringSchemeType();
        regatta.useStartTimeInference = useStartTimeInferenceCheckBox.getValue();
        setCourseAreaInRegatta(regatta);
        return regatta;
    }
    
    @Override
    protected void setupAdditionalWidgetsOnPanel(VerticalPanel panel){
        Grid formGrid = (Grid)panel.getWidget(0);
        formGrid.insertRow(0);
        formGrid.insertRow(0);
        formGrid.setWidget(0, 0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 0, new Label(stringMessages.boatClass() + ":"));
        formGrid.setWidget(1, 1, boatClassEntryField);
    }
}
