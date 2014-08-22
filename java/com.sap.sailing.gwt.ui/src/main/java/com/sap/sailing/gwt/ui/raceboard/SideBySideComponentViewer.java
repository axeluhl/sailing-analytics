package com.sap.sailing.gwt.ui.raceboard;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;

/**
 *  Component Viewer that uses a {@link TouchSplitLayoutPanelWithBetterDraggers}
 *  to display its components.
 */
public class SideBySideComponentViewer implements ComponentViewer {
    
    class ResizableAbsolutePanel extends AbsolutePanel implements RequiresResize {
        public void onResize() {
            WidgetCollection children = getChildren();
            for (Widget widget : children) {
                if (widget instanceof RequiresResize) {
                    ((RequiresResize)widget).onResize();
                }
            }
        }
    }

    private final int MIN_LEADERBOARD_WIDTH = 370;
    
    private final LeaderboardPanel leftComponent;
    private final Panel leaderboardContentPanel;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    private final ScrollPanel leftScrollPanel;
    private final StringMessages stringMessages;

    private LayoutPanel mainPanel;
    
    private TouchSplitLayoutPanelWithBetterDraggers splitLayoutPanel; 
    private int savedSplitPosition = -1;
    private boolean layoutForLeftComponentForcedOnce = false;
    
    public SideBySideComponentViewer(final LeaderboardPanel leftComponentP, final Component<?> rightComponentP, List<Component<?>> components, final StringMessages stringMessages, Widget... additionalButtons) {
        this.stringMessages = stringMessages;
        this.leftComponent = leftComponentP;
        this.leaderboardContentPanel = leftComponentP.getContentPanel();
        this.rightComponent = rightComponentP;
        this.components = components;
        leftScrollPanel = new ScrollPanel();
        leftScrollPanel.add(leftComponentP.getEntryWidget());
        leftScrollPanel.setTitle(leftComponentP.getEntryWidget().getTitle());
        mainPanel = new LayoutPanel();
        mainPanel.setSize("100%", "100%");
        mainPanel.getElement().getStyle().setMarginTop(-11, Unit.PX);
        mainPanel.setStyleName("SideBySideComponentViewer-MainPanel");
        splitLayoutPanel = new TouchSplitLayoutPanelWithBetterDraggers(/* horizontal splitter width */ 3, /*vertical splitter height*/ 25);
        mainPanel.add(splitLayoutPanel);
        
        initializeComponents();

        savedSplitPosition = MIN_LEADERBOARD_WIDTH;
        splitLayoutPanel.insert(leftScrollPanel, leftComponent, Direction.WEST, savedSplitPosition, null);
        
        ResizableAbsolutePanel panelForMapAndHorizontalToggleButtons = new ResizableAbsolutePanel();
        panelForMapAndHorizontalToggleButtons.add(rightComponent.getEntryWidget());
        
        splitLayoutPanel.insert(panelForMapAndHorizontalToggleButtons, rightComponent, Direction.CENTER, 0, null);
        for (Widget widget : additionalButtons) {
            panelForMapAndHorizontalToggleButtons.add(widget);
        }
        splitLayoutPanel.lastComponentHasBeenAdded(this, panelForMapAndHorizontalToggleButtons);
    }
    
    private void initializeComponents() {
        for (final Component<?> component : components) {
            splitLayoutPanel.insert(component.getEntryWidget(), component, Direction.SOUTH, 200, null);
        }
    }
    
    public <SettingsType> void showSettingsDialog(Component<SettingsType> component) {
        if (component.hasSettings()) {
            new SettingsDialog<SettingsType>(component, stringMessages).show();
        }
    }

    public void forceLayout() {
        if (!leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard is not visible, but the map is
            if (isWidgetInSplitPanel(leftScrollPanel)) {
                splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, leaderboardContentPanel, /*hidden*/true, savedSplitPosition);
            }
        } else if (leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, leaderboardContentPanel, /*hidden*/false, savedSplitPosition);
        } else if (!leftComponent.isVisible() && !rightComponent.isVisible()) {
        }

        for (Component<?> component : components) {
            final boolean isComponentVisible = component.isVisible();
            splitLayoutPanel.setWidgetVisibility(component.getEntryWidget(), component, component.getEntryWidget(), !isComponentVisible, 200);
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
    
    public void setLeftComponentWidth(int width) {
        // TODO: The information provided by width is wrong
        // need to find a way to get the correct information
        if (!layoutForLeftComponentForcedOnce) {
            savedSplitPosition = MIN_LEADERBOARD_WIDTH;
            forceLayout();
        }
        layoutForLeftComponentForcedOnce = true;
    }
}
