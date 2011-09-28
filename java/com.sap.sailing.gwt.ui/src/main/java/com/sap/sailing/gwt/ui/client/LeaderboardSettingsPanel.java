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

public class LeaderboardSettingsPanel extends DataEntryDialog<Result> {
    private final LegDetailSelectionProvider selectionProvider;
    private final Map<LegDetailColumnType, CheckBox> checkboxes;
    private final StringConstants stringConstants;
    private final IntegerBox delayBetweenAutoAdvancesInSecondsBox;
    private final IntegerBox delayInSecondsBox;
    private CheckBox firstCheckbox;
    
    public static class Result {
        private final List<LegDetailColumnType> legDetailsToShow;
        private final long delayBetweenAutoAdvancesInMilliseconds;
        private final long delayInMilliseconds;
        
        public Result(List<LegDetailColumnType> legDetailsToShow, long delayBetweenAutoAdvancesInMilliseconds, long delayInMilliseconds) {
            this.legDetailsToShow = legDetailsToShow;
            this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
            this.delayInMilliseconds = delayInMilliseconds;
        }

        public List<LegDetailColumnType> getLegDetailsToShow() {
            return legDetailsToShow;
        }

        public long getDelayBetweenAutoAdvancesInMilliseconds() {
            return delayBetweenAutoAdvancesInMilliseconds;
        }

        public long getDelayInMilliseconds() {
            return delayInMilliseconds;
        }
    }
    
    public LeaderboardSettingsPanel(LegDetailSelectionProvider selectionProvider, long delayBetweenAutoAdvancesInMilliseconds,
            String title, String message, String okButtonName,
            String cancelButtonName,
            com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator<Result> validator, AsyncCallback<Result> callback, StringConstants stringConstants, long delayInMilliseconds) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        this.selectionProvider = selectionProvider;
        this.stringConstants = stringConstants;
        checkboxes = new LinkedHashMap<LegDetailSelectionProvider.LegDetailColumnType, CheckBox>();
        delayBetweenAutoAdvancesInSecondsBox = createIntegerBox((int) delayBetweenAutoAdvancesInMilliseconds/1000, 4);
        delayInSecondsBox = createIntegerBox((int) delayInMilliseconds/1000, 4);
    }

    @Override
    protected Widget getAdditionalWidget() {
        List<LegDetailColumnType> currentSelection = selectionProvider.getLegDetailsToShow();
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(5);
        Label delayLabel = new Label(stringConstants.delayInSeconds());
        vp.add(delayLabel);
        vp.add(delayInSecondsBox);
        Label delayBetweenAutoAdvancesLabel = new Label(stringConstants.delayBetweenAutoAdvances());
        vp.add(delayBetweenAutoAdvancesLabel);
        vp.add(delayBetweenAutoAdvancesInSecondsBox);
        firstCheckbox = null;
        for (LegDetailColumnType type : LegDetailColumnType.values()) {
            CheckBox checkbox = createCheckbox(type.toString(stringConstants));
            if (firstCheckbox == null) {
                firstCheckbox = checkbox;
            }
            checkbox.setValue(currentSelection.contains(type));
            checkboxes.put(type, checkbox);
            vp.add(checkbox);
        }
        return vp;
    }

    @Override
    protected Result getResult() {
        List<LegDetailColumnType> result = new ArrayList<LegDetailSelectionProvider.LegDetailColumnType>();
        for (Map.Entry<LegDetailColumnType, CheckBox> entry : checkboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                result.add(entry.getKey());
            }
        }
        Integer delayBetweenAutoAdvancesValue = delayBetweenAutoAdvancesInSecondsBox.getValue();
        Integer delayInSecondsValue = delayInSecondsBox.getValue();
        return new Result(result, 1000*(delayBetweenAutoAdvancesValue==null?0:delayBetweenAutoAdvancesValue.longValue()),
                1000*(delayInSecondsValue==null?0:delayInSecondsValue.longValue()));
    }

    @Override
    public void show() {
        super.show();
        delayInSecondsBox.setFocus(true);
    }
    
}
