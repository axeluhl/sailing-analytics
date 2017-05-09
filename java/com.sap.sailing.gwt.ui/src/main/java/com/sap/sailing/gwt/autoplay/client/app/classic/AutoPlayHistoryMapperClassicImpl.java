package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard.LiveRaceWithRaceboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.ClassicConfigPlace;

@WithTokenizers({ ClassicConfigPlace.Tokenizer.class, PlayerPlace.Tokenizer.class,
        LiveRaceWithRaceboardPlace.Tokenizer.class, LeaderboardPlace.Tokenizer.class })
public interface AutoPlayHistoryMapperClassicImpl extends PlaceHistoryMapper {
}
