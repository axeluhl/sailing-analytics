package com.sap.sailing.gwt.home.mobile.places.event.minileaderboard;

import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MiniLeaderboardViewImpl extends AbstractEventView<MiniLeaderboardView.Presenter> implements MiniLeaderboardView {
   
    private static final StringMessages MSG = StringMessages.INSTANCE;
    
    private final MinileaderboardBox minileaderboard;

    public MiniLeaderboardViewImpl(MiniLeaderboardView.Presenter presenter, FlagImageResolver flagImageResolver) {
        super(presenter, presenter.isMultiRegattaEvent(), true, presenter.getRegatta() != null);
        setViewContent(minileaderboard = new MinileaderboardBox(false, flagImageResolver));
        if(presenter.getRegatta() != null) {
            refreshManager.add(minileaderboard, new GetMiniLeaderbordAction(getEventId(), getRegattaId()));
        } else {
            // This forces the "There are no results available yet" message to show
            minileaderboard.setData(new GetMiniLeaderboardDTO());
        }
        minileaderboard.setAction(MSG.details(), presenter.getRegattaLeaderboardNavigation(getRegattaId()));
    }

}
