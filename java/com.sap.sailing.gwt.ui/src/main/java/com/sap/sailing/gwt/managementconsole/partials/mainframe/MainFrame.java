package com.sap.sailing.gwt.managementconsole.partials.mainframe;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.partials.header.Header;

public class MainFrame extends ResizeComposite {

    interface MainFrameUiBinder extends UiBinder<Widget, MainFrame> {
    }

    private static MainFrameUiBinder uiBinder = GWT.create(MainFrameUiBinder.class);

    @UiField
    MainFrameResources local_res;

    @UiField
    Header header;

    @UiField
    ScrollPanel contentContainer;

    public MainFrame() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }

    public AcceptsOneWidget getContentContainer() {
        return contentContainer;
    }

    public Header getHeader() {
        return header;
    }
}
