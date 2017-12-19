package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.UUID;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class ExpeditionAllInOneImportResultDialog extends DataEntryDialog<Void> {

    private final VerticalPanel verticalPanel;

    public ExpeditionAllInOneImportResultDialog(UUID eventId, String regattaName, String raceName,
            String leaderboardName, String leaderboardGroupName) {
        super("TODO: title", "TODO Message", StringMessages.INSTANCE.ok(), StringMessages.INSTANCE.cancel(),
                /* validator */ null, new DialogCallback<Void>() {
                    @Override
                    public void ok(Void editedObject) {
                    }

                    @Override
                    public void cancel() {
                    }
                });
        verticalPanel = new VerticalPanel();
        verticalPanel.add(new Anchor("TODO open RaceBoard",
                EntryPointWithSettingsLinkFactory.createRaceBoardLinkWithDefaultSettings(eventId, leaderboardName, leaderboardGroupName, regattaName, raceName)));
        verticalPanel.add(new Anchor("TODO open Event",
                EntryPointLinkFactory.createEventPlaceLink(eventId.toString(), Collections.emptyMap())));
    }

    @Override
    protected Widget getAdditionalWidget() {
        return verticalPanel;
    }

    @Override
    protected Void getResult() {
        return null;
    }
}
