package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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

    private ListBox boatChangeFactorListBox;

    protected static class PairingListParameterValidator extends AbstractPairingListParameterValidator {
        public PairingListParameterValidator(StringMessages stringMessages) {
            super(stringMessages);
        }
    }

    public PairingListCreationSetupDialog(StrippedLeaderboardDTO leaderboardDTO, StringMessages stringMessages,
            DialogCallback<PairingListTemplateDTO> callback) {
        super(leaderboardDTO, stringMessages.pairingList(), stringMessages,
                new PairingListParameterValidator(stringMessages), callback);
        this.competitorCountTextBox = createIntegerBox(leaderboardDTO.competitorsCount, 2);
        this.competitorCountTextBox.addValueChangeHandler(evt -> {
            updateBoatChangeFactorSelection(leaderboardDTO.getRaceList().get(0).getFleets().size());
        });
        this.competitorCountTextBox.ensureDebugId("CompetitorCountBox");
        this.flightMultiplierTextBox = createIntegerBox(1, 2);
        this.flightMultiplierTextBox.setEnabled(false);
        this.flightMultiplierTextBox.ensureDebugId("FlightMultiplierIntegerBox");
        this.flightMultiplierCheckBox = createCheckbox(this.stringMessages.amountOfFlightRepeats());
        this.flightMultiplierCheckBox.setTitle(this.stringMessages.multiplierInfo());
        this.flightMultiplierCheckBox.ensureDebugId("FlightMultiplierCheckBox");
        this.ensureDebugId("PairingListCreationSetupDialog");
        this.boatChangeFactorListBox = new ListBox();
        this.boatChangeFactorListBox.setVisibleItemCount(1);
        updateBoatChangeFactorSelection(leaderboardDTO.getRaceList().get(0).getFleets().size());

        this.flightMultiplierCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                flightMultiplierTextBox.setEnabled(event.getValue());
                if (event.getValue()) {
                    if (Util.size(getCheckedSelectedCheckBoxes()) > 0) {
                        enableOrDisableAllSelectedSeriesCheckBoxes(false, true);
                    }
                } else {
                    flightMultiplierTextBox.setText("1");
                    enableOrDisableAllSelectedSeriesCheckBoxes(true, false);
                }
            }
        });
        List<CheckBox> checkboxes = new ArrayList<CheckBox>();
        for (String seriesName : getSeriesNamesFromAllRaces(leaderboardDTO.getRaceList())) {
            CheckBox current = createCheckbox(seriesName);
            current.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    validateCheckboxes(event.getValue());
                }
            });
            current.ensureDebugId("SelectedFlightsCheckbox: " + seriesName);
            checkboxes.add(current);
        }
        selectedSeriesCheckboxes = checkboxes;
        Util.get(selectedSeriesCheckboxes, 0).setValue(true);
        validateCheckboxes(true);
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
        Grid formGrid = new Grid(Util.size(selectedSeriesCheckboxes) + 3, 2);
        panel.add(formGrid);
        formGrid.setWidget(0, 0, new Label(stringMessages.setCompetitors()));
        formGrid.setWidget(0, 1, this.competitorCountTextBox);
        formGrid.setWidget(1, 0, this.flightMultiplierCheckBox);
        formGrid.setWidget(1, 1, this.flightMultiplierTextBox);
        formGrid.setWidget(2, 0, new Label(stringMessages.setBoatChangeFactor()));
        formGrid.setWidget(2, 1, boatChangeFactorListBox);
        formGrid.setWidget(3, 0, new Label(stringMessages.seriesHint()));
        int count = 0;
        for (CheckBox current : selectedSeriesCheckboxes) {
            formGrid.setWidget(3 + count, 1, current);
            count++;
        }
        return panel;
    }

    @Override
    protected PairingListTemplateDTO getResult() {
        PairingListTemplateDTO dto = new PairingListTemplateDTO(this.competitorCountTextBox.getValue(),
                this.flightMultiplierTextBox.getValue(), this.boatChangeFactorListBox.getSelectedIndex());
        if (Util.size(this.getCheckedSelectedCheckBoxes()) > 0) {
            String seriesName = Util.get(this.getCheckedSelectedCheckBoxes(), 0).getText();
            dto.setGroupCount(
                    this.getOneRaceFromSeriesName(seriesName, leaderboardDTO.getRaceList()).getFleets().size());
        } else {
            dto.setGroupCount(0);
        }
        if (this.flightMultiplierCheckBox.getValue()) {
            dto.setFlightMultiplier(this.flightMultiplierTextBox.getValue());
        } else {
            dto.setFlightMultiplier(1);
        }
        List<String> selectedFlightNames = getSelectedFlightNames();
        dto.setSelectedFlightNames(selectedFlightNames);
        dto.setFlightCount(selectedFlightNames.size());
        dto.setTolerance(Integer.parseInt(boatChangeFactorListBox.getSelectedValue()));
        return dto;
    }

    private List<String> getSelectedFlightNames() {
        List<String> selectedFlightNames = new ArrayList<>();
        for (CheckBox box : getCheckedSelectedCheckBoxes()) {
            selectedFlightNames.addAll(getRaceColumnNamesFromSeriesName(box.getText(), leaderboardDTO.getRaceList()));
        }
        return selectedFlightNames;
    }

    public void setDefaultCompetitorCount(final int competitorCount) {
        if (this.competitorCountTextBox.getValue() == 0) {
            this.competitorCountTextBox.setValue(competitorCount);
            this.validateAndUpdate();
        }
    }

    private void validateCheckboxes(boolean enabled) {
        if (enabled) {
            disableSelectedSeriesCheckBoxes(leaderboardDTO);
            if (flightMultiplierCheckBox.getValue()) {
                if (Util.size(getCheckedSelectedCheckBoxes()) > 0) {
                    enableOrDisableAllSelectedSeriesCheckBoxes(false, true);
                }
            } else {
                if (Util.size(getCheckedSelectedCheckBoxes()) > 1) {
                    flightMultiplierCheckBox.setEnabled(false);
                } else {
                    flightMultiplierCheckBox.setEnabled(true);
                }
            }
        } else {
            if (Util.size(getCheckedSelectedCheckBoxes()) <= 0) {
                enableOrDisableAllSelectedSeriesCheckBoxes(true, false);
            }
            if (Util.size(getCheckedSelectedCheckBoxes()) < 2) {
                flightMultiplierCheckBox.setEnabled(true);
            }
        }
        updateBoatChangeFactorSelection(getSelectedFlightNames().size());
    }

    private Iterable<String> getSeriesNamesFromAllRaces(final Iterable<RaceColumnDTO> raceColumns) {
        List<String> result = new ArrayList<>();
        for (RaceColumnDTO raceColumn : raceColumns) {
            if (!raceColumn.isMedalRace()) {
                if (result.contains(raceColumn.getSeriesName())) {

                } else {
                    result.add(raceColumn.getSeriesName());
                }
            }
        }
        return result;
    }

    private RaceColumnDTO getOneRaceFromSeriesName(final String seriesName, final Iterable<RaceColumnDTO> raceColumns) {
        for (RaceColumnDTO raceColumn : raceColumns) {
            if (!raceColumn.isMedalRace() && seriesName.equals(raceColumn.getSeriesName())) {
                return raceColumn;
            }
        }
        return null;
    }

    private List<String> getRaceColumnNamesFromSeriesName(final String seriesName,
            final Iterable<RaceColumnDTO> raceColumns) {
        List<String> result = new ArrayList<>();
        for (RaceColumnDTO raceColumn : raceColumns) {
            if (!raceColumn.isMedalRace() && seriesName.equals(raceColumn.getSeriesName())) {
                result.add(raceColumn.getName());
            }
        }
        return result;
    }

    public Iterable<CheckBox> getCheckedSelectedCheckBoxes() {
        List<CheckBox> result = new ArrayList<>();
        for (CheckBox box : selectedSeriesCheckboxes) {
            if (box.getValue()) {
                result.add(box);
            }
        }
        return result;
    }

    private void disableSelectedSeriesCheckBoxes(final StrippedLeaderboardDTO leaderboardDTO) {
        Iterable<CheckBox> boxes = getCheckedSelectedCheckBoxes();
        if (Util.size(boxes) <= 1) {
            RaceColumnDTO race = getOneRaceFromSeriesName(Util.get(boxes, 0).getText(), leaderboardDTO.getRaceList());
            for (CheckBox box : selectedSeriesCheckboxes) {
                if (race.getFleets().size() == getOneRaceFromSeriesName(box.getText(), leaderboardDTO.getRaceList())
                        .getFleets().size()) {
                } else {
                    box.setEnabled(false);
                }
            }
        }
    }

    private void enableOrDisableAllSelectedSeriesCheckBoxes(final boolean enabled, final boolean exclusiveSelected) {
        if (exclusiveSelected) {
            for (CheckBox box : selectedSeriesCheckboxes) {
                if (box.getValue()) {
                    continue;
                }
                box.setEnabled(enabled);
            }
        } else {
            if (Util.size(getCheckedSelectedCheckBoxes()) > 0) {
                return;
            }
            for (CheckBox box : selectedSeriesCheckboxes) {
                box.setEnabled(enabled);
            }
        }
    }
    
    private void updateBoatChangeFactorSelection(final int flightCount) {
        this.boatChangeFactorListBox.clear();
        IntStream.range(0, (getCompetitorCountInput() / flightCount) + 1).forEach(i -> {
            this.boatChangeFactorListBox.addItem(String.valueOf(i));
        });
    }
    
    private int getCompetitorCountInput() {
        try {
            return Integer.parseInt(competitorCountTextBox.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
