package com.sap.sailing.gwt.home.desktop.places.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.start.StartViewDTO;

public interface StartView {
    Widget asWidget();
    
    void setData(StartViewDTO data);
}
