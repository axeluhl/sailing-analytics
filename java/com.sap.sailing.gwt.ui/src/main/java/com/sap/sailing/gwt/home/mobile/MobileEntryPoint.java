package com.sap.sailing.gwt.home.mobile;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.mobile.app.MobileActivityMapper;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.resources.SharedResources;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;

public class MobileEntryPoint extends AbstractMvpEntryPoint<StringMessages> {

    @Override
    public void doOnModuleLoad() {

        SharedResources sRes = GWT.create(SharedResources.class);
        sRes.mediaCss().ensureInjected();
        sRes.mainCss().ensureInjected();
        MobileApplicationClientFactory clientFactory = new MobileApplicationClientFactory();
        ApplicationHistoryMapper applicationHistoryMapper = GWT.create(ApplicationHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new MobileActivityMapper(clientFactory));
    }

    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }

}