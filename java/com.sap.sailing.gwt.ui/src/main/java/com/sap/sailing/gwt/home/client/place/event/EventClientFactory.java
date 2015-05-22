package com.sap.sailing.gwt.home.client.place.event;

import com.sap.sailing.gwt.home.client.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface EventClientFactory extends SailingClientFactory {
    
    DispatchSystem getDispatch();
}
