package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayActivityMapperSixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayHistoryMapperSixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.PlaceNavigatorSixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.SixtyInchSetting;
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
        AutoPlayClientFactorySixtyInch cf = GWT.create(AutoPlayClientFactorySixtyInchImpl.class);

        AutoPlayHistoryMapperSixtyInch applicationHistoryMapper = GWT.create(AutoPlayHistoryMapperSixtyInch.class);
        initMvp(cf, applicationHistoryMapper, new AutoPlayActivityMapperSixtyInchImpl(cf));

        RootLayoutPanel.get().add(cf.getRoot());

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();

        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
        
        // loadPlace CTX if exists here! waiting for settings merge before
        SixtyInchSetting fromPlace = null;
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}