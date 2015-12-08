package com.sap.sailing.gwt.autoplay.client.place.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ProxyRaceMapComponent implements Component<RaceMapSettings> {
    private final StringMessages stringMessages;
    private RaceMapSettings settings;
    
    public ProxyRaceMapComponent(RaceMapSettings raceMapSettings, StringMessages stringMessages) {
        this.settings = raceMapSettings;
        this.stringMessages = stringMessages;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RaceMapSettings> getSettingsDialogComponent() {
        return new RaceMapSettingsDialogComponent(settings, stringMessages, false);
    }

    @Override
    public void updateSettings(RaceMapSettings newSettings) {
        this.settings = newSettings;
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

    @Override
    public RaceMapSettings getSettings() {
        return settings;
    }
}