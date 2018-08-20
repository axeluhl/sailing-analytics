package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferences;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class UserProfilePreferencesViewImpl extends Composite implements UserProfilePreferencesView {

    interface MyBinder extends UiBinder<Widget, UserProfilePreferencesViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true) AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true) UserPreferences userPreferencesUi;

    private final FlagImageResolver flagImageResolver;
    
    public UserProfilePreferencesViewImpl(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        userPreferencesUi = new UserPreferences(presenter.getUserPreferencesPresenter(), flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
    
}