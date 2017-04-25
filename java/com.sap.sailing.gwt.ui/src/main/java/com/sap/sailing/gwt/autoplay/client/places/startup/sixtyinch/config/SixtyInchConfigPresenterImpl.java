package com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.config;

import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;
import com.sap.sse.gwt.client.event.LocaleChangeEventHandler;

public class SixtyInchConfigPresenterImpl extends AbstractActivity implements SixtyInchConfigView.Presenter {
    private final AutoPlayClientFactorySixtyInch clientFactory;

    public static final String LOAD_EVENTS_DATA_CATEGORY = "loadEventsData";

    private SixtyInchConfigView view;

    public SixtyInchConfigPresenterImpl(SixtyInchConfigPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            SixtyInchConfigView view) {
        this.clientFactory = clientFactory;
        this.view = view;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        clientFactory.getSailingService().getEvents(new MarkedAsyncCallback<List<EventDTO>>(new AsyncCallback<List<EventDTO>>() {
            
            @Override
            public void onSuccess(List<EventDTO> result) {
                panel.setWidget(view.asWidget());
                
                view.asWidget().ensureDebugId("AutoPlayStartView");
                view.setEvents(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        }, LOAD_EVENTS_DATA_CATEGORY));
        
        eventBus.addHandler(LocaleChangeEvent.TYPE, new LocaleChangeEventHandler() {
            @Override
            public void onLocaleChange(final LocaleChangeEvent event) {
                UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
                urlBuilder.removeParameter("locale");
                urlBuilder.setParameter("locale", event.getNewLocaleID());
                Window.Location.replace(urlBuilder.buildString());
            }
        });
    }
}
