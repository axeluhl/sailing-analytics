package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;

public class MainMediaPhotoThumb extends Composite {

    
    interface MainMediaPhotoThumbUiBinder extends UiBinder<Widget, MainMediaPhotoThumb> {
    }
    
    private static MainMediaPhotoThumbUiBinder uiBinder = GWT.create(MainMediaPhotoThumbUiBinder.class);

    public MainMediaPhotoThumb(PlaceNavigator navigator) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
    }

}
