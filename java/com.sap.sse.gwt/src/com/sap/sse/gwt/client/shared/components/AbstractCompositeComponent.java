package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.common.settings.Settings;

public abstract class AbstractCompositeComponent<SettingsType extends Settings> extends Composite implements Component<SettingsType> {
    private Component<?> parentComponent;

    public AbstractCompositeComponent(Component<?> parent) {
        this.parentComponent = parent;
        if (parentComponent == null) {
            GWT.log("No parent for component, validate if correct " + this.getClass().getName());
        }
    }

    public Component<?> getParentComponent() {
        return parentComponent;
    }

    @Override
    public ArrayList<String> getPath() {
        return ComponentPathDeterminer.determinePath(this);
    }

}
