package com.sap.sailing.gwt.ui.shared.components;

public interface ComponentGroup<SettingsType> extends Component<SettingsType> {

    boolean addComponent(Component<?> component);

    boolean removeComponent(Component<?> component);

    Iterable<Component<?>> getComponents();
}
