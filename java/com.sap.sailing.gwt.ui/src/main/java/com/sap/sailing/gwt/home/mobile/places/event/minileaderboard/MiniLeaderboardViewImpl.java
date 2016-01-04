package com.sap.sailing.gwt.home.mobile.places.event.minileaderboard;

import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MiniLeaderboardViewImpl extends AbstractEventView<MiniLeaderboardView.Presenter> implements MiniLeaderboardView {
   
    private static final StringMessages MSG = StringMessages.INSTANCE;
    
    private final MinileaderboardBox minileaderboard;

    public MiniLeaderboardViewImpl(MiniLeaderboardView.Presenter presenter) {
        super(presenter, presenter.isMultiRegattaEvent(), true);
        setViewContent(minileaderboard = new MinileaderboardBox(false));
        refreshManager.add(minileaderboard, new GetMiniLeaderbordAction(getEventId(), getRegattaId()));
        minileaderboard.setAction(MSG.details(), presenter.getRegattaLeaderboardNavigation(getRegattaId()));
    }

}
