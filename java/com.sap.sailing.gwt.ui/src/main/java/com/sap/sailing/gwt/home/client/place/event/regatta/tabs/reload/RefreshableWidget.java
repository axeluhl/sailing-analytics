package com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public interface RefreshableWidget<D extends DTO> extends IsWidget {
    void setData(D data, long nextUpdate, int updateNo);
}
