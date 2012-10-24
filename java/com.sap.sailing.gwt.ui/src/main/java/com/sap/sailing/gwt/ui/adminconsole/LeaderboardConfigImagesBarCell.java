package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardConfigImagesBarCell extends ImagesBarCell {
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private StringMessages stringMessages;
    
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
        return Arrays.asList(new ImageSpec("ACTION_EDIT", stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())),
                new ImageSpec("ACTION_EDIT_SCORES", stringMessages.actionEditScores(), makeImagePrototype(resources.scoresIcon())),
                new ImageSpec("ACTION_REMOVE", stringMessages.actionRemove(), makeImagePrototype(resources.removeIcon())));
    }
}