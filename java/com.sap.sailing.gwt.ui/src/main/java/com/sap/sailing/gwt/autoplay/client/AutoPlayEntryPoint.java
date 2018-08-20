package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayActivityMapperImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactoryImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayHistoryMapper;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class AutoPlayEntryPoint
        extends AbstractMvpEntryPoint<StringMessages, AutoPlayClientFactory> {
    @Override
    public void doOnModuleLoad() {

        AutoPlayClientFactory clientFactory = new AutoPlayClientFactoryImpl();

        AutoPlayHistoryMapper applicationHistoryMapper = GWT.create(AutoPlayHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new AutoPlayActivityMapperImpl(clientFactory));

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();
        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
    }

    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}