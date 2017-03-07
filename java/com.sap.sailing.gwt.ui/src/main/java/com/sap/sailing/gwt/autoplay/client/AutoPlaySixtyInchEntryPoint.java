package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayActivityMapperSixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayHistoryMapperSixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class AutoPlaySixtyInchEntryPoint extends AbstractMvpEntryPoint<StringMessages, AutoPlayClientFactory> {
    @Override
    public void doOnModuleLoad() {
        AutoPlayClientFactory clientFactory = GWT.create(AutoPlayClientFactory.class);

        AutoPlayHistoryMapperSixtyInch applicationHistoryMapper = GWT.create(AutoPlayHistoryMapperSixtyInch.class);
        initMvp(clientFactory, applicationHistoryMapper, new AutoPlayActivityMapperSixtyInchImpl(clientFactory));

        RootLayoutPanel.get().add(clientFactory.getRoot());

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();

        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
        
        SixtyInchOrchestrator orchestrator = new SixtyInchOrchestrator(clientFactory);
        orchestrator.start();
        
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}