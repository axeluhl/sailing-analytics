package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

public class HTML5ProgressBar extends Widget {
    private Element progress;

    public HTML5ProgressBar() {
        progress = DOM.createElement("progress");
        progress.getStyle().setWidth(100, Unit.PCT);
        progress.getStyle().setHeight(2, Unit.EM);
        setElement(progress);
        setPercent(0);
    }

    public void setPercent(int percent) {
        progress.setAttribute("value", Double.toString(percent/100f));
    }
}
