package com.sap.sailing.gwt.home.desktop.places.user.profile.settingstab;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsEntry;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SettingsEntryDialog extends DataEntryDialog<UserSettingsEntry> {

    private final FlowPanel fp;

    public SettingsEntryDialog(UserSettingsEntry entry, Runnable deleteCallback) {
        super(StringMessages.INSTANCE.settings(), StringMessages.INSTANCE.settingsForId(entry.getKey()), StringMessages.INSTANCE.remove(), StringMessages.INSTANCE.cancel(), null, false, new DialogCallback<UserSettingsEntry>() {
            @Override
            public void ok(UserSettingsEntry editedObject) {
                deleteCallback.run();
            }

            @Override
            public void cancel() {
            }
        });
        
        fp = new FlowPanel();
        
        final String userProfileSettings = entry.getProfileData();
        boolean hasUserData = (userProfileSettings != null && !userProfileSettings.isEmpty());
        final String localSettings = entry.getLocalData();
        
        TextArea textArea = createTextArea(hasUserData ? userProfileSettings : localSettings);
        textArea.setReadOnly(true);
        fp.add(textArea);
    }

    public TextArea createTextArea(final String initialValue) {
        final TextArea textArea = super.createTextArea(initialValue);
        textArea.getElement().getStyle().setWidth(25, Unit.EM);
        textArea.getElement().getStyle().setHeight(15, Unit.EM);
        return textArea;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        return fp;
    }

    @Override
    protected UserSettingsEntry getResult() {
        return null;
    }

}
