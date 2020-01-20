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

    public SystemInformationPanel(final ServerInfoDTO serverInfo, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        super();
        fullVersionText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
                + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna "
                + "aliquyam erat, sed diam voluptua. At vero eos et accusam et justo "
                + "duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata "
                + "sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, "
                + "consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt "
                + "ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero "
                + "eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, "
                + "no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        buildVersionText = new Label("");
        buildVersionText.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggleExpand();
            }
        });
        addFloatingWidget(buildVersionText);
        buildVersionText.setText("Version information is hidden (click to expand)");
        // if (serverInfo != null) {
        // buildVersionText.setText(stringMessages.version(serverInfo.getBuildVersion()));
        // } else {
        // buildVersionText.setText(stringMessages.version(stringMessages.unknown()));
        // }
    }

    protected void toggleExpand() {
        if (expanded) {
            buildVersionText.setText("Version information is hidden (click to expand)");
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
