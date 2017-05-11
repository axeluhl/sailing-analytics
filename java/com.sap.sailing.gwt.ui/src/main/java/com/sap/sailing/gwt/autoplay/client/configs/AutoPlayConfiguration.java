package com.sap.sailing.gwt.autoplay.client.configs;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public abstract class AutoPlayConfiguration {

    public abstract void startRootNode(AutoPlayClientFactory cf, AutoPlayContextDefinition context,
            PerspectiveCompositeSettings<?> settings);



}
