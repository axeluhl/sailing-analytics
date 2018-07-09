package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class TaggingPanel extends ComponentWithoutSettings {
    
    private final Widget widget;
    private final StringMessages stringMessages;
    
    public TaggingPanel(Widget widget, StringMessages stringMessages, Component<?> parent, ComponentContext<?> context) {
        super(parent, context);
        this.widget = widget;
        this.stringMessages = stringMessages;
        
        widget.setTitle(stringMessages.tagging());
        widget.getElement().getStyle().setMargin(6, Unit.PX);
        widget.getElement().getStyle().setMarginTop(10, Unit.PX);
    }

    @Override
    public String getId() {
        return "TaggingPanel";
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.tagging();
    }

    @Override
    public Widget getEntryWidget() {
        return widget;
    }

    @Override
    public boolean isVisible() {
        return widget.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        widget.setVisible(visibility);
    }

    @Override
    public String getDependentCssClassName() {
        return "tags";
    }
}
