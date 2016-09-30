package com.sap.sse.gwt.client.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * A panel that represents a vertical tabbed set of pages, each of which contains another widget. Its child widgets are
 * shown as the user selects the various tabs associated with them. The tabs can contain arbitrary text, HTML, or
 * widgets.
 */
public class VerticalTabLayoutPanel extends AbstractTabLayoutPanel {

    interface VerticalTabLayoutPanelResources extends ClientBundle {
        @Source("VerticalTabLayoutPanel.css")
        CssResource verticalTabPanelStyles();
    }

    private final VerticalTabLayoutPanelResources resources = GWT.create(VerticalTabLayoutPanelResources.class);

    /**
     * Creates an empty vertical tab panel.
     */
    public VerticalTabLayoutPanel(double barHeight, Unit barUnit) {
        super(barHeight, barUnit);
        resources.verticalTabPanelStyles().ensureInjected();
        tabBar.setStyleName("gwt-VerticalTabLayoutPanelTabs");
        setStyleName("gwt-VerticalTabLayoutPanel");
    }

    @Override
    public String getContentConteinerStyle() {
        return "gwt-VerticalTabLayoutPanelContentContainer";
    }

    @Override
    public String getContentStyle() {
        return "gwt-VerticalTabLayoutPanelContent";
    }

    @Override
    public String getTabStyle() {
        return "gwt-VerticalTabLayoutPanelTab";
    }

    @Override
    public String getTabInnerStyle() {
        return "gwt-VerticalTabLayoutPanelTabInner";
    }

}