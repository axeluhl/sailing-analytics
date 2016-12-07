package com.sap.sse.security.ui.authentication.generic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
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
 * Widget to be shown as replacement for the actual content if the user isn't logged in an therefore doesn't see
 * anything. It consists of a message and a button that triggers the login that can be shown based on the given
 * {@link NotLoggedInPresenter}.
 */
public class GenericNotLoggedInView extends Composite implements NotLoggedInView {

    private static NotLoggedInViewUiBinder uiBinder = GWT.create(NotLoggedInViewUiBinder.class);

    interface NotLoggedInViewUiBinder extends UiBinder<Widget, GenericNotLoggedInView> {
    }
    
    @UiField
    DivElement messageUi;
    
    @UiField
    Button buttonUi;
    private NotLoggedInPresenter presenter;

    public GenericNotLoggedInView() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setPresenter(NotLoggedInPresenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("buttonUi")
    void onClick(ClickEvent e) {
        presenter.doTriggerLoginForm();
    }
    
    @Override
    public void setMessage(String message) {
        messageUi.setInnerText(message);
    }
    
    @Override
    public void setSignInText(String signInText) {
        buttonUi.setText(signInText);
    }
}
