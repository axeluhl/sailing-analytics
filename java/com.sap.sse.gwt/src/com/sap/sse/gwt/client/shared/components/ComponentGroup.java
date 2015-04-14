package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public interface ComponentGroup<SettingsType extends Settings> extends Component<SettingsType> {

    boolean addComponent(Component<?> component);

    boolean removeComponent(Component<?> component);

    Iterable<Component<?>> getComponents();
}
