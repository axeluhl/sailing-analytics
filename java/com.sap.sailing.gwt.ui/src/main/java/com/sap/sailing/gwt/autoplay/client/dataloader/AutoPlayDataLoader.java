package com.sap.sailing.gwt.autoplay.client.dataloader;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;

public interface AutoPlayDataLoader {
    void startLoading(EventBus eventBus, AutoPlayClientFactorySixtyInch clientFactory);
    void stopLoading();
}
