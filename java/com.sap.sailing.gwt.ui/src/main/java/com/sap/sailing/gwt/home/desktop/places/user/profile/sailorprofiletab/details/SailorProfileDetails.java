package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/**
 * View element which wraps around {@link ShowAndEditSailorProfile} and provides an
 * {@link AuthorizedContentDecoratorDesktop}
 */
public class SailorProfileDetails extends Composite implements SailorProfileDetailsView {

    interface MyBinder extends UiBinder<Widget, SailorProfileDetails> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true)
    ShowAndEditSailorProfile editSailorProfileUi;

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        editSailorProfileUi = new ShowAndEditSailorProfile(presenter.getSharedSailorProfilePresenter(),
                presenter.getFlagImageResolver(), this, presenter.getClientFactory().getUserService());
        initWidget(uiBinder.createAndBindUi(this));
        presenter.getSharedSailorProfilePresenter().getDataProvider().setView(editSailorProfileUi);
    }

    @Override
    public NeedsAuthenticationContext authentificationContextConsumer() {
        return decoratorUi;
    }

    public EditSailorProfileDetailsView getEditView() {
        return editSailorProfileUi;
    }
}
