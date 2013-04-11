package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MediaActionBarCell extends ImagesBarCell {

    public static final String ACTION_REMOVE = "ACTION_REMOVE";
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;

    public MediaActionBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(new ImageSpec(ACTION_REMOVE,
                        stringMessages.actionRemove(), makeImagePrototype(resources.removeIcon())));
    }
}
