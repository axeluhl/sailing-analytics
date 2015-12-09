package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LoggedInUserInfoViewImpl extends Composite implements LoggedInUserInfoView {
    
    interface LoggedInUserInfoUiBinder extends UiBinder<Widget, LoggedInUserInfoViewImpl> {
    }
    
    private static LoggedInUserInfoUiBinder uiBinder = GWT.create(LoggedInUserInfoUiBinder.class);
    
    @UiField DivElement userImageUi;
    @UiField DivElement userRealnameUi;
    @UiField DivElement userUsernameUi;

    @UiField Anchor userProfileUi;
    @UiField Anchor signOutUi;
    private Presenter presenter;
    
    public LoggedInUserInfoViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        setUserInfo();
    }
    
    public void setUserInfo() {
        userImageUi.getStyle().setBackgroundImage("url('images/home/userdefault.svg')");
        userRealnameUi.setInnerText("Max Mustermann");
        userUsernameUi.setInnerText("user001");
    }
    
    @UiHandler("userProfileUi")
    void onUserProfileUiControlClicked(ClickEvent event) {
        this.presenter.gotoProfileUi();
    }
    
    @UiHandler("signOutUi")
    void onSignOutUiControlClicked(ClickEvent event) {
        this.presenter.signOut();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
}
