package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sse.gwt.client.player.TimeListener;

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
        savedSplitPosition = 500;
        splitLayoutPanel.insert(leftScrollPanel, leftComponent, Direction.WEST, savedSplitPosition, null);
        
        AbsolutePanel absoluteMapAndToggleButtonsPanel = new AbsolutePanel();
        VerticalPanel panelForTogglingLayoutPanelComponents = new VerticalPanel();
        panelForTogglingLayoutPanelComponents.setStyleName("gwt-SplitLayoutPanel-NorthSouthToggleButton-Panel");
        absoluteMapAndToggleButtonsPanel.add(rightComponent.getEntryWidget());
        absoluteMapAndToggleButtonsPanel.add(panelForTogglingLayoutPanelComponents);

        for (final Component<?> component : components) {
            // adding components regardless of their visibility as we want to control visibility
            // in the layout panel
            splitLayoutPanel.insert(component.getEntryWidget(), component, Direction.SOUTH, 200, null);
            Button togglerButton = new Button(component.getEntryWidget().getTitle());
            togglerButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                  splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(component.getEntryWidget(), component, component.isVisible(), 200);
                  forceLayout();
                  if (component.isVisible() && component instanceof TimeListener) {
                      ((TimeListener)component).timeChanged(new Date(), null);
                  }
              }
            });
            togglerButton.setStyleName("gwt-SplitLayoutPanel-NorthSouthToggleButton");
            panelForTogglingLayoutPanelComponents.add(togglerButton);
        }
        splitLayoutPanel.insert(absoluteMapAndToggleButtonsPanel, rightComponent, Direction.CENTER, 0, null);
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
                splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(leftScrollPanel, leftComponent, /*hidden*/true, savedSplitPosition);
            }
        } else if (leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(leftScrollPanel, leftComponent, /*hidden*/false, savedSplitPosition);
        } else if (!leftComponent.isVisible() && !rightComponent.isVisible()) {
        }

        for (Component<?> component : components) {
            boolean isComponentInSplitPanel = isWidgetInSplitPanel(component.getEntryWidget());
            if (isComponentInSplitPanel) {
                boolean isComponentVisible = component.isVisible();
                splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(component.getEntryWidget(), component, !isComponentVisible, 200);
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
