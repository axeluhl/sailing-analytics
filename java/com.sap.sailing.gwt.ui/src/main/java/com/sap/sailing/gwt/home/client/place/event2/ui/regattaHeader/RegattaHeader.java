package com.sap.sailing.gwt.home.client.place.event2.ui.regattaHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaReferenceDTO;

public class RegattaHeader extends Composite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, RegattaHeader> {
    }
    
    @UiField StringMessages i18n;
    
    
    public RegattaHeader(RegattaReferenceDTO regattaRefDTO) {
        
        RegattaHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
