package com.sap.sailing.gwt.managementconsole.partials.authentication.decorator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInView;

/**
 * The management console application's {@link NotLoggedInView} implementation used within the
 * {@link AuthorizedDecorator decorator} to be shown if the user is not authorized/allowed to see the actual content.
 */
class NotLoggedInViewImpl extends Composite implements NotLoggedInView {

    private static NotLoggedInViewImplUiBinder uiBinder = GWT.create(NotLoggedInViewImplUiBinder.class);

    interface NotLoggedInViewImplUiBinder extends UiBinder<Widget, NotLoggedInViewImpl> {
    }

    @UiField
    Element messageUi;

    @UiField
    Button controlUi;

    private NotLoggedInPresenter presenter;

    NotLoggedInViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setPresenter(final NotLoggedInPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setMessage(final String message) {
        this.messageUi.setInnerText(message);
    }

    @Override
    public void setSignInText(final String signInText) {
        this.controlUi.setText(signInText);
    }

    @UiHandler("controlUi")
    void onControlClicked(final ClickEvent event) {
        presenter.doTriggerLoginForm();
    }

}
