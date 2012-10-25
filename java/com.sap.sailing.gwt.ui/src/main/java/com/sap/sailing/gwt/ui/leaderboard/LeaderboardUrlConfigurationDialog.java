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
            // TODO display the resulting URL for copy/paste or navigation
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
        private Anchor resultingUrl;
        
        public LeaderboardUrlConfigurationDialogComponent(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
            this.stringMessages = stringMessages;
            List<RaceColumnDTO> raceList = leaderboard.getRaceList();
            List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
            for (RaceColumnDTO raceColumn : raceList) {
                namesOfRaceColumnsToShow.add(raceColumn.name);
            }
            LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                    namesOfRaceColumnsToShow, /* namesOfRacesToShow */null, /* nameOfRaceToSort */null, /* autoExpandPreSelectedRace */
                    false);
            List<DetailType> overallDetailsToShow = Collections.emptyList();
            leaderboardSettingsDialogComponent = new LeaderboardSettingsDialogComponent(settings.getManeuverDetailsToShow(),
                settings.getLegDetailsToShow(), settings.getRaceDetailsToShow(), overallDetailsToShow, raceList,
                /* select all races by default */ raceList, /* autoExpandPreSelectedRace */ false,
                /* delayBetweenAutoAdvancesInMilliseconds */ 3000l,
                /* delayInMilliseconds */ 3000l, stringMessages);
        }

        private void updateURL() {
            resultingUrl.setHref("http://"+embeddedCheckbox.getValue().toString());
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
                public String getErrorMessage(LeaderboardUrlSettings valueToValidate) {
                    updateURL();
                    return superValidator.getErrorMessage(valueToValidate.getLeaderboardSettings());
                }
            };
            return result;
        }

        private FlowPanel createAdditionalUrlSettingsPanel(DataEntryDialog<?> dialog) {
            FlowPanel additionalPanel = new FlowPanel();
            additionalPanel.addStyleName("SettingsDialogComponent");
            FlowPanel content = new FlowPanel();
            content.addStyleName("dialogInnerContent");
            additionalPanel.add(content);
            content.add(dialog.createHeadline(stringMessages.additionalUrlSettings(), true));
            embeddedCheckbox = dialog.createCheckbox(stringMessages.embedded());
            content.add(embeddedCheckbox);
            resultingUrl = new Anchor(stringMessages.leaderboard());
            content.add(resultingUrl);
            return additionalPanel;
        }

        @Override
        public LeaderboardUrlSettings getResult() {
            return new LeaderboardUrlSettings(leaderboardSettingsDialogComponent.getResult(), embeddedCheckbox.getValue());
        }

        @Override
        public FocusWidget getFocusWidget() {
            return leaderboardSettingsDialogComponent.getFocusWidget();
        }
    }
}
