package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchAsync;

public interface MiniLeaderboardView {

    public interface Presenter {

        DispatchAsync getDispatch();

        EventContext getCtx();
    }

    Widget asWidget();


}
