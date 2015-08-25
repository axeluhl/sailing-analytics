package com.sap.sailing.gwt.home.mobile.places.event.regatta;

import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;

public interface RegattaView extends EventViewBase {
    
    public interface Presenter extends EventViewBase.Presenter, NewsItemLinkProvider {
        
    }

}
