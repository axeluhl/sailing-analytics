package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.ImagesBarCell;

public class FlagImageCell extends ImagesBarCell {
    static final String ACTION_EDIT = "ACTION_EDIT";
    private final StringMessages stringMessages;
    private static RegattaOverviewResources resources = GWT.create(RegattaOverviewResources.class);

    public FlagImageCell(StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }

    public FlagImageCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.flagAP())),
                new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.arrowUp())));
    }
}