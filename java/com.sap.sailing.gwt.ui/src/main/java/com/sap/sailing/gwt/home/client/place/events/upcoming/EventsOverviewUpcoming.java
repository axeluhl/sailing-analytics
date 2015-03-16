package com.sap.sailing.gwt.home.client.place.events.upcoming;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.events.CollapseAnimation;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;

public class EventsOverviewUpcoming extends Composite {
    
    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewUpcoming> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final HomePlacesNavigator navigator;

    private final List<String> tickerStrings;
    
    @UiField HTMLPanel header;
    @UiField FlowPanel eventsPlaceholder;
    @UiField SpanElement eventsCount;
    @UiField SpanElement ticker;
    @UiField SpanElement colon;
    
    private boolean isContentVisible = true;
    
    private int currentTickerOffset;
    
    private final CollapseAnimation eventsAnimation;

    private Timer tickerTimer = new Timer() {
        @Override
        public void run() {
            nextTicker(false);
        }
    };
    
    private final CollapseAnimation tickerAnimation;

    public EventsOverviewUpcoming(HomePlacesNavigator navigator) {
        this.navigator = navigator;
        tickerStrings = new ArrayList<>();
        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        eventsAnimation = new CollapseAnimation(eventsPlaceholder.getElement());
        tickerAnimation = new CollapseAnimation(ticker);
        
        header.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onHeaderCicked();
            }
        }, ClickEvent.getType());
    }

    public void updateEvents(ArrayList<EventListEventDTO> arrayList) {
        setVisible(arrayList.size() > 0);
        eventsPlaceholder.clear();
        tickerStrings.clear();
        for (EventListEventDTO event : arrayList) {
            EventsOverviewUpcomingEvent upcomingEvent = new EventsOverviewUpcomingEvent(event, navigator);
            tickerStrings.add(event.getDisplayName() + ", " + event.getVenue() + ", "
                    + EventDatesFormatterUtil.formatDateRangeWithoutYear(event.getStartDate(), event.getEndDate()));
            eventsPlaceholder.getElement().appendChild(upcomingEvent.getElement());
        }
        eventsCount.setInnerText(""+arrayList.size());
    }
    
    private void onHeaderCicked() {
        isContentVisible = !isContentVisible;
        updateContentVisibility();
    }
    
    private void updateContentVisibility() {
        eventsAnimation.animate(isContentVisible);
        if(isContentVisible) {
            colon.getStyle().setDisplay(Display.NONE);
            getElement().removeClassName(EventsOverviewUpcomingResources.INSTANCE.css().accordioncollapsed());
            ticker.setInnerText("");
            tickerTimer.cancel();
            tickerAnimation.cancel();
        } else {
            colon.getStyle().clearDisplay();
            getElement().addClassName(EventsOverviewUpcomingResources.INSTANCE.css().accordioncollapsed());
            if(tickerStrings.isEmpty()) {
                ticker.setInnerText("");
            } else {
                nextTicker(true);
            }
        }
    }

    
    protected void nextTicker(boolean restart) {
        currentTickerOffset = restart ? 0 : (currentTickerOffset + 1) % tickerStrings.size();
        ticker.setInnerText(tickerStrings.get(currentTickerOffset));
        tickerAnimation.animate(true);
        if(tickerStrings.size() > 1) {
            tickerTimer.schedule(3000);
        }
    }
}
