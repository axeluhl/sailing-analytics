package com.sap.sailing.gwt.home.mobile.places.user.profile.settings;

import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsEntry;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Implementation of {@link UserSettingsView} where users can change their preferred selections and notifications.
 */
public class UserSettings extends Composite implements UserSettingsView {

    private final Panel panel;

    public UserSettings(UserSettingsView.Presenter presenter) {
        panel = new FlowPanel();
        initWidget(panel);
        
        presenter.setView(this);
    }

    @Override
    public void setEntries(List<UserSettingsEntry> entries) {
        panel.clear();
        if (entries.isEmpty()) {
            // TODO nicer styling
            panel.add(new Label(StringMessages.INSTANCE.noDataFound()));
        } else {
            // TODO nicer styling
            panel.add(new Label(StringMessages.INSTANCE.userProfileSettingsTabDescription()));
            for (UserSettingsEntry userSettingsEntry : entries) {
                final MobileSection mobileSection = new MobileSection();
                mobileSection.addHeader(new Label(userSettingsEntry.getKeyWithoutContext()));
                mobileSection.addContent(new Label(userSettingsEntry.getDocumentSettingsId()));
                panel.add(mobileSection);
            }
        }
    }
}
