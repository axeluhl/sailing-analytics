package com.sap.sailing.gwt.home.shared.partials.dialog.confirm;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.home.shared.partials.dialog.DialogResources;
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
        DialogResources.INSTANCE.css().ensureInjected();
        PopupPanel dialog = new PopupPanel();

        TwoOptionsDialogPanel dialogPanel = new TwoOptionsDialogPanel(message, title, callback, dialog);
        dialogPanel.addStyleName(DialogResources.INSTANCE.css().dialog());
        dialogPanel.setButtonLabels(StringMessages.INSTANCE.yes(), StringMessages.INSTANCE.no());
        dialogPanel.setFirstButtonDestructive();

        dialog.setWidget(dialogPanel);
        dialog.addStyleName(DialogResources.INSTANCE.css().backgroundPanel());
        dialog.show();

        dialog.addDomHandler(e -> {
            dialog.hide();
            callback.cancel();
        }, ClickEvent.getType());
        dialog.sinkEvents(Event.ONCLICK | Event.ONTOUCHEND);
    }

}
