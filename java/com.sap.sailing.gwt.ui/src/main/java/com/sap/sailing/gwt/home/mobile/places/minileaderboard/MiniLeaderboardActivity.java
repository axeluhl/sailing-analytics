package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardView.Presenter;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;

public class MiniLeaderboardActivity extends AbstractActivity implements Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final RegattaLeaderboardPlace place;

    public MiniLeaderboardActivity(RegattaLeaderboardPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final MiniLeaderboardView view = new MiniLeaderboardViewImpl(this);
        panel.setWidget(view.asWidget());

    }

    @Override
    public EventContext getCtx() {
        return place.getCtx();
    }

    @Override
    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
}
