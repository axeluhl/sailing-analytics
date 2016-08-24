package com.sap.sailing.gwt.autoplay.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppActivityMapper;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayAppHistoryMapper;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.AbstractMvpEntryPoint;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;

public class AutoPlayEntryPoint extends AbstractMvpEntryPoint<StringMessages, AutoPlayAppClientFactory> {
    @Override
    public void doOnModuleLoad() {
        AutoPlayAppClientFactory clientFactory = GWT.create(AutoPlayAppClientFactory.class);
        
        AutoPlayAppHistoryMapper applicationHistoryMapper = GWT.create(AutoPlayAppHistoryMapper.class);
        initMvp(clientFactory, applicationHistoryMapper, new AutoPlayAppActivityMapper(clientFactory));
        
        SAPHeaderWithAuthentication header = initHeader();
		new FixedSailingAuthentication(clientFactory.getUserService(), header.getAuthenticationMenuView());
        
        RootPanel.get().add(header);

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();
        Document.get().getBody().addClassName(SharedResources.INSTANCE.mainCss().desktop());
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
    
    private SAPHeaderWithAuthentication initHeader() {
        SAPHeaderWithAuthentication header = new SAPHeaderWithAuthentication(getStringMessages().sapSailingAnalytics(),
                getStringMessages().autoplayConfiguration());
        header.getElement().getStyle().setPosition(Position.FIXED);
        header.getElement().getStyle().setTop(0, Unit.PX);
        header.getElement().getStyle().setWidth(100, Unit.PCT);
        return header;
    }
}