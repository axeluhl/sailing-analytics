package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;

public class DesktopPlayerView extends Composite implements PlayerView {
    private static PlayerViewUiBinder uiBinder = GWT.create(PlayerViewUiBinder.class);

    interface PlayerViewUiBinder extends UiBinder<Widget, DesktopPlayerView> {
    }

    @UiField DockLayoutPanel dockPanel;
    
    public DesktopPlayerView(PlaceNavigator navigator) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void clear() {
        dockPanel.clear();
    }
    
    @Override
    public DockLayoutPanel getDockPanel() {
        return dockPanel;
    }
}
