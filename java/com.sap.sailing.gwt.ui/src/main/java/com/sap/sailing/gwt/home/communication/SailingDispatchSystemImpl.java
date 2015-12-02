package com.sap.sailing.gwt.home.communication;

import com.sap.sailing.gwt.dispatch.client.impl.DispatchSystemImpl;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;

public class SailingDispatchSystemImpl extends DispatchSystemImpl<SailingDispatchContext> implements
        SailingDispatchSystem {
    
    public SailingDispatchSystemImpl() {
        super(RemoteServiceMappingConstants.dispatchServiceRemotePath);
    }
}
