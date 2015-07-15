package com.sap.sailing.gwt.home.client.place.event.partials.eventregatta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.EventView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.partials.Regatta;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sse.common.Util.Triple;

public class EventRegattaList extends Composite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaList> {
    }

//    @UiField DivElement seriesLeaderboardDiv;
//    @UiField AnchorElement seriesLeaderboardAnchor;
    
    @UiField DivElement regattaGroupsNavigationPanel;
    @UiField DivElement regattaListNavigationDiv;
    @UiField HTMLPanel regattaListItemsPanel;
    @UiField AnchorElement filterNoLeaderboardGroupsAnchor;
    
    private final Map<String, List<Regatta>> regattaElementsByLeaderboardGroup;
    private final List<AnchorElement> leaderboardGroupFilterAnchors;
    private final Presenter presenter;
    
    public EventRegattaList(Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> regattaStructure, 
            EventView.Presenter presenter) {
        this.presenter = presenter;
        
        EventRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        EventViewDTO event = presenter.getCtx().getEventDTO();

        regattaElementsByLeaderboardGroup = new HashMap<>();
        leaderboardGroupFilterAnchors = new ArrayList<AnchorElement>();
        boolean hasMultipleLeaderboardGroups = presenter.getCtx().getLeaderboardGroups().size() > 1;
        
        if(event.getType() == EventType.SERIES_EVENT) {
            regattaListNavigationDiv.getStyle().setDisplay(Display.NONE);
        } else {
            if(hasMultipleLeaderboardGroups) {
                // fill the navigation panel
                registerFilterLeaderboardGroupEvent(filterNoLeaderboardGroupsAnchor, null);
                for(LeaderboardGroupDTO leaderboardGroup: presenter.getCtx().getLeaderboardGroups()) {
                    AnchorElement filterLeaderboardGroupAnchor = Document.get().createAnchorElement();
                    filterLeaderboardGroupAnchor.addClassName(EventRegattaListResources.INSTANCE.css().item());
                    filterLeaderboardGroupAnchor.setHref("javascript:;");
                    
                    String leaderboardGroupName = LongNamesUtil.shortenLeaderboardGroupName(event.getDisplayName(), leaderboardGroup.getName());
                    filterLeaderboardGroupAnchor.setInnerText(leaderboardGroupName);
                    regattaGroupsNavigationPanel.appendChild(filterLeaderboardGroupAnchor);
                    leaderboardGroupFilterAnchors.add(filterLeaderboardGroupAnchor);
                    
                    registerFilterLeaderboardGroupEvent(filterLeaderboardGroupAnchor, leaderboardGroup);
                }
            } else {
                regattaListNavigationDiv.getStyle().setDisplay(Display.NONE);
            }
        }

        for (LeaderboardGroupDTO leaderboardGroup : presenter.getCtx().getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO> r = regattaStructure.get(leaderboard.name);
                if (r != null) {
                    Regatta regatta = new Regatta(false, presenter);
                    regatta.setData(r.getC(), hasMultipleLeaderboardGroups, r.getB(), r.getA());
                    regattaListItemsPanel.add(regatta);
                    List<Regatta> regattaElements = regattaElementsByLeaderboardGroup.get(leaderboardGroup.getName());
                    if (regattaElements == null) {
                        regattaElements = new ArrayList<Regatta>();
                        regattaElementsByLeaderboardGroup.put(leaderboardGroup.getName(), regattaElements);
                    }
                    regattaElements.add(regatta);
                }
            }
        }
    }   
    
    private void registerFilterLeaderboardGroupEvent(final AnchorElement filterLeaderboardGroupAnchor, final LeaderboardGroupDTO leaderboardGroup) {
        Event.sinkEvents(filterLeaderboardGroupAnchor, Event.ONCLICK);
        Event.setEventListener(filterLeaderboardGroupAnchor, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                switch (DOM.eventGetType(event)) {
                case Event.ONCLICK:
                    filterRegattaListByLeaderboardGroup(leaderboardGroup);
                    for (AnchorElement anchor : leaderboardGroupFilterAnchors) {
                        anchor.removeClassName(EventRegattaListResources.INSTANCE.css().selectedItem());
                    }
                    filterLeaderboardGroupAnchor.addClassName(EventRegattaListResources.INSTANCE.css().selectedItem());
                    break;
                }
            }
        });
    }

    private void filterRegattaListByLeaderboardGroup(LeaderboardGroupDTO leaderboardGroup) {
        if (leaderboardGroup != null) {
            filterNoLeaderboardGroupsAnchor.removeClassName(EventRegattaListResources.INSTANCE.css().selectedItem());
            // hide all regattas of the not selected leaderboardgroup
            for (LeaderboardGroupDTO lg : presenter.getCtx().getLeaderboardGroups()) {
                boolean isVisible = leaderboardGroup.getName().equals(lg.getName());
                List<Regatta> regattaElements = regattaElementsByLeaderboardGroup.get(lg.getName());
                for (Regatta regatta : regattaElements) {
                    regatta.setVisible(isVisible);
                }
            }
        } else {
            // make all regattas visible
            filterNoLeaderboardGroupsAnchor.addClassName(EventRegattaListResources.INSTANCE.css().selectedItem());
            for (LeaderboardGroupDTO lg : presenter.getCtx().getLeaderboardGroups()) {
                List<Regatta> regattaElements = regattaElementsByLeaderboardGroup.get(lg.getName());
                for (Regatta regatta : regattaElements) {
                    regatta.setVisible(true);
                }
            }
        }
    }
}
