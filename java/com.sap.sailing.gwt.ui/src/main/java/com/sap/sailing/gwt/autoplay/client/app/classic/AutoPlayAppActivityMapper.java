package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard.LeaderboardPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard.LeaderboardViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard.LiveRaceWithRaceboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard.LiveRaceWithRaceboardPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard.LiveRaceWithRaceboardViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerActivityProxy;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.ClassicConfigPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.ClassicConfigPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.ClassicConfigViewImpl;

public class AutoPlayAppActivityMapper implements ActivityMapper {
    private final AutoPlayClientFactory clientFactory;

    public AutoPlayAppActivityMapper(AutoPlayClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {

        if (place instanceof ClassicConfigPlace) {
            return new ClassicConfigPresenterImpl((ClassicConfigPlace) place, clientFactory,
                    new ClassicConfigViewImpl(clientFactory));
        } else if (place instanceof PlayerPlace) {
            return new PlayerActivityProxy((PlayerPlace) place, clientFactory);
        } else if (place instanceof LiveRaceWithRaceboardPlace) {
            return new LiveRaceWithRaceboardPresenterImpl((LiveRaceWithRaceboardPlace) place, clientFactory,
                    new LiveRaceWithRaceboardViewImpl());
        } else if (place instanceof LeaderboardPlace) {
            return new LeaderboardPresenterImpl((LeaderboardPlace) place, clientFactory, new LeaderboardViewImpl());
        } else {
            return null;
        }
    }
}
