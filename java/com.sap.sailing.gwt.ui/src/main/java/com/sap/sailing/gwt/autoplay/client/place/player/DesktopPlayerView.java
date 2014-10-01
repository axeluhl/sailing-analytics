package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;

public class DesktopPlayerView extends Composite implements PlayerView {
    private static PlayerViewUiBinder uiBinder = GWT.create(PlayerViewUiBinder.class);

    interface PlayerViewUiBinder extends UiBinder<Widget, DesktopPlayerView> {
    }

    @UiField DockLayoutPanel dockPanel;
    
    public DesktopPlayerView(PlaceNavigator navigator) {
        initWidget(uiBinder.createAndBindUi(this));
        RootLayoutPanel.get().add(this);
    }

    @Override
    public void clearDockPanel() {
        int childWidgetCount = dockPanel.getWidgetCount();
        for(int i = childWidgetCount-1; i >=0; i--) {
            Widget widget = dockPanel.getWidget(i);
            dockPanel.remove(widget);
        }
    }
    
    @Override
    public DockLayoutPanel getDockPanel() {
        return dockPanel;
    }

}
