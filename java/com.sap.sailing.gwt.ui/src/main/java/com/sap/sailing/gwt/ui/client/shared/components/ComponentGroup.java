package com.sap.sailing.gwt.ui.client.shared.components;

import com.sap.sailing.domain.common.settings.Settings;

public interface ComponentGroup<SettingsType extends Settings> extends Component<SettingsType> {

    boolean addComponent(Component<?> component);

    boolean removeComponent(Component<?> component);

    Iterable<Component<?>> getComponents();
}
