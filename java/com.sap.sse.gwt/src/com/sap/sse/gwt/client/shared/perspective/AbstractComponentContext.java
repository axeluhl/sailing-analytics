package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;

public abstract class AbstractComponentContext<PL extends PerspectiveLifecycle<PS>, PS extends Settings> {
    
    protected final PL rootPerspectiveLifecycle;
    
    public AbstractComponentContext(PL rootPerspectiveLifecycle) {
        this.rootPerspectiveLifecycle = rootPerspectiveLifecycle;
    }
    
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        throw new UnsupportedOperationException("Make Default action is unsupported for this type of ComponentContext");
    }
    
    public PL getRootPerspectiveLifecycle() {
        return rootPerspectiveLifecycle;
    }
    
    public PerspectiveCompositeSettings<PS> getDefaultSettingsForRootPerspective() {
        return rootPerspectiveLifecycle.createDefaultSettings();
    }
    
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        return false;
    }
}
