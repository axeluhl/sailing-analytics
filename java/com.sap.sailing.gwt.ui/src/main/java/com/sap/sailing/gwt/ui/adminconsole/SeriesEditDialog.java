package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.listedit.StringListEditorComposite;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.ui.DataEntryDialog;

public class SeriesEditDialog extends DataEntryDialog<SeriesDescriptor> {

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    
    private CheckBox isMedalCheckbox;
    private CheckBox startWithZeroScoreCheckbox;
    private CheckBox firstColumnIsNonDiscardableCarryForwardCheckbox;
    private CheckBox useSeriesResultDiscardingThresholdsCheckbox;
    private final StringMessages stringMessages;
    private VerticalPanel additionalWidgetPanel;
    private final SeriesDTO selectedSeries;
    private final DiscardThresholdBoxes discardThresholdBoxes;
    
    private static class RaceDialogValidator implements Validator<SeriesDescriptor> {
        private StringMessages stringMessages;
        private RegattaDTO regatta;
        
        public RaceDialogValidator(RegattaDTO regatta, StringMessages stringMessages) {
            this.stringMessages = stringMessages;
            this.regatta = regatta;
        }

        @Override
        public String getErrorMessage(SeriesDescriptor valueToValidate) {
            Set<String> raceColumnNamesOfOtherSeries = new HashSet<String>();
            SeriesDTO seriesToValidate = valueToValidate.getSeries();
            List<RaceColumnDTO> raceColumnsToValidate = valueToValidate.getRaces();
            String errorMessage = null;
            for (SeriesDTO seriesDTO: regatta.series) {
                if (!seriesDTO.getName().equals(seriesToValidate.getName())) {
                    for (RaceColumnDTO raceColumn : seriesDTO.getRaceColumns()) {
                        raceColumnNamesOfOtherSeries.add(raceColumn.getName());
                    }
                }
            }
            boolean raceColumnNameNotEmpty = true;
            RaceColumnDTO wrongRaceColumn = null;
            for (RaceColumnDTO raceColumn : raceColumnsToValidate) {
                raceColumnNameNotEmpty = raceColumn.getName() != null && raceColumn.getName().length() > 0;
                if (!raceColumnNameNotEmpty) {
                    wrongRaceColumn = raceColumn;
                    break;
                }
            }
            boolean raceColumnUniqueInSeries = true;
            boolean raceColumnUniqueInRegatta = true;
            HashSet<String> setToFindDuplicates = new HashSet<String>();
            for (RaceColumnDTO raceColumn : raceColumnsToValidate) {
                if (!setToFindDuplicates.add(raceColumn.getName())) {
                    raceColumnUniqueInSeries = false;
                    wrongRaceColumn = raceColumn;
                    break;
                } else if(raceColumnNamesOfOtherSeries.contains(raceColumn.getName())) {
                    raceColumnUniqueInRegatta = false;
                    wrongRaceColumn = raceColumn;
                    break;
                } 
            }
            if (!raceColumnNameNotEmpty) {
                errorMessage = stringMessages.race() + " " + wrongRaceColumn.getName() + ": "
                        + stringMessages.pleaseEnterAName();
            } else if (!raceColumnUniqueInSeries) {
                errorMessage = stringMessages.race() + " " +  wrongRaceColumn.getName() + ": "
                        + stringMessages.raceWithThisNameAlreadyExists();
            }  else if (!raceColumnUniqueInRegatta) {
                errorMessage = stringMessages.race() + " " +  wrongRaceColumn.getName() + ": "
                        + stringMessages.raceWithThisNameAlreadyExistsInRegatta();
            } else {
                errorMessage = DiscardThresholdBoxes.getErrorMessage(valueToValidate.getResultDiscardingThresholds(), stringMessages);
            }
            return errorMessage;
        }
    }

    public SeriesEditDialog(RegattaDTO regatta, SeriesDTO selectedSeries, StringMessages stringMessages,DialogCallback<SeriesDescriptor> callback) {
        super(stringMessages.actionEditSeries(), null, stringMessages.ok(), stringMessages.cancel(),
                new RaceDialogValidator(regatta, stringMessages), callback);
        this.selectedSeries = selectedSeries;
        this.stringMessages = stringMessages;
        discardThresholdBoxes = new DiscardThresholdBoxes(this, selectedSeries.getDiscardThresholds(), stringMessages);
    }
    
    @Override
    protected SeriesDescriptor getResult() {
        SeriesDTO selectedSeries = getSelectedSeries();
        List<RaceColumnDTO> races = new ArrayList<RaceColumnDTO>();
        for (String name : raceNamesEditor.getValue()) {
            RaceColumnDTO raceColumnDTO = findRaceColumnInSeriesByName(selectedSeries, name);
            if (raceColumnDTO == null) {
                raceColumnDTO = new RaceColumnDTO(/* isValidInTotalScore not relevant here; not in scope of a leaderboard */ null);
                raceColumnDTO.setName(name);
            }
            races.add(raceColumnDTO);
        }
        return new SeriesDescriptor(selectedSeries, races, isMedalCheckbox.getValue(),
                useSeriesResultDiscardingThresholdsCheckbox.getValue() ? discardThresholdBoxes.getDiscardThresholds()
                        : null, startWithZeroScoreCheckbox.getValue(),
                firstColumnIsNonDiscardableCarryForwardCheckbox.getValue());
    }

    private RaceColumnDTO findRaceColumnInSeriesByName(SeriesDTO series, String raceColumnName) {
        RaceColumnDTO result = null;
        if (series != null) {
            for (RaceColumnDTO raceColumn : series.getRaceColumns()) {
                if (raceColumn.getName().equals(raceColumnName)) {
                    result = raceColumn;
                    break;
                }
            }
        }
        return result;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        additionalWidgetPanel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            additionalWidgetPanel.add(additionalWidget);
        }
        HorizontalPanel seriesPanel = new HorizontalPanel();
        seriesPanel.setSpacing(3);
        String seriesName = getSelectedSeries().getName();
        seriesPanel.add(new Label(stringMessages.series() + ": " + seriesName));
        additionalWidgetPanel.add(seriesPanel);
        isMedalCheckbox = createCheckbox(stringMessages.medalSeries());
        isMedalCheckbox.setValue(selectedSeries.isMedal());
        additionalWidgetPanel.add(isMedalCheckbox);
        startWithZeroScoreCheckbox = createCheckbox(stringMessages.startsWithZeroScore());
        startWithZeroScoreCheckbox.setValue(selectedSeries.isStartsWithZeroScore());
        additionalWidgetPanel.add(startWithZeroScoreCheckbox);
        firstColumnIsNonDiscardableCarryForwardCheckbox = createCheckbox(stringMessages.firstRaceIsNonDiscardableCarryForward());
        firstColumnIsNonDiscardableCarryForwardCheckbox.setValue(selectedSeries.isFirstColumnIsNonDiscardableCarryForward());
        additionalWidgetPanel.add(firstColumnIsNonDiscardableCarryForwardCheckbox);
        useSeriesResultDiscardingThresholdsCheckbox = createCheckbox(stringMessages.seriesDefinesResultDiscardingRule());
        useSeriesResultDiscardingThresholdsCheckbox.setValue(selectedSeries.getDiscardThresholds() != null);
        useSeriesResultDiscardingThresholdsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                discardThresholdBoxes.getWidget().setVisible(event.getValue());
            }
        });
        additionalWidgetPanel.add(useSeriesResultDiscardingThresholdsCheckbox);
        additionalWidgetPanel.add(discardThresholdBoxes.getWidget());
        discardThresholdBoxes.getWidget().setVisible(useSeriesResultDiscardingThresholdsCheckbox.getValue());
        raceNamesEditor = new StringListEditorComposite(getExistingRacesOfSeries(), new RaceNamesEditorUi(stringMessages, resources.removeIcon(), seriesName));
        additionalWidgetPanel.add(raceNamesEditor);
        return additionalWidgetPanel;
    }
    
    private StringListEditorComposite raceNamesEditor;

    private List<String> getExistingRacesOfSeries() {
        List<String> names = new ArrayList<String>();
        SeriesDTO selectedSeries = getSelectedSeries();
        if(selectedSeries != null && !selectedSeries.getRaceColumns().isEmpty()) {
            for(RaceColumnDTO raceColumn: selectedSeries.getRaceColumns()) {
                names.add(raceColumn.getName());
            }
        }
        return names;
    }

    private SeriesDTO getSelectedSeries() {
        return selectedSeries;
    }
    
    private class RaceNamesEditorUi extends StringListEditorComposite.ExpandedUi {
        
        private final ListBox addRacesListBox;
        private final TextBox raceNamePrefixTextBox;
        private final Button addRacesBtn;
        
        private final String seriesName;
        
        public RaceNamesEditorUi(StringMessages stringMessages, ImageResource removeImage, String seriesName) {
            super(stringMessages, removeImage, Collections.<String>emptyList());
            this.addRacesListBox = createListBox(false);
            this.raceNamePrefixTextBox = createTextBox(null);
            this.addRacesBtn = new Button(stringMessages.add());
            this.seriesName = seriesName;
        }
        
        @Override
        protected Widget createAddWidget() {
            HorizontalPanel addRacesPanel = new HorizontalPanel();
            addRacesPanel.setSpacing(3);
            addRacesPanel.add(new Label(stringMessages.add()));
            addRacesPanel.add(addRacesListBox);
            for(int i = 1; i <= 50; i++) {
                addRacesListBox.addItem("" + i);
            }
            addRacesListBox.setSelectedIndex(0);
            if ("Default".equals(seriesName)) {
                raceNamePrefixTextBox.setText("R");
            } else {
                raceNamePrefixTextBox.setText(seriesName.substring(0, 1).toUpperCase());
            }
            raceNamePrefixTextBox.setWidth("20px");
            addRacesPanel.add(raceNamePrefixTextBox);
            addRacesBtn.addStyleName("inlineButton");
            addRacesBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    SeriesDTO selectedSeries = getSelectedSeries();
                    if(selectedSeries != null) {
                        String racePrefix = raceNamePrefixTextBox.getText();
                        int racesCountToCreate = addRacesListBox.getSelectedIndex()+1;
                        int currentSize = context.getValue().size();
                        for(int i = 1; i <= racesCountToCreate; i++) {
                            String raceName = racePrefix;
                            if(racesCountToCreate != 1 || selectedSeries.getRaceColumns().size() > 0) {
                                raceName += (currentSize + i);
                            }
                            addValue(raceName);
                        }
                        validate();
                    } else {
                        Window.alert("Please select a series first.");
                    }
                }
            });
            addRacesPanel.add(addRacesBtn);
            return addRacesPanel;
        }
    }
}
