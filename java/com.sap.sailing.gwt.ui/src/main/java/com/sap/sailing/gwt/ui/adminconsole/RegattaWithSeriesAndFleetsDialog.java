package com.sap.sailing.gwt.ui.adminconsole;

import java.util.EnumSet;
import java.util.List;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.common.Distance;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;

public abstract class RegattaWithSeriesAndFleetsDialog extends AbstractRegattaWithSeriesAndFleetsDialog<RegattaDTO> {
    protected TextBox nameEntryField;
    protected SuggestBox boatClassEntryField;
    protected CheckBox canBoatsOfCompetitorsChangePerRaceCheckBox;
    protected ListBox competitorRegistrationTypeListBox = createListBox(false);

    public RegattaWithSeriesAndFleetsDialog(RegattaDTO regatta, Iterable<SeriesDTO> series, List<EventDTO> existingEvents, EventDTO defaultEvent,
            String title, String okButton, StringMessages stringMessages,
            Validator<RegattaDTO> validator, DialogCallback<RegattaDTO> callback) {
        super(regatta, series, existingEvents, defaultEvent, title, okButton, stringMessages, validator, callback);
        this.stringMessages = stringMessages;
        nameEntryField = createTextBox(null);
        nameEntryField.ensureDebugId("NameTextBox");
        nameEntryField.setVisibleLength(40);
        nameEntryField.setText(regatta.getName());
        boatClassEntryField = createSuggestBox(new BoatClassMasterdataSuggestOracle());
        boatClassEntryField.getValueBox().ensureDebugId("BoatClassTextBox");
        if (regatta.boatClass != null) {
            boatClassEntryField.setText(regatta.boatClass.getName());
        }
        canBoatsOfCompetitorsChangePerRaceCheckBox = createCheckbox("");
        canBoatsOfCompetitorsChangePerRaceCheckBox.ensureDebugId("CanBoatsOfCompetitorsChangePerRaceCheckBox");
        canBoatsOfCompetitorsChangePerRaceCheckBox.setValue(regatta.canBoatsOfCompetitorsChangePerRace);
        
        EnumSet.allOf(CompetitorRegistrationType.class).forEach(t->competitorRegistrationTypeListBox.addItem(t.getLabel(stringMessages), t.name()));
        competitorRegistrationTypeListBox.setSelectedIndex(regatta.competitorRegistrationType.ordinal());
    }

    @Override
    protected ListEditorComposite<SeriesDTO> createSeriesEditor(Iterable<SeriesDTO> series) {
        return new SeriesWithFleetsListEditor(series, stringMessages, IconResources.INSTANCE.removeIcon(), isEnableFleetRemoval());
    }

    protected abstract boolean isEnableFleetRemoval();

    @Override
    protected RegattaDTO getResult() {
        RegattaDTO result = getRegattaDTO();
        result.setName(nameEntryField.getText().trim()); // trim to particularly avoid trailing blanks
        result.boatClass = new BoatClassDTO(boatClassEntryField.getText(), Distance.NULL, Distance.NULL);
        result.canBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRaceCheckBox.getValue();
        result.competitorRegistrationType = CompetitorRegistrationType.valueOf(competitorRegistrationTypeListBox.getSelectedValue());
        return result;
    }
    
    @Override
    protected void setupAdditionalWidgetsOnPanel(VerticalPanel panel, Grid formGrid) {
        formGrid.insertRow(0);
        formGrid.insertRow(0);
        formGrid.insertRow(0);
        formGrid.setWidget(0, 0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 0, new Label(stringMessages.boatClass() + ":"));
        formGrid.setWidget(1, 1, boatClassEntryField);
        formGrid.setWidget(2, 0, new Label(stringMessages.canBoatsOfCompetitorsChangePerRace() + ":"));
        formGrid.setWidget(2, 1, canBoatsOfCompetitorsChangePerRaceCheckBox);
        formGrid.setWidget(3, 0, new Label(stringMessages.competitorRegistrationType() + ":"));
        formGrid.setWidget(3, 1, competitorRegistrationTypeListBox);
    }
}
