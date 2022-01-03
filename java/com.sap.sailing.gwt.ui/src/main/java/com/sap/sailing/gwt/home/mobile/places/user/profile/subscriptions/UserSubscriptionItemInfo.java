package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;

class UserSubscriptionItemInfo extends UIObject {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, UserSubscriptionItemInfo> {
    }

    interface Style extends CssResource {
        String textColorBlue();

        String textColorRed();
    }

    @UiField
    Style style;

    @UiField
    Element labelUi, valueUi;

    UserSubscriptionItemInfo(final String label, final String value) {
        setElement(uiBinder.createAndBindUi(this));
        labelUi.setInnerText(label);
        valueUi.setInnerText(value);
    }

    UserSubscriptionItemInfo highlightValue(final boolean success) {
        valueUi.addClassName(success ? style.textColorBlue() : style.textColorRed());
        return this;
    }

}
