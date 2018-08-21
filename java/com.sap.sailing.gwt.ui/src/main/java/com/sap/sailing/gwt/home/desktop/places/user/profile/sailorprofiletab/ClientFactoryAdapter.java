package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.ClientFactoryWithDispatchAndError;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.HasLoginFormAndFactory;

public class ClientFactoryAdapter implements HasLoginFormAndFactory {
    private final UserProfileView.Presenter presenter;

    public ClientFactoryAdapter(UserProfileView.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void doTriggerLoginForm() {
        presenter.doTriggerLoginForm();

    }

    @Override
    public ClientFactoryWithDispatchAndError getClientFactory() {
        return presenter.getClientFactory();
    }
}
