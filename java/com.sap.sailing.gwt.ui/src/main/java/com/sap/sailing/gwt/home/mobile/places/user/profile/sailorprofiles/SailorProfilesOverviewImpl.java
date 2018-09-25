package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileOverview;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/**
 * Implementation of {@link SailorProfileOverview} where users can see an overview over all his sailor profiles.
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
        SharedSailorProfileResources.INSTANCE.css().ensureInjected();

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
        Button addButton = new Button("+ " + StringMessages.INSTANCE.addSailorProfileMessage());
        SimplePanel panel = new SimplePanel(addButton);
        panel.addStyleName(SailorProfileMobileResources.INSTANCE.css().overviewTableFooterMobile());
        addButton.addStyleName(SharedResources.INSTANCE.mainCss().buttonprimary());
        addButton.addStyleName(SharedResources.INSTANCE.mainCss().button());
        addButton.addStyleName(SailorProfileMobileResources.INSTANCE.css().footerAddButton());
        contentUi.add(panel);
        addButton.addClickHandler((event) -> {
            presenter.getClientFactory().getPlaceController().goTo(new SailorProfilePlace(true));
        });
    }
}
