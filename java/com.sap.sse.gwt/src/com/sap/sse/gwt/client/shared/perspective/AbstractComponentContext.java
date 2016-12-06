package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public abstract class AbstractComponentContext<L extends ComponentLifecycle<S, ?>, S extends Settings> {
    
    protected final L rootLifecycle;
    
    public AbstractComponentContext(L rootLifecycle) {
        this.rootLifecycle = rootLifecycle;
    }
    
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        throw new UnsupportedOperationException("Make Default action is unsupported for this type of ComponentContext");
    }
    
    public L getRootLifecycle() {
        return rootLifecycle;
    }
    
    public S getDefaultSettings() {
        return rootLifecycle.createDefaultSettings();
    }
    
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        return false;
    }
}
