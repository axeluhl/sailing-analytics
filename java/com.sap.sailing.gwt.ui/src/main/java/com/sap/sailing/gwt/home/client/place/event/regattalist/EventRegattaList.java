package com.sap.sailing.gwt.home.client.place.event.regattalist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventComposite;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.regatta.Regatta;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaList extends AbstractEventComposite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaList> {
    }

//    @UiField DivElement seriesLeaderboardDiv;
//    @UiField AnchorElement seriesLeaderboardAnchor;
    
    @UiField DivElement regattaGroupsNavigationPanel;
    @UiField DivElement regattaListNavigationDiv;
    @UiField HTMLPanel regattaListItemsPanel;
    @UiField AnchorElement filterNoLeaderboardGroupsAnchor;
    @UiField SpanElement allBoatClassesSelected;
    
    private final Map<String, List<Regatta>> regattaElementsByLeaderboardGroup;
    private final List<AnchorElement> leaderboardGroupFilterAnchors;
    
    public EventRegattaList(EventDTO event, Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> regattaStructure, 
            Timer timerForClientServerOffset, HomePlacesNavigator placeNavigator, EventPlaceNavigator pageNavigator) {
        super(event, pageNavigator);
        
        EventRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        regattaElementsByLeaderboardGroup = new HashMap<>();
        leaderboardGroupFilterAnchors = new ArrayList<AnchorElement>();
        boolean isSeries = event.isFakeSeries(); 
        boolean hasMultipleLeaderboardGroups = event.getLeaderboardGroups().size() > 1;
        
        if(isSeries) {
            regattaListNavigationDiv.getStyle().setDisplay(Display.NONE);
        } else {
            if(hasMultipleLeaderboardGroups) {
                // fill the navigation panel
                registerFilterLeaderboardGroupEvent(filterNoLeaderboardGroupsAnchor, null);
                for(LeaderboardGroupDTO leaderboardGroup: event.getLeaderboardGroups()) {
                    AnchorElement filterLeaderboardGroupAnchor = Document.get().createAnchorElement();
                    filterLeaderboardGroupAnchor.setClassName(SharedResources.INSTANCE.mainCss().navbar_button());
                    filterLeaderboardGroupAnchor.setHref("javascript:;");
                    
                    String leaderboardGroupName = LongNamesUtil.shortenLeaderboardGroupName(event.getName(), leaderboardGroup.getName());
                    filterLeaderboardGroupAnchor.setInnerText(leaderboardGroupName);
                    regattaGroupsNavigationPanel.appendChild(filterLeaderboardGroupAnchor);
                    leaderboardGroupFilterAnchors.add(filterLeaderboardGroupAnchor);
                    
                    registerFilterLeaderboardGroupEvent(filterLeaderboardGroupAnchor, leaderboardGroup);
                }
            } else {
                regattaListNavigationDiv.getStyle().setDisplay(Display.NONE);
            }
        }

        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO> r = regattaStructure.get(leaderboard.name);
                if (r != null) {
                    Regatta regatta = new Regatta(event, timerForClientServerOffset, false, placeNavigator, pageNavigator);
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
                         
                         for(AnchorElement anchor: leaderboardGroupFilterAnchors) {
                             anchor.removeClassName(SharedResources.INSTANCE.mainCss().navbar_buttonactive());
                         }
                         filterLeaderboardGroupAnchor.addClassName(SharedResources.INSTANCE.mainCss().navbar_buttonactive());
                         break;
                }
            }
        });
    }

    private void filterRegattaListByLeaderboardGroup(LeaderboardGroupDTO leaderboardGroup) {
        if (leaderboardGroup != null) {
            allBoatClassesSelected.getStyle().setDisplay(Display.NONE);
            filterNoLeaderboardGroupsAnchor.removeClassName(SharedResources.INSTANCE.mainCss().navbar_buttonactive());
            // hide all regattas of the not selected leaderboardgroup
            for (LeaderboardGroupDTO lg : getEvent().getLeaderboardGroups()) {
                boolean isVisible = leaderboardGroup.getName().equals(lg.getName());
                List<Regatta> regattaElements = regattaElementsByLeaderboardGroup.get(lg.getName());
                for (Regatta regatta : regattaElements) {
                    regatta.setVisible(isVisible);
                }
            }
        } else {
            // make all regattas visible
            allBoatClassesSelected.getStyle().setDisplay(Display.INLINE_BLOCK);
            filterNoLeaderboardGroupsAnchor.addClassName(SharedResources.INSTANCE.mainCss().navbar_buttonactive());
            for (LeaderboardGroupDTO lg : getEvent().getLeaderboardGroups()) {
                List<Regatta> regattaElements = regattaElementsByLeaderboardGroup.get(lg.getName());
                for (Regatta regatta : regattaElements) {
                    regatta.setVisible(true);
                }
            }
        }
    }
}
