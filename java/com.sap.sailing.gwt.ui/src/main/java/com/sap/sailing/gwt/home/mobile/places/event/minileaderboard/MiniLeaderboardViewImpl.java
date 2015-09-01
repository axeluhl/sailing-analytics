package com.sap.sailing.gwt.home.mobile.places.event.minileaderboard;

import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;

public class MiniLeaderboardViewImpl extends AbstractEventView<MiniLeaderboardView.Presenter> implements MiniLeaderboardView {
   
    private static final StringMessages MSG = StringMessages.INSTANCE;
    
    private final MinileaderboardBox minileaderboard;

    public MiniLeaderboardViewImpl(MiniLeaderboardView.Presenter presenter) {
        super(presenter, presenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA, true);
        setViewContent(minileaderboard = new MinileaderboardBox(false));
        minileaderboard.setShowIfEmpty(true);
        refreshManager.add(minileaderboard, new GetMiniLeaderbordAction(getEventId(), getRegattaId()));
        minileaderboard.setAction(MSG.details(), presenter.getRegattaLeaderboardNavigation(getRegattaId()));
    }

}
