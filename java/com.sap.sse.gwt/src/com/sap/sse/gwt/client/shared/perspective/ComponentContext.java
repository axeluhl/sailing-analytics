package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public interface ComponentContext<L extends ComponentLifecycle<S, ?>, S extends Settings> {

    void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings);

    L getRootLifecycle();

    S getDefaultSettings();

    boolean hasMakeCustomDefaultSettingsSupport(Component<?> component);

}
