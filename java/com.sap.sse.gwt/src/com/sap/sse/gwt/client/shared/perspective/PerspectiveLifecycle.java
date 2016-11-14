package com.sap.sse.gwt.client.shared.perspective;

import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A lifecycle interface for a {@link Perspective}
 *
 * @param <PS>
 *            the perspective specific settings type
 * @author Frank Mittag
 */
public interface PerspectiveLifecycle<PS extends Settings> extends
        ComponentLifecycle<PerspectiveCompositeSettings<PS>, PerspectiveCompositeTabbedSettingsDialogComponent<PS>> {
    Iterable<ComponentLifecycle<?,?>> getComponentLifecycles();
    
    PS createPerspectiveOwnDefaultSettings();
    
    PS clonePerspectiveOwnSettings(PS settings);
    
    SettingsDialogComponent<PS> getPerspectiveOwnSettingsDialogComponent(PS settings);

    Map<String, Settings> cloneComponentIdsAndSettings(PerspectiveCompositeSettings<PS> settings);

    Map<String, Settings> createDefaultComponentIdsAndSettings();
}
