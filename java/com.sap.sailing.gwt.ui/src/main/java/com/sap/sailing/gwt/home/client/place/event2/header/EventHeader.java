package com.sap.sailing.gwt.home.client.place.event2.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.BreadcrumbPane;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventHeader extends Composite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, EventHeader> {
    }
    
    @UiField StringMessages i18n;
    
    @UiField BreadcrumbPane breadcrumbs;
    
    @UiField ImageElement eventLogo;
    @UiField SpanElement eventName;
    @UiField DivElement eventState;
    @UiField DivElement eventDate;
    @UiField SpanElement eventVenueName;
    @UiField SpanElement eventVenueCountry;
    @UiField AnchorElement eventLink;
    @UiField DivElement competitors;
    @UiField SpanElement competitorsCount;
    @UiField DivElement races;
    @UiField SpanElement racesCount;
    @UiField DivElement trackedRaces;
    @UiField SpanElement trackedRacesCount;
    @UiField DivElement eventCategory;

    private final HomePlacesNavigator placeNavigator;

    private EventDTO event;
    
    public EventHeader(EventDTO event) {
        this(event, null, null);
    }
    
    public EventHeader(EventDTO event, EventView.Presenter presenter, HomePlacesNavigator placeNavigator) {
        this.event = event;
        this.placeNavigator = placeNavigator;
        
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        initBreadCrumbs();
        initFields();
    }

    private void initFields() {
        String logoUrl = event.getLogoImageURL() != null ? event.getLogoImageURL() : EventHeaderResources.INSTANCE.defaultEventLogoImage().getSafeUri().asString();
        eventLogo.setSrc(logoUrl);
        eventLogo.setAlt(event.getName());
        eventName.setInnerText(event.getName());
        
        if(event.isFinished()) {
            eventState.setInnerText(i18n.finished());
            eventState.setAttribute("data-labeltype", "finished");
        } else if(event.isRunning()) {
            eventState.setInnerText(i18n.live());
            eventState.setAttribute("data-labeltype", "live");
        } else {
            hide(eventState);
        }
        
        eventDate.setInnerHTML(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));
        eventVenueName.setInnerText(event.venue.getName());
//        TODO eventVenueCountry
        if(event.getOfficialWebsiteURL() != null) {
            String title = withoutPrefix(event.getOfficialWebsiteURL(), "http://", "https://");
            if(title.length() > 35) {
                title = TextMessages.INSTANCE.officalEventWebsite();
            }
            eventLink.setInnerText(title);
            eventLink.setHref(event.getOfficialWebsiteURL());
        } else {
            hide(eventLink);
        }
        
        // TODO Multi-Regatta-Event
//        if(event.regattas.size() > 1) {
////            TODO competitorsCount.setInnerText(text);
////            TODO racesCount;
////            TODO eventCategory.setInnerText(event.get);
//            if(event.isFinished()) {
////                TODO trackedRacesCount;
//            } else {
//                hide(trackedRaces);
//            }
//        } else {
            hide(competitors, races, trackedRaces);
//        }
    }

    private String withoutPrefix(String title, String... prefixes) {
        for (String prefix : prefixes) {
            if(title.startsWith(prefix)) {
                return title.substring(prefix.length(), title.length());
            }
        }
        return title;
    }

    private void hide(Element... elementsToHide) {
        for (Element element : elementsToHide) {
            element.getStyle().setDisplay(Display.NONE);
        }
    }

    private void initBreadCrumbs() {
        breadcrumbs.addBreadcrumbItem(i18n.home(), "TODO" /* placeNavigator.getHomeNavigation().getTargetUrl() */, new Runnable() {
            @Override
            public void run() {
                // TODO
//                presenter.
//                placeNavigator.getHomeNavigation().getPlace()
            }
        });
        breadcrumbs.addBreadcrumbItem(i18n.events(), "TODO" /* placeNavigator.getEventsNavigation().getTargetUrl() */, new Runnable() {
            @Override
            public void run() {
                // TODO
//                presenter.
//                placeNavigator.getEventsNavigation().getPlace()
            }
        });
        breadcrumbs.addBreadcrumbItem(event.getName(), "TODO", new Runnable() {
            @Override
            public void run() {
                // TODO
            }
        });
    }
}
