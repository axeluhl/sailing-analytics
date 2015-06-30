package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import java.util.Collection;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.mobile.places.RegattaLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public interface MiniLeaderboardView {

    public interface Presenter extends RegattaLeaderboardNavigationProvider {

        DispatchSystem getDispatch();

        EventContext getCtx();
    }

    Widget asWidget();

    void setQuickFinderValues(Collection<RegattaMetadataDTO> regattas);

}
