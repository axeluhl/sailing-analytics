package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentContext;

public abstract class AbstractRootPerspectiveComposite<PL extends PerspectiveLifecycle<PS>, PS extends Settings> extends AbstractPerspectiveComposite<PL, PS> {

    public AbstractRootPerspectiveComposite(
            PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings) {
        super(null, perspectiveLifecycleWithAllSettings);
        getComponentTreeNodeInfo().setComponentContext(new ComponentContext(this));
    }
    
}
