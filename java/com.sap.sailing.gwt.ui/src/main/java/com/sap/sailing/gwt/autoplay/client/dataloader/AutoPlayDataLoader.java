package com.sap.sailing.gwt.autoplay.client.dataloader;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;

public interface AutoPlayDataLoader<CF extends AutoPlayClientFactory<?>> {
    void startLoading(EventBus eventBus, CF clientFactory);
    void stopLoading();
}
