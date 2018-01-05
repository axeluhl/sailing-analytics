package com.sap.sailing.gwt.ui.datamining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.shared.FoilingSegmentsDataMiningSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.polarmining.FoilingSegmentsDataMiningSettingsDialogComponent;
import com.sap.sailing.gwt.ui.polarmining.PolarDataMiningSettingsDialogComponent;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettingsImpl;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class DataMiningSettingsInfoManager {
    
    private final Map<Class<?>, DataMiningSettingsInfo> infosMappedBySettingsType;
    
    public DataMiningSettingsInfoManager() {
        infosMappedBySettingsType = new HashMap<>();
        
        // GWT doesn't support Class.isAssignableFrom and Class.getInterfaces.
        // Adding every implementation of the desired type is necessary.
        PolarDataMiningSettingsInfo polarDataMiningSettingsInfo = new PolarDataMiningSettingsInfo();
        infosMappedBySettingsType.put(PolarDataMiningSettings.class, polarDataMiningSettingsInfo);
        infosMappedBySettingsType.put(PolarDataMiningSettingsImpl.class, polarDataMiningSettingsInfo);
        
        FoilingSegmentsDataMiningSettingsInfo foilingDataMiningSettingsInfo = new FoilingSegmentsDataMiningSettingsInfo();
        infosMappedBySettingsType.put(FoilingSegmentsDataMiningSettings.class, foilingDataMiningSettingsInfo);
    }

    public DataMiningSettingsInfo getSettingsInfo(Class<?> settingsType) {
        return infosMappedBySettingsType.get(settingsType);
    }
    
    private class PolarDataMiningSettingsInfo implements DataMiningSettingsInfo {
        @SuppressWarnings("unchecked")
        @Override
        public <SettingsType extends SerializableSettings> SettingsDialogComponent<SettingsType> createSettingsDialogComponent(SettingsType settings) {
            return (SettingsDialogComponent<SettingsType>) new PolarDataMiningSettingsDialogComponent((PolarDataMiningSettings) settings);
        }

        @Override
        public String getLocalizedName(StringMessages stringMessages) {
            return stringMessages.polars();
        }

        @Override
        public String getId() {
            return "PolarDataMiningSettingsInfo";
        }
    }

    private class FoilingSegmentsDataMiningSettingsInfo implements DataMiningSettingsInfo {
        @SuppressWarnings("unchecked")
        @Override
        public <SettingsType extends SerializableSettings> SettingsDialogComponent<SettingsType> createSettingsDialogComponent(SettingsType settings) {
            return (SettingsDialogComponent<SettingsType>) new FoilingSegmentsDataMiningSettingsDialogComponent((FoilingSegmentsDataMiningSettings) settings);
        }

        @Override
        public String getLocalizedName(StringMessages stringMessages) {
            return stringMessages.foilingSegments();
        }

        @Override
        public String getId() {
            return "FoilingSegmentsDataMiningSettingsInfo";
        }
    }
}
