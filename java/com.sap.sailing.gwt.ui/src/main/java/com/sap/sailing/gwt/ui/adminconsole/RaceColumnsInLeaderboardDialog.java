package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

public class RaceColumnsInLeaderboardDialog extends DataEntryDialog<List<RaceColumnDTO>> {
    private final ListBox addRacesListBox;
    private Button addRacesBtn;
    private final List<TextBox> raceNameEntryFields;
    private final List<CheckBox> isMedalRaceCheckboxes;
    private final StringMessages stringConstants;
    private final TextBox raceNamePrefixTextBox;
    private Grid raceColumnsGrid;
    private VerticalPanel additionalWidgetPanel;
    private List<RaceColumnDTO> existingRaces;
    
    private static class RaceDialogValidator implements Validator<List<RaceColumnDTO>> {
        private StringMessages stringConstants;

        public RaceDialogValidator(StringMessages stringConstants) {
            this.stringConstants = stringConstants;
        }

        @Override
        public String getErrorMessage(List<RaceColumnDTO> raceColumnsWithFleetToValidate) {
            String errorMessage = null;

            List<RaceColumnDTO> raceColumnsToValidate = new ArrayList<RaceColumnDTO>();
            for (RaceColumnDTO raceColumnDTO : raceColumnsToValidate) {
                raceColumnsToValidate.add(raceColumnDTO);
            }
            
            if(errorMessage == null) {
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
                            + stringConstants.pleaseEnterAName();
                } else if (!raceColumnUnique) {
                    errorMessage = stringConstants.race() + " " + (index2 + 1) + ": "
                            + stringConstants.raceWithThisNameAlreadyExists();
                }
            }
            
            return errorMessage;
        }

    }

    public RaceColumnsInLeaderboardDialog(List<RaceColumnDTO> existingRaces, StringMessages stringConstants,
            AsyncCallback<List<RaceColumnDTO>> callback) {
        super(stringConstants.actionEditRaces(), null, stringConstants.ok(), stringConstants.cancel(),
                new RaceDialogValidator(stringConstants), callback);
        this.existingRaces = existingRaces;
        this.stringConstants = stringConstants;
        addRacesListBox = createListBox(false);
        raceNamePrefixTextBox = createTextBox(null);
        raceNameEntryFields = new ArrayList<TextBox>();
        isMedalRaceCheckboxes = new ArrayList<CheckBox>();
        raceColumnsGrid = new Grid(0, 0);
    }

    private Widget createRaceNameWidget(String defaultName, boolean enabled) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setVisibleLength(40);
        textBox.setEnabled(enabled);
        raceNameEntryFields.add(textBox);
        return textBox; 
    }

    @Override
    protected List<RaceColumnDTO> getResult() {
        List<RaceColumnDTO> racesWithFleet = new ArrayList<RaceColumnDTO>();
        int racesCount = raceNameEntryFields.size();
        for(int i = 0; i < racesCount; i++) {
            String raceColumnName = raceNameEntryFields.get(i).getValue();
            RaceColumnDTO raceColumnDTO = new RaceColumnDTO();
            raceColumnDTO.name = raceColumnName;
            raceColumnDTO.setMedalRace(false);
            racesWithFleet.add(raceColumnDTO);
        }
        return racesWithFleet;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        additionalWidgetPanel = new VerticalPanel();

        raceNamePrefixTextBox.setText("R");
       
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
        addRacesBtn.addStyleName("inlineButton");
        addRacesBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                    String racePrefix = raceNamePrefixTextBox.getText();
                    int racesCountToCreate = addRacesListBox.getSelectedIndex()+1;
                    int currentSize = raceNameEntryFields.size();
                    for(int i = 1; i <= racesCountToCreate; i++) {
                        String raceName = racePrefix;
                        if(racesCountToCreate != 1 || existingRaces.size() > 0) {
                            raceName += (currentSize + i);
                        }
                        createRaceNameWidget(raceName, true);
                    }
                    updateRaceColumnsGrid(additionalWidgetPanel);

            }
        });
        addRacesPanel.add(addRacesBtn);
        additionalWidgetPanel.add(addRacesPanel);
        
        additionalWidgetPanel.add(createHeadlineLabel(stringConstants.races()));
        additionalWidgetPanel.add(raceColumnsGrid);

        for(RaceColumnDTO raceColumn: existingRaces) {
            createRaceNameWidget(raceColumn.getRaceColumnName(), false);
        }
        updateRaceColumnsGrid(additionalWidgetPanel);

        return additionalWidgetPanel;
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
                raceColumnsGrid.setWidget(i+1, 1, isMedalRaceCheckboxes.get(i));
            }
        } else {
            raceColumnsGrid = new Grid(0, 0);
        }

        parentPanel.insert(raceColumnsGrid, widgetIndex);
    }

    @Override
    public void show() {
        super.show();
    }
}
