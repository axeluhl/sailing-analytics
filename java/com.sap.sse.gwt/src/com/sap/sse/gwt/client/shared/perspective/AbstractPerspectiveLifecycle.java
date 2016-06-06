package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * An abstract base class for perspective lifecycle's.
 * @param <PS>
 *            the perspective settings type
 * @param <PCS>
 *            the perspective composite settings type
 * @param <SDP>
 *            the settings dialog component type 
 * @author Frank Mittag
 *
 */
public abstract class AbstractPerspectiveLifecycle<PS extends Settings, PCS extends PerspectiveCompositeSettings<PS>, 
        SDP extends SettingsDialogComponent<PCS>> implements PerspectiveLifecycle<PS, PCS, SDP> {

    protected final List<ComponentLifecycle<?,?>> componentLifecycles;
    
    public AbstractPerspectiveLifecycle() {
        componentLifecycles = new ArrayList<>();
    }
    
    public PerspectiveCompositeTabbedSettingsDialogComponent<PS> getSettingsDialogComponent(PerspectiveCompositeSettings<PS> settings) {
        PerspectiveLifecycleWithAllSettings<?, PS> perspectiveLifecycleWithAllSettings = new PerspectiveLifecycleWithAllSettings<>(this, settings); 
        
        return new PerspectiveCompositeTabbedSettingsDialogComponent<PS>(perspectiveLifecycleWithAllSettings);
    }
    
    protected CompositeSettings getComponentIdsAndDefaultSettings() {
        Map<Serializable, Settings> componentIdsAndSettings = new HashMap<>();
        for (ComponentLifecycle<?,?> componentLifecycle : componentLifecycles) {
            componentIdsAndSettings.put(componentLifecycle.getComponentId(), componentLifecycle.createDefaultSettings());
        }
        CompositeSettings compositeSettings = new CompositeSettings(componentIdsAndSettings);
        return compositeSettings;
    }

    public Iterable<ComponentLifecycle<?,?>> getComponentLifecycles() {
        return componentLifecycles;
    }

    @Override
    public Serializable getComponentId() {
        return getLocalizedShortName();
    }
}
