package com.sap.sailing.gwt.home.desktop.places.user.profile.settingstab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class UserProfileSettingsViewImpl extends Composite implements UserProfileSettingsView {

    interface MyBinder extends UiBinder<Widget, UserProfileSettingsViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true) AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true) UserSettings userSettingsUi;
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        userSettingsUi = new UserSettings(presenter.getUserSettingsPresenter());
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        CommonControlsCSS.ensureInjected();
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
    
}