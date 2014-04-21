package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

public class UiModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(LocaleSelection.LocaleSelectionCompositeUiBinder.class).in(Singleton.class);
    }
}