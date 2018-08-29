package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileOverview;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfileResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/**
 * Implementation of {@link UserSettingsView} where users can change their preferred selections and notifications.
 */
public class SailorProfilesOverviewImpl extends Composite implements SailorProfileOverview {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, SailorProfilesOverviewImpl> {
    }

    @UiField
    FlowPanel contentUi;

    private SailingProfileOverviewPresenter presenter;

    public SailorProfilesOverviewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();
    }

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        this.presenter = presenter;

    }

    @Override
    public NeedsAuthenticationContext authentificationContextConsumer() {
        return null;
    }

    @Override
    public void setProfileList(Collection<SailorProfileDTO> entries) {
        for (SailorProfileDTO entry : entries) {
            SailorProfileOverviewEntry entryOverview = new SailorProfileOverviewEntry(entry, presenter);
            contentUi.add(entryOverview);
        }
        createFooter();
    }

    private void createFooter() {
        Label lab = new Label("+ " + StringMessages.INSTANCE.addSailorProfileMessage());
        SailorProfileResources.INSTANCE.css().ensureInjected();
        lab.addStyleName(SailorProfileResources.INSTANCE.css().overviewTableFooterMobile());
        contentUi.add(lab);
        lab.addClickHandler((event) -> {
            presenter.getClientFactory().getPlaceController().goTo(new SailorProfilePlace(true));
        });
    }
}
