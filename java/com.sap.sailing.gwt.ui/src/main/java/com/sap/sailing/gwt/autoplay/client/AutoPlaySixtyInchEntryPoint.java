package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayHistoryMapperSixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigatorSixtyInch;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.AutoPlayActivityMapperSixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.SixtyInchOrchestrator;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.autoplay.client.resources.SixtyInchBundle;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class AutoPlaySixtyInchEntryPoint
        extends AbstractMvpEntryPoint<StringMessages, AutoPlayClientFactory<PlaceNavigatorSixtyInch>> {
    private final SixtyInchBundle RES = GWT.create(SixtyInchBundle.class);
    @Override
    public void doOnModuleLoad() {
        RES.style().ensureInjected();
        AutoPlayClientFactorySixtyInch clientFactory = GWT.create(AutoPlayClientFactorySixtyInchImpl.class);

        AutoPlayHistoryMapperSixtyInch applicationHistoryMapper = GWT.create(AutoPlayHistoryMapperSixtyInch.class);
        initMvp(clientFactory, applicationHistoryMapper, new AutoPlayActivityMapperSixtyInchImpl(clientFactory));

        RootLayoutPanel.get().add(clientFactory.getRoot());

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();

        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
        
        Orchestrator orchestrator = new SixtyInchOrchestrator(clientFactory);
        clientFactory.getPlaceNavigator().setOrchestrator(orchestrator);
        
        // loadPlace CTX if exists here! waiting for settings merge before
        SixtyInchSetting fromPlace = null;
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}