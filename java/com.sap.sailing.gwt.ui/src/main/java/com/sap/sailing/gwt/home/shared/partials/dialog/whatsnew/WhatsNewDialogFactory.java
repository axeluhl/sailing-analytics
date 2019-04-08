package com.sap.sailing.gwt.home.shared.partials.dialog.whatsnew;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewResources;
import com.sap.sailing.gwt.home.shared.partials.dialog.DialogFactory;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * This is a factory class to create a WhatsNewDialog, if the changelog changed for more than
 * {@link #THRESHOLD_WHATS_NEW} characters since the last login of the current user.
 */
public final class WhatsNewDialogFactory {

    private static final long THRESHOLD_WHATS_NEW = 10;
    private static final Logger LOG = Logger.getLogger(WhatsNewDialogFactory.class.getName());

    private static boolean isUserNotified = false;

    private WhatsNewDialogFactory() {
    }

    /** Automatically shows the whats-new dialog after login, if necessary. */
    public static void registerWithUserService(UserService userService, PlaceController placeController) {
        userService.addUserStatusEventHandler((userDTO, b) -> {
            if (userDTO != null) {
                showWhatsNewDialogIfNecessaryAndUpdatePreference(userService, placeController);
            }
        }, false);
    }

    /** Shows a What's New Dialog. */
    private static void showWhatsNewDialog(PlaceController placeController, DialogCallback<Void> dialogCallback) {
        final PopupPanel dialog = DialogFactory.createDialog(StringMessages.INSTANCE.whatsNewDialogMessage(),
                StringMessages.INSTANCE.whatsNewDialogTitle(), false, StringMessages.INSTANCE.showChangelog(),
                StringMessages.INSTANCE.cancel(), dialogCallback);
        isUserNotified = true;
        dialog.show();
    }

    /**
     * Shows a dialog, if the changelog changed for more than {@link #THRESHOLD_WHATS_NEW} characters since the last
     * login of the current user.
     */
    private static void showWhatsNewDialogIfNecessaryAndUpdatePreference(UserService userService,
            PlaceController placeController) {
        if (isUserNotified) {
            return;
        }
        final long charactersInWhatsChangedDocument = WhatsNewResources.INSTANCE.getSailingAnalyticsNotesHtml()
                .getText().length();
        userService.getPreference(WhatsNewSettings.PREF_NAME, new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                SettingsToJsonSerializerGWT settingsToJsonSerializerGWT = new SettingsToJsonSerializerGWT();

                DialogCallback<Void> dialogCallback = new DialogCallback<Void>() {
                    @Override
                    public void ok(Void editedObject) {
                        updateOrCreatePreference(charactersInWhatsChangedDocument, settingsToJsonSerializerGWT);
                        placeController.goTo(new WhatsNewPlace(WhatsNewNavigationTabs.SailingAnalytics));
                    }

                    @Override
                    public void cancel() {
                        updateOrCreatePreference(charactersInWhatsChangedDocument, settingsToJsonSerializerGWT);
                    }
                };

                if (result != null) {
                    // deserialize whats-new-setting
                    WhatsNewSettings pref = settingsToJsonSerializerGWT.deserialize(new WhatsNewSettings(), result);

                    if (pref.getNumberOfCharsOnLastLogin() > charactersInWhatsChangedDocument + THRESHOLD_WHATS_NEW) {
                        // check if length change is over threshold
                        WhatsNewDialogFactory.showWhatsNewDialog(placeController, dialogCallback);
                    }
                } else {
                    // create preference
                    WhatsNewDialogFactory.showWhatsNewDialog(placeController, dialogCallback);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                LOG.log(Level.SEVERE, caught.getMessage(), caught);
            }

            /** Updates or creates a {@link WhatsNewSettings} object. */
            private void updateOrCreatePreference(long charactersInWhatsChangedDocument,
                    SettingsToJsonSerializerGWT settingsToJsonSerializerGWT) {
                String serializedSetting = settingsToJsonSerializerGWT
                        .serializeToString(new WhatsNewSettings(charactersInWhatsChangedDocument));
                userService.setPreference(WhatsNewSettings.PREF_NAME, serializedSetting, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        LOG.log(Level.SEVERE, caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(Void result) {
                    }
                });
            }
        });
    }

}
