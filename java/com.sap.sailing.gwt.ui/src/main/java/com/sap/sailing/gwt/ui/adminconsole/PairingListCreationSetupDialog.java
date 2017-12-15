package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
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
    private Iterable<CheckBox> selectedSeriesCheckboxes;
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
        this.competitorCountTextBox.ensureDebugId("CompetitorCountBox");
        this.flightMultiplierTextBox = createIntegerBox(0, 2);
        this.flightMultiplierTextBox.setEnabled(false);
        this.flightMultiplierTextBox.setValue(1);
        this.flightMultiplierTextBox.ensureDebugId("FlightMultiplierIntegerBox");
        this.flightMultiplierCheckBox = createCheckbox("Flight Multiplier");
        this.ensureDebugId("PairingListCreationSetupDialog");
        
        this.flightMultiplierCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                flightMultiplierTextBox.setEnabled(event.getValue());
            }
        });
        flightMultiplierCheckBox.ensureDebugId("CompetitorCountTextBox");
        
        List<CheckBox> checkboxes = new ArrayList<CheckBox>();
        for(String seriesName : getSeriesNamesFromAllRaces(leaderboardDTO.getRaceList())) {
            CheckBox current = createCheckbox(seriesName);
            current.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if(event.getValue()) {
                        disableSelectedSeriesCheckBoxes(leaderboardDTO);
                    } else {
                        if(Util.size(getCheckedSelectedCheckBoxes())>0) {
                            
                        } else {
                            enableAllSelectedSeriesCheckBoxes();
                        }
                    }
                }
            });
            current.ensureDebugId("SelectedFlightsCheckbox: " + seriesName);
            checkboxes.add(current);
        }
        selectedSeriesCheckboxes = checkboxes;
        this.flightMultiplierCheckBox.ensureDebugId("FlightMultiplierCheckBox");
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        
        CaptionPanel infoPanel = new CaptionPanel();
        infoPanel.setCaptionText("Info");
        panel.add(infoPanel);
              
        ScrollPanel infoScrollPanel = new ScrollPanel();
        infoScrollPanel.setPixelSize((Window.getClientWidth() / 3), 150);
        infoScrollPanel.add(new Label(stringMessages.pairingListCreationInfo()));
        
        infoPanel.add(infoScrollPanel);
        
        Grid formGrid = new Grid(4, 2);
        panel.add(formGrid);
        
        //TODO add stringMessages
        formGrid.setWidget(0, 0, new Label("Please set the competitors count:"));
        formGrid.setWidget(0, 1, this.competitorCountTextBox);
        formGrid.setWidget(1, 0, this.flightMultiplierCheckBox);
        formGrid.setWidget(1, 1, this.flightMultiplierTextBox);
        
        formGrid.setWidget(2, 0, new Label("Please select one or more series that should be in a PairingList:"));
        int count = 0;
        for(CheckBox current : selectedSeriesCheckboxes) {
            formGrid.setWidget(2+count, 1, current);
            count++;
        }
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

        if (this.flightMultiplierCheckBox.getValue()) {
            dto.setFlightMultiplier(this.flightMultiplierTextBox.getValue());
        } else {
            dto.setFlightMultiplier(1);
        }

        List<String> selectedFlightNames = new ArrayList<>();
        for (CheckBox box : getCheckedSelectedCheckBoxes()) {
            selectedFlightNames.addAll(getRaceColumnNamesFromSeriesName(box.getText(), leaderboardDTO.getRaceList()));
        }
        
        dto.setSelectedFlightNames(selectedFlightNames);
        
        return dto; 
    }
    
    public void setDefaultCompetitorCount(int competitorCount) {
        if (this.competitorCountTextBox.getValue() == 0) {
            this.competitorCountTextBox.setValue(competitorCount);
            this.validateAndUpdate();
        }
    }
    
    //private void changeFlights
    
    // filter medal race
    private Iterable<String> getSeriesNamesFromAllRaces(Iterable<RaceColumnDTO> raceColumns) {
        List<String> result = new ArrayList<>();
        for (RaceColumnDTO raceColumn : raceColumns) {
            if (raceColumn.isMedalRace()) {

            } else {
                if (result.contains(raceColumn.getSeriesName())) {

                } else {
                    result.add(raceColumn.getSeriesName());
                }
            }
        }
        return result;
    }
    
    private RaceColumnDTO getOneRaceFromSeriesName(String seriesName, Iterable<RaceColumnDTO> raceColumns) {
        for(RaceColumnDTO raceColumn : raceColumns) {
            if(!raceColumn.isMedalRace() && seriesName.equals(raceColumn.getSeriesName())) {
                return raceColumn;
            }
        }
        return null;
    }
    
    private List<String> getRaceColumnNamesFromSeriesName(String seriesName, Iterable<RaceColumnDTO> raceColumns) {
        List<String> result = new ArrayList<>();
        for(RaceColumnDTO raceColumn : raceColumns) {
            if(!raceColumn.isMedalRace() && seriesName.equals(raceColumn.getSeriesName())) {
                result.add(raceColumn.getName());
            }
        }
        return result;
    }
    
    public Iterable<CheckBox> getCheckedSelectedCheckBoxes() {
        List<CheckBox> result = new ArrayList<>();
        for(CheckBox box : selectedSeriesCheckboxes) {
            if(box.getValue()) {
                result.add(box);
            }
        }
        return result;
    }
    
    private void disableSelectedSeriesCheckBoxes(StrippedLeaderboardDTO leaderboardDTO) {
        Iterable<CheckBox> boxes = getCheckedSelectedCheckBoxes();
        if(Util.size(boxes)>1) {

        } else {
            
            RaceColumnDTO race = getOneRaceFromSeriesName(Util.get(boxes, 0).getText(), leaderboardDTO.getRaceList());
            for(CheckBox box : selectedSeriesCheckboxes) {
                if(race.getFleets().size() == getOneRaceFromSeriesName(box.getText(), leaderboardDTO.getRaceList()).getFleets().size()) {
                    
                } else {
                    box.setEnabled(false);
                }
            }
        }
    }
    
    private void enableAllSelectedSeriesCheckBoxes() {
        for(CheckBox box : selectedSeriesCheckboxes) {
            box.setEnabled(true);
        }
    }
    
}
