package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentGroup;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class SimpleComponentGroup<SettingsType> implements ComponentGroup<SettingsType> {

    private String groupName;
    
    private final List<Component<?>> components;

    public SimpleComponentGroup(String groupName) {
        this.groupName = groupName;
        components = new ArrayList<Component<?>>(); 
    }
    
    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SettingsType> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(SettingsType newSettings) {
    }

    @Override
    public String getLocalizedShortName() {
        return groupName;
    }

    @Override
    public Widget getEntryWidget() {
        return null;
    }

    @Override
    public boolean addComponent(Component<?> component) {
        return components.add(component);
    }

    @Override
    public boolean removeComponent(Component<?> component) {
        return components.remove(component);
    }

    @Override
    public Iterable<Component<?>> getComponents() {
        return components;
    }


}
