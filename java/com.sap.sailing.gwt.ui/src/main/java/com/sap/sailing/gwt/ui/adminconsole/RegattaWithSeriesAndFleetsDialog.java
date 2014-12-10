package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;

public abstract class RegattaWithSeriesAndFleetsDialog extends AbstractRegattaWithSeriesAndFleetsDialog<RegattaDTO> {
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    protected TextBox nameEntryField;
    protected TextBox boatClassEntryField;

    public RegattaWithSeriesAndFleetsDialog(RegattaDTO regatta, Iterable<SeriesDTO> series, List<EventDTO> existingEvents,
            String title, String okButton, StringMessages stringMessages,
            Validator<RegattaDTO> validator, DialogCallback<RegattaDTO> callback) {
        super(regatta, series, existingEvents, title, okButton, stringMessages, validator, callback);
        this.stringMessages = stringMessages;
        nameEntryField = createTextBox(null);
        nameEntryField.ensureDebugId("NameTextBox");
        nameEntryField.setVisibleLength(40);
        nameEntryField.setText(regatta.getName());
        boatClassEntryField = createTextBox(null);
        boatClassEntryField.ensureDebugId("BoatClassTextBox");
        boatClassEntryField.setVisibleLength(20);
        if (regatta.boatClass != null) {
            boatClassEntryField.setText(regatta.boatClass.getName());
        }
    }

    @Override
    protected ListEditorComposite<SeriesDTO> createSeriesEditor(Iterable<SeriesDTO> series) {
        return new SeriesWithFleetsListEditor(series, stringMessages, resources.removeIcon(), isEnableFleetRemoval());
    }

    protected abstract boolean isEnableFleetRemoval();

    @Override
    protected RegattaDTO getResult() {
        RegattaDTO result = getRegattaDTO();
        result.setName(nameEntryField.getText());
        result.boatClass = new BoatClassDTO(boatClassEntryField.getText(), 0.0);
        return result;
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
