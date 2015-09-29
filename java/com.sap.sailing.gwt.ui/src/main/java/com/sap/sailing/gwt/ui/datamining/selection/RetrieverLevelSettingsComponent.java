package com.sap.sailing.gwt.ui.datamining.selection;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public abstract class RetrieverLevelSettingsComponent implements Component<SerializableSettings> {

    private final DataRetrieverLevelDTO retrieverLevel;
    private final String localizedName;

    public RetrieverLevelSettingsComponent(DataRetrieverLevelDTO retrieverLevel, String localizedName) {
        this.retrieverLevel = retrieverLevel;
        this.localizedName = localizedName;
    }
    
    public DataRetrieverLevelDTO getRetrieverLevel() {
        return retrieverLevel;
    }

    @Override
    public String getLocalizedShortName() {
        return localizedName;
    }

    @Override
    public Widget getEntryWidget() {
        throw new RuntimeException("Virtual component doesn't have a widget of its own");
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
        throw new RuntimeException("Virtual component doesn't know how to make itself visible");
    }

    @Override
    public boolean hasSettings() {
        return retrieverLevel.hasSettings();
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

}
