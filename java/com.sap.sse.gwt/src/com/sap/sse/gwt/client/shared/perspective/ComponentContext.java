package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * A {@link ComponentContext} manages all default settings of perspectives and components.
 * It supplies components with initial settings and determines whether the settings of components are storable or not.
 * @author Vladislav Chumak
 *
 * @param <L> The {@link ComponentLifecycle} type of the root component/perspective containing all settings for itself and its subcomponents.
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all settings for itself and its subcomponents.
 */
public interface ComponentContext<L extends ComponentLifecycle<S, ?>, S extends Settings> {

    /**
     * Stores the 
     * @param component
     * @param newDefaultSettings
     */
    void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings);

    L getRootLifecycle();

    S getDefaultSettings();

    boolean hasMakeCustomDefaultSettingsSupport(Component<?> component);

}
