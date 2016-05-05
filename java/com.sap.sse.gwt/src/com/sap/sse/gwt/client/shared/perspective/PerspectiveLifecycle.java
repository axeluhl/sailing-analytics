package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A lifecycle interface for perspectives
 *
 * @param <PCS>
 *            the perspective composite settings type
 * @param <SDP>
 *            the settings dialog component type
 * @author Frank Mittag
 */
public interface PerspectiveLifecycle<PCS extends PerspectiveCompositeLifecycleSettings<?,?>, SDP extends SettingsDialogComponent<PCS>> extends ComponentLifecycle<PCS, SDP> {
    
    Iterable<ComponentLifecycle<?,?>> getComponentLifecycles();
    
    CompositeLifecycleSettings getComponentLifecyclesAndDefaultSettings();
}
