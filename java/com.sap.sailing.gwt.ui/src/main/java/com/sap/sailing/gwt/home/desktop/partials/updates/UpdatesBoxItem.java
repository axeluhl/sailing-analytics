package com.sap.sailing.gwt.home.desktop.partials.updates;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.home.communication.event.news.AbstractRaceNewsEntryDTO;
import com.sap.sailing.gwt.home.communication.event.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.util.ConditionalDateTimeFormatter;
import com.sap.sse.gwt.client.LinkUtil;

public class UpdatesBoxItem extends Widget {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, UpdatesBoxItem> {
    }
    
    @UiField AnchorElement link;
    @UiField DivElement icon;
    @UiField SpanElement titleUi;
    @UiField SpanElement boatClassUi;
    @UiField DivElement messageUi;
    @UiField DivElement timestampUi;
    
    public UpdatesBoxItem(NewsEntryDTO entry, Date currentTimestamp, EventView.Presenter presenter) {
        UpdatesBoxResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        titleUi.setInnerText(entry.getTitle());
        messageUi.setInnerText(entry.getMessage());
        
        String boatClass = entry.getBoatClass();
        if(boatClass != null && !boatClass.isEmpty()) {
            icon.getStyle().setBackgroundImage("url(\"" + BoatClassImageResolver.getBoatClassIconResource(boatClass).getSafeUri().asString() + "\")");
            boatClassUi.setInnerText(" / " + boatClass);
        } else {
            boatClassUi.removeFromParent();
        }
        if(boatClass != null && !boatClass.isEmpty()) {
            
        }
        
        Date newsTimestamp = entry.getTimestamp();
        if(newsTimestamp == null || currentTimestamp == null) {
            timestampUi.removeFromParent();
        } else {
            timestampUi.setInnerText(ConditionalDateTimeFormatter.format(newsTimestamp, currentTimestamp, StringMessages.INSTANCE));
            if (currentTimestamp.getTime() - 600000 < newsTimestamp.getTime()) {
                timestampUi.addClassName(UpdatesBoxResources.INSTANCE.css().updatesbox_item_live());
            }
        }
        
        String directLink = entry.getExternalURL();
        if(directLink != null) {
            link.setHref(directLink);
            link.setTarget("_blank");
        } else {
            PlaceNavigation<?> placeNavigation = null;
            if(entry instanceof LeaderboardNewsEntryDTO) {
                directLink = ((LeaderboardNewsEntryDTO) entry).getExternalURL();
                placeNavigation = presenter.getRegattaLeaderboardNavigation(((LeaderboardNewsEntryDTO) entry).getLeaderboardName());
            } else if(entry instanceof AbstractRaceNewsEntryDTO) {
                AbstractRaceNewsEntryDTO raceEntry = (AbstractRaceNewsEntryDTO) entry;
                if(raceEntry.getRegattaAndRaceIdentfier() != null) {
                    directLink = presenter.getRaceViewerURL(raceEntry.getLeaderboardName(), raceEntry.getLeaderboardGroupName(),
                            raceEntry.getLeaderboardGroupId(), raceEntry.getRegattaAndRaceIdentfier());
                }
            }
            if(placeNavigation != null) {
                link.setHref(placeNavigation.getTargetUrl());
                Event.sinkEvents(link, Event.ONCLICK);
                final PlaceNavigation<?> pn = placeNavigation;
                Event.setEventListener(link, new EventListener() {
                    @Override
                    public void onBrowserEvent(Event event) {
                        if(LinkUtil.handleLinkClick(event)) {
                            event.preventDefault();
                            pn.goToPlace();
                        }
                    }
                });
            } else {
                link.removeFromParent();
                while(link.hasChildNodes()) {
                    Node firstChild = link.getChild(0);
                    firstChild.removeFromParent();
                    getElement().appendChild(firstChild);
                }
                addStyleName(UpdatesBoxResources.INSTANCE.css().updatesbox_item());
            }
        }
    }
}
