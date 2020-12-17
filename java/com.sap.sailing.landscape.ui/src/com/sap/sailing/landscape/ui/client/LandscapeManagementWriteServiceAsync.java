package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LandscapeManagementWriteServiceAsync {
    void getRegions(AsyncCallback<ArrayList<String>> asyncCallback);
}
