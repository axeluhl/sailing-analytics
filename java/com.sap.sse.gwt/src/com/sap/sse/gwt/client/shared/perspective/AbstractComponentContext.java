package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public abstract class AbstractComponentContext<L extends ComponentLifecycle<S, ?>, S extends Settings> implements ComponentContext<L, S> {
    
    protected final L rootLifecycle;
    
    public AbstractComponentContext(L rootLifecycle) {
        this.rootLifecycle = rootLifecycle;
    }
    
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        throw new UnsupportedOperationException("Make Default action is unsupported for this type of ComponentContext");
    }
    
    @Override
    public L getRootLifecycle() {
        return rootLifecycle;
    }
    
    @Override
    public S getDefaultSettings() {
        return rootLifecycle.createDefaultSettings();
    }
    
    @Override
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        return false;
    }
}
