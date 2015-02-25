package com.sap.sailing.gwt.home.client.place.event.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class CompactEventHeader extends Composite {
    private static CompactHeaderUiBinder uiBinder = GWT.create(CompactHeaderUiBinder.class);

    interface CompactHeaderUiBinder extends UiBinder<Widget, CompactEventHeader> {
    }

    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);

    @UiField Anchor regattaLink;
    @UiField Anchor regattaLink2;
    @UiField ImageElement eventLogo;
    @UiField SpanElement eventNameSpan;
    
    private final EventDTO event;
    private final HomePlacesNavigator placeNavigator;
    private final PlaceNavigation<EventPlace> regattaNavigation;

    public CompactEventHeader(EventDTO event, String leaderboardName, HomePlacesNavigator placeNavigator) {
        this.event = event;
        this.placeNavigator = placeNavigator;
        
        EventHeaderResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        regattaNavigation = placeNavigator.getRegattaNavigation(event.id.toString(), leaderboardName, event.getBaseURL(), event.isOnRemoteServer());
        regattaLink.setHref(regattaNavigation.getTargetUrl());
        regattaLink2.setHref(regattaNavigation.getTargetUrl());
        
        updateUI();
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
        boolean isSeries = event.isFakeSeries(); 

        String eventName = event.getName();
        if(isSeries) {
            LeaderboardGroupDTO leaderboardGroupDTO = event.getLeaderboardGroups().get(0);
            eventName = leaderboardGroupDTO.getDisplayName() != null ? leaderboardGroupDTO.getDisplayName() : leaderboardGroupDTO.getName();
            
            StrippedLeaderboardDTO leaderboardFittingToEvent = findLeaderboardWithSameCourseArea(event);
            if(leaderboardFittingToEvent != null) {
            } else {
            }
        } else {
        }
        eventNameSpan.setInnerText(eventName);
        String logoUrl = event.getLogoImageURL() != null ? event.getLogoImageURL() : EventHeaderResources.INSTANCE.defaultEventLogoImage().getSafeUri().asString();
        eventLogo.setSrc(logoUrl);
    }
    
    @UiHandler("regattaLink")
    void gotoRegattaClicked(ClickEvent event) {
        handleClickEvent(event, regattaNavigation);
    }

    @UiHandler("regattaLink2")
    void gotoRegatta2Clicked(ClickEvent event) {
        handleClickEvent(event, regattaNavigation);
    }

    private void handleClickEvent(ClickEvent e, PlaceNavigation<?> placeNavigation) {
        if (HYPERLINK_IMPL.handleAsClick((Event) e.getNativeEvent())) {
            placeNavigator.goToPlace(placeNavigation);
            e.preventDefault();
         }
    }

}
