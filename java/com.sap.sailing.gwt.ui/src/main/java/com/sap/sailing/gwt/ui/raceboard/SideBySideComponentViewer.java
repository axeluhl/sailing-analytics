package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.gwt.client.player.TimeListener;

/**
 *  Component Viewer that uses a {@link TouchSplitLayoutPanelWithBetterDraggers}
 *  to display its components.
 */
public class SideBySideComponentViewer implements ComponentViewer {
    
    /**
     * Panel that combines another Panel and associated close and
     * settings buttons.
     * 
     * @author Simon Marcel Pamies
     */
    abstract class PanelWithCloseAndSettingsButton extends AbsolutePanel implements RequiresResize {
        private final Widget containedPanel;
        private final HorizontalPanel panelForCloseAndSettingsButton;
        public PanelWithCloseAndSettingsButton(Widget containedPanel) {
            this.containedPanel = containedPanel;
            this.containedPanel.getElement().getStyle().setMarginTop(10, Unit.PX); // make some room for buttons
            this.panelForCloseAndSettingsButton = new HorizontalPanel();
            this.panelForCloseAndSettingsButton.setStyleName("gwt-SplitLayoutPanel-CloseAndSettingsButton-Panel");
            Button settingsButton = new Button();
            settingsButton.setStyleName("gwt-SplitLayoutPanel-SettingsButton");
            settingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    settingsClicked();
                }
            });
            panelForCloseAndSettingsButton.add(settingsButton);
            Button closeButton = new Button();
            closeButton.setStyleName("gwt-SplitLayoutPanel-CloseButton");
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    closeClicked();
                }
            });
            panelForCloseAndSettingsButton.add(closeButton);
            add(panelForCloseAndSettingsButton);
            add(this.containedPanel);
            setTitle(this.containedPanel.getTitle());
            setVisible(this.containedPanel.isVisible());
        }
        
        public void onResize() {
            if (this.containedPanel instanceof RequiresResize) {
                ((RequiresResize) this.containedPanel).onResize();
            }
        }
        
        public abstract void closeClicked();
        public abstract void settingsClicked();
    }
    
    private final int MAX_LEADERBOARD_WIDTH = 500;
    
    private final LeaderboardPanel leftComponent;
    private final Panel leaderboardContentPanel;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    private final Map<Component<?>, Panel> componentPanels;
    private final Panel leftPanel;
    private final StringMessages stringMessages;

    private final HorizontalPanel panelForTogglingSouthLayoutPanelComponents;
    private final HorizontalPanel panelForTogglingEastLayoutPanelComponents;
    
    private LayoutPanel mainPanel;
    
    private TouchSplitLayoutPanelWithBetterDraggers splitLayoutPanel; 
    private int savedSplitPosition = -1;
    
    public SideBySideComponentViewer(final LeaderboardPanel leftComponentP, final Component<?> rightComponentP, List<Component<?>> components, final StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.leftComponent = leftComponentP;
        this.leaderboardContentPanel = leftComponentP.getContentPanel();
        this.rightComponent = rightComponentP;
        this.components = components;
        this.componentPanels = new HashMap<Component<?>, Panel>();
        ScrollPanel leftScrollPanel = new ScrollPanel();
        leftScrollPanel.add(leftComponentP.getEntryWidget());
        leftScrollPanel.setTitle(leftComponentP.getEntryWidget().getTitle());
        mainPanel = new LayoutPanel();
        mainPanel.setSize("100%", "100%");
        mainPanel.setStyleName("SideBySideComponentViewer-MainPanel");
        splitLayoutPanel = new TouchSplitLayoutPanelWithBetterDraggers(/* splitter width */ 3);
        mainPanel.add(splitLayoutPanel);
        
        AbsolutePanel absoluteMapAndToggleButtonsPanel = new AbsolutePanel();
        panelForTogglingSouthLayoutPanelComponents = new HorizontalPanel();
        panelForTogglingSouthLayoutPanelComponents.setStyleName("gwt-SplitLayoutPanel-NorthSouthToggleButton-Panel");
        
        panelForTogglingEastLayoutPanelComponents = new HorizontalPanel();
        panelForTogglingEastLayoutPanelComponents.setStyleName("gwt-SplitLayoutPanel-EastToggleButton-Panel");
        
        absoluteMapAndToggleButtonsPanel.add(rightComponent.getEntryWidget());
        absoluteMapAndToggleButtonsPanel.add(panelForTogglingSouthLayoutPanelComponents);
        absoluteMapAndToggleButtonsPanel.add(panelForTogglingEastLayoutPanelComponents);

        final Button leaderboardTogglerButton = new Button(leftComponentP.getEntryWidget().getTitle());
        leaderboardTogglerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                  leftComponentP.setVisible(!leftComponentP.isVisible());
                  forceLayout();
                  if (leftComponentP.isVisible() && leftComponentP instanceof TimeListener) {
                      ((TimeListener)leftComponentP).timeChanged(new Date(), null);
                  }
                  leaderboardTogglerButton.removeFromParent();
            }
        });
        leaderboardTogglerButton.setStyleName("gwt-SplitLayoutPanel-EastToggleButton");
        panelForTogglingEastLayoutPanelComponents.add(leaderboardTogglerButton);

        // add a panel for close and settings buttons in leaderboard
        leftPanel = new PanelWithCloseAndSettingsButton(leftScrollPanel) {
            @Override
            public void closeClicked() {
                splitLayoutPanel.setWidgetVisibility(this, leftComponent, leaderboardContentPanel, true, 500);
                panelForTogglingEastLayoutPanelComponents.add(leaderboardTogglerButton);
            }
            @Override
            public void settingsClicked() {
                showSettingsDialog(leftComponent);
            }
        };
        
        initializeComponents();

        savedSplitPosition = 400;
        splitLayoutPanel.insert(leftPanel, leftComponentP, Direction.WEST, savedSplitPosition, null);
        splitLayoutPanel.insert(absoluteMapAndToggleButtonsPanel, rightComponent, Direction.CENTER, 0, null);

        Button showMapControlButton = new Button();
        showMapControlButton.setStyleName("gwt-SplitLayoutPanel-MapSettingsButton");
        showMapControlButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showSettingsDialog(rightComponent);
            }
        });
        absoluteMapAndToggleButtonsPanel.add(showMapControlButton);
    }
    
    private void initializeComponents() {
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
                  panelForTogglingSouthLayoutPanelComponents.remove(togglerButton);
              }
            });
            togglerButton.setStyleName("gwt-SplitLayoutPanel-NorthSouthToggleButton");
            panelForTogglingSouthLayoutPanelComponents.add(togglerButton);
            final Panel componentPanel = new PanelWithCloseAndSettingsButton(component.getEntryWidget()) {
                @Override
                public void settingsClicked() {
                    showSettingsDialog(component);
                }
                @Override
                public void closeClicked() {
                    splitLayoutPanel.setWidgetVisibility(this, component, this, true, 200);
                    panelForTogglingSouthLayoutPanelComponents.add(togglerButton);
                }
            };
            componentPanels.put(component, componentPanel);
            splitLayoutPanel.insert(componentPanel, component, Direction.SOUTH, 200, null);
        }
    }
    
    public <SettingsType> void showSettingsDialog(Component<SettingsType> component) {
        if (component.hasSettings()) {
            new SettingsDialog<SettingsType>(component, stringMessages).show();
        }
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
                splitLayoutPanel.setWidgetVisibility(leftPanel, leftComponent, leaderboardContentPanel, /*hidden*/true, savedSplitPosition);
            }
        } else if (leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibility(leftPanel, leftComponent, leaderboardContentPanel, /*hidden*/false, savedSplitPosition);
        } else if (!leftComponent.isVisible() && !rightComponent.isVisible()) {
        }

        for (Component<?> component : components) {
            final boolean isComponentVisible = component.isVisible();
            splitLayoutPanel.setWidgetVisibility(componentPanels.get(component), component, componentPanels.get(component), !isComponentVisible, 200);
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
    
    public void setLeaderboardWidth(int width) {
        if (width > 0 && savedSplitPosition != width) {
            savedSplitPosition = Math.min(width, MAX_LEADERBOARD_WIDTH);
            forceLayout();
        }
    }
}
