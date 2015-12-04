package com.sap.sailing.gwt.ui.client.shared.perspective;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;

/**
 * An abstract base class for perspectives.
 * @author Frank
 *
 */
public abstract class AbstractPerspective<SettingsType extends Settings> implements Perspective<SettingsType> {

    protected final List<Component<?>> components;
    
    public AbstractPerspective() {
        components = new ArrayList<Component<?>>();
    }
    
    @Override
    public Iterable<Component<?>> getComponents() {
        return components;
    }
}
