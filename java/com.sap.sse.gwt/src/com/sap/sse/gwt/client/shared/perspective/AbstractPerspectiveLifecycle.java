package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * An abstract base class for perspective lifecycles.
 * @author Frank
 *
 */
public abstract class AbstractPerspectiveLifecycle<P extends Perspective<S>, S extends Settings, SDP extends SettingsDialogComponent<S>, PCA extends PerspectiveConstructorArgs<P, S>>
    implements PerspectiveLifecycle<P, S, SDP, PCA> {

    protected final List<ComponentLifecycle<?,?,?,?>> componentLifecycles;
    
    public AbstractPerspectiveLifecycle() {
        componentLifecycles = new ArrayList<>();
    }
    
    @Override
    public Iterable<ComponentLifecycle<?,?,?,?>> getComponentLifecycles() {
        return componentLifecycles;
    }

    @Override
    public String getPerspectiveName() {
        return getLocalizedShortName();
    }
}
