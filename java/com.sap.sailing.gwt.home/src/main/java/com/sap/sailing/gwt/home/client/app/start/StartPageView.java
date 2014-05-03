package com.sap.sailing.gwt.home.client.app.start;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

public class StartPageView extends Composite implements StartPagePresenter.MyView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    
    @UiField Label bannerSeriesName;
    @UiField Label bannerName;
    @UiField Label bannerLocation;
    
    @UiField Label upcomingMessage;
    @UiField Label upcomingName;
    @UiField Label upcomingAction;
    
    @UiField(provided=true)
    Event event1;

    @UiField(provided=true)
    Event event2;

    @UiField(provided=true)
    Event event3;

    @UiField(provided=true)
    Event event4;

    @UiField(provided=true)
    Event event5;

    @UiField(provided=true)
    Event event6;
    
    interface StartPageViewUiBinder extends UiBinder<Widget, StartPageView> {
    }

    public StartPageView() {
        super();

        event1 = new Event();
        event2 = new Event();
        event3 = new Event();
        event4 = new Event();
        event5 = new Event();
        event6 = new Event();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {
    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
    }
    
	@Override
	public void setEvents(List<EventDTO> events) {
		int i = 1;
		for(EventDTO event: events) {
			switch (i) {
				case 1: event1.setEvent(event); break;
				case 2: event2.setEvent(event); break;
				case 3: event3.setEvent(event); break;
				case 4: event4.setEvent(event); break;
				case 5: event5.setEvent(event); break;
				case 6: event6.setEvent(event); break;
			}
			i++;
		}
	}
}

