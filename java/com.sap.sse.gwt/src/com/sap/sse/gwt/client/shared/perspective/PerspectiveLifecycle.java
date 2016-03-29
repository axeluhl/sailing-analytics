package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A lifecycle interface for perspectives
 *
 * @param <P>
 *            the perspective type
 * @param <S>
 *            the settings type
 * @param <SDP>
 *            the settings dialog component type
 * @author Frank Mittag
 */
public interface PerspectiveLifecycle<P extends Perspective<S>, S extends Settings, SDP extends SettingsDialogComponent<S>>
    extends ComponentLifecycle<P, S, SDP> {
    
    Iterable<ComponentLifecycle<?,?,?>> getComponentLifecycles();
    
    CompositeLifecycleSettings getComponentLifecyclesAndDefaultSettings();
}
