package com.sap.sailing.gwt.home.client.place.event.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventHeader extends Composite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, EventHeader> {
    }

    private final EventDTO event;

//    @UiField Anchor overviewLink;
      @UiField Anchor regattasLink;
//    @UiField Anchor scheduleLink;
//    @UiField Anchor mediaLink;
//
//    @UiField Anchor overviewLink2;
      @UiField Anchor regattasLink2;
//    @UiField Anchor scheduleLink2;
//    @UiField Anchor mediaLink2;
//
//    @UiField Anchor overviewLink3;
//    @UiField Anchor regattasLink3;
//    @UiField Anchor scheduleLink3;
//    @UiField Anchor mediaLink3;
      @UiField Anchor officalWebsiteLink;
      
    @UiField DivElement eventHeaderWrapperDiv;
    @UiField SpanElement eventName;
    @UiField SpanElement eventName2;
//    @UiField SpanElement eventName3;
    @UiField SpanElement eventDate;
    @UiField SpanElement eventDescription;
    @UiField SpanElement venueName;
    @UiField DivElement isLiveDiv;
    @UiField DivElement isFinishedDiv;
    
    @UiField ImageElement eventLogo;
    @UiField ImageElement eventLogo2;
//    @UiField ImageElement eventLogo3;
    
    private final String defaultLogoUrl = "http://static.sapsailing.com/newhome/default_event_logo.png";

//    private final List<Anchor> links1;
//    private final List<Anchor> links2;
//    private final List<Anchor> links3;
    
    private final EventPageNavigator pageNavigator;

    public EventHeader(EventDTO event, EventPageNavigator pageNavigator) {
        this.event = event;
        this.pageNavigator = pageNavigator;
        
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

//        links1 = Arrays.asList(new Anchor[] { overviewLink, regattasLink, scheduleLink, mediaLink });
//        links2 = Arrays.asList(new Anchor[] { overviewLink2, regattasLink2, scheduleLink2, mediaLink2 });
//        links3 = Arrays.asList(new Anchor[] { overviewLink3, regattasLink3, scheduleLink3, mediaLink3 });
//        setActiveLink(links1, overviewLink);
//        setActiveLink(links2, overviewLink2);
//        setActiveLink(links3, overviewLink3);

        if(event.isRunning()) {
            isFinishedDiv.getStyle().setDisplay(Display.NONE);
        } else if (event.isFinished()) {
            isLiveDiv.getStyle().setDisplay(Display.NONE);
        }
        
        setDataNavigationType("normal");
        updateUI();
    }
    
    public void setDataNavigationType(String dataNavigationType) {
        eventHeaderWrapperDiv.setAttribute("data-navigationtype", dataNavigationType);
    }
    
    private void updateUI() {
        eventName.setInnerHTML(event.getName());
        eventName2.setInnerHTML(event.getName());
//        eventName3.setInnerHTML(event.getName());
        venueName.setInnerHTML(event.venue.getName());
        eventDate.setInnerHTML(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));
        
        if(event.getDescription() != null) {
            eventDescription.setInnerHTML(event.getDescription());
        }
        if(event.getOfficialWebsiteURL() != null) {
            officalWebsiteLink.setHref(event.getOfficialWebsiteURL());
        }
        
        String logoUrl = event.getLogoImageURL() != null ? event.getLogoImageURL() : defaultLogoUrl;
        eventLogo.setSrc(logoUrl);
        eventLogo2.setSrc(logoUrl);
//        eventLogo3.setSrc(logoUrl);
    }
    
//    @UiHandler("overviewLink")
//    void overviewClicked(ClickEvent event) {
//        showOverview();
//    }
//
//    @UiHandler("overviewLink2")
//    void overview2Clicked(ClickEvent event) {
//        showOverview();
//    }
//
//    @UiHandler("overviewLink3")
//    void overview3Clicked(ClickEvent event) {
//        showOverview();
//    }

//    private void showOverview() {
//        pageNavigator.goToOverview();
//        setActiveLink(links1, overviewLink);
//        setActiveLink(links2, overviewLink2);
//        setActiveLink(links3, overviewLink3);
//    }

    @UiHandler("regattasLink")
    void regattasClicked(ClickEvent event) {
        showRegattas();        
    }

    @UiHandler("regattasLink2")
    void regattas2Clicked(ClickEvent event) {
        showRegattas();        
    }

//    @UiHandler("regattasLink3")
//    void regattas3Clicked(ClickEvent event) {
//        showRegattas();        
//    }
//
    private void showRegattas() {
        pageNavigator.goToRegattas();
//        setActiveLink(links1, regattasLink);
//        setActiveLink(links2, regattasLink2);
//        setActiveLink(links3, regattasLink3);
    }
    
//    @UiHandler("scheduleLink")
//    void scheduleClicked(ClickEvent event) {
//        showSchedule();
//    }
//
//    @UiHandler("scheduleLink2")
//    void schedule2Clicked(ClickEvent event) {
//        showSchedule();
//    }
//
//    @UiHandler("scheduleLink3")
//    void schedule3Clicked(ClickEvent event) {
//        showSchedule();
//    }
//
//    private void showSchedule() {
//        pageNavigator.goToSchedule();
//        setActiveLink(links1, scheduleLink);
//        setActiveLink(links2, scheduleLink2);
//        setActiveLink(links3, scheduleLink3);
//    }
//    
//    @UiHandler("mediaLink")
//    void mediaClicked(ClickEvent event) {
//        showMedia();
//    }
//
//    @UiHandler("mediaLink2")
//    void media2Clicked(ClickEvent event) {
//        showMedia();
//    }
//
//    @UiHandler("mediaLink3")
//    void media3Clicked(ClickEvent event) {
//        showMedia();
//    }
//
//    private void showMedia() {
//        pageNavigator.goToMedia();
//        setActiveLink(links1, mediaLink);
//        setActiveLink(links2, mediaLink2);
//        setActiveLink(links3, mediaLink3);
//    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
        initNormalNavigation();
    }
    
    private void initNormalNavigation() {
//        Window.addWindowScrollHandler(new Window.ScrollHandler() {
//            public void onWindowScroll(Window.ScrollEvent event) {
//                int scrollY = Math.max(0, Window.getScrollTop());
//                int navNormalOffset = eventNavigationNormalDiv.getOffsetTop();
//
//                if (scrollY > navNormalOffset) {
//                    eventNavigationFloatingDiv.addClassName(EventHeaderResources.INSTANCE.css().eventnavigationfixed());
//                } else {
//                    eventNavigationFloatingDiv.removeClassName(EventHeaderResources.INSTANCE.css()
//                            .eventnavigationfixed());
//                }
//            }
//        });
    }
    
//    private void setActiveLink(List<Anchor> linksToSet, Anchor link) {
//        final String activeStyle = EventHeaderResources.INSTANCE.css().eventnavigation_linkactive();
//        for (Anchor l : linksToSet) {
//            if (l == link) {
//                l.addStyleName(activeStyle);
//            } else {
//                l.removeStyleName(activeStyle);
//            }
//        }
//    }
}
