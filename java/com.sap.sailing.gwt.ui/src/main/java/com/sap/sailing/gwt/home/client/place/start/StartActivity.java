package com.sap.sailing.gwt.home.client.place.start;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class StartActivity extends AbstractActivity {
    private final StartClientFactory clientFactory;

    public StartActivity(StartPlace place, StartClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        final StartView view = clientFactory.createStartView();
        panel.setWidget(view.asWidget());
        
        clientFactory.getSailingService().getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onSuccess(List<EventDTO> result) {
                fillStartPageEvents(view, result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    @SuppressWarnings("deprecation")
    protected void fillStartPageEvents(StartView view, List<EventDTO> events) {
        EventDTO featuredEvent = null;
        List<EventDTO> recentEvents = new ArrayList<EventDTO>();
        Date now = new Date();
        int currentYear = now.getYear();
        
        for(EventDTO event: events) {
            if(event.startDate != null && event.endDate != null) {
                if(featuredEvent == null && event.startDate.after(now)) {
                    featuredEvent = event; 
                    view.setFeaturedEvent(event);
                }
                if(event.endDate.before(now) && event.endDate.getYear() == currentYear) {
                    recentEvents.add(event);
                }
            }
        }
        view.setRecentEvents(recentEvents);
    }
}
