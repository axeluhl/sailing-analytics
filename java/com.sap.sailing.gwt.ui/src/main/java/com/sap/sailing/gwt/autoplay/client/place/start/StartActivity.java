package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;
import com.sap.sse.gwt.client.event.LocaleChangeEventHandler;

public class StartActivity extends AbstractActivity {
    private final StartClientFactory clientFactory;

    public static final String LOAD_EVENTS_DATA_CATEGORY = "loadEventsData";

    public StartActivity(StartPlace place, StartClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        clientFactory.getSailingService().getEvents(new MarkedAsyncCallback<List<EventDTO>>(new AsyncCallback<List<EventDTO>>() {
            
            @Override
            public void onSuccess(List<EventDTO> result) {
                final StartView view = clientFactory.createStartView();
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
