package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorMobile;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.userprofile.mobile.userheader.UserHeader;

public class AbstractUserProfileView extends Composite implements UserProfileViewBase {

    private static AbstractUserProfileViewUiBinder uiBinder = GWT.create(AbstractUserProfileViewUiBinder.class);

    interface AbstractUserProfileViewUiBinder extends UiBinder<Widget, AbstractUserProfileViewLayout> {
    }
    
    static class AbstractUserProfileViewLayout {
        @UiField(provided = true) final AuthorizedContentDecoratorMobile decoratorUi;
        @UiField(provided = true) final UserHeader userHeaderUi;
        @UiField Quickfinder quickfinderUi;
        @UiField SimplePanel contentContainerUi;
        
        private AbstractUserProfileViewLayout(UserProfileViewBase.Presenter presenter) {
            this.decoratorUi = new AuthorizedContentDecoratorMobile(presenter);
            this.userHeaderUi = new UserHeader(SharedResources.INSTANCE);
        }
    }
    
    protected final StringMessages i18n = StringMessages.INSTANCE;
    private final AbstractUserProfileViewLayout layout;

    protected AbstractUserProfileView(UserProfileViewBase.Presenter presenter) {
        this.layout = new AbstractUserProfileViewLayout(presenter);
        initWidget(uiBinder.createAndBindUi(this.layout));
        this.layout.quickfinderUi.addPlaceholderItem(i18n.profileQuickfinder());
        this.layout.quickfinderUi.addItem(i18n.details(), presenter.getUserProfileNavigation());
        this.layout.quickfinderUi.addItem(i18n.favoritesAndNotifications(), presenter.getUserPreferencesNavigation());
        this.layout.quickfinderUi.addItem(i18n.sailorProfiles(), presenter.getSailorProfilesNavigation());
        if(ExperimentalFeatures.SHOW_MY_SETTINGS_IN_USER_PROFILE) {
            this.layout.quickfinderUi.addItem(i18n.settings(), presenter.getUserSettingsNavigation());
        }
    }
    
    protected final void setViewContent(IsWidget content) {
        this.layout.contentContainerUi.setWidget(content);
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.layout.decoratorUi.setAuthenticationContext(authenticationContext);
        this.layout.userHeaderUi.setAuthenticationContext(authenticationContext);
    }

}
