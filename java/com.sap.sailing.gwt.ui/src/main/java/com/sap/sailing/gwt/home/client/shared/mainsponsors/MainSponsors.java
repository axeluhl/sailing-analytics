package com.sap.sailing.gwt.home.client.shared.mainsponsors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MainSponsors extends Composite {

    interface MainSponsorsUiBinder extends UiBinder<Widget, MainSponsors> {
    }
    
    private static MainSponsorsUiBinder uiBinder = GWT.create(MainSponsorsUiBinder.class);

    public MainSponsors() {
        MainSponsorsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
