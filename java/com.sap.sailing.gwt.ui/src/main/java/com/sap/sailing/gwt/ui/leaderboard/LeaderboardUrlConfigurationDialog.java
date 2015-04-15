package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardUrlSettings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class LeaderboardUrlConfigurationDialog extends SettingsDialog<LeaderboardUrlSettings> {
    public LeaderboardUrlConfigurationDialog(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        super(new ProxyLeaderboardUrlComponent(stringMessages, leaderboard), stringMessages, /* animationEnabled */ false);
    }

    public LeaderboardUrlConfigurationDialog(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard, DialogCallback<LeaderboardUrlSettings> callback) {
        super(new ProxyLeaderboardUrlComponent(stringMessages, leaderboard), stringMessages, callback);
    }
    
    private static class ProxyLeaderboardUrlComponent implements Component<LeaderboardUrlSettings> {
        private final StringMessages stringMessages;
        private final LeaderboardUrlConfigurationDialogComponent settingsDialogComponent;
        
        public ProxyLeaderboardUrlComponent(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
            this.stringMessages = stringMessages;
            this.settingsDialogComponent = new LeaderboardUrlConfigurationDialogComponent(leaderboard, stringMessages);
        }

        @Override
        public boolean hasSettings() {
            return true;
        }

        @Override
        public SettingsDialogComponent<LeaderboardUrlSettings> getSettingsDialogComponent() {
            return settingsDialogComponent;
        }

        @Override
        public void updateSettings(LeaderboardUrlSettings newSettings) {
            // no-op; the resulting URL has already been updated to the anchor in the dialog
        }

        @Override
        public String getLocalizedShortName() {
            return stringMessages.leaderboardConfiguration();
        }

        @Override
        public Widget getEntryWidget() {
            throw new UnsupportedOperationException(
                    "Internal error. This settings dialog does not actually belong to a LeaderboardPanel");
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public void setVisible(boolean visibility) {
            // no-op
        }

        @Override
        public String getDependentCssClassName() {
            return "leaderboardUrlConfigurationDialog";
        }
    }
    
    private static class LeaderboardUrlConfigurationDialogComponent implements SettingsDialogComponent<LeaderboardUrlSettings> {
        private final LeaderboardSettingsDialogComponent leaderboardSettingsDialogComponent;
        private final StringMessages stringMessages;
        private CheckBox embeddedCheckbox;
        private CheckBox hideToolbarCheckbox;
        private CheckBox showRaceDetailsCheckbox;
        private CheckBox autoExpandLastRaceBox;
        private CheckBox autoRefreshCheckbox;
        private CheckBox showChartsCheckbox;
        private ListBox chartDetailListBox;
        private CheckBox showOverallLeaderboardCheckbox;
        private CheckBox showSeriesLeaderboardsCheckbox;
        private Anchor resultingUrl;
        private final String leaderboardName;
        private final String leaderboardDisplayName;
        
        private final LeaderboardType leaderboardType; 
        
        public LeaderboardUrlConfigurationDialogComponent(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
            this.stringMessages = stringMessages;
            this.leaderboardType = leaderboard.type;
            this.leaderboardName = leaderboard.name;
            this.leaderboardDisplayName = leaderboard.displayName;
            List<RaceColumnDTO> raceList = leaderboard.getRaceList();
            List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
            for (RaceColumnDTO raceColumn : raceList) {
                namesOfRaceColumnsToShow.add(raceColumn.getName());
            }
            LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                    namesOfRaceColumnsToShow, /* namesOfRacesToShow */null, /* nameOfRaceToSort */null, /* autoExpandPreSelectedRace */
                    false, /* showRegattaRank */ true);
            leaderboardSettingsDialogComponent = new LeaderboardSettingsDialogComponent(settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), settings.getOverallDetailsToShow(), raceList, 
                /* select all races by default */ raceList, new ExplicitRaceColumnSelection(),
                /* autoExpandPreSelectedRace */ false, settings.isShowAddedScores(),
                /* delayBetweenAutoAdvancesInMilliseconds */ 3000l, settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), 
                settings.isShowCompetitorSailIdColumn(), settings.isShowCompetitorFullNameColumn(), stringMessages);
        }

        private void updateURL(LeaderboardUrlSettings settings, String leaderboardName, String leaderboardDisplayName) {
            resultingUrl.setHref(LeaderboardUrlSettings.getUrl(leaderboardName, leaderboardDisplayName, settings));
        }

        /**
         * In addition to the usual leaderboard settings widget, add a checkbox for embedded mode
         */
        @Override
        public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
            FlowPanel panel = (FlowPanel) leaderboardSettingsDialogComponent.getAdditionalWidget(dialog);
            panel.add(createAdditionalUrlSettingsPanel(dialog));
            return panel;
        }

        @Override
        public Validator<LeaderboardUrlSettings> getValidator() {
            final Validator<LeaderboardSettings> superValidator = leaderboardSettingsDialogComponent.getValidator();
            Validator<LeaderboardUrlSettings> result = new Validator<LeaderboardUrlSettings>() {
                @Override
                public String getErrorMessage(LeaderboardUrlSettings settings) {
                    String errorMessage = superValidator.getErrorMessage(settings.getLeaderboardSettings());
                    if (errorMessage == null) {
                        updateURL(settings, leaderboardName, leaderboardDisplayName);
                    }
                    return errorMessage;
                }
            };
            return result;
        }

        private FlowPanel createAdditionalUrlSettingsPanel(DataEntryDialog<?> dialog) {
            FlowPanel urlSettingsPanel = new FlowPanel();
            urlSettingsPanel.addStyleName("SettingsDialogComponent");
            urlSettingsPanel.add(dialog.createHeadline(stringMessages.additionalUrlSettings(), true));
            FlowPanel urlSettingsContent = new FlowPanel();
            urlSettingsContent.addStyleName("dialogInnerContent");
            urlSettingsPanel.add(urlSettingsContent);
            embeddedCheckbox = dialog.createCheckbox(stringMessages.embedded());
            urlSettingsContent.add(embeddedCheckbox);
            hideToolbarCheckbox = dialog.createCheckbox(stringMessages.hideToolbar());
            urlSettingsContent.add(hideToolbarCheckbox);
            autoExpandLastRaceBox = dialog.createCheckbox(stringMessages.expandLastRace());
            urlSettingsContent.add(autoExpandLastRaceBox);
            showRaceDetailsCheckbox = dialog.createCheckbox(stringMessages.showRaceDetails());
            urlSettingsContent.add(showRaceDetailsCheckbox);
            autoRefreshCheckbox = dialog.createCheckbox(stringMessages.autoRefresh());
            urlSettingsContent.add(autoRefreshCheckbox);

            if(leaderboardType.isMetaLeaderboard()) {
                showSeriesLeaderboardsCheckbox = dialog.createCheckbox(stringMessages.showSeriesLeaderboards());
                urlSettingsContent.add(showSeriesLeaderboardsCheckbox);
            } else {
                showOverallLeaderboardCheckbox = dialog.createCheckbox(stringMessages.showOverallLeaderboard());
                urlSettingsContent.add(showOverallLeaderboardCheckbox);
            }
            showChartsCheckbox = dialog.createCheckbox(stringMessages.showCharts()+":");
            urlSettingsContent.add(showChartsCheckbox);

            chartDetailListBox = dialog.createListBox(false);
            urlSettingsContent.add(chartDetailListBox);
            if (leaderboardType.isMetaLeaderboard()) {
                chartDetailListBox.addItem(DetailType.OVERALL_RANK.name());
                chartDetailListBox.addItem(DetailType.REGATTA_TOTAL_POINTS_SUM.name());
            } else {
                chartDetailListBox.addItem(DetailType.REGATTA_RANK.name());
                chartDetailListBox.addItem(DetailType.REGATTA_TOTAL_POINTS_SUM.name());
            }
            chartDetailListBox.setSelectedIndex(0);
            
            resultingUrl = new Anchor(stringMessages.leaderboard() + " URL");
            urlSettingsContent.add(resultingUrl);
            return urlSettingsPanel;
        }

        @Override
        public LeaderboardUrlSettings getResult() {
            boolean showOverallLeaderboard = leaderboardType.isMetaLeaderboard() ? false : showOverallLeaderboardCheckbox.getValue();
            boolean showSeriesLeaderboards = leaderboardType.isMetaLeaderboard() ? showSeriesLeaderboardsCheckbox.getValue() : false;
            DetailType chartType = showChartsCheckbox.getValue() ? DetailType.valueOf(chartDetailListBox.getItemText(chartDetailListBox.getSelectedIndex())) : null; 

            return new LeaderboardUrlSettings(leaderboardSettingsDialogComponent.getResult(),
                    embeddedCheckbox.getValue(), hideToolbarCheckbox.getValue(),
                    showRaceDetailsCheckbox.getValue(), autoRefreshCheckbox.getValue(), autoExpandLastRaceBox.getValue(),
                    showChartsCheckbox.getValue(), chartType, showOverallLeaderboard, showSeriesLeaderboards);
        }

        @Override
        public FocusWidget getFocusWidget() {
            return leaderboardSettingsDialogComponent.getFocusWidget();
        }
    }
}
