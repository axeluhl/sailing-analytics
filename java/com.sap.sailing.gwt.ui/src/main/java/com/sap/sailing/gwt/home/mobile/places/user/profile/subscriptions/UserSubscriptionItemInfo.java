package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;

class UserSubscriptionItemInfo extends UIObject {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, UserSubscriptionItemInfo> {
    }

    @UiField
    Element labelUi, valueUi;
    @UiField
    ImageElement imageUi;

    UserSubscriptionItemInfo(final String label, final String value) {
        setElement(uiBinder.createAndBindUi(this));
        labelUi.setInnerText(label);
        valueUi.setInnerText(value);
        imageUi.removeFromParent();
    }

    UserSubscriptionItemInfo(final String label, final DataResource value) {
        setElement(uiBinder.createAndBindUi(this));
        labelUi.setInnerText(label);
        imageUi.setSrc(value.getSafeUri().asString());
        valueUi.removeFromParent();
    }

}
