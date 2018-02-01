package com.sap.sailing.gwt.ui.datamining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.shared.ManeuverSettings;
import com.sap.sailing.datamining.shared.ManeuverSettingsImpl;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettingsImpl;
import com.sap.sailing.datamining.shared.FoilingSegmentsDataMiningSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.ManeuverSettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.presentation.ManeuverSpeedDetailsSettingsDialogComponent;
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

        ManeuverSpeedDetailsSettingsInfo maneuverSpeedDetailsSettingsInfo = new ManeuverSpeedDetailsSettingsInfo();
        infosMappedBySettingsType.put(ManeuverSpeedDetailsSettings.class, maneuverSpeedDetailsSettingsInfo);
        infosMappedBySettingsType.put(ManeuverSpeedDetailsSettingsImpl.class, maneuverSpeedDetailsSettingsInfo);

        ManeuverSettingsInfo maneuverSettingsInfo = new ManeuverSettingsInfo();
        infosMappedBySettingsType.put(ManeuverSettings.class, maneuverSettingsInfo);
        infosMappedBySettingsType.put(ManeuverSettingsImpl.class, maneuverSettingsInfo);

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

    private class ManeuverSettingsInfo implements DataMiningSettingsInfo {

        @SuppressWarnings("unchecked")
        @Override
        public <SettingsType extends SerializableSettings> SettingsDialogComponent<SettingsType> createSettingsDialogComponent(
                SettingsType settings) {
            return (SettingsDialogComponent<SettingsType>) new ManeuverSettingsDialogComponent(
                    (ManeuverSettings) settings);
        }

        @Override
        public String getLocalizedName(StringMessages stringMessages) {
            return stringMessages.maneuver();
        }

        @Override
        public String getId() {
            return "ManeuverSettingsInfo";
        }

    }

    private class ManeuverSpeedDetailsSettingsInfo implements DataMiningSettingsInfo {

        @SuppressWarnings("unchecked")
        @Override
        public <SettingsType extends SerializableSettings> SettingsDialogComponent<SettingsType> createSettingsDialogComponent(
                SettingsType settings) {
            return (SettingsDialogComponent<SettingsType>) new ManeuverSpeedDetailsSettingsDialogComponent(
                    (ManeuverSpeedDetailsSettings) settings);
        }

        @Override
        public String getLocalizedName(StringMessages stringMessages) {
            return stringMessages.maneuverSpeedDetails();
        }

        @Override
        public String getId() {
            return "ManeuverSpeedDetailsSettingsInfo";
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
