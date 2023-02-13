package com.sap.sailing.gwt.home.shared.partials.dialog.whatsnew;

import java.util.function.IntConsumer;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewResources;
import com.sap.sailing.gwt.home.shared.partials.dialog.DialogFactory;
import com.sap.sailing.gwt.settings.client.whatsnew.AbstractWhatsNewDialogFactory;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Implementation of {@link AbstractWhatsNewDialogFactory} for the Home.html change log.
 */
public final class WhatsNewDialogFactory extends AbstractWhatsNewDialogFactory<WhatsNewSettings> {

    /** Automatically shows the whats-new dialog after login, if necessary. */
    public static void register(final UserService userService, final PlaceController placeController) {
        new WhatsNewDialogFactory(userService, placeController).register();
    }

    private static boolean isUserNotified = false;
    private final PlaceController placeController;

    private WhatsNewDialogFactory(final UserService userService, final PlaceController placeController) {
        super(userService);
        this.placeController = placeController;
    }

    @Override
    protected void currentCharacterCount(final IntConsumer callback) {
        callback.accept(WhatsNewResources.INSTANCE.getSailingAnalyticsNotesHtml().getText().length());
    }

    @Override
    protected String getPrefName() {
        return WhatsNewSettings.PREF_NAME;
    }

    @Override
    protected WhatsNewSettings getInstanceForDeserialization() {
        return new WhatsNewSettings();
    }

    @Override
    protected WhatsNewSettings getInstanceForSerialization(final Long charCount) {
        return new WhatsNewSettings(charCount);
    }

    @Override
    protected boolean isUserAlreadyNotified() {
        return isUserNotified;
    }

    @Override
    protected void showDialog(final DialogCallback<Void> callback) {
        final PopupPanel dialog = DialogFactory.createDialog(StringMessages.INSTANCE.whatsNewDialogMessage(),
                StringMessages.INSTANCE.whatsNewDialogTitle(), false, StringMessages.INSTANCE.showChangelog(),
                StringMessages.INSTANCE.cancel(), callback);
        isUserNotified = true;
        dialog.ensureDebugId("HomeWhatsNewDialog");
        dialog.show();
    }

    @Override
    protected void openReleaseNotes() {
        placeController.goTo(new WhatsNewPlace(WhatsNewNavigationTabs.SailingAnalytics));
    }
}
