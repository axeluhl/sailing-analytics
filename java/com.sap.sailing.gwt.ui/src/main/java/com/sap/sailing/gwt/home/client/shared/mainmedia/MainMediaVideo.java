package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;

public class MainMediaVideo extends Composite {

    
    interface MainMediaVideoThumbUiBinder extends UiBinder<Widget, MainMediaVideo> {
    }
    
    private static MainMediaVideoThumbUiBinder uiBinder = GWT.create(MainMediaVideoThumbUiBinder.class);

    @UiField SpanElement videoTime;
    @UiField SpanElement videoTitle;
    
//    @UiField videoThumbImage
//    @UiField videoFile
//    @UiField videoLink
    
    public MainMediaVideo(PlaceNavigator navigator) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
    }

}
