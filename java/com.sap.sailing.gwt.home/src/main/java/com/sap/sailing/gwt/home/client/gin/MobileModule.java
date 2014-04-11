package com.sap.sailing.gwt.home.client.gin;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.sap.sailing.gwt.home.client.app.ApplicationMobileModule;

public class MobileModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new ApplicationMobileModule());
    }
}
