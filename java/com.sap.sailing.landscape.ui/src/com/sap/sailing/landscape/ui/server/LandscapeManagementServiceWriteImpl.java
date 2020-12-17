package com.sap.sailing.landscape.ui.server;

import java.util.ArrayList;

import com.sap.sailing.landscape.ui.client.LandscapeManagementWriteService;
import com.sap.sse.gwt.server.ResultCachingProxiedRemoteServiceServlet;

public class LandscapeManagementServiceWriteImpl extends ResultCachingProxiedRemoteServiceServlet
        implements LandscapeManagementWriteService {
    private static final long serialVersionUID = -3332717645383784425L;

    @Override
    public ArrayList<String> getRegions() {
        // TODO Implement LandscapeManagementWriteService.getRegions(...)
        return null;
    }
}
