package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardRaceConfigImagesBarCell extends ImagesBarCell {

    private StringMessages stringMessages;

    public LeaderboardRaceConfigImagesBarCell(StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }

    public LeaderboardRaceConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(new ImageSpec("ACTION_EDIT", stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())),
                new ImageSpec("ACTION_UNLINK", stringMessages.actionRaceUnlink(), makeImagePrototype(resources.unlinkIcon())),
                new ImageSpec("ACTION_REMOVE", stringMessages.actionRaceRemove(), makeImagePrototype(resources.removeIcon())));
    }
}