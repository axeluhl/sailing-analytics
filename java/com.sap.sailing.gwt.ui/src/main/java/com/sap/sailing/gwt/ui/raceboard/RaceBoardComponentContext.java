package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorageAndPatching;
import com.sap.sse.security.ui.settings.UserSettingsStorageManagerWithPatching;

public class RaceBoardComponentContext extends ComponentContextWithSettingsStorageAndPatching<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> {

    public RaceBoardComponentContext(
            RaceBoardPerspectiveLifecycle rootLifecycle,
            UserSettingsStorageManagerWithPatching<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> settingsStorageManager) {
        super(rootLifecycle, settingsStorageManager);
    }
    
    //TODO add methods for convenient settings patching for modes considering the discussed requirements
    public<CS extends Settings> void addModesPatching(Component<? extends Settings> component, CS additiveSettings, OnSettingsPatchedCallback<CS> patchCallback) {
        //TODO implement additive patching
    }
    
    public interface OnSettingsPatchedCallback<CS extends Settings> {
        void settingsPatched(CS patchedSettings);
    }

}
