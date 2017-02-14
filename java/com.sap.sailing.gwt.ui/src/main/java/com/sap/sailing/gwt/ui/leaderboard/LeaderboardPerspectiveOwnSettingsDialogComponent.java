package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class LeaderboardPerspectiveOwnSettingsDialogComponent
        implements SettingsDialogComponent<LeaderboardPerspectiveOwnSettings> {

    private final LeaderboardPerspectiveOwnSettings initialSettings;
    private CheckBox showRaceDetails;
    private CheckBox hideToolbar;
    private CheckBox autoExpandLastRaceColumn;
    private CheckBox showCharts;
    private CheckBox showOverallLeaderboard;
    private CheckBox showSeriesLeaderboards;
    private TextBox zoomTo;

    public LeaderboardPerspectiveOwnSettingsDialogComponent(LeaderboardPerspectiveOwnSettings settings) {
        this.initialSettings = settings;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        StringMessages stringMessages = StringMessages.INSTANCE;

        showRaceDetails = dialog.createCheckbox(stringMessages.showRaceDetails());
        showRaceDetails.setValue(initialSettings.isShowRaceDetails());
        vp.add(showRaceDetails);

        hideToolbar = dialog.createCheckbox(stringMessages.hideToolbar());
        hideToolbar.setValue(initialSettings.isHideToolbar());
        vp.add(hideToolbar);

        autoExpandLastRaceColumn = dialog.createCheckbox(stringMessages.expandLastRace());
        autoExpandLastRaceColumn.setValue(initialSettings.isAutoExpandLastRaceColumn());
        vp.add(autoExpandLastRaceColumn);

        showCharts = dialog.createCheckbox(stringMessages.showCharts());
        showCharts.setValue(initialSettings.isShowCharts());
        vp.add(showCharts);

        showOverallLeaderboard = dialog.createCheckbox(stringMessages.showOverallLeaderboard());
        showOverallLeaderboard.setValue(initialSettings.isShowOverallLeaderboard());
        vp.add(showOverallLeaderboard);

        showSeriesLeaderboards = dialog.createCheckbox(stringMessages.showSeriesLeaderboards());
        showSeriesLeaderboards.setValue(initialSettings.isShowSeriesLeaderboards());
        vp.add(showSeriesLeaderboards);

        zoomTo = dialog.createTextBox(stringMessages.zoom());
        zoomTo.setValue(initialSettings.getZoomTo());
        vp.add(zoomTo);
        return vp;
    }

    @Override
    public LeaderboardPerspectiveOwnSettings getResult() {
        return new LeaderboardPerspectiveOwnSettings(showRaceDetails.getValue(), hideToolbar.getValue(),
                autoExpandLastRaceColumn.getValue(), showCharts.getValue(), showOverallLeaderboard.getValue(),
                showSeriesLeaderboards.getValue(), zoomTo.getValue());
    }

    @Override
    public Validator<LeaderboardPerspectiveOwnSettings> getValidator() {
        return new Validator<LeaderboardPerspectiveOwnSettings>() {
            @Override
            public String getErrorMessage(LeaderboardPerspectiveOwnSettings valueToValidate) {
                String errorMessage = null;
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return showRaceDetails;
    }
}
