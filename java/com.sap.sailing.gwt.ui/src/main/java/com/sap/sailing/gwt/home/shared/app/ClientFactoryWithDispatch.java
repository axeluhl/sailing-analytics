package com.sap.sailing.gwt.home.shared.app;

import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

public interface ClientFactoryWithDispatch {
    SailingDispatchSystem getDispatch();
}
