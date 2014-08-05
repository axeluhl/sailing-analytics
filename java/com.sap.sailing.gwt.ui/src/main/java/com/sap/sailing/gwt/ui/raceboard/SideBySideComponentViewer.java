package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sse.gwt.client.player.TimeListener;

public class SideBySideComponentViewer implements ComponentViewer {
    
    abstract class PanelWithCloseAndSettingsButton extends AbsolutePanel {
        final Widget containedPanel;
        public PanelWithCloseAndSettingsButton(Widget containedPanel) {
            this.containedPanel = containedPanel;
            HorizontalPanel panelForCloseAndSettingsButton = new HorizontalPanel();
            Button closeButton = new Button("X");
            closeButton.setStyleName("gwt-SplitLayoutPanel-CloseButton");
            closeButton.addStyleName("gwt-Button");
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    closeClicked();
                }
            });
            panelForCloseAndSettingsButton.add(closeButton);
            Button settingsButton = new Button("S");
            settingsButton.setStyleName("gwt-SplitLayoutPanel-SettingsButton");
            settingsButton.addStyleName("gwt-Button");
            settingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    settingsClicked();
                }
            });
            panelForCloseAndSettingsButton.add(settingsButton);
            add(panelForCloseAndSettingsButton);
            add(this.containedPanel);
            setTitle(this.containedPanel.getTitle());
            setVisible(this.containedPanel.isVisible());
        }
        
        public abstract void closeClicked();
        public abstract void settingsClicked();
    }
    
    /** there is no easy replacement for the HorizontalSplitPanel available */ 
    private final Component<?> leftComponent;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    private final Map<Component<?>, Panel> componentPanels;
    private final Panel leftPanel;
    
    private LayoutPanel mainPanel;
    
    private TouchSplitLayoutPanelWithBetterDraggers splitLayoutPanel; 
    private int savedSplitPosition = -1;
    
    public SideBySideComponentViewer(Component<?> leftComponentP, Component<?> rightComponentP, List<Component<?>> components) {
        this.leftComponent = leftComponentP;
        this.rightComponent = rightComponentP;
        this.components = components;
        this.componentPanels = new HashMap<Component<?>, Panel>();
        ScrollPanel leftScrollPanel = new ScrollPanel();
        leftScrollPanel.add(leftComponentP.getEntryWidget());
        leftScrollPanel.setTitle(leftComponentP.getEntryWidget().getTitle());
        mainPanel = new LayoutPanel();
        mainPanel.setSize("100%", "100%");
        splitLayoutPanel = new TouchSplitLayoutPanelWithBetterDraggers(/* splitter width */ 3);
        mainPanel.add(splitLayoutPanel);
        
        // add a panel for close and settings buttons in leaderboard
        leftPanel = new PanelWithCloseAndSettingsButton(leftScrollPanel) {
            @Override
            public void closeClicked() {
            }
            @Override
            public void settingsClicked() {
                splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(this, leftComponent, true, 500);
            }
        };
        
        savedSplitPosition = 500;
        splitLayoutPanel.insert(leftPanel, leftComponentP, Direction.WEST, savedSplitPosition, null);

        initializeComponents();
    }
    
    private void initializeComponents() {
        // add a panel for split pane toggle buttons
        AbsolutePanel absoluteMapAndToggleButtonsPanel = new AbsolutePanel();
        VerticalPanel panelForTogglingLayoutPanelComponents = new VerticalPanel();
        panelForTogglingLayoutPanelComponents.setStyleName("gwt-SplitLayoutPanel-NorthSouthToggleButton-Panel");
        absoluteMapAndToggleButtonsPanel.add(rightComponent.getEntryWidget());
        absoluteMapAndToggleButtonsPanel.add(panelForTogglingLayoutPanelComponents);

        for (final Component<?> component : components) {
            final Button togglerButton = new Button(component.getEntryWidget().getTitle());
            togglerButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                  component.setVisible(!component.isVisible());
                  forceLayout();
                  if (component.isVisible() && component instanceof TimeListener) {
                      ((TimeListener)component).timeChanged(new Date(), null);
                  }
                  togglerButton.setVisible(false);
              }
            });
            togglerButton.setStyleName("gwt-SplitLayoutPanel-NorthSouthToggleButton");
            panelForTogglingLayoutPanelComponents.add(togglerButton);
            final Panel componentPanel = new PanelWithCloseAndSettingsButton(component.getEntryWidget()) {
                @Override
                public void settingsClicked() {
                }
                @Override
                public void closeClicked() {
                    splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(this, component, true, 200);
                    togglerButton.setVisible(true);
                }
            };
            componentPanels.put(component, componentPanel);
            splitLayoutPanel.insert(componentPanel, component, Direction.SOUTH, 200, null);
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
            if (isWidgetInSplitPanel(leftPanel)) {
                splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(leftPanel, leftComponent, /*hidden*/true, savedSplitPosition);
            }
        } else if (leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(leftPanel, leftComponent, /*hidden*/false, savedSplitPosition);
        } else if (!leftComponent.isVisible() && !rightComponent.isVisible()) {
        }

        for (Component<?> component : components) {
            final boolean isComponentVisible = component.isVisible();
            splitLayoutPanel.setWidgetVisibilityAndPossiblyShowSplitter(componentPanels.get(component), component, !isComponentVisible, 200);
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
