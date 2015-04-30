package com.sap.sailing.gwt.home.client.place.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;

public interface StartView {
    Widget asWidget();
    
    void setData(StartViewDTO data);
}
