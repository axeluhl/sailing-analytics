package com.sap.sailing.gwt.home.mobile.places.user.profile.settings;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.filter.UserSettingsByKeyTextBoxFilter;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsEntry;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.Filter;

/**
 * Implementation of {@link UserSettingsView} where users can change their preferred selections and notifications.
 */
public class UserSettings extends Composite implements UserSettingsView {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserSettings> {
    }

    @UiField
    UserSettingsByKeyTextBoxFilter userSettingsFilterUi;
    @UiField
    FlowPanel settingsContainerUi;

    private final Presenter presenter;

    public UserSettings(UserSettingsView.Presenter presenter) {
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
        userSettingsFilterUi.addFilterValueChangeHandler(filter -> presenter.updateData());
        presenter.setView(this);
    }

    @Override
    public Filter<UserSettingsEntry> getFilter() {
        return userSettingsFilterUi.getFilter();
    }

    @Override
    public void setEntries(List<UserSettingsEntry> entries) {
        settingsContainerUi.clear();
        if (entries.isEmpty()) {
            final Label noDataFoundWidget = new Label(StringMessages.INSTANCE.noDataFound());
            noDataFoundWidget.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
            settingsContainerUi.add(noDataFoundWidget);
        }
        entries.forEach(entry -> settingsContainerUi.add(new UserSettingsItem(entry, () -> presenter.remove(entry))));
    }
}
