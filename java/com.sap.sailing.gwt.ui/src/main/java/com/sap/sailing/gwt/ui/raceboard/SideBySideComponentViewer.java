package com.sap.sailing.gwt.ui.raceboard;

import java.util.List;

import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;

public class SideBySideComponentViewer implements ComponentViewer {
    
    /** there is no easy replacement for the HorizontalSplitPanel available */ 
    private final Component<?> leftComponent;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    private final ScrollPanel leftScrollPanel;
    
    private LayoutPanel mainPanel;
    
    private TouchSplitLayoutPanelWithBetterDraggers splitLayoutPanel; 
    private int savedSplitPosition = -1;
    
    public SideBySideComponentViewer(Component<?> leftComponent, Component<?> rightComponent, List<Component<?>> components) {
        this.leftComponent = leftComponent;
        this.rightComponent = rightComponent;
        this.components = components;
        leftScrollPanel = new ScrollPanel();
        leftScrollPanel.add(leftComponent.getEntryWidget());
        leftScrollPanel.setTitle(leftComponent.getEntryWidget().getTitle());
        mainPanel = new LayoutPanel();
        mainPanel.setSize("100%", "100%");
        splitLayoutPanel = new TouchSplitLayoutPanelWithBetterDraggers(/* splitter width */ 3);
        mainPanel.add(splitLayoutPanel);
        for (Component<?> component : components) {
            if (component.isVisible()) {
                splitLayoutPanel.addSouth(component.getEntryWidget(), 200);
            }
        }
        savedSplitPosition = 500;
        splitLayoutPanel.addWest(leftScrollPanel, savedSplitPosition);
        splitLayoutPanel.add(rightComponent.getEntryWidget());
    }

    public void forceLayout() {
        if (leftComponent.isVisible() && !rightComponent.isVisible()) {
            // the leaderboard is visible, but not the map
            if (isWidgetInSplitPanel(rightComponent.getEntryWidget())) {
                splitLayoutPanel.remove(rightComponent.getEntryWidget());
            }
        } else if (!leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard is not visible, but the map is
            if (isWidgetInSplitPanel(leftScrollPanel)) {
                splitLayoutPanel.remove(leftScrollPanel);
            }
        } else if (leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard and the map are visible
            if (!isWidgetInSplitPanel(leftScrollPanel) || !isWidgetInSplitPanel(rightComponent.getEntryWidget())) {
                if (!isWidgetInSplitPanel(leftScrollPanel)) {
                    splitLayoutPanel.insertWest(leftScrollPanel, savedSplitPosition, rightComponent.getEntryWidget());
                } else {
                    splitLayoutPanel.insertEast(rightComponent.getEntryWidget(), savedSplitPosition, leftScrollPanel);
                }
            }
        } else if (!leftComponent.isVisible() && !rightComponent.isVisible()) {
        }

        for (Component<?> component : components) {
            boolean isComponentInSplitPanel = isWidgetInSplitPanel(component.getEntryWidget());
            if (component.isVisible()) {
                if (!isComponentInSplitPanel) {
                    splitLayoutPanel.insertSouth(component.getEntryWidget(), 200, splitLayoutPanel.getWidget(0));
                }
            } else {
                if (isComponentInSplitPanel) {
                    splitLayoutPanel.remove(component.getEntryWidget());
                }
            }
        }
        splitLayoutPanel.forceLayout();
    }

    private boolean isWidgetInSplitPanel(Widget widget) {
        int widgetIndex = splitLayoutPanel.getWidgetIndex(widget);
        return widgetIndex >= 0;
    }

    public Panel getViewerWidget() {
        return mainPanel;
    }

    public Component<?> getRootComponent() {
        return null;
    }

    public String getViewerName() {
        return "";
    }
}
