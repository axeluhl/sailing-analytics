package com.sap.sailing.gwt.home.communication;

import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.gwt.dispatch.client.impl.DispatchSystemImpl;

public class SailingDispatchSystemImpl extends DispatchSystemImpl<SailingDispatchContext> implements
        SailingDispatchSystem {
    
    public SailingDispatchSystemImpl() {
        super(RemoteServiceMappingConstants.dispatchServiceRemotePath);
    }
}
