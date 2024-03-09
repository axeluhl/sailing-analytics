package com.sap.sailing.gwt.settings.client.embeddedmapandwindchart;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class EmbeddedMapAndWindChartContextDefinition extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {

    private static final long serialVersionUID = 8018301079159485102L;

    private transient StringSetting regattaLikeName;
    private transient StringSetting raceColumnName;
    private transient StringSetting fleetName;

    public EmbeddedMapAndWindChartContextDefinition() {
        super(null);
    }

    public EmbeddedMapAndWindChartContextDefinition(final String regattaLikeName, final String raceColumnName,
            final String fleetName) {
        this();
        this.regattaLikeName.setValue(regattaLikeName);
        this.raceColumnName.setValue(raceColumnName);
        this.fleetName.setValue(fleetName);
    }

    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        regattaLikeName = new StringSetting("regattaLikeName", this);
        raceColumnName = new StringSetting("raceColumnName", this);
        fleetName = new StringSetting("fleetName", this);

    }

    public String getRegattaLikeName() {
        return regattaLikeName.getValue();
    }

    public String getRaceColumnName() {
        return raceColumnName.getValue();
    }

    public String getFleetName() {
        return fleetName.getValue();
    }

    public boolean isValidContext() {
        return regattaLikeName.isNotBlank() && raceColumnName.isNotBlank() && fleetName.isNotBlank();
    }

}
