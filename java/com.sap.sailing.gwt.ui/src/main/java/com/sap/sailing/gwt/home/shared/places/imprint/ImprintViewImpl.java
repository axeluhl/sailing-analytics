package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.sponsoring.Sponsoring;

public class ImprintViewImpl extends Composite implements ImprintView {
    private static SponsoringPageViewUiBinder uiBinder = GWT.create(SponsoringPageViewUiBinder.class);

    interface SponsoringPageViewUiBinder extends UiBinder<Widget, ImprintViewImpl> {
    }

    @UiField Sponsoring sponsoring;

    public ImprintViewImpl() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
