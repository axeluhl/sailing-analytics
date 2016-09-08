package com.sap.sse.gwt.client.shared.perspective;

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
    
    SettingsDialogComponent<PS> getPerspectiveOwnSettingsDialogComponent(PS settings);
}
