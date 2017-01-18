package com.sap.sse.gwt.client.shared.perspective;

import java.util.Collection;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A perspective is a composition of UI components into a view/page. The perspective itself is also an {@link Component}
 * and can have {@link Settings}.
 * 
 * @param <PS>
 *            the {@link Perspective} settings type
 */
public interface Perspective<PS extends Settings> extends Component<PerspectiveCompositeSettings<PS>> {
    Collection<Component<? extends Settings>> getComponents();
    
    SettingsDialogComponent<PS> getPerspectiveOwnSettingsDialogComponent();
    
    boolean hasPerspectiveOwnSettings();
}
