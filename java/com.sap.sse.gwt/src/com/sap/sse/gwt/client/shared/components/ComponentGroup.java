package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;

public interface ComponentGroup<SettingsType extends AbstractSettings> extends Component<SettingsType> {

    boolean addComponent(Component<?> component);

    boolean removeComponent(Component<?> component);

    Iterable<Component<?>> getComponents();
}
