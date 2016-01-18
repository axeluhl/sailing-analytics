package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A lifecycle interface for perspectives
 * @author Frank
 *
 * @param <P> the perspective type
 * @param <S> the settings type
 * @param <SDP> the settings dialog component type
 */
public interface PerspectiveLifecycle<P extends Perspective<S>, S extends Settings, SDP extends SettingsDialogComponent<S>, 
    PCA extends PerspectiveConstructorArgs<P, S>> extends ComponentLifecycle<P, S, SDP, PCA> {
    Iterable<ComponentLifecycle<?,?,?,?>> getComponentLifecycles();
    
    P createComponent(PCA PerspectiveConstructorArgs, S settings);
    
    String getPerspectiveName();
}
