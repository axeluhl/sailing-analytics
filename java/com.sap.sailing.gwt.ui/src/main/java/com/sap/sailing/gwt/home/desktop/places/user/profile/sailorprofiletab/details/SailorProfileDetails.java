package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.wrapper.SailorProfileOverviewWrapper;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfile;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class SailorProfileDetails extends Composite implements SailorProfileDetailsView {

    interface MyBinder extends UiBinder<Widget, SailorProfileDetails> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true)
    EditSailorProfile editSailorProfileUi;
    
    private final FlagImageResolver flagImageResolver;

    public SailorProfileDetails(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }

    @Override
    public void setPresenter(SailorProfileOverviewWrapper.Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        editSailorProfileUi = new EditSailorProfile(presenter.getSharedSailorProfilePresenter(), flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
}
