package com.sap.sailing.gwt.ui.pwa.mobile.places.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MobileEventsViewImpl extends Composite implements MobileEventsView {

    interface MobileEventsViewUiBinder extends UiBinder<Widget, MobileEventsViewImpl> {
    }

    private static MobileEventsViewUiBinder uiBinder = GWT.create(MobileEventsViewUiBinder.class);
 
    private Presenter presenter; 
    
    public MobileEventsViewImpl(final Presenter presenter) {
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }
  
}
