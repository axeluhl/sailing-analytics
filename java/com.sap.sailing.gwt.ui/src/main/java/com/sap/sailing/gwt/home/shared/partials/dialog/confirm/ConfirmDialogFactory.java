package com.sap.sailing.gwt.home.shared.partials.dialog.confirm;

import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.home.shared.partials.dialog.DialogFactory;
import com.sap.sailing.gwt.home.shared.partials.dialog.TwoOptionsDialogPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/** factory class for confirm dialog with yes/no button */
public final class ConfirmDialogFactory {

    /**
     * Creates a yes/no {@link TwoOptionsDialogPanel} with {@link #message}, which calls
     * {@link DialogCallback#ok(Object)} or {@link DialogCallback#cancel()}.
     */
    public static void showConfirmDialog(final String message, final String title,
            final DialogCallback<Void> callback) {
        PopupPanel dialog = DialogFactory.createDialog(message, title, true, StringMessages.INSTANCE.yes(),
                StringMessages.INSTANCE.no(), callback);
        dialog.show();
    }

}
