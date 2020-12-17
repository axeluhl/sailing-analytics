package com.sap.sailing.landscape.ui.server;

import java.util.ArrayList;

import com.sap.sailing.landscape.ui.client.LandscapeManagementWriteService;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.server.ResultCachingProxiedRemoteServiceServlet;
import com.sap.sse.landscape.aws.AwsLandscape;

public class LandscapeManagementWriteServiceImpl extends ResultCachingProxiedRemoteServiceServlet
        implements LandscapeManagementWriteService {
    private static final long serialVersionUID = -3332717645383784425L;
    private final AwsLandscape<?, ?, ?> landscape;

    public LandscapeManagementWriteServiceImpl() {
        landscape = AwsLandscape.obtain();
    }
    
    @Override
    public ArrayList<String> getRegions() {
        final ArrayList<String> result = new ArrayList<>();
        Util.addAll(Util.map(landscape.getRegions(), r->r.getId()), result);
        return result;
    }
}
