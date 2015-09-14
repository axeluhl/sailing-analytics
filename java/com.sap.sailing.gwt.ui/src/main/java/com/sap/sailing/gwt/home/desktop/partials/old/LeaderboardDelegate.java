package com.sap.sailing.gwt.home.desktop.partials.old;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public interface LeaderboardDelegate<T extends Widget> {

    void setLeaderboardPanel(T leaderboardPanel);

    Widget getAutoRefreshControl();

    Widget getSettingsControl();

    HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler);

    Element getLastScoringUpdateTimeElement();

    Element getLastScoringUpdateTextElement();

    Element getLastScoringCommentElement();

    Element getScoringSchemeElement();

}
