package com.sap.sailing.gwt.autoplay.client.place.start;

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
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        clientFactory.getSailingService().getEvents(new AsyncCallback<List<EventDTO>>() {
            
            @Override
            public void onSuccess(List<EventDTO> result) {
                final StartView view = clientFactory.createStartView();
                panel.setWidget(view.asWidget());
                
                view.setEvents(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }
}
