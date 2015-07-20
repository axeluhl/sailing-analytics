package com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public interface RefreshableWidget<D extends DTO> {
    void setData(D data);
}
