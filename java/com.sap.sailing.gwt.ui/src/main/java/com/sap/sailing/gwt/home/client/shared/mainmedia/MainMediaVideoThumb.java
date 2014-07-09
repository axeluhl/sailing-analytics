package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MainMediaVideoThumb extends Composite {

    
    interface MainMediaVideoThumbUiBinder extends UiBinder<Widget, MainMediaVideoThumb> {
    }
    
    private static MainMediaVideoThumbUiBinder uiBinder = GWT.create(MainMediaVideoThumbUiBinder.class);

    public MainMediaVideoThumb() {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
    }

}
