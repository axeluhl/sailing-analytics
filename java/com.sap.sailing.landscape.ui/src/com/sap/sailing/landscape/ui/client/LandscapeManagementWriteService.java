package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;

public interface LandscapeManagementWriteService extends RemoteService {
    ArrayList<String> getRegions();
}
