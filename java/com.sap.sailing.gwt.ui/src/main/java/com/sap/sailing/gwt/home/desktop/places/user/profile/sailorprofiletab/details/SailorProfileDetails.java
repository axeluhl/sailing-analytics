package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfile;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class SailorProfileDetails extends Composite implements SailorProfileDetailsView {

    interface MyBinder extends UiBinder<Widget, SailorProfileDetails> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true)
    EditSailorProfile editSailorProfileUi;

    private SailorProfileEntry entry;

    public void setEntry(SailorProfileEntry entry) {
        this.entry = entry;
        propagateEntryIfNecessary();
    }

    private void propagateEntryIfNecessary() {
        if (this.entry != null && editSailorProfileUi != null) {
            editSailorProfileUi.setEntry(entry);
        }
    }

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        editSailorProfileUi = new EditSailorProfile(presenter.getSharedSailorProfilePresenter(),
                presenter.getFlagImageResolver());
        initWidget(uiBinder.createAndBindUi(this));
        propagateEntryIfNecessary();
    }

    @Override
    public NeedsAuthenticationContext getAuthenticationContext() {
        return decoratorUi;
    }
}
