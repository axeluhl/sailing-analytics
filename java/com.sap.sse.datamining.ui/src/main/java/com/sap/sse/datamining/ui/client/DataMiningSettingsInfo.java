package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public interface DataMiningSettingsInfo {

    String getId();

    <SettingsType extends SerializableSettings> SettingsDialogComponent<SettingsType> createSettingsDialogComponent(
            SettingsType settings);

    String getLocalizedName();

}
