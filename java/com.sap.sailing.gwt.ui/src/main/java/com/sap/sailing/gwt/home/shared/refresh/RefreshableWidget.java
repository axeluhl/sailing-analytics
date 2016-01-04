package com.sap.sailing.gwt.home.shared.refresh;

import com.sap.sailing.gwt.dispatch.client.DTO;

public interface RefreshableWidget<D extends DTO> {
    void setData(D data);
}
