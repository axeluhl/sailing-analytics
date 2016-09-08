package com.sap.sailing.gwt.home.shared.app;

import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

/**
 * Definition of ClientFactories that provide access to the dispatch based remote framework through
 * {@link SailingDispatchSystem}.
 */
public interface ClientFactoryWithDispatch {
    SailingDispatchSystem getDispatch();
}
