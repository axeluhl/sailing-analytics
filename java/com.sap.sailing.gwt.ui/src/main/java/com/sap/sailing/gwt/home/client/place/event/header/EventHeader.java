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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventComposite;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace.SeriesNavigationTabs;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventHeader extends AbstractEventComposite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, EventHeader> {
    }

    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);

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
      @UiField Anchor officalWebsiteAnchor;
      @UiField Anchor twitterAnchor;
      @UiField Anchor facebookAnchor;
      @UiField DivElement seriesLeaderboardDiv;
      @UiField Anchor seriesLeaderboardAnchor;
      
    @UiField DivElement eventHeaderWrapperDiv;
    @UiField DivElement regattaNameInSeriesDiv;
    @UiField SpanElement eventNameSpan;
    @UiField SpanElement eventName2Span;
//    @UiField SpanElement eventName3;
    @UiField DivElement eventDateDiv;
    @UiField DivElement eventDescriptionDiv;
    @UiField SpanElement venueNameSpan;
    @UiField DivElement isLiveDiv;
    @UiField DivElement isFinishedDiv;
    
    @UiField ImageElement eventLogo;
    @UiField ImageElement eventLogo2;
//    @UiField ImageElement eventLogo3;

//  private final List<Anchor> links1;
//  private final List<Anchor> links2;
//  private final List<Anchor> links3;
    
    private final HomePlacesNavigator placeNavigator;
    private PlaceNavigation<SeriesPlace> seriesAnalyticsNavigation = null; 
    private PlaceNavigation<MultiregattaRegattasPlace> regattasNavigation = null;
    
    public EventHeader(EventDTO event, HomePlacesNavigator placeNavigator, EventPlaceNavigator pageNavigator) {
        super(event, pageNavigator);
        
        this.placeNavigator = placeNavigator;
        initResources();

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
        } else {
            isFinishedDiv.getStyle().setDisplay(Display.NONE);
            isLiveDiv.getStyle().setDisplay(Display.NONE);
        }
        
        setFullsizeHeader();
        updateUI();
    }

    public EventHeader(EventDTO event, HomePlacesNavigator placeNavigator) {
        super(event, null);

        this.placeNavigator = placeNavigator;
        initResources();
        
        isFinishedDiv.getStyle().setDisplay(Display.NONE);
        isLiveDiv.getStyle().setDisplay(Display.NONE);
        
        setFullsizeHeader();
        updateUI();
    }

    private void initResources() {
        EventHeaderResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setFullsizeHeader() {
        eventHeaderWrapperDiv.setAttribute("data-navigationtype", "normal");
    }
    
    public void setCompactHeader() {
        eventHeaderWrapperDiv.setAttribute("data-navigationtype", "compact");
        EventDTO event = getEvent();
        
        PlaceNavigation<MultiregattaRegattasPlace> regattaDetailsNavigation  = placeNavigator.getEventRegattasNavigation(event.id.toString(), event.getBaseURL(), event.isOnRemoteServer());
        regattasLink.setHref(regattaDetailsNavigation.getTargetUrl());
        regattasLink2.setHref(regattaDetailsNavigation.getTargetUrl());
    }

    private StrippedLeaderboardDTO findLeaderboardWithSameCourseArea(EventDTO event) {
        for(LeaderboardGroupDTO leaderboardGroup: event.getLeaderboardGroups()) {
            for(StrippedLeaderboardDTO leaderboard: leaderboardGroup.getLeaderboards()) {
                for(CourseAreaDTO courseArea: event.venue.getCourseAreas()) {
                    if(leaderboard.defaultCourseAreaId != null && leaderboard.defaultCourseAreaId.equals(courseArea.id)) {
                        return leaderboard;
                    }
                }
            }
        }
        return null;
    }
    
    private void updateUI() {
        EventDTO event = getEvent();
        boolean isSeries = event.isFakeSeries(); 

        String eventName = event.getName();
        
        regattasNavigation = placeNavigator.getEventRegattasNavigation(event.id.toString(), event.getBaseURL(), event.isOnRemoteServer());
        if(isSeries) {
            LeaderboardGroupDTO leaderboardGroupDTO = event.getLeaderboardGroups().get(0);
            eventName = leaderboardGroupDTO.getDisplayName() != null ? leaderboardGroupDTO.getDisplayName() : leaderboardGroupDTO.getName();

            String overallLeaderboardName = leaderboardGroupDTO.getName() + " " + LeaderboardNameConstants.OVERALL;
            seriesAnalyticsNavigation = placeNavigator.getSeriesAnalyticsNavigation(event.id.toString(), SeriesNavigationTabs.OverallLeaderboard, overallLeaderboardName, event.getBaseURL(), event.isOnRemoteServer());
            seriesLeaderboardAnchor.setHref(seriesAnalyticsNavigation.getTargetUrl());
            
            StrippedLeaderboardDTO leaderboardFittingToEvent = findLeaderboardWithSameCourseArea(event);
            if (leaderboardFittingToEvent != null) {
                regattaNameInSeriesDiv.setInnerText(leaderboardFittingToEvent.displayName);
            } else {
                regattaNameInSeriesDiv.getStyle().setDisplay(Display.NONE);
            }
        } else {
            seriesLeaderboardDiv.getStyle().setDisplay(Display.NONE);
            regattaNameInSeriesDiv.getStyle().setDisplay(Display.NONE);
        }
        
        eventNameSpan.setInnerHTML(eventName);
        eventName2Span.setInnerHTML(eventName);
//        eventName3.setInnerHTML(event.getName());
        venueNameSpan.setInnerHTML(event.venue.getName());
        eventDateDiv.setInnerHTML(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));
        
        if(event.getDescription() != null) {
            eventDescriptionDiv.setInnerHTML(event.getDescription());
        }
        if(event.getOfficialWebsiteURL() != null) {
            String title = event.getOfficialWebsiteURL();
            if(title.startsWith("http://")) {
                title = title.substring("http://".length(), title.length());
            }
            if(title.length() > 35) {
                title = TextMessages.INSTANCE.officalEventWebsite();
            }
            officalWebsiteAnchor.setText(title);
            officalWebsiteAnchor.setHref(event.getOfficialWebsiteURL());
        } else {
            officalWebsiteAnchor.setVisible(false);
        }
        if(event.getTwitterURL() != null) {
            twitterAnchor.setHref(event.getTwitterURL());
        } else {
            twitterAnchor.setVisible(false);
        }
        if(event.getFacebookURL() != null) {
            facebookAnchor.setHref(event.getFacebookURL());
        } else {
            facebookAnchor.setVisible(false);
        }
        
        String logoUrl = event.getLogoImageURL() != null ? event.getLogoImageURL() : EventHeaderResources.INSTANCE.defaultEventLogoImage().getSafeUri().asString();;
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
        showRegattas(event);        
    }

    @UiHandler("regattasLink2")
    void regattas2Clicked(ClickEvent event) {
        showRegattas(event);        
    }

    @UiHandler("seriesLeaderboardAnchor")
    void seriesLeaderboardClicked(ClickEvent e) {
        EventDTO event = getEvent();
        if(event.isFakeSeries()) {
            handleClickEvent(e, seriesAnalyticsNavigation);
        }
    }
    
//    @UiHandler("regattasLink3")
//    void regattas3Clicked(ClickEvent event) {
//        showRegattas();        
//    }
//
    private void showRegattas(ClickEvent e) {
        handleClickEvent(e, regattasNavigation);
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

    private void handleClickEvent(ClickEvent e, PlaceNavigation<?> placeNavigation) {
        if (HYPERLINK_IMPL.handleAsClick((Event) e.getNativeEvent())) {
            placeNavigator.goToPlace(placeNavigation);
            e.preventDefault();
         }
    }
    
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
