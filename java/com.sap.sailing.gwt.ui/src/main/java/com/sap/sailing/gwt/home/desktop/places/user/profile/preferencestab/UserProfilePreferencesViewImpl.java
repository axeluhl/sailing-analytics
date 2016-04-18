package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class UserProfilePreferencesViewImpl extends Composite implements UserProfilePreferencesView {

    interface MyBinder extends UiBinder<Widget, UserProfilePreferencesViewImpl> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    
    public UserDetailsView getUserDetailsView() {
        return null;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        initWidget(ourUiBinder.createAndBindUi(this));
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
}