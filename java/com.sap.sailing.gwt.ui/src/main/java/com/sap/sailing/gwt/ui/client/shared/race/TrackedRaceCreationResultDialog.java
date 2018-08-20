package com.sap.sailing.gwt.ui.client.shared.race;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Summary dialog to be shown after actions that generate a new TrackedRace. the dialog shows a link to the RaceBoard
 * for the generated TrackedRace. In addition, if available a link to the associated event is shown.
 */
public class TrackedRaceCreationResultDialog extends DataEntryDialog<Void> {

    private final VerticalPanel verticalPanel;

    public TrackedRaceCreationResultDialog(String title, String message, UUID eventId, String regattaName,
            String raceName, String leaderboardName, String leaderboardGroupName) {
        this(title, message, eventId, regattaName, createSingleRaceList(raceName), leaderboardName,
                leaderboardGroupName);
    }

    private static List<Pair<String, String>> createSingleRaceList(String raceName) {
        ArrayList<Pair<String, String>> rv = new ArrayList<>();
        rv.add(new Pair<String, String>(raceName, null));
        return rv;
    }

    public TrackedRaceCreationResultDialog(String title, String message, UUID eventId, String regattaName,
            List<Pair<String, String>> raceNameRaceColumnName, String leaderboardName, String leaderboardGroupName) {
        super(title, message, StringMessages.INSTANCE.ok(), StringMessages.INSTANCE.cancel(), /* validator */ null,
                new DialogCallback<Void>() {
                    @Override
                    public void ok(Void editedObject) {
                    }

                    @Override
                    public void cancel() {
                    }
                });

        verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(20);
        boolean multiRace = raceNameRaceColumnName.size() > 1;
        for (Pair<String, String> raceName : raceNameRaceColumnName) {
            String anchorLabel = multiRace ? StringMessages.INSTANCE.importFinishedGotoRaceboardWithName(raceName.getB())
                    : StringMessages.INSTANCE.importFinishedGotoRaceboard();
            Anchor raceboardAnchor = new Anchor(anchorLabel,
                    EntryPointWithSettingsLinkFactory.createRaceBoardLinkWithDefaultSettings(eventId, leaderboardName,
                            leaderboardGroupName, regattaName, raceName.getA()));
            raceboardAnchor.setTarget("_blank");
            verticalPanel.add(raceboardAnchor);
        }

        if (eventId != null) {
            Anchor eventAnchor = new Anchor(StringMessages.INSTANCE.importFinishedGotoEvent(),
                    EntryPointLinkFactory.createEventPlaceLink(eventId.toString(), Collections.emptyMap()));
            eventAnchor.setTarget("_blank");
            verticalPanel.add(eventAnchor);
        }

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