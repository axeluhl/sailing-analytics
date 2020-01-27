package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;

public class SystemInformationPanel extends FlowPanel {

    private final Label buildVersionText;
    private String fullVersionText;
    private boolean expanded;
    private StringMessages stringMessages;

    public SystemInformationPanel(final ServerInfoDTO serverInfo, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        fullVersionText = ""
                + "Version: 94fdf889aea829180a09fae3f7283703cd2deb1c-build-202001222210 "
                + "System: :dbserver.internal.sapsailing.com:10202/dev-2010-rabbit.internal.sapsailing.com:/sapsailinganalytics-dev "
                + "Started: 202001222249";
        buildVersionText = new Label("");
        buildVersionText.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggleExpand();
            }
        });
        addFloatingWidget(buildVersionText);
        buildVersionText.setText(stringMessages.versionInfoHidden());
        // if (serverInfo != null) {
        // buildVersionText.setText(stringMessages.version(serverInfo.getBuildVersion()));
        // } else {
        // buildVersionText.setText(stringMessages.version(stringMessages.unknown()));
        // }
    }

    protected void toggleExpand() {
        if (expanded) {
            buildVersionText.setText(stringMessages.versionInfoHidden());
        } else {
            buildVersionText.setText(fullVersionText);
        }
        expanded = !expanded;
    }

    private void addFloatingWidget(Widget w) {
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        w.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        add(w);
    }
}
