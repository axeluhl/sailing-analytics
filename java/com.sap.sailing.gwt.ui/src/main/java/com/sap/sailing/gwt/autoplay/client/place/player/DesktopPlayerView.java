package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class DesktopPlayerView extends Composite implements PlayerView {
    private static PlayerViewUiBinder uiBinder = GWT.create(PlayerViewUiBinder.class);

    interface PlayerViewUiBinder extends UiBinder<Widget, DesktopPlayerView> {
    }

    private final EventDTO event;
    private final String leaderboardName;
    
    public DesktopPlayerView(EventDTO event, String leaderboardName, PlaceNavigator navigator) {
        this.event = event;
        this.leaderboardName = leaderboardName;
        
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    private void updateUI() {
        
    }
}
