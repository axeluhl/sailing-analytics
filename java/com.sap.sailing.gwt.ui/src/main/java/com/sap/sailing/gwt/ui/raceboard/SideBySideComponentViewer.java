package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaComponent;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManagerComponent;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.dialog.WindowBox;

/**
 *  Component Viewer that uses a {@link TouchSplitLayoutPanel}
 *  to display its components.
 *  
 *  TODO: Refactor to make sure it is really only performing operations
 *  that are related to view. Currently it is digging too deep into
 *  components and setting titles or even creating video buttons.
 */
public class SideBySideComponentViewer implements ComponentViewer {
    
    private static final int DEFAULT_SOUTH_SPLIT_PANEL_HEIGHT = 200;
    private final int MIN_LEADERBOARD_WIDTH = 432; // works well for 505 and ESS

    /**
     * Absolute Panel that informs its children about a resize
     */
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

    private final LeaderboardPanel leftComponent;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    private final ScrollPanel leftScrollPanel;
    private final Button videoControlButton;
    private final StringMessages stringMessages;

    private LayoutPanel mainPanel;
    
    private TouchSplitLayoutPanel splitLayoutPanel; 
    private int savedSplitPosition = -1;
    private boolean layoutForLeftComponentForcedOnce = false;
    
    public SideBySideComponentViewer(final LeaderboardPanel leftComponentP, final Component<?> rightComponentP, final MediaPlayerManagerComponent mediaPlayerManagerComponent, List<Component<?>> components, final StringMessages stringMessages, UserDTO user) {
        this.stringMessages = stringMessages;
        this.leftComponent = leftComponentP;
        this.rightComponent = rightComponentP;
        this.components = components;
        this.videoControlButton = createVideoControlButton(mediaPlayerManagerComponent);
        this.leftScrollPanel = new ScrollPanel();
        this.leftScrollPanel.add(leftComponentP.getEntryWidget());
        this.leftScrollPanel.setTitle(leftComponentP.getEntryWidget().getTitle());
        this.mainPanel = new LayoutPanel();
        this.mainPanel.setSize("100%", "100%");
        this.mainPanel.getElement().getStyle().setMarginTop(-12, Unit.PX);
        this.mainPanel.setStyleName("SideBySideComponentViewer-MainPanel");
        this.splitLayoutPanel = new TouchSplitLayoutPanel(/* horizontal splitter width */ 3, /*vertical splitter height*/ 25);
        this.mainPanel.add(splitLayoutPanel);
        
        // initialize components - they need to be added before other widgets to get the right width
        initializeComponents();

        // initialize the leaderboard component
        savedSplitPosition = MIN_LEADERBOARD_WIDTH;
        splitLayoutPanel.insert(leftScrollPanel, leftComponent, Direction.WEST, savedSplitPosition);
        
        // create a panel that will contain the horizontal toggle buttons
        ResizableAbsolutePanel panelForMapAndHorizontalToggleButtons = new ResizableAbsolutePanel();
        panelForMapAndHorizontalToggleButtons.add(rightComponent.getEntryWidget());
        splitLayoutPanel.insert(panelForMapAndHorizontalToggleButtons, rightComponent, Direction.CENTER, 0);
        
        // add additional toggle buttons panel that currently only contains the video button
        List<Pair<Button, Component<?>>> additionalVerticalButtons = new ArrayList<Pair<Button,Component<?>>>();
        additionalVerticalButtons.add(new Pair<Button, Component<?>>(videoControlButton, mediaComponent));
        if (user != null) {
            additionalVerticalButtons.add(new Pair<Button, Component<?>>(mediaComponent.getMediaSelectionButton(), mediaComponent));
        }
        
        // ensure that toggle buttons are positioned right
        splitLayoutPanel.lastComponentHasBeenAdded(this, panelForMapAndHorizontalToggleButtons, additionalVerticalButtons);
    }
    
    /**
     * Create the video control button that shows or hides the video popup
     */
    private Button createVideoControlButton(final MediaPlayerManagerComponent mediaPlayerManagerComponent) {
        final Button videoControlButton = new Button(new SafeHtml() {
            private static final long serialVersionUID = 8679639887708833213L;
            @Override
            public String asString() {
                if (Document.get().getClientWidth() <= 1024) {
                    return "&nbsp;";
                } else {
                    return stringMessages.showVideoPopup();
                }
            }
        });
        videoControlButton.setTitle(stringMessages.showVideoPopup());
        Button closeButton = new Button();
        closeButton.setStyleName("VideoPopup-Close-Button");
        final WindowBox dialog = new WindowBox(stringMessages.videoComponentShortName(), stringMessages.videoComponentShortName(), mediaComponent.getEntryWidget(), null);
        dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                mediaPlayerManagerComponent.setVisible(false);
                    if (Document.get().getClientWidth() > 1024) {
                        videoControlButton.setText(stringMessages.showVideoPopup());
                    }
            }
        });
        videoControlButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!dialog.isShowing()) {
                    if (mediaComponent.isPotentiallyPlayable(mediaComponent.getDefaultVideo())) {
                        mediaComponent.setVisible(true);
                        dialog.setPopupPosition(47, Document.get().getClientHeight()-355);
                        dialog.show();
                        if (Document.get().getClientWidth() > 1024) {
                            videoControlButton.setText(stringMessages.hideVideoPopup());
                        }
                    } else {
                        Window.alert("This race has no default video associated.");
                    }
                } else {
                    mediaComponent.setVisible(false);
                    dialog.hide();
                    if (Document.get().getClientWidth() > 1024) {
                        videoControlButton.setText(stringMessages.showVideoPopup());
                    }
                }
            }
        });
        // hide button initially as we defer showing the button to the asynchroneous
        // task that gets launched by the media service to get video tracks
        videoControlButton.setVisible(false);
        return videoControlButton;
    }
    
    private void initializeComponents() {
        for (final Component<?> component : components) {
            splitLayoutPanel.insert(component.getEntryWidget(), component, Direction.SOUTH, 200);
        }
    }
    
    public <SettingsType> void showSettingsDialog(Component<SettingsType> component) {
        if (component.hasSettings()) {
            new SettingsDialog<SettingsType>(component, stringMessages).show();
        }
    }
    
    public Button getVideoControlButton() {
        return videoControlButton;
    }

    /**
     * Called whenever the layout of {@link TouchSplitLayoutPanel} or other components
     * change. Controls the visibility based on the {@link Component}s visibility.
     * Each {@link Component} is in charge to not display any data or update itself
     * when it is not visible.
     */
    public void forceLayout() {
        if (!leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard is not visible, but the map is
            if (isWidgetInSplitPanel(leftScrollPanel)) {
                if (leftScrollPanel.getOffsetWidth() > 0) {
                    savedSplitPosition = leftScrollPanel.getOffsetWidth();
                }
                splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, /*hidden*/true, savedSplitPosition);
            }
        } else if (leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, /*hidden*/false, savedSplitPosition);
        } else if (!leftComponent.isVisible() && !rightComponent.isVisible()) {
        }

        for (Component<?> component : components) {
            final boolean isComponentVisible = component.isVisible();
            splitLayoutPanel.setWidgetVisibility(component.getEntryWidget(), component, !isComponentVisible, DEFAULT_SOUTH_SPLIT_PANEL_HEIGHT);
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
