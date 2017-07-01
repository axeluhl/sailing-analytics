package com.sap.sse.gwt.client.shared.perspective;

import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A lifecycle interface for a {@link Perspective}, it can support own Settings that are PerspeciveWide and manage
 * various subcomponent lifecycles.
 */
public interface PerspectiveLifecycle<PS extends Settings> extends
        ComponentLifecycle<PerspectiveCompositeSettings<PS>> {
    Iterable<ComponentLifecycle<?>> getComponentLifecycles();
    
    PS createPerspectiveOwnDefaultSettings();
    
    SettingsDialogComponent<PS> getPerspectiveOwnSettingsDialogComponent(PS settings);

    Map<String, Settings> createDefaultComponentIdsAndSettings();

    <SS extends Settings> ComponentLifecycle<SS> getLifecycleForId(String id);
}
