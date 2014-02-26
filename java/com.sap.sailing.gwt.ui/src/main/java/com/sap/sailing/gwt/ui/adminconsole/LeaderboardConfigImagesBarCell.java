package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.ImagesBarCell;

public class LeaderboardConfigImagesBarCell extends ImagesBarCell {
    public static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_EDIT_SCORES = "ACTION_EDIT_SCORES";
    public static final String ACTION_EDIT_COMPETITORS = "ACTION_EDIT_COMPETITORS";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    public static final String ACTION_CONFIGURE_URL = "ACTION_CONFIGURE_URL";
    public static final String ACTION_EXPORT_XML = "ACTION_EXPORT_XML";
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
        ArrayList<ImageSpec> result = new ArrayList<ImageSpec>();
        result.add(new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())));
        result.add(new ImageSpec(ACTION_EDIT_SCORES, stringMessages.actionEditScores(), makeImagePrototype(resources.scoresIcon())));
        result.add(new ImageSpec(ACTION_EDIT_COMPETITORS, stringMessages.actionEditCompetitors(), makeImagePrototype(resources.competitorsIcon())));
        result.add(new ImageSpec(ACTION_CONFIGURE_URL, stringMessages.actionConfigureUrl(), makeImagePrototype(resources.settingsActionIcon())));
        result.add(new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(resources.removeIcon())));
        result.add(new ImageSpec(ACTION_EXPORT_XML, stringMessages.actionExportXML(), makeImagePrototype(resources.exportXMLIcon())));
        return result;
    }
}