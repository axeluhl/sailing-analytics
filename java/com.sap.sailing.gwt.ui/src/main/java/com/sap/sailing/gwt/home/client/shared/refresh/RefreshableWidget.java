package com.sap.sailing.gwt.home.client.shared.refresh;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public interface RefreshableWidget<D extends DTO> {
    void setData(D data);
}
