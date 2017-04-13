package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;

public interface SettingsPatch<S extends Settings> {

    S patchSettings(S settingsToPatch);
    
}
