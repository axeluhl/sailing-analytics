package com.sap.sailing.gwt.home.communication;

import com.sap.sailing.landscape.common.RemoteServiceMappingConstants;
import com.sap.sse.gwt.dispatch.client.system.DispatchSystemDefaultImpl;

public class SailingDispatchSystemImpl extends DispatchSystemDefaultImpl<SailingDispatchContext> implements
        SailingDispatchSystem {
    
    public SailingDispatchSystemImpl() {
        super(RemoteServiceMappingConstants.dispatchServiceRemotePath);
    }
}
