package com.sap.sailing.gwt.home.mobile.partials.recents;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.home.communication.eventlist.EventListEventSeriesDTO;
import com.sap.sailing.gwt.home.communication.eventlist.EventListYearDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.mobile.partials.stage.Stage;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.MobileStatisticsBoxView;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.statistics.YearStatisticsBox;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.utils.CollapseAnimation;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventsOverviewRecentYear extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewRecentYear> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    @UiField SpanElement year;
    @UiField SpanElement eventsCount;
    @UiField FlowPanel recentEventsTeaserPanel;
    @UiField (provided = true) Stage eventStage;
    @UiField DivElement contentDiv;
    @UiField HTMLPanel headerDiv;
    @UiField StringMessages i18n;
    
    private boolean isContentVisible;
    
    private final CollapseAnimation animation;
    
    public EventsOverviewRecentYear(EventListYearDTO yearDTO, MobilePlacesNavigator navigator, boolean showInitial) {
        List<EventListEventDTO> events = yearDTO.getEvents();
        eventStage = new Stage(navigator, false);
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        this.year.setInnerText(String.valueOf(yearDTO.getYear()));
        this.eventsCount.setInnerText(i18n.eventsCount(yearDTO.getEventCount()));
        boolean first = true;
        for (EventListEventDTO eventDTO : events) {
            final PlaceNavigation<EventDefaultPlace> eventNavigation = navigator.getEventNavigation(
                    eventDTO.getId().toString(), eventDTO.getBaseURL(), eventDTO.isOnRemoteServer());
            final EventsOverviewRecentYearEvent recentEvent = new EventsOverviewRecentYearEvent(eventNavigation,
                    eventDTO, eventDTO.getState().getListStateMarker(), first || eventDTO.isRunning());
            final EventListEventSeriesDTO eventSeries = eventDTO.getEventSeries();
            if (eventSeries != null) {
                final String baseUrl = eventDTO.getBaseURL();
                final SeriesContext ctx = SeriesContext
                        .createWithLeaderboardGroupId(eventSeries.getSeriesLeaderboardGroupId());
                final PlaceNavigation<?> seriesNavigation = navigator.getEventSeriesNavigation(ctx, baseUrl,
                        eventDTO.isOnRemoteServer());
                recentEvent.setSeriesInformation(seriesNavigation, eventDTO.getEventSeries());
            }
            recentEventsTeaserPanel.add(recentEvent);
            if (first) {
                first = false;
            }
        }
        final YearStatisticsBox statisticsBox = new YearStatisticsBox(new MobileStatisticsBoxView(
                StringMessages.INSTANCE.statisticsFor(Integer.toString(yearDTO.getYear()))), yearDTO);
        statisticsBox.getElement().getStyle().setPaddingLeft(1, Unit.EM);
        statisticsBox.getElement().getStyle().setPaddingRight(1, Unit.EM);
        recentEventsTeaserPanel.add(statisticsBox);
        
        eventStage.setFeaturedEvents(events);
//        eventStage.removeFromParent();
        headerDiv.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onHeaderClicked();
            }
        }, ClickEvent.getType());

        isContentVisible = showInitial;
        animation = new CollapseAnimation(contentDiv, showInitial);
        updateAccordionState();
    }

    void onHeaderClicked() {
        isContentVisible = !isContentVisible;
        updateContentVisibility();
    }
    
    private void updateContentVisibility() {
        animation.animate(isContentVisible);
        updateAccordionState();
    }

    private void updateAccordionState() {
        if(isContentVisible) {
            getElement().removeClassName(EventsOverviewRecentResources.INSTANCE.css().accordioncollapsed());
        } else {
            getElement().addClassName(EventsOverviewRecentResources.INSTANCE.css().accordioncollapsed());
        }
    }
}
