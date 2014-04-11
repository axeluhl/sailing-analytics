package com.sap.sailing.gwt.home.client.gin;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.sap.sailing.gwt.home.client.app.ApplicationDesktopModule;

public class DesktopModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new ApplicationDesktopModule());
    }
}
