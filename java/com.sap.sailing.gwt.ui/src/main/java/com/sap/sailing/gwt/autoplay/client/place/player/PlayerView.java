package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public interface PlayerView {
    Widget asWidget();
    
    public void clear();
    public DockLayoutPanel getDockPanel();
}
