package com.sap.sailing.gwt.home.mobile.places.event.minileaderboard;

import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardView.Presenter;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

public class MiniLeaderboardActivity extends AbstractEventActivity<MiniLeaderboardPlace> implements Presenter {

    public MiniLeaderboardActivity(MiniLeaderboardPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
    }

    @Override
    protected EventViewBase initView() {
        final MiniLeaderboardView view = new MiniLeaderboardViewImpl(this);
        initQuickfinder(view, true);
        return view;
    }
}
