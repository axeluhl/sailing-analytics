package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A lifecycle interface for a {@link Perspective}
 *
 * @param <PS>
 *      the perspective specific settings type
 * @param <PCS>
 *      the perspective composite settings type
 * @param <SDP>
 *      the settings dialog component type
 * @author Frank Mittag
 */
public interface PerspectiveLifecycle<PS extends Settings, PCS extends PerspectiveCompositeLifecycleSettings<?,?>, SDP extends SettingsDialogComponent<PCS>> extends ComponentLifecycle<PCS, SDP> {
    
    Iterable<ComponentLifecycle<?,?>> getComponentLifecycles();
    
    PS createPerspectiveDefaultSettings();
    
    SettingsDialogComponent<PS> getPerspectiveSettingsDialogComponent(PS settings);
}
