package com.sap.sailing.gwt.home.client.gin;

import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.sap.sailing.gwt.home.client.shared.PageNameConstants;
import com.sap.sailing.gwt.home.client.shared.UiModule;

public class SharedModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new DefaultModule());
    	install(new UiModule());

        bindConstant().annotatedWith(DefaultPlace.class).to(PageNameConstants.startPage);
        bindConstant().annotatedWith(ErrorPlace.class).to(PageNameConstants.startPage);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(PageNameConstants.startPage);
    }
}
