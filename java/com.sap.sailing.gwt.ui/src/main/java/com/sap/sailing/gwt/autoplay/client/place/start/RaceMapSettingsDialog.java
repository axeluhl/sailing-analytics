package com.sap.sailing.gwt.autoplay.client.place.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettingsDialogComponent;

public class RaceMapSettingsDialog extends SettingsDialog<RaceMapSettings> {

    public RaceMapSettingsDialog(RaceMapSettings raceMapSettings, StringMessages stringMessages, DialogCallback<RaceMapSettings> callback) {
        super(new ProxyRaceMapComponent(raceMapSettings, stringMessages), stringMessages, callback);
    }
    
    public static class ProxyRaceMapComponent implements Component<RaceMapSettings> {
        private final StringMessages stringMessages;
        private final RaceMapSettingsDialogComponent settingsDialogComponent;
        
        public ProxyRaceMapComponent(RaceMapSettings raceMapSettings, StringMessages stringMessages) {
            this.stringMessages = stringMessages;
            this.settingsDialogComponent = new RaceMapSettingsDialogComponent(raceMapSettings, stringMessages, false);
        }

        @Override
        public boolean hasSettings() {
            return true;
        }

        @Override
        public SettingsDialogComponent<RaceMapSettings> getSettingsDialogComponent() {
            return settingsDialogComponent;
        }

        @Override
        public void updateSettings(RaceMapSettings newSettings) {
            // no-op; the resulting URL has already been updated to the anchor in the dialog
        }

        @Override
        public String getLocalizedShortName() {
            return stringMessages.settingsForComponent(stringMessages.map());
        }

        @Override
        public Widget getEntryWidget() {
            throw new UnsupportedOperationException(
                    "Internal error. This settings dialog does not actually belong to a RaceMap");
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
            return "raceMapSettingsDialog";
        }
    }    
}
