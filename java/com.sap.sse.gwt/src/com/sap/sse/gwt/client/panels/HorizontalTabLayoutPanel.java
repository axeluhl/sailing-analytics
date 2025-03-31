package com.sap.sse.gwt.client.panels;

import com.google.gwt.dom.client.Style.Unit;

/**
 * A panel that represents a horizontal tabbed set of pages, each of which contains another widget. Its child widgets
 * are shown as the user selects the various tabs associated with them. The tabs can contain arbitrary text, HTML, or
 * widgets.
 */
public class HorizontalTabLayoutPanel extends AbstractTabLayoutPanel {

    private static final int BIG_ENOUGH_TO_NOT_WRAP = 16384;

    /**
     * Creates an empty horizontal tab panel.
     */
    public HorizontalTabLayoutPanel(double barHeight, Unit barUnit) {
        super(barHeight, barUnit);

        // Make the tab bar extremely wide so that tabs themselves never wrap.
        // (Its layout container is overflow:hidden)
        tabBar.getElement().getStyle().setWidth(BIG_ENOUGH_TO_NOT_WRAP, Unit.PX);

        tabBar.setStyleName("gwt-TabLayoutPanelTabs");
        setStyleName("gwt-TabLayoutPanel");
    }

    @Override
    public String getContentConteinerStyle() {
        return "gwt-TabLayoutPanelContentContainer";
    }

    @Override
    public String getContentStyle() {
        return "gwt-TabLayoutPanelContent";
    }

    @Override
    public String getTabStyle() {
        return "gwt-TabLayoutPanelTab";
    }

    @Override
    public String getTabInnerStyle() {
        return "gwt-TabLayoutPanelTabInner";
    }
}
