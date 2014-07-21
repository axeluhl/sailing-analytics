package com.sap.sailing.gwt.home.client.place.event.regattalist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public class EventRegattaListNavigationButton extends UIObject {
    private static EventRegattaListNavigationButtonUiBinder uiBinder = GWT.create(EventRegattaListNavigationButtonUiBinder.class);

    interface EventRegattaListNavigationButtonUiBinder extends UiBinder<DivElement, EventRegattaListNavigationButton> {
    }
    
    @UiField AnchorElement regattaGroupLink;
    
    public EventRegattaListNavigationButton(LeaderboardGroup leaderboardGroup) {
        EventRegattaListResources.INSTANCE.css().ensureInjected();
        
        setElement(uiBinder.createAndBindUi(this));
        
        regattaGroupLink.setInnerText(leaderboardGroup.getName());
    }
}
