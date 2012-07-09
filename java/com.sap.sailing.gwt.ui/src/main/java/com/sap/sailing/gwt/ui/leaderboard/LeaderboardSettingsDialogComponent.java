package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.VerticalPanel;
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
        HorizontalPanel hp = new HorizontalPanel();
        VerticalPanel vpMeneuvers = new VerticalPanel();
        vpMeneuvers.setSpacing(5);
        vpMeneuvers.add(new Label(stringConstants.maneuverTypes()));
        List<DetailType> currentMeneuverDetailSelection = maneuverDetailSelection;
        for (DetailType detailType : ManeuverCountRaceColumn.getAvailableManeuverDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(detailType, stringConstants));
            checkbox.setValue(currentMeneuverDetailSelection.contains(detailType));
            maneuverDetailCheckboxes.put(detailType, checkbox);
            vpMeneuvers.add(checkbox);
        }
        
        hp.add(vpMeneuvers);
        
        hp.add(createCurrentRaceDetailSelection(dialog));
        
        hp.add(legDetailsToShow(dialog));
        
        hp.add(createSelectedRacesPanel(dialog));
        
        return hp;
    }

	private FlowPanel createCurrentRaceDetailSelection(DataEntryDialog<?> dialog) {
		FlowPanel vpLeft = new FlowPanel();

        // add headline
		vpLeft.addStyleName("TEST");
        vpLeft.add(dialog.createHeadline(stringConstants.timing()));
        Label delayLabel = new Label(stringConstants.delayInSeconds());
        vpLeft.add(delayLabel);
        vpLeft.add(delayInSecondsBox);
        
        
        Label delayBetweenAutoAdvancesLabel = new Label(stringConstants.delayBetweenAutoAdvances());
        vpLeft.add(delayBetweenAutoAdvancesLabel);
        vpLeft.add(delayBetweenAutoAdvancesInSecondsBox);
        vpLeft.add(new Label(stringConstants.raceDetailsToShow()));
        List<DetailType> currentRaceDetailSelection = raceDetailSelection;
        for (DetailType type : LeaderboardPanel.getAvailableRaceDetailColumnTypes()) {
            CheckBox checkbox = dialog.createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentRaceDetailSelection.contains(type));
            raceDetailCheckboxes.put(type, checkbox);
            vpLeft.add(checkbox);
        }
        return vpLeft;
	}

	private FlowPanel legDetailsToShow(DataEntryDialog<?> dialog) {
		FlowPanel legDetailsToShow = new FlowPanel();
		
		legDetailsToShow.add(dialog.createHeadline(stringConstants.legDetailsToShow()));
		
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
