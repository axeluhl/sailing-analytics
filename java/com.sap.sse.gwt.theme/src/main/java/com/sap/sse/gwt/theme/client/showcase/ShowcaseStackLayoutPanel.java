package com.sap.sse.gwt.theme.client.showcase;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ShowcaseStackLayoutPanel extends StackLayoutPanel {
    private SimplePanel target;

    public ShowcaseStackLayoutPanel(Unit unit) {
        super(unit);
    }

    public void setTarget(SimplePanel target) {
        this.target = target;
    }

    @Override
    public void add(Widget widget, String header, boolean asHtml, double headerSize) {
        if (widget instanceof Section) {
            Section section = (Section) widget;
            section.setTargetContentArea(target);
        }
        super.add(widget, header, asHtml, headerSize);
    }
}
