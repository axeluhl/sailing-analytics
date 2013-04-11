package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint.LeaderboardUrlSettings;
import com.sap.sailing.gwt.ui.shared.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class LeaderboardUrlConfigurationDialog extends SettingsDialog<LeaderboardUrlSettings> {

    public LeaderboardUrlConfigurationDialog(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        super(new ProxyLeaderboardUrlComponent(stringMessages, leaderboard), stringMessages, /* animationEnabled */ false);
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
    }
    
    private static class LeaderboardUrlConfigurationDialogComponent implements SettingsDialogComponent<LeaderboardUrlSettings> {
        private final LeaderboardSettingsDialogComponent leaderboardSettingsDialogComponent;
        private final StringMessages stringMessages;
        private CheckBox embeddedCheckbox;
        private CheckBox showRaceDetailsCheckbox;
        private CheckBox autoExpandLastRaceBox;
        private CheckBox autoRefreshCheckbox;
        private Anchor resultingUrl;
        private final String leaderboardName;
        private final String leaderboardDisplayName;
        
        public LeaderboardUrlConfigurationDialogComponent(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
            this.stringMessages = stringMessages;
            this.leaderboardName = leaderboard.name;
            this.leaderboardDisplayName = leaderboard.displayName;
            List<RaceColumnDTO> raceList = leaderboard.getRaceList();
            List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
            for (RaceColumnDTO raceColumn : raceList) {
                namesOfRaceColumnsToShow.add(raceColumn.name);
            }
            LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                    namesOfRaceColumnsToShow, /* namesOfRacesToShow */null, /* nameOfRaceToSort */null, /* autoExpandPreSelectedRace */
                    false, /* showMetaLeaderboardsOnSamePage */ false);
            List<DetailType> overallDetailsToShow = Collections.emptyList();
            leaderboardSettingsDialogComponent = new LeaderboardSettingsDialogComponent(settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), overallDetailsToShow, raceList, 
                /* select all races by default */ raceList, new ExplicitRaceColumnSelection(),
                /* autoExpandPreSelectedRace */ false,
                /* showOverallLeaderboardOnSamePage */ false,
                /* delayBetweenAutoAdvancesInMilliseconds */ 3000l, /* delayInMilliseconds */ 3000l, stringMessages);
        }

        private void updateURL(LeaderboardUrlSettings settings, String leaderboardName, String leaderboardDisplayName) {
            resultingUrl.setHref(LeaderboardEntryPoint.getUrl(leaderboardName, leaderboardDisplayName, settings));
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
            autoExpandLastRaceBox = dialog.createCheckbox(stringMessages.expandLastRace());
            urlSettingsContent.add(autoExpandLastRaceBox);
            showRaceDetailsCheckbox = dialog.createCheckbox(stringMessages.showRaceDetails());
            urlSettingsContent.add(showRaceDetailsCheckbox);
            autoRefreshCheckbox = dialog.createCheckbox(stringMessages.autoRefresh());
            urlSettingsContent.add(autoRefreshCheckbox);
            resultingUrl = new Anchor(stringMessages.leaderboard());
            urlSettingsContent.add(resultingUrl);
            return urlSettingsPanel;
        }

        @Override
        public LeaderboardUrlSettings getResult() {
            return new LeaderboardUrlSettings(leaderboardSettingsDialogComponent.getResult(),
                    embeddedCheckbox.getValue(),
                    showRaceDetailsCheckbox.getValue(), autoRefreshCheckbox.getValue(), autoExpandLastRaceBox.getValue());
        }

        @Override
        public FocusWidget getFocusWidget() {
            return leaderboardSettingsDialogComponent.getFocusWidget();
        }
    }
}
