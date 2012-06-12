package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class RaceColumnInRegattaSeriesDialog extends DataEntryDialog<Pair<SeriesDTO, List<RaceColumnDTO>>> {
    private final RegattaDTO regatta;
    private final ListBox seriesListBox;
    private final ListBox addRacesListBox;
    private Button addRacesBtn;
    private final List<TextBox> raceNameEntryFields;
    private final List<Button> raceNameDeleteButtons;
    private final StringMessages stringConstants;
    private final TextBox raceNamePrefixTextBox;
    private final boolean hasOneSeries;
    private Grid raceColumnsGrid;
    private VerticalPanel additionalWidgetPanel;

    private static class RaceDialogValidator implements Validator<Pair<SeriesDTO, List<RaceColumnDTO>>> {
        private StringMessages stringConstants;

        public RaceDialogValidator(RegattaDTO regatta, StringMessages stringConstants) {
            this.stringConstants = stringConstants;
        }

        @Override
        public String getErrorMessage(Pair<SeriesDTO, List<RaceColumnDTO>> valueToValidate) {
            String errorMessage = null;

            SeriesDTO series = valueToValidate.getA();
            if(series == null) {
                errorMessage = "You must select a series";
            }

            if(errorMessage == null) {
                List<RaceColumnDTO> raceColumnsToValidate = valueToValidate.getB();
                int index = 0;
                boolean raceColumnNameNotEmpty = true;

                for (RaceColumnDTO raceColumn : raceColumnsToValidate) {
                    raceColumnNameNotEmpty = raceColumn.name != null && raceColumn.name.length() > 0;
                    if (!raceColumnNameNotEmpty) {
                        break;
                    }
                    index++;
                }

                int index2 = 0;
                boolean raceColumnUnique = true;

                HashSet<String> setToFindDuplicates = new HashSet<String>();
                for (RaceColumnDTO raceColumn : raceColumnsToValidate) {
                    if (!setToFindDuplicates.add(raceColumn.name)) {
                        raceColumnUnique = false;
                        break;
                    }
                    index2++;
                }

                if (!raceColumnNameNotEmpty) {
                    errorMessage = stringConstants.race() + " " + (index + 1) + ": "
                            + stringConstants.pleaseEnterNonEmptyName();
                } else if (!raceColumnUnique) {
                    errorMessage = stringConstants.race() + " " + (index2 + 1) + ": "
                            + stringConstants.raceWithThisNameAlreadyExists();
                }
            }
            
            return errorMessage;
        }

    }

    public RaceColumnInRegattaSeriesDialog(RegattaDTO regatta, StringMessages stringConstants,
            AsyncCallback<Pair<SeriesDTO, List<RaceColumnDTO>>> callback) {
        super(stringConstants.actionEditRaces(), null, stringConstants.ok(), stringConstants.cancel(),
                new RaceDialogValidator(regatta, stringConstants), callback);
        this.regatta = regatta;
        this.stringConstants = stringConstants;
        this.hasOneSeries = regatta.series.size() == 1;
        seriesListBox = createListBox(false);
        addRacesListBox = createListBox(false);
        raceNamePrefixTextBox = createTextBox(null);
        raceNameEntryFields = new ArrayList<TextBox>();
        raceNameDeleteButtons = new ArrayList<Button>();
        raceColumnsGrid = new Grid(0, 0);
    }

    private Widget createRaceNameWidget(String defaultName, boolean enabled) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setVisibleLength(40);
        textBox.setEnabled(enabled);
        raceNameEntryFields.add(textBox);
        return textBox; 
    }

    private Widget createRaceNameDeleteButtonWidget() {
        final Button raceNameDeleteBtn = new Button(stringConstants.delete()); 
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
            }
        });
        return raceNameDeleteBtn; 
    }
    
    @Override
    protected Pair<SeriesDTO, List<RaceColumnDTO>> getResult() {
        List<RaceColumnDTO> races = new ArrayList<RaceColumnDTO>();
        int racesCount = raceNameEntryFields.size();
        for(int i = 0; i < racesCount; i++) {
            RaceColumnDTO raceColumnDTO = new RaceColumnDTO();
            raceColumnDTO.name = raceNameEntryFields.get(i).getValue();
            races.add(raceColumnDTO);
        }
        return new Pair<SeriesDTO, List<RaceColumnDTO>>(getSelectedSeries(), races);
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
        seriesPanel.add(new Label(stringConstants.series() + ":"));
        if(hasOneSeries) {
            String seriesName = regatta.series.get(0).name;
            seriesListBox.addItem(seriesName);
            seriesListBox.setSelectedIndex(0);
            if("Default".equals(seriesName)) {
                raceNamePrefixTextBox.setText("R");
            } else {
                raceNamePrefixTextBox.setText(seriesName.substring(0, 1).toUpperCase());
            }
        } else {
            seriesListBox.addItem("Please select a series");
            for (SeriesDTO series : regatta.series) {
                seriesListBox.addItem(series.name);
            }
        }
        
        seriesListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                SeriesDTO selectedSeries = getSelectedSeries();
                if(selectedSeries == null) {
                    raceNamePrefixTextBox.setText("");
                } else {
                    raceNamePrefixTextBox.setText(selectedSeries.name.substring(0, 1).toUpperCase());
                }
                fillExistingRacesOfSeries();
            }
        });
        
        seriesPanel.add(seriesListBox);
        additionalWidgetPanel.add(seriesPanel);
        
        // add races controls
        HorizontalPanel addRacesPanel = new HorizontalPanel();
        addRacesPanel.setSpacing(3);
        addRacesPanel.add(new Label("Add a number of races:"));
        addRacesPanel.add(addRacesListBox);
        for(int i = 1; i <= 10; i++) {
            addRacesListBox.addItem("" + i);
        }
        addRacesListBox.setSelectedIndex(0);
        raceNamePrefixTextBox.setWidth("20px");
        addRacesPanel.add(raceNamePrefixTextBox);
        addRacesBtn = new Button(stringConstants.add());
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
                } else {
                    Window.alert("Please select a series first.");
                }
            }
        });
        addRacesPanel.add(addRacesBtn);
        additionalWidgetPanel.add(addRacesPanel);
        
        additionalWidgetPanel.add(createHeadlineLabel(stringConstants.races()));
        additionalWidgetPanel.add(raceColumnsGrid);

        if(hasOneSeries) {
            fillExistingRacesOfSeries();
        }

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
            raceColumnsGrid.setHTML(0, 0, stringConstants.name());
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
        SeriesDTO result = null;
        int selIndex = seriesListBox.getSelectedIndex();
        if(selIndex > 0 || hasOneSeries) { // the zero index represents the 'no selection' text
            String itemText = seriesListBox.getItemText(selIndex);
            for(SeriesDTO seriesDTO: regatta.series) {
                if(seriesDTO.name.equals(itemText)) {
                    result = seriesDTO;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void show() {
        super.show();
    }
}
