package com.sap.sailing.gwt.ui.client.media.rebuild;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class MediaItemWidget {
    private static MediaItemWidgetUiBinder uiBinder = GWT.create(MediaItemWidgetUiBinder.class);
    

    interface MediaItemWidgetUiBinder extends UiBinder<Widget, MediaItemWidget> {
    }
}
