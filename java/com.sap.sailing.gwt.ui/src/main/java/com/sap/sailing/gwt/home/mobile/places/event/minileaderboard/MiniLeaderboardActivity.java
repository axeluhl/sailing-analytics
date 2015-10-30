package com.sap.sailing.gwt.home.mobile.places.event.minileaderboard;

import java.util.List;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardView.Presenter;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MiniLeaderboardActivity extends AbstractEventActivity<MiniLeaderboardPlace> implements Presenter {

    public MiniLeaderboardActivity(MiniLeaderboardPlace place, EventViewDTO eventDTO, NavigationPathDisplay navigationPathDisplay, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        StringMessages i18n = StringMessages.INSTANCE;
        List<NavigationItem> navigationItems = getNavigationPathToRegattaLevel();
        navigationItems.add(new NavigationItem(i18n.results(), getRegattaMiniLeaderboardNavigation(getRegattaId())));
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
    }

    @Override
    protected EventViewBase initView() {
        final MiniLeaderboardView view = new MiniLeaderboardViewImpl(this);
        initQuickfinder(view, true);
        return view;
    }
}
