package com.sap.sailing.landscape.ui.client;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class ReverseProxyImagesBarCell extends ImagesBarCell{
    static final String ACTION_REMOVE = DefaultActions.DELETE.name();
    static final String ACTION_ROTATE_HTTPD_LOGS = "Rotate httpd logs"; 

    private final StringMessages stringMessages;

    public  ReverseProxyImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }
    
    public ReverseProxyImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }
    
    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(new ImageSpec(ACTION_REMOVE, stringMessages.remove(), IconResources.INSTANCE.removeIcon()),
                new ImageSpec(ACTION_ROTATE_HTTPD_LOGS, stringMessages.rotateHttpdLogs(), IconResources.INSTANCE.refreshIcon()));
    }
}
