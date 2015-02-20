package com.sap.sailing.gwt.home.client.place.event2;

import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.tabs.overview.EventRegattaOverviewPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public abstract class EventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    public EventActivity(PLACE place, EventClientFactory clientFactory) {
        this.currentPlace = place;
        this.ctx = new EventContext(clientFactory, place.getCtx());

        this.clientFactory = clientFactory;
    }

    @Override
    public EventContext getCtx() {

        return ctx;
    }

    @Override
    public void handleTabPlaceSelection(TabActivity<?, EventContext, ? extends EventView.Presenter> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire(ctx);
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public boolean needsSelectionInHeader() {
        EventDTO eventDTO = ctx.getEventDTO();
        if(eventDTO.isFakeSeries()) {
            return true;
        }
        if(eventDTO.getLeaderboardGroups().isEmpty()) {
            return false;
        }
        if(eventDTO.getLeaderboardGroups().size() > 1) {
            return true;
        }
        List<StrippedLeaderboardDTO> leaderboards = eventDTO.getLeaderboardGroups().get(0).getLeaderboards();
        if(leaderboards.size() <= 1) {
            return false;
        }
        return true;
    }
    
    @Override
    public void forPlaceSelection(PlaceCallback callback) {
        EventDTO event = ctx.getEventDTO();
        if(event.isFakeSeries()) {
            // TODO
//            LeaderboardGroupDTO leaderboardGroup = event.getLeaderboardGroups().get(0);
//            for(StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
//                leaderboard.get
//            }
        } else {
            for(LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
                for(StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                    EventRegattaOverviewPlace place = new EventRegattaOverviewPlace(event.id.toString(), leaderboard.regattaName);
                    callback.forPlace(place, event.getName() + " - " + leaderboard.regattaName);
                }
            }
        }
    }
    
    @Override
    public String getUrl(Place place) {
        // TODO implement
        return "TODO URL";
    }
}
