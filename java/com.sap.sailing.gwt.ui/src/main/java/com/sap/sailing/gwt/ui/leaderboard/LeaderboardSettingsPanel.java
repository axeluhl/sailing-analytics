package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsPanel.Result;
import com.sap.sailing.server.api.DetailType;

public class LeaderboardSettingsPanel extends DataEntryDialog<Result> {
    private final List<String> raceColumnSelection;
    private final List<String> raceAllRaceColumns;
    private final List<DetailType> maneuverDetailSelection;
    private final List<DetailType> legDetailSelection;
    private final List<DetailType> raceDetailSelection;
    private final Map<String, CheckBox> raceColumnCheckboxes;
    private final Map<DetailType, CheckBox> maneuverDetailCheckboxes;
    private final Map<DetailType, CheckBox> legDetailCheckboxes;
    private final Map<DetailType, CheckBox> raceDetailCheckboxes;
    private final StringConstants stringConstants;
    private final LongBox delayBetweenAutoAdvancesInSecondsBox;
    private final LongBox delayInSecondsBox;
    
    public static class Result {
        private final List<String> raceColumnsToShow;
        private final List<DetailType> maneuverDetailsToShow;
        private final List<DetailType> legDetailsToShow;
        private final List<DetailType> raceDetailsToShow;
        private final long delayBetweenAutoAdvancesInMilliseconds;
        private final long delayInMilliseconds;
        
        public Result(List<DetailType> meneuverDetailsToShow, List<DetailType> legDetailsToShow, List<DetailType> raceDetailsToShow, List<String> raceColumnsToShow, long delayBetweenAutoAdvancesInMilliseconds, long delayInMilliseconds) {
            this.legDetailsToShow = legDetailsToShow;
            this.raceDetailsToShow = raceDetailsToShow;
            this.raceColumnsToShow = raceColumnsToShow;
            this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
            this.delayInMilliseconds = delayInMilliseconds;
            this.maneuverDetailsToShow = meneuverDetailsToShow;
        }
      
        public List<DetailType> getManeuverDetailsToShow() {
            return maneuverDetailsToShow;
        }

        public List<DetailType> getLegDetailsToShow() {
            return legDetailsToShow;
        }

        public List<DetailType> getRaceDetailsToShow() {
            return raceDetailsToShow;
        }
        
        public List<String> getRaceColumnsToShow(){
            return raceColumnsToShow;
        }

        public long getDelayBetweenAutoAdvancesInMilliseconds() {
            return delayBetweenAutoAdvancesInMilliseconds;
        }

        public long getDelayInMilliseconds() {
            return delayInMilliseconds;
        }
    }
    
    public LeaderboardSettingsPanel(List<DetailType> maneuverDetailSelection, List<DetailType> legDetailSelection, List<DetailType> raceDetailSelection, List<String> raceAllRaceColumns, List<String> raceColumnSelection,
            long delayBetweenAutoAdvancesInMilliseconds, String title, String message,
            String okButtonName,
            String cancelButtonName, com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator<Result> validator, AsyncCallback<Result> callback, StringConstants stringConstants, long delayInMilliseconds) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        this.maneuverDetailSelection = maneuverDetailSelection;
        this.raceColumnSelection = raceColumnSelection;
        this.raceAllRaceColumns = raceAllRaceColumns;
        this.legDetailSelection = legDetailSelection;
        this.raceDetailSelection = raceDetailSelection;
        this.stringConstants = stringConstants;
        raceColumnCheckboxes = new LinkedHashMap<String, CheckBox>();
        maneuverDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        legDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        raceDetailCheckboxes = new LinkedHashMap<DetailType, CheckBox>();
        delayBetweenAutoAdvancesInSecondsBox = createLongBox((int) delayBetweenAutoAdvancesInMilliseconds/1000, 4);
        delayInSecondsBox = createLongBox((int) delayInMilliseconds/1000, 4);
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel hp = new HorizontalPanel();
        VerticalPanel vpMeneuvers = new VerticalPanel();
        vpMeneuvers.setSpacing(5);
        vpMeneuvers.add(new Label(stringConstants.maneuverTypes()));
        List<DetailType> currentMeneuverDetailSelection = maneuverDetailSelection;
        for (DetailType detailType : ManeuverCountRaceColumn.getAvailableManeuverDetailColumnTypes()) {
            CheckBox checkbox = createCheckbox(DetailTypeFormatter.format(detailType, stringConstants));
            checkbox.setValue(currentMeneuverDetailSelection.contains(detailType));
            maneuverDetailCheckboxes.put(detailType, checkbox);
            vpMeneuvers.add(checkbox);
        }
        hp.add(vpMeneuvers);
        VerticalPanel vpLeft = new VerticalPanel();
        vpLeft.setSpacing(5);
        VerticalPanel vpRight = new VerticalPanel();
        vpRight.setSpacing(5);
        vpLeft.add(new Label(stringConstants.timing()));
        Label delayLabel = new Label(stringConstants.delayInSeconds());
        vpLeft.add(delayLabel);
        vpLeft.add(delayInSecondsBox);
        Label delayBetweenAutoAdvancesLabel = new Label(stringConstants.delayBetweenAutoAdvances());
        vpLeft.add(delayBetweenAutoAdvancesLabel);
        vpLeft.add(delayBetweenAutoAdvancesInSecondsBox);
        vpLeft.add(new Label(stringConstants.raceDetailsToShow()));
        List<DetailType> currentRaceDetailSelection = raceDetailSelection;
        for (DetailType type : LeaderboardPanel.getAvailableRaceDetailColumnTypes()) {
            CheckBox checkbox = createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentRaceDetailSelection.contains(type));
            raceDetailCheckboxes.put(type, checkbox);
            vpLeft.add(checkbox);
        }
        vpLeft.add(new Label(stringConstants.legDetailsToShow()));
        List<DetailType> currentLegDetailSelection = legDetailSelection;
        for (DetailType type : LegColumn.getAvailableLegDetailColumnTypes()) {
            CheckBox checkbox = createCheckbox(DetailTypeFormatter.format(type, stringConstants));
            checkbox.setValue(currentLegDetailSelection.contains(type));
            legDetailCheckboxes.put(type, checkbox);
            vpLeft.add(checkbox);
        }
        hp.add(vpLeft);
        
        vpRight.add(new Label(stringConstants.selectedRaces()));
        List<String> allColumns = raceAllRaceColumns;
        for (String expandableSortableColumn : allColumns) {
            CheckBox checkbox = createCheckbox(expandableSortableColumn);
            checkbox.setValue(raceColumnSelection.contains(expandableSortableColumn));
            raceColumnCheckboxes.put(expandableSortableColumn, checkbox);
            vpRight.add(checkbox);
        }
        hp.add(vpRight);
        return hp;
    }

    @Override
    protected Result getResult() {
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
        List<String> raceColumnsToShow = new ArrayList<String>();
        for (Map.Entry<String, CheckBox> entry : raceColumnCheckboxes.entrySet()) {
            if(entry.getValue().getValue()){
                raceColumnsToShow.add(entry.getKey());
            }
        }
        Long delayBetweenAutoAdvancesValue = delayBetweenAutoAdvancesInSecondsBox.getValue();
        Long delayInSecondsValue = delayInSecondsBox.getValue();
        return new Result(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow, raceColumnsToShow, 1000*(delayBetweenAutoAdvancesValue==null?0:delayBetweenAutoAdvancesValue.longValue()),
                1000*(delayInSecondsValue==null?0:delayInSecondsValue.longValue()));
    }

    @Override
    public void show() {
        super.show();
        delayInSecondsBox.setFocus(true);
    }
    
}
