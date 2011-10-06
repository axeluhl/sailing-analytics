package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.LeaderboardSettingsPanel.Result;
import com.sap.sailing.gwt.ui.client.LegDetailSelectionProvider.LegDetailColumnType;
import com.sap.sailing.gwt.ui.client.RaceDetailSelectionProvider.RaceDetailColumnType;

public class LeaderboardSettingsPanel extends DataEntryDialog<Result> {
    private final LegDetailSelectionProvider legDetailSelectionProvider;
    private final RaceDetailSelectionProvider raceDetailSelectionProvider;
    private final Map<LegDetailColumnType, CheckBox> legDetailCheckboxes;
    private final Map<RaceDetailColumnType, CheckBox> raceDetailCheckboxes;
    private final StringConstants stringConstants;
    private final IntegerBox delayBetweenAutoAdvancesInSecondsBox;
    private final IntegerBox delayInSecondsBox;
    
    public static class Result {
        private final List<LegDetailColumnType> legDetailsToShow;
        private final List<RaceDetailColumnType> raceDetailsToShow;
        private final long delayBetweenAutoAdvancesInMilliseconds;
        private final long delayInMilliseconds;
        
        public Result(List<LegDetailColumnType> legDetailsToShow, List<RaceDetailColumnType> raceDetailsToShow,
                long delayBetweenAutoAdvancesInMilliseconds, long delayInMilliseconds) {
            this.legDetailsToShow = legDetailsToShow;
            this.raceDetailsToShow = raceDetailsToShow;
            this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
            this.delayInMilliseconds = delayInMilliseconds;
        }

        public List<LegDetailColumnType> getLegDetailsToShow() {
            return legDetailsToShow;
        }

        public List<RaceDetailColumnType> getRaceDetailsToShow() {
            return raceDetailsToShow;
        }

        public long getDelayBetweenAutoAdvancesInMilliseconds() {
            return delayBetweenAutoAdvancesInMilliseconds;
        }

        public long getDelayInMilliseconds() {
            return delayInMilliseconds;
        }
    }
    
    public LeaderboardSettingsPanel(LegDetailSelectionProvider legDetailSelectionProvider, RaceDetailSelectionProvider raceDetailSelectionProvider,
            long delayBetweenAutoAdvancesInMilliseconds, String title, String message,
            String okButtonName,
            String cancelButtonName, com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator<Result> validator, AsyncCallback<Result> callback, StringConstants stringConstants, long delayInMilliseconds) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        this.legDetailSelectionProvider = legDetailSelectionProvider;
        this.raceDetailSelectionProvider = raceDetailSelectionProvider;
        this.stringConstants = stringConstants;
        legDetailCheckboxes = new LinkedHashMap<LegDetailSelectionProvider.LegDetailColumnType, CheckBox>();
        raceDetailCheckboxes = new LinkedHashMap<RaceDetailSelectionProvider.RaceDetailColumnType, CheckBox>();
        delayBetweenAutoAdvancesInSecondsBox = createIntegerBox((int) delayBetweenAutoAdvancesInMilliseconds/1000, 4);
        delayInSecondsBox = createIntegerBox((int) delayInMilliseconds/1000, 4);
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(5);
        vp.add(new Label(stringConstants.timing()));
        Label delayLabel = new Label(stringConstants.delayInSeconds());
        vp.add(delayLabel);
        vp.add(delayInSecondsBox);
        Label delayBetweenAutoAdvancesLabel = new Label(stringConstants.delayBetweenAutoAdvances());
        vp.add(delayBetweenAutoAdvancesLabel);
        vp.add(delayBetweenAutoAdvancesInSecondsBox);
        vp.add(new Label(stringConstants.raceDetailsToShow()));
        List<RaceDetailColumnType> currentRaceDetailSelection = raceDetailSelectionProvider.getRaceDetailsToShow();
        for (RaceDetailColumnType type : RaceDetailColumnType.values()) {
            CheckBox checkbox = createCheckbox(type.toString(stringConstants));
            checkbox.setValue(currentRaceDetailSelection.contains(type));
            raceDetailCheckboxes.put(type, checkbox);
            vp.add(checkbox);
        }
        vp.add(new Label(stringConstants.legDetailsToShow()));
        List<LegDetailColumnType> currentLegDetailSelection = legDetailSelectionProvider.getLegDetailsToShow();
        for (LegDetailColumnType type : LegDetailColumnType.values()) {
            CheckBox checkbox = createCheckbox(type.toString(stringConstants));
            checkbox.setValue(currentLegDetailSelection.contains(type));
            legDetailCheckboxes.put(type, checkbox);
            vp.add(checkbox);
        }
        return vp;
    }

    @Override
    protected Result getResult() {
        List<RaceDetailColumnType> raceDetailsToShow = new ArrayList<RaceDetailSelectionProvider.RaceDetailColumnType>();
        for (Map.Entry<RaceDetailColumnType, CheckBox> entry : raceDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                raceDetailsToShow.add(entry.getKey());
            }
        }
        List<LegDetailColumnType> legDetailsToShow = new ArrayList<LegDetailSelectionProvider.LegDetailColumnType>();
        for (Map.Entry<LegDetailColumnType, CheckBox> entry : legDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                legDetailsToShow.add(entry.getKey());
            }
        }
        Integer delayBetweenAutoAdvancesValue = delayBetweenAutoAdvancesInSecondsBox.getValue();
        Integer delayInSecondsValue = delayInSecondsBox.getValue();
        return new Result(legDetailsToShow, raceDetailsToShow, 1000*(delayBetweenAutoAdvancesValue==null?0:delayBetweenAutoAdvancesValue.longValue()),
                1000*(delayInSecondsValue==null?0:delayInSecondsValue.longValue()));
    }

    @Override
    public void show() {
        super.show();
        delayInSecondsBox.setFocus(true);
    }
    
}
