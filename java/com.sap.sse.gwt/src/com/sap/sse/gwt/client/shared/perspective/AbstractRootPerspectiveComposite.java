package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public abstract class AbstractRootPerspectiveComposite<PL extends PerspectiveLifecycle<PS>, PS extends Settings> extends AbstractPerspectiveComposite<PL, PS> {

    public AbstractRootPerspectiveComposite(AbstractComponentContextWithSettingsStorage<PL, PS> componentContext,
            PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings) {
        super(componentContext, perspectiveLifecycleWithAllSettings);
    }
    
}
