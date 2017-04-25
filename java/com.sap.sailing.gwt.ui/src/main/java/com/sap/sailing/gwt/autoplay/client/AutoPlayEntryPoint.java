package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayAppActivityMapper;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayClientFactoryClassic;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayClientFactoryClassicImpl;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayHistoryMapperDesktopImpl;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class AutoPlayEntryPoint extends AbstractMvpEntryPoint<StringMessages, AutoPlayClientFactory<PlaceNavigator>> {
    @Override
    public void doOnModuleLoad() {

        AutoPlayClientFactoryClassic clientFactory = new AutoPlayClientFactoryClassicImpl();

        AutoPlayHistoryMapperDesktopImpl applicationHistoryMapper = GWT.create(AutoPlayHistoryMapperDesktopImpl.class);
        initMvp(clientFactory, applicationHistoryMapper, new AutoPlayAppActivityMapper(clientFactory));

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();
        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
    }

    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}