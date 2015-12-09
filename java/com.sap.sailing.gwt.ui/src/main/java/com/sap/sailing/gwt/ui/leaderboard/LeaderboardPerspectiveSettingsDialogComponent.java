package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class LeaderboardPerspectiveSettingsDialogComponent implements SettingsDialogComponent<LeaderboardPerspectiveSettings> {
    CheckBox leaderboardAutoZoomBox;
    DoubleBox leaderboardZoomFactorBox;

    private final StringMessages stringMessages;
    private final LeaderboardPerspectiveSettings initialSettings;
    
    public LeaderboardPerspectiveSettingsDialogComponent(LeaderboardPerspectiveSettings settings, StringMessages stringMessages) {
        this.initialSettings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        Grid grid = new Grid(2,2);
        vp.add(grid);

        leaderboardAutoZoomBox = dialog.createCheckbox("");
        leaderboardAutoZoomBox.setValue(initialSettings.isLeaderboardAutoZoom());
        leaderboardAutoZoomBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                leaderboardZoomFactorBox.setEnabled(!event.getValue());
            }
        });
        
        grid.setWidget(0, 0, new Label(stringMessages.autoZoom()));
        grid.setWidget(0, 1, leaderboardAutoZoomBox);
        
        leaderboardZoomFactorBox = dialog.createDoubleBox(5);
        leaderboardZoomFactorBox.setValue(initialSettings.getLeaderboardZoomFactor());
        leaderboardZoomFactorBox.setEnabled(!initialSettings.isLeaderboardAutoZoom());
        
        grid.setWidget(1, 0, new Label(stringMessages.zoomFactor()));
        grid.setWidget(1, 1, leaderboardZoomFactorBox);

        return vp;
    }
    

    @Override
    public LeaderboardPerspectiveSettings getResult() {
        LeaderboardPerspectiveSettings result = new LeaderboardPerspectiveSettings(leaderboardAutoZoomBox.getValue(), leaderboardZoomFactorBox.getValue());
        return result;
    }
    
    @Override
    public Validator<LeaderboardPerspectiveSettings> getValidator() {
        return new Validator<LeaderboardPerspectiveSettings>() {
            @Override
            public String getErrorMessage(LeaderboardPerspectiveSettings valueToValidate) {
                String errorMessage = null;
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return leaderboardAutoZoomBox;
    }
}
