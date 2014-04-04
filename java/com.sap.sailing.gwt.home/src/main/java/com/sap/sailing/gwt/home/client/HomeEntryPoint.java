package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

public class HomeEntryPoint implements EntryPoint {
    public final MyGinjector ginjector = GWT.create(MyGinjector.class);
    
    @Override
    public void onModuleLoad() {

    	DelayedBindRegistry.bind(ginjector);

        ginjector.getPlaceManager().revealCurrentPlace();
    }
}
