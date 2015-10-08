package com.sap.sailing.gwt.home.desktop.places.event;

import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface EventClientFactory extends SailingClientFactory {
    
    DispatchSystem getDispatch();
}
