package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class SeriesEditDialog extends DataEntryDialog<SeriesDescriptor> {
    private final ListBox addRacesListBox;
    private Button addRacesBtn;
    private CheckBox isMedalCheckbox;
    private CheckBox useSeriesResultDiscardingThresholdsCheckbox;
    private final List<TextBox> raceNameEntryFields;
    private final List<Button> raceNameDeleteButtons;
    private final StringMessages stringMessages;
    private final TextBox raceNamePrefixTextBox;
    private Grid raceColumnsGrid;
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
                if (!seriesDTO.name.equals(seriesToValidate.name)) {
                    for (RaceColumnDTO raceColumn : seriesDTO.getRaceColumns()) {
                        raceColumnNamesOfOtherSeries.add(raceColumn.name);
                    }
                }
            }
            boolean raceColumnNameNotEmpty = true;
            RaceColumnDTO wrongRaceColumn = null;
            for (RaceColumnDTO raceColumn : raceColumnsToValidate) {
                raceColumnNameNotEmpty = raceColumn.name != null && raceColumn.name.length() > 0;
                if (!raceColumnNameNotEmpty) {
                    wrongRaceColumn = raceColumn;
                    break;
                }
            }
            boolean raceColumnUniqueInSeries = true;
            boolean raceColumnUniqueInRegatta = true;
            HashSet<String> setToFindDuplicates = new HashSet<String>();
            for (RaceColumnDTO raceColumn : raceColumnsToValidate) {
                if (!setToFindDuplicates.add(raceColumn.name)) {
                    raceColumnUniqueInSeries = false;
                    wrongRaceColumn = raceColumn;
                    break;
                } else if(raceColumnNamesOfOtherSeries.contains(raceColumn.name)) {
                    raceColumnUniqueInRegatta = false;
                    wrongRaceColumn = raceColumn;
                    break;
                } 
            }
            if (!raceColumnNameNotEmpty) {
                errorMessage = stringMessages.race() + " " + wrongRaceColumn.name + ": "
                        + stringMessages.pleaseEnterAName();
            } else if (!raceColumnUniqueInSeries) {
                errorMessage = stringMessages.race() + " " +  wrongRaceColumn.name + ": "
                        + stringMessages.raceWithThisNameAlreadyExists();
            }  else if (!raceColumnUniqueInRegatta) {
                errorMessage = stringMessages.race() + " " +  wrongRaceColumn.name + ": "
                        + stringMessages.raceWithThisNameAlreadyExistsInRegatta();
            } else {
                errorMessage = DiscardThresholdBoxes.getErrorMessage(valueToValidate.getResultDiscardingThresholds(), stringMessages);
            }
            return errorMessage;
        }
    }

    public SeriesEditDialog(RegattaDTO regatta, SeriesDTO selectedSeries, StringMessages stringMessages,DialogCallback<SeriesDescriptor> callback) {
        super(stringMessages.actionEditRaces(), null, stringMessages.ok(), stringMessages.cancel(),
                new RaceDialogValidator(regatta, stringMessages), callback);
        this.selectedSeries = selectedSeries;
        this.stringMessages = stringMessages;
        addRacesListBox = createListBox(false);
        raceNamePrefixTextBox = createTextBox(null);
        raceNameEntryFields = new ArrayList<TextBox>();
        raceNameDeleteButtons = new ArrayList<Button>();
        raceColumnsGrid = new Grid(0, 0);
        discardThresholdBoxes = new DiscardThresholdBoxes(this, selectedSeries.getDiscardThresholds(), stringMessages);
    }

    private Widget createRaceNameWidget(String defaultName, boolean enabled) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setVisibleLength(40);
        textBox.setEnabled(enabled);
        raceNameEntryFields.add(textBox);
        return textBox; 
    }

    private Widget createRaceNameDeleteButtonWidget() {
        final Button raceNameDeleteBtn = new Button(stringMessages.delete()); 
        raceNameDeleteBtn.addStyleName("inlineButton");
        raceNameDeleteButtons.add(raceNameDeleteBtn);
        raceNameDeleteBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int index = 0;
                for(Button btn: raceNameDeleteButtons) {
                    if(raceNameDeleteBtn == btn) {
                        break;
                    }
                    index++;
                }
                raceNameEntryFields.remove(index);
                raceNameDeleteButtons.remove(index);
                updateRaceColumnsGrid(additionalWidgetPanel);
                validate();
            }
        });
        return raceNameDeleteBtn; 
    }
    
    @Override
    protected SeriesDescriptor getResult() {
        SeriesDTO selectedSeries = getSelectedSeries();
        List<RaceColumnDTO> races = new ArrayList<RaceColumnDTO>();
        int racesCount = raceNameEntryFields.size();
        for (int i = 0; i < racesCount; i++) {
            String raceColumnName = raceNameEntryFields.get(i).getValue();
            RaceColumnDTO raceColumnDTO = findRaceColumnInSeriesByName(selectedSeries, raceColumnName);
            if (raceColumnDTO == null) {
                raceColumnDTO = new RaceColumnDTO(/* isValidInTotalScore not relevant here; not in scope of a leaderboard */ null);
                raceColumnDTO.name = raceColumnName;
            }
            races.add(raceColumnDTO);
        }
        return new SeriesDescriptor(selectedSeries, races, isMedalCheckbox.getValue(),
                useSeriesResultDiscardingThresholdsCheckbox.getValue() ?
                        discardThresholdBoxes.getDiscardThresholds() : null);
    }

    private RaceColumnDTO findRaceColumnInSeriesByName(SeriesDTO series, String raceColumnName) {
        RaceColumnDTO result = null;
        if(series != null) {
            for(RaceColumnDTO raceColumn: series.getRaceColumns()) {
                if(raceColumn.name.equals(raceColumnName)) {
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
        String seriesName = getSelectedSeries().name;
        seriesPanel.add(new Label(stringMessages.series() + ": " + seriesName));
        additionalWidgetPanel.add(seriesPanel);
        isMedalCheckbox = createCheckbox(stringMessages.medalSeries());
        isMedalCheckbox.setValue(selectedSeries.isMedal());
        additionalWidgetPanel.add(isMedalCheckbox);
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
        // add races controls
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
        addRacesBtn = new Button(stringMessages.add());
        addRacesBtn.addStyleName("inlineButton");
        addRacesBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SeriesDTO selectedSeries = getSelectedSeries();
                if(selectedSeries != null) {
                    String racePrefix = raceNamePrefixTextBox.getText();
                    int racesCountToCreate = addRacesListBox.getSelectedIndex()+1;
                    int currentSize = raceNameEntryFields.size();
                    for(int i = 1; i <= racesCountToCreate; i++) {
                        String raceName = racePrefix;
                        if(racesCountToCreate != 1 || selectedSeries.getRaceColumns().size() > 0) {
                            raceName += (currentSize + i);
                        }
                        createRaceNameWidget(raceName, true);
                        createRaceNameDeleteButtonWidget();
                    }
                    updateRaceColumnsGrid(additionalWidgetPanel);
                    validate();
                } else {
                    Window.alert("Please select a series first.");
                }
            }
        });
        addRacesPanel.add(addRacesBtn);
        additionalWidgetPanel.add(addRacesPanel);
        
        additionalWidgetPanel.add(createHeadlineLabel(stringMessages.races()));
        additionalWidgetPanel.add(raceColumnsGrid);

        fillExistingRacesOfSeries();

        return additionalWidgetPanel;
    }

    private void fillExistingRacesOfSeries() {
        SeriesDTO selectedSeries = getSelectedSeries();
        raceNameEntryFields.clear();
        raceNameDeleteButtons.clear();
        if(selectedSeries != null && !selectedSeries.getRaceColumns().isEmpty()) {
            for(RaceColumnDTO raceColumn: selectedSeries.getRaceColumns()) {
                createRaceNameWidget(raceColumn.name, false);
                createRaceNameDeleteButtonWidget();
            }
        }
        updateRaceColumnsGrid(additionalWidgetPanel);
    }
    
    private void updateRaceColumnsGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(raceColumnsGrid);
        parentPanel.remove(raceColumnsGrid);
        int raceNamesCount = raceNameEntryFields.size();
        if(raceNamesCount > 0) {
            raceColumnsGrid = new Grid(raceNamesCount + 1, 3);
            raceColumnsGrid.setCellSpacing(4);
            raceColumnsGrid.setHTML(0, 0, stringMessages.name());
            for(int i = 0; i < raceNamesCount; i++) {
                raceColumnsGrid.setWidget(i+1, 0, raceNameEntryFields.get(i));
                raceColumnsGrid.setWidget(i+1, 1, raceNameDeleteButtons.get(i));
            }
        } else {
            raceColumnsGrid = new Grid(0, 0);
        }

        parentPanel.insert(raceColumnsGrid, widgetIndex);
    }

    private SeriesDTO getSelectedSeries() {
        return selectedSeries;
    }

    @Override
    public void show() {
        super.show();
    }
}
