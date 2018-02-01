package com.sap.sailing.gwt.home.mobile.places.user.profile.settings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsEntry;

public class UserSettingsItem extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<MobileSection, UserSettingsItem> {
    }
    
    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField HTMLPanel contentContainerUi;
    @UiField DivElement settingsDataUi;
    private final Runnable deleteCallback;

    public UserSettingsItem(UserSettingsEntry userSettingsEntry, Runnable deleteCallback) {
        this.deleteCallback = deleteCallback;
        MobileSection mobileSection = uiBinder.createAndBindUi(this);
        mobileSection.setEdgeToEdgeContent(true);
        initWidget(mobileSection);


        sectionHeaderUi.setSectionTitle(userSettingsEntry.getKeyWithoutContext());
        sectionHeaderUi.setSubtitle(userSettingsEntry.getDocumentSettingsId());
        final String userProfileSettings = userSettingsEntry.getProfileData();
        boolean hasUserData = (userProfileSettings != null && !userProfileSettings.isEmpty());
        final String localSettings = userSettingsEntry.getLocalData();
        settingsDataUi.setInnerText(hasUserData ? userProfileSettings : localSettings);

        sectionHeaderUi.initCollapsibility(contentContainerUi.getElement(), false);
    }
    
    @UiHandler("deleteControlUi")
    void onRemoveControlClicked(ClickEvent event) {
        this.deleteCallback.run();
    }

}
