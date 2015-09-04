package com.sap.sailing.gwt.home.desktop;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.desktop.app.DesktopActivityMapper;
import com.sap.sailing.gwt.home.desktop.app.DesktopClientFactory;
import com.sap.sailing.gwt.home.desktop.app.TabletAndDesktopApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.gwt.resources.Highcharts;

public class DesktopEntryPoint extends AbstractMvpEntryPoint<StringMessages> {
    @Override
    public void doOnModuleLoad() {
        CommonControlsCSS.ensureInjected();
        Highcharts.ensureInjected();

        DesktopClientFactory clientFactory = new TabletAndDesktopApplicationClientFactory();
        ApplicationHistoryMapper applicationHistoryMapper = GWT.create(ApplicationHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new DesktopActivityMapper(clientFactory));

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}