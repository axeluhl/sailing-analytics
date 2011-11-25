package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.LeaderboardSettingsPanel.Result;

public class LeaderboardSettingsPanel extends DataEntryDialog<Result> {
    private final List<String> raceColumnSelection;
    private final List<String> raceAllRaceColumns;
    private final List<DetailColumnType> legDetailSelection;
    private final List<DetailColumnType> raceDetailSelection;
    private final Map<String, CheckBox> raceColumnCheckboxes;
    private final Map<DetailColumnType, CheckBox> legDetailCheckboxes;
    private final Map<DetailColumnType, CheckBox> raceDetailCheckboxes;
    private final StringConstants stringConstants;
    private final IntegerBox delayBetweenAutoAdvancesInSecondsBox;
    private final IntegerBox delayInSecondsBox;
    
    public static class Result {
        private final List<String> raceColumnsToShow;
        private final List<DetailColumnType> legDetailsToShow;
        private final List<DetailColumnType> raceDetailsToShow;
        private final long delayBetweenAutoAdvancesInMilliseconds;
        private final long delayInMilliseconds;
        
        public Result(List<DetailColumnType> legDetailsToShow, List<DetailColumnType> raceDetailsToShow, List<String> raceColumnsToShow, long delayBetweenAutoAdvancesInMilliseconds, long delayInMilliseconds) {
            this.legDetailsToShow = legDetailsToShow;
            this.raceDetailsToShow = raceDetailsToShow;
            this.raceColumnsToShow = raceColumnsToShow;
            this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
            this.delayInMilliseconds = delayInMilliseconds;
        }

        public List<DetailColumnType> getLegDetailsToShow() {
            return legDetailsToShow;
        }

        public List<DetailColumnType> getRaceDetailsToShow() {
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
    
    public LeaderboardSettingsPanel(List<DetailColumnType> legDetailSelection, List<DetailColumnType> raceDetailSelection, List<String> raceAllRaceColumns, List<String> raceColumnSelection,
            long delayBetweenAutoAdvancesInMilliseconds, String title, String message,
            String okButtonName,
            String cancelButtonName, com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator<Result> validator, AsyncCallback<Result> callback, StringConstants stringConstants, long delayInMilliseconds) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        this.raceColumnSelection = raceColumnSelection;
        this.raceAllRaceColumns = raceAllRaceColumns;
        this.legDetailSelection = legDetailSelection;
        this.raceDetailSelection = raceDetailSelection;
        this.stringConstants = stringConstants;
        raceColumnCheckboxes = new LinkedHashMap<String, CheckBox>();
        legDetailCheckboxes = new LinkedHashMap<DetailColumnType, CheckBox>();
        raceDetailCheckboxes = new LinkedHashMap<DetailColumnType, CheckBox>();
        delayBetweenAutoAdvancesInSecondsBox = createIntegerBox((int) delayBetweenAutoAdvancesInMilliseconds/1000, 4);
        delayInSecondsBox = createIntegerBox((int) delayInMilliseconds/1000, 4);
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel hp = new HorizontalPanel();
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
        List<DetailColumnType> currentRaceDetailSelection = raceDetailSelection;
        for (DetailColumnType type : LeaderboardPanel.getAvailableRaceDetailColumnTypes()) {
            CheckBox checkbox = createCheckbox(type.toString(stringConstants));
            checkbox.setValue(currentRaceDetailSelection.contains(type));
            raceDetailCheckboxes.put(type, checkbox);
            vpLeft.add(checkbox);
        }
        vpLeft.add(new Label(stringConstants.legDetailsToShow()));
        List<DetailColumnType> currentLegDetailSelection = legDetailSelection;
        for (DetailColumnType type : LegColumn.getAvailableLegDetailColumnTypes()) {
            CheckBox checkbox = createCheckbox(type.toString(stringConstants));
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
        List<DetailColumnType> raceDetailsToShow = new ArrayList<DetailColumnType>();
        for (Map.Entry<DetailColumnType, CheckBox> entry : raceDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                raceDetailsToShow.add(entry.getKey());
            }
        }
        List<DetailColumnType> legDetailsToShow = new ArrayList<DetailColumnType>();
        for (Map.Entry<DetailColumnType, CheckBox> entry : legDetailCheckboxes.entrySet()) {
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
        Integer delayBetweenAutoAdvancesValue = delayBetweenAutoAdvancesInSecondsBox.getValue();
        Integer delayInSecondsValue = delayInSecondsBox.getValue();
        return new Result(legDetailsToShow, raceDetailsToShow, raceColumnsToShow, 1000*(delayBetweenAutoAdvancesValue==null?0:delayBetweenAutoAdvancesValue.longValue()),
                1000*(delayInSecondsValue==null?0:delayInSecondsValue.longValue()));
    }

    @Override
    public void show() {
        super.show();
        delayInSecondsBox.setFocus(true);
    }
    
}
