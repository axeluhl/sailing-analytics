package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays; 

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sse.gwt.client.IconResources;

public class LeaderboardConfigImagesBarCell extends ImagesBarCell {
    public static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_EDIT_SCORES = "ACTION_EDIT_SCORES";
    public static final String ACTION_EDIT_COMPETITORS = "ACTION_EDIT_COMPETITORS";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    public static final String ACTION_CONFIGURE_URL = "ACTION_CONFIGURE_URL";
    public static final String ACTION_EXPORT_XML = "ACTION_EXPORT_XML";
    public static final String ACTION_OPEN_COACH_DASHBOARD = "ACTION_OPEN_COACH_DASHBOARD";
    public static final String ACTION_SHOW_REGATTA_LOG = "ACTION_SHOW_REGATTA_LOG";
    public static final String ACTION_CREATE_PAIRINGLIST = "ACTION_CREATE_PAIRINGLIST";
    public static final String ACTION_PRINT_PAIRINGLIST = "ACTION_PRINT_PAIRINGLIST";
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;
    
    public LeaderboardConfigImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public LeaderboardConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())),
                new ImageSpec(ACTION_EDIT_SCORES, stringMessages.actionEditScores(), makeImagePrototype(resources.scoresIcon())),
                new ImageSpec(ACTION_EDIT_COMPETITORS, stringMessages.actionEditCompetitors(), makeImagePrototype(resources.competitorsIcon())),
                new ImageSpec(ACTION_CONFIGURE_URL, stringMessages.actionConfigureUrl(), makeImagePrototype(resources.settingsActionIcon())),
                new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())),
                new ImageSpec(ACTION_EXPORT_XML, stringMessages.actionExportXML(), makeImagePrototype(resources.exportXMLIcon())),
                new ImageSpec(ACTION_OPEN_COACH_DASHBOARD, stringMessages.actionOpenDashboard(), makeImagePrototype(resources.openCoachDashboard())),
                new ImageSpec(ACTION_SHOW_REGATTA_LOG, stringMessages.regattaLog(), makeImagePrototype(resources.flagIcon())),
                new ImageSpec(ACTION_CREATE_PAIRINGLIST, stringMessages.pairingLists(), makeImagePrototype(resources.pairingList())),
                new ImageSpec(ACTION_PRINT_PAIRINGLIST, stringMessages.print() + " " + stringMessages.pairingList(), makeImagePrototype(resources.printPairingList())));
    }
}