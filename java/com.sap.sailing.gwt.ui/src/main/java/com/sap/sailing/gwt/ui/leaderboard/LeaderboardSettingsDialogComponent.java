package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class LeaderboardSettingsDialogComponent implements SettingsDialogComponent<LeaderboardSettings> {
    private final List<RaceColumnDTO> raceColumnSelection;
    private final List<RaceColumnDTO> raceAllRaceColumns;
    private final List<DetailType> maneuverDetailSelection;
    private final List<DetailType> legDetailSelection;
    private final List<DetailType> raceDetailSelection;
    private final Map<RaceColumnDTO, CheckBox> raceColumnCheckboxes;
    private final Map<DetailType, CheckBox> maneuverDetailCheckboxes;
    private final Map<DetailType, CheckBox> legDetailCheckboxes;
    private final Map<DetailType, CheckBox> raceDetailCheckboxes;
    private final StringMessages stringConstants;
    private LongBox delayBetweenAutoAdvancesInSecondsBox;
    private LongBox delayInSecondsBox;
    private final boolean autoExpandPreSelectedRace;
    private final long delayBetweenAutoAdvancesInMilliseconds;
    private final long delayInMilliseconds;

    public LeaderboardSettingsDialogComponent(List<DetailType> maneuverDetailSelection,
            List<DetailType> legDetailSelection, List<DetailType> raceDetailSelection, List<RaceColumnDTO> raceAllRaceColumns,
            List<RaceColumnDTO> raceColumnSelection, boolean autoExpandPreSelectedRace, long delayBetweenAutoAdvancesInMilliseconds, long delayInMilliseconds,
            StringMessages stringConstants) {
        this.maneuverDetailSelection = maneuverDetailSelection;
        this.raceColumnSelection = raceColumnSelection;
        this.raceAllRaceColumns = raceAllRaceColumns;
        this.legDetailSelection = legDetailSelection;
        this.raceDetailSelection = raceDetailSelection;
        this.stringConstants = stringConstants;
        raceColumnCheckboxes = new LinkedHashMap<RaceColumnDTO, CheckBox>();
        maneuverDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        legDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        raceDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        this.delayInMilliseconds = delayInMilliseconds;
    }
    
    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        delayBetweenAutoAdvancesInSecondsBox = dialog.createLongBox(delayBetweenAutoAdvancesInMilliseconds/1000l, 4);
        delayInSecondsBox = dialog.createLongBox(delayInMilliseconds/1000l, 4);
        FlowPanel dialogPanel = new FlowPanel();
        dialogPanel.add(createSelectedRacesPanel(dialog));
        dialogPanel.add(createRaceDetailPanel(dialog));
        dialogPanel.add(createLegDetailsPanel(dialog));
        dialogPanel.add(createMeneuverDetailsPanel(dialog));
        dialogPanel.add(createTimingDetailsPanel(dialog));
        dialogPanel.add(createHelpPanel(dialog));
        return dialogPanel;
    }

	private FlowPanel createHelpPanel(DataEntryDialog<?> dialog) {
		FlowPanel helpPanel = new FlowPanel();

		helpPanel.add(dialog.createHeadline("Description"));
		helpPanel.addStyleName("SettingsDialogComponent helpPanel");
		
		return helpPanel;
	}
    
	private FlowPanel createMeneuverDetailsPanel(DataEntryDialog<?> dialog) {
		FlowPanel meneuverPanel = new FlowPanel();
		
		meneuverPanel.add(dialog.createHeadline(stringConstants.maneuverTypes()));
		meneuverPanel.addStyleName("SettingsDialogComponent meneuverSettings");
		
        List<DetailType> currentMeneuverDetailSelection = maneuverDetailSelection;
        for (DetailType detailType : ManeuverCountRaceColumn.getAvailableManeuverDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(detailType, stringConstants));
            checkbox.setValue(currentMeneuverDetailSelection.contains(detailType));
            maneuverDetailCheckboxes.put(detailType, checkbox);
            meneuverPanel.add(checkbox);
        }
		return meneuverPanel;
	}
    
    private FlowPanel createTimingDetailsPanel(DataEntryDialog<?> dialog) {
    	FlowPanel timingPanel = new FlowPanel();

	    timingPanel.add(dialog.createHeadline(stringConstants.timing()));
	    timingPanel.addStyleName("SettingsDialogComponent timingSettings");
	    
	    Label delayLabel = new Label(stringConstants.delayInSeconds());
	    timingPanel.add(delayLabel);
	    timingPanel.add(delayInSecondsBox);
        Label delayBetweenAutoAdvancesLabel = new Label(stringConstants.delayBetweenAutoAdvances());
        timingPanel.add(delayBetweenAutoAdvancesLabel);
        timingPanel.add(delayBetweenAutoAdvancesInSecondsBox);
    	return timingPanel;
    }

	private FlowPanel createRaceDetailPanel(DataEntryDialog<?> dialog) {
		FlowPanel raceDetailDialog = new FlowPanel();

        raceDetailDialog.add(dialog.createHeadline(stringConstants.raceDetailsToShow()));
        raceDetailDialog.addStyleName("SettingsDialogComponent raceDetailSettings");

        List<DetailType> currentRaceDetailSelection = raceDetailSelection;
        for (DetailType type : LeaderboardPanel.getAvailableRaceDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentRaceDetailSelection.contains(type));
            raceDetailCheckboxes.put(type, checkbox);
            raceDetailDialog.add(checkbox);
        }
        
        return raceDetailDialog;
	}

	private FlowPanel createLegDetailsPanel(DataEntryDialog<?> dialog) {
		FlowPanel legDetailsToShow = new FlowPanel();
		
		legDetailsToShow.add(dialog.createHeadline(stringConstants.legDetailsToShow()));
		legDetailsToShow.addStyleName("SettingsDialogComponent legDetailsSettings");
		
        List<DetailType> currentLegDetailSelection = legDetailSelection;
        for (DetailType type : LegColumn.getAvailableLegDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentLegDetailSelection.contains(type));
            legDetailCheckboxes.put(type, checkbox);
            legDetailsToShow.add(checkbox);
        }
        return legDetailsToShow;
	}

	private FlowPanel createSelectedRacesPanel(DataEntryDialog<?> dialog) {
        FlowPanel selectedRacesPanel = new FlowPanel();
        
		selectedRacesPanel.add(dialog.createHeadline(stringConstants.selectedRaces()));
		selectedRacesPanel.addStyleName("SettingsDialogComponent selectedRacesSettings");
		
        List<RaceColumnDTO> allColumns = raceAllRaceColumns;
        for (RaceColumnDTO expandableSortableColumn : allColumns) {
            CheckBox checkbox = dialog.createCheckbox(expandableSortableColumn.getRaceColumnName());
            checkbox.setValue(raceColumnSelection.contains(expandableSortableColumn));
            raceColumnCheckboxes.put(expandableSortableColumn, checkbox);
            selectedRacesPanel.add(checkbox);
        }
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
            if(entry.getValue().getValue()){
                namesOfRaceColumnsToShow.add(entry.getKey().getRaceColumnName());
            }
        }
        Long delayBetweenAutoAdvancesValue = delayBetweenAutoAdvancesInSecondsBox.getValue();
        Long delayInSecondsValue = delayInSecondsBox.getValue();
        return new LeaderboardSettings(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow,
                namesOfRaceColumnsToShow, /* nameOfRacesToShow */null, autoExpandPreSelectedRace,
                1000l * (delayBetweenAutoAdvancesValue == null ? 0l : delayBetweenAutoAdvancesValue.longValue()),
                1000 * (delayInSecondsValue == null ? 0 : delayInSecondsValue.longValue()), null, true,
                /* updateUponPlayStateChange */ true);
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
