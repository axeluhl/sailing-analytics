package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class LeaderboardSettingsDialogComponent implements SettingsDialogComponent<LeaderboardSettings> {
    private final Iterable<RaceColumnDTO> raceColumnSelection;
    private final Map<RaceColumnDTO, CheckBox> raceColumnCheckboxes;
    private final List<RaceColumnDTO> raceAllRaceColumns;
    private final List<DetailType> maneuverDetailSelection;
    private final Map<DetailType, CheckBox> maneuverDetailCheckboxes;
    private final List<DetailType> legDetailSelection;
    private final Map<DetailType, CheckBox> legDetailCheckboxes;
    private final List<DetailType> raceDetailSelection;
    private final Map<DetailType, CheckBox> raceDetailCheckboxes;
    private final List<DetailType> overallDetailSelection;
    private final Map<DetailType, CheckBox> overallDetailCheckboxes;
    private final StringMessages stringConstants;
    private LongBox refreshIntervalInSecondsBox;
    private LongBox delayInSecondsBox;
    private final boolean autoExpandPreSelectedRace;
    private final long delayBetweenAutoAdvancesInMilliseconds;
    private final long delayInMilliseconds;

    public LeaderboardSettingsDialogComponent(List<DetailType> maneuverDetailSelection,
            List<DetailType> legDetailSelection, List<DetailType> raceDetailSelection, List<DetailType> overallDetailSelection,
            List<RaceColumnDTO> raceAllRaceColumns,
            Iterable<RaceColumnDTO> raceColumnSelection, boolean autoExpandPreSelectedRace, long delayBetweenAutoAdvancesInMilliseconds,
            long delayInMilliseconds, StringMessages stringConstants) {
        this.raceAllRaceColumns = raceAllRaceColumns;
        this.maneuverDetailSelection = maneuverDetailSelection;
        maneuverDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        this.raceColumnSelection = raceColumnSelection;
        raceColumnCheckboxes = new LinkedHashMap<RaceColumnDTO, CheckBox>();
        this.legDetailSelection = legDetailSelection;
        legDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        this.raceDetailSelection = raceDetailSelection;
        raceDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        this.overallDetailSelection = overallDetailSelection;
        overallDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        this.stringConstants = stringConstants;
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        this.delayInMilliseconds = delayInMilliseconds;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        FlowPanel dialogPanel = new FlowPanel();
        dialogPanel.add(createSelectedRacesPanel(dialog));
        dialogPanel.add(createOverallDetailPanel(dialog));
        dialogPanel.add(createRaceDetailPanel(dialog));
        dialogPanel.add(createLegDetailsPanel(dialog));
        dialogPanel.add(createManeuverDetailsPanel(dialog));
        dialogPanel.add(createTimingDetailsPanel(dialog));
        return dialogPanel;
    }

    private FlowPanel createManeuverDetailsPanel(DataEntryDialog<?> dialog) {
        FlowPanel meneuverPanel = new FlowPanel();
        meneuverPanel.add(dialog.createHeadline(stringConstants.maneuverTypes(), true));
        meneuverPanel.addStyleName("SettingsDialogComponent");
        FlowPanel meneuverContent = new FlowPanel();
        meneuverContent.addStyleName("dialogInnerContent");
        List<DetailType> currentMeneuverDetailSelection = maneuverDetailSelection;
        for (DetailType detailType : ManeuverCountRaceColumn.getAvailableManeuverDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(detailType, stringConstants));
            checkbox.setValue(currentMeneuverDetailSelection.contains(detailType));
            maneuverDetailCheckboxes.put(detailType, checkbox);
            meneuverContent.add(checkbox);
        }
        meneuverPanel.add(meneuverContent);
        return meneuverPanel;
    }

    private FlowPanel createTimingDetailsPanel(DataEntryDialog<?> dialog) {
        FlowPanel timingPanel = new FlowPanel();
        refreshIntervalInSecondsBox = dialog.createLongBox(delayBetweenAutoAdvancesInMilliseconds / 1000l, 4);
        delayInSecondsBox = dialog.createLongBox(delayInMilliseconds / 1000l, 4);
        timingPanel.add(dialog.createHeadline(stringConstants.timing(), true));
        timingPanel.addStyleName("SettingsDialogComponent");
        FlowPanel timingContent = new FlowPanel();
        timingContent.addStyleName("dialogInnerContent");
        FlowPanel delayInSecondsWrapper = new FlowPanel();
        delayInSecondsWrapper.getElement().getStyle().setFloat(Float.LEFT);
        delayInSecondsWrapper.getElement().getStyle().setPaddingRight(20, Unit.PX);
        Label delayLabel = new Label(stringConstants.delayInSeconds() + ":");
        delayInSecondsWrapper.add(delayLabel);
        delayInSecondsWrapper.add(delayInSecondsBox);
        timingContent.add(delayInSecondsWrapper);
        FlowPanel refreshIntervalWrapper = new FlowPanel();
        Label refreshIntervalLabel = new Label(stringConstants.refreshInterval() + ":");
        refreshIntervalWrapper.add(refreshIntervalLabel);
        refreshIntervalWrapper.add(refreshIntervalInSecondsBox);
        timingContent.add(refreshIntervalWrapper);
        timingPanel.add(timingContent);
        return timingPanel;
    }

    private FlowPanel createRaceDetailPanel(DataEntryDialog<?> dialog) {
        FlowPanel raceDetailDialog = new FlowPanel();
        raceDetailDialog.add(dialog.createHeadline(stringConstants.raceDetailsToShow(), true));
        raceDetailDialog.addStyleName("SettingsDialogComponent");
        FlowPanel raceDetailDialogContent = new FlowPanel();
        raceDetailDialogContent.addStyleName("dialogInnerContent");
        List<DetailType> currentRaceDetailSelection = raceDetailSelection;
        for (DetailType type : LeaderboardPanel.getAvailableRaceDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentRaceDetailSelection.contains(type));
            raceDetailCheckboxes.put(type, checkbox);
            raceDetailDialogContent.add(checkbox);
        }
        raceDetailDialog.add(raceDetailDialogContent);
        return raceDetailDialog;
    }

    private FlowPanel createOverallDetailPanel(DataEntryDialog<?> dialog) {
        FlowPanel overallDetailDialog = new FlowPanel();
        overallDetailDialog.add(dialog.createHeadline(stringConstants.overallDetailsToShow(), true));
        overallDetailDialog.addStyleName("SettingsDialogComponent overallDetailSettings");
        FlowPanel overallDetailDialogContent = new FlowPanel();
        overallDetailDialogContent.addStyleName("dialogInnerContent");
        List<DetailType> currentOverallDetailSelection = overallDetailSelection;
        for (DetailType type : LeaderboardPanel.getAvailableOverallDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentOverallDetailSelection.contains(type));
            overallDetailCheckboxes.put(type, checkbox);
            overallDetailDialogContent.add(checkbox);
        }
        overallDetailDialog.add(overallDetailDialogContent);
        return overallDetailDialog;
    }

    private FlowPanel createLegDetailsPanel(DataEntryDialog<?> dialog) {
        FlowPanel legDetailsToShow = new FlowPanel();
        legDetailsToShow.add(dialog.createHeadline(stringConstants.legDetailsToShow(), true));
        legDetailsToShow.addStyleName("SettingsDialogComponent");
        FlowPanel legDetailsContent = new FlowPanel();
        legDetailsContent.addStyleName("dialogInnerContent");
        List<DetailType> currentLegDetailSelection = legDetailSelection;
        for (DetailType type : LegColumn.getAvailableLegDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentLegDetailSelection.contains(type));
            legDetailCheckboxes.put(type, checkbox);
            legDetailsContent.add(checkbox);
        }
        legDetailsToShow.add(legDetailsContent);
        return legDetailsToShow;
    }

    private FlowPanel createSelectedRacesPanel(DataEntryDialog<?> dialog) {
        FlowPanel selectedRacesPanel = new FlowPanel();
        selectedRacesPanel.addStyleName("SettingsDialogComponent");
        FlowPanel selectedRacesContent = new FlowPanel();
        selectedRacesContent.addStyleName("dialogInnerContent");
        selectedRacesPanel.add(dialog.createHeadline(stringConstants.selectedRaces(), true));
        List<RaceColumnDTO> allColumns = raceAllRaceColumns;
        // Attention: We need to consider that there are regattas with more than 30 races
        int racesCount = allColumns.size();
        int maxRacesPerRow = 10;
        if (racesCount > 0) {
            int rowIndex = 0;
            int columnIndex = 0;
            int rowCount = racesCount / maxRacesPerRow;
            if(racesCount % maxRacesPerRow != 0) {
                rowCount++;
            }
            Grid grid = new Grid(rowCount, maxRacesPerRow);
            for (RaceColumnDTO expandableSortableColumn : allColumns) {
                CheckBox checkbox = dialog.createCheckbox(expandableSortableColumn.getRaceColumnName());
                checkbox.setValue(Util.contains(raceColumnSelection, expandableSortableColumn));
                raceColumnCheckboxes.put(expandableSortableColumn, checkbox);
                grid.setWidget(rowIndex, columnIndex++, checkbox);
                if(columnIndex == maxRacesPerRow) {
                    rowIndex++;
                    columnIndex = 0;
                }
            }
            selectedRacesContent.add(grid);
        } else {
            selectedRacesContent.add(new Label(stringConstants.noRacesYet()));
        }
        selectedRacesPanel.add(selectedRacesContent);
        return selectedRacesPanel;
    }

    @Override
    public LeaderboardSettings getResult() {
        List<DetailType> maneuverDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : maneuverDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                maneuverDetailsToShow.add(entry.getKey());
            }
        }
        List<DetailType> overallDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : overallDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                overallDetailsToShow.add(entry.getKey());
            }
        }
        List<DetailType> raceDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : raceDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                raceDetailsToShow.add(entry.getKey());
            }
        }
        List<DetailType> legDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : legDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                legDetailsToShow.add(entry.getKey());
            }
        }
        List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
        for (Map.Entry<RaceColumnDTO, CheckBox> entry : raceColumnCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                namesOfRaceColumnsToShow.add(entry.getKey().getRaceColumnName());
            }
        }
        Long delayBetweenAutoAdvancesValue = refreshIntervalInSecondsBox.getValue();
        Long delayInSecondsValue = delayInSecondsBox.getValue();
        return new LeaderboardSettings(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow,
                overallDetailsToShow, namesOfRaceColumnsToShow, /* nameOfRacesToShow */null,
                autoExpandPreSelectedRace,
                1000l * (delayBetweenAutoAdvancesValue == null ? 0l : delayBetweenAutoAdvancesValue.longValue()), 1000 * (delayInSecondsValue == null ? 0 : delayInSecondsValue.longValue()), null,
                true, /* updateUponPlayStateChange */ true);
    }

    @Override
    public Validator<LeaderboardSettings> getValidator() {
        return new Validator<LeaderboardSettings>() {
            @Override
            public String getErrorMessage(LeaderboardSettings valueToValidate) {
                if (valueToValidate.getLegDetailsToShow().isEmpty()) {
                    return stringConstants.selectAtLeastOneLegDetail();
                } else if (valueToValidate.getDelayBetweenAutoAdvancesInMilliseconds() < 1000) {
                    return stringConstants.chooseUpdateIntervalOfAtLeastOneSecond();
                } else {
                    return null;
                }
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return delayInSecondsBox;
    }
}
