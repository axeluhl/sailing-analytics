package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class UserProfileDetailsViewImpl extends Composite implements UserProfilDetailsView {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserProfileDetailsViewImpl> {
    }


    public UserProfileDetailsViewImpl(Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
