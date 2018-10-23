package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaManagementControl;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManager;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManager.PlayerChangeListener;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManagerComponent;
import com.sap.sailing.gwt.ui.client.media.MediaSingleSelectionControl;
import com.sap.sailing.gwt.ui.client.shared.charts.EditMarkPassingsPanel;
import com.sap.sailing.gwt.ui.client.shared.charts.EditMarkPositionPanel;
import com.sap.sailing.gwt.ui.client.shared.racemap.maneuver.ManeuverTablePanel;
import com.sap.sailing.gwt.ui.raceboard.TouchSplitLayoutPanel.Splitter;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Component Viewer that uses a {@link TouchSplitLayoutPanel} to display its components.
 * 
 * TODO: Refactor to make sure it is really only performing operations that are related to view. Currently it is digging
 * too deep into components and setting titles or even creating video buttons.
 */
public class SideBySideComponentViewer implements UserStatusEventHandler {

    private static final int DEFAULT_SOUTH_SPLIT_PANEL_HEIGHT = 200;
    private final int MIN_LEADERBOARD_WIDTH = Math.min(432, Window.getClientWidth() - 40); // fallback value "432" works well for 505 and ESS
    private final int MIN_TAGGING_WIDTH = Math.min(440, Window.getClientWidth() - 40);

    /**
     * Absolute Panel that informs its children about a resize
     */
    class ResizableAbsolutePanel extends AbsolutePanel implements RequiresResize {
        public void onResize() {
            WidgetCollection children = getChildren();
            for (Widget widget : children) {
                if (widget instanceof RequiresResize) {
                    ((RequiresResize) widget).onResize();
                }
            }
        }
    }

    private Component<?> leftComponent;
    private final Component<?> centerComponent;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    private final ScrollPanel leftScrollPanel;
    private final Panel rightPanel;
    private final StringMessages stringMessages;
    private final Button mediaSelectionButton;
    private final Button mediaManagementButton;
    private final EditMarkPassingsPanel markPassingsPanel;
    private final EditMarkPositionPanel markPositionPanel;
    private final MediaPlayerManagerComponent mediaPlayerManagerComponent;

    private LayoutPanel mainPanel;

    private TouchSplitLayoutPanel splitLayoutPanel;
    private final UserService userService;

    public SideBySideComponentViewer(final Component<?> leftComponentP, final Component<?> centerComponentP,
            final Component<?> rightComponentP, final MediaPlayerManagerComponent mediaPlayerManagerComponent,
            List<Component<?>> components, final StringMessages stringMessages, UserService userService,
            EditMarkPassingsPanel markPassingsPanel, EditMarkPositionPanel markPositionPanel,
            ManeuverTablePanel maneuverTablePanel) {
        this.mediaPlayerManagerComponent = mediaPlayerManagerComponent;
        this.stringMessages = stringMessages;
        this.leftComponent = leftComponentP;
        this.centerComponent = centerComponentP;
        this.rightComponent = rightComponentP;
        this.components = components;
        this.userService = userService;
        this.mediaSelectionButton = createMediaSelectionButton(mediaPlayerManagerComponent);
        this.mediaManagementButton = createMediaManagementButton(mediaPlayerManagerComponent);
        this.markPassingsPanel = markPassingsPanel;
        this.markPositionPanel = markPositionPanel;
        markPositionPanel.setComponentViewer(this);
        mediaPlayerManagerComponent.addPlayerChangeListener(new PlayerChangeListener() {
            public void notifyStateChange() {
                String caption;
                String tooltip;
                switch (mediaPlayerManagerComponent.getAssignedMediaTracks().size()) {
                case 0:
                    caption = stringMessages.mediaNoVideosCaption();
                    tooltip = caption;
                    mediaSelectionButton.setVisible(false);
                    break;
                case 1:
                    mediaSelectionButton.setVisible(true);
                    if (mediaPlayerManagerComponent.isPlaying()) {
                        caption = stringMessages.mediaHideVideoCaption();
                        tooltip = stringMessages.mediaHideVideoTooltip();
                    } else {
                        caption = stringMessages.mediaShowVideoCaption();
                        tooltip = stringMessages.mediaShowVideoTooltip(mediaPlayerManagerComponent.getAssignedMediaTracks().iterator().next().title);
                    }
                    break;
                default:
                    mediaSelectionButton.setVisible(true);
                    caption = stringMessages.mediaSelectVideoCaption(mediaPlayerManagerComponent.getAssignedMediaTracks().size());
                    tooltip = stringMessages.mediaSelectVideoTooltip();
                    break;
                }
                if (mediaPlayerManagerComponent.isPlaying()) {
                    mediaSelectionButton.addStyleDependentName("mediaplaying");
                } else {
                    mediaSelectionButton.removeStyleDependentName("mediaplaying");
                }
                mediaSelectionButton.setText(caption);
                mediaSelectionButton.setTitle(tooltip);
                mediaManagementButton.setVisible(
                        mediaPlayerManagerComponent.getAssignedMediaTracks().stream().anyMatch(
                                track->mediaPlayerManagerComponent.allowsEditing(track.dbId)));
            }
        });
        this.leftScrollPanel = new ScrollPanel();
        this.leftScrollPanel.add(leftComponentP.getEntryWidget());
        this.leftScrollPanel.setTitle(leftComponentP.getEntryWidget().getTitle());
        // Right panel needs to implement interface ProvidesResize as the only child 
        // is a HeaderPanel which implements RequiresResize.
        this.rightPanel = new SimpleLayoutPanel();
        this.rightPanel.add(rightComponentP.getEntryWidget());
        this.rightPanel.setTitle(rightComponentP.getEntryWidget().getTitle());
        this.mainPanel = new LayoutPanel();
        this.mainPanel.setSize("100%", "100%");
        this.mainPanel.getElement().getStyle().setMarginTop(-12, Unit.PX);
        this.mainPanel.setStyleName("SideBySideComponentViewer-MainPanel");
        this.splitLayoutPanel = new TouchSplitLayoutPanel(/* horizontal splitter width */3, /* vertical splitter height */ 25);
        this.mainPanel.add(splitLayoutPanel);

        // initialize components - they need to be added before other widgets to get the right width
        initializeComponents();

        // initialize the leaderboard component
        splitLayoutPanel.insert(leftScrollPanel, leftComponent, Direction.WEST, MIN_LEADERBOARD_WIDTH);
        
        // initialize the tagging component
        splitLayoutPanel.insert(rightPanel, rightComponent, Direction.EAST, MIN_TAGGING_WIDTH);

        // create a panel that will contain the horizontal toggle buttons
        ResizableAbsolutePanel panelForMapAndHorizontalToggleButtons = new ResizableAbsolutePanel();
        panelForMapAndHorizontalToggleButtons.add(centerComponent.getEntryWidget());
        splitLayoutPanel.insert(panelForMapAndHorizontalToggleButtons, centerComponent, Direction.CENTER, 0);

        // add additional toggle buttons panel that currently only contains the video button
        List<Pair<Button, String>> additionalVerticalButtons = new ArrayList<Pair<Button, String>>();
        additionalVerticalButtons.add(new Pair<Button, String>(mediaSelectionButton,
                mediaPlayerManagerComponent.getDependentCssClassName()));
            additionalVerticalButtons.add(new Pair<Button, String>(mediaManagementButton,
                    "managemedia"));
        userService.addUserStatusEventHandler(this, true);
        // ensure that toggle buttons are positioned right
        splitLayoutPanel.lastComponentHasBeenAdded(this, panelForMapAndHorizontalToggleButtons,
                additionalVerticalButtons);
    }
    
    public void setLeftComponent(Component<?> component) {
        leftComponent = component;
        leftScrollPanel.setWidget(leftComponent.getEntryWidget());
        leftScrollPanel.setTitle(leftComponent.getEntryWidget().getTitle());
    }

    /**
     * Create the video control button that shows or hides the video popup
     * 
     * @param userAgent
     */
    private Button createMediaSelectionButton(final MediaPlayerManager mediaPlayerManager) {
        final Button result = new Button();
        final MediaSingleSelectionControl multiSelectionControl = new MediaSingleSelectionControl(mediaPlayerManager,
                result, stringMessages);
        result.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (mediaPlayerManager.getAssignedMediaTracks().size() == 1) {
                    if (mediaPlayerManager.isPlaying()) {
                        mediaPlayerManager.stopAll();
                    } else {
                        mediaPlayerManager.playDefault();
    
                    }
                } else {
                    multiSelectionControl.show();
                }
            }
        });

        // hide button initially as we defer showing the button to the asynchroneous
        // task that gets launched by the media service to get video tracks
        result.setVisible(false);
        return result;
    }

    /**
     * Create the video control button that shows or hides the video popup
     * 
     * @param userAgent
     */
    private Button createMediaManagementButton(final MediaPlayerManager mediaPlayerManager) {
        final Button result = new Button(stringMessages.mediaManageMediaCaption());
        result.setTitle(stringMessages.mediaManageMediaTooltip());
        // onClick
        final MediaManagementControl multiSelectionControl = new MediaManagementControl(mediaPlayerManager,
                result, stringMessages);
        result.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                multiSelectionControl.show();
            }
        });
        // hide button initially as we defer showing the button to the asynchronous
        // task that gets launched by the media service to get video tracks
        result.setVisible(false);
        return result;
    }

    private void initializeComponents() {
        for (final Component<?> component : components) {
            splitLayoutPanel.insert(component.getEntryWidget(), component, Direction.SOUTH, 200);
        }
    }

    public <SettingsType extends AbstractSettings> void showSettingsDialog(Component<SettingsType> component) {
        if (component.hasSettings()) {
            new SettingsDialog<SettingsType>(component, stringMessages).show();
        }
    }

    /**
     * Called whenever the layout of {@link TouchSplitLayoutPanel} or other components change. Controls the visibility
     * based on the {@link Component}s visibility. Each {@link Component} is in charge to not display any data or update
     * itself when it is not visible.
     */
    public void forceLayout() {
        if (!leftComponent.isVisible() && centerComponent.isVisible()) {
            // the leaderboard is not visible, but the map is
            if (isWidgetInSplitPanel(leftScrollPanel)) {
                splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, /* hidden */true,
                        MIN_LEADERBOARD_WIDTH);
            }
        } else if (leftComponent.isVisible() && centerComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, /* hidden */false, MIN_LEADERBOARD_WIDTH);
        } else if (!leftComponent.isVisible() && !centerComponent.isVisible()) {
        }
        
        if (!rightComponent.isVisible() && centerComponent.isVisible()) {
            // the tagging is not visible, but the map is
            if (isWidgetInSplitPanel(rightPanel)) {
                splitLayoutPanel.setWidgetVisibility(rightPanel, rightComponent, /* hidden */true,
                        MIN_TAGGING_WIDTH);
            }
        } else if (rightComponent.isVisible() && centerComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibility(rightPanel, rightComponent, /* hidden */false, MIN_TAGGING_WIDTH);
        } else if (!rightComponent.isVisible() && !centerComponent.isVisible()) {
        }

        for (Component<?> component : components) {
            final boolean isComponentVisible = component.isVisible();
            splitLayoutPanel.setWidgetVisibility(component.getEntryWidget(), component, !isComponentVisible,
                    DEFAULT_SOUTH_SPLIT_PANEL_HEIGHT);
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

    @Override
    public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
        final Splitter markPassingsSplitter = splitLayoutPanel.getAssociatedSplitter(markPassingsPanel);
        final Splitter markPositionSplitter = splitLayoutPanel.getAssociatedSplitter(markPositionPanel);
        boolean forceLayout = false;
        if (userService.hasPermission(SecuredDomainType.MANAGE_MARK_PASSINGS.getPermission(), /* TODO race ownership */ null, /* TODO race acl */ null)) {
            if (markPassingsSplitter != null) { // if the panel is not present, the splitter may not be found
                markPassingsSplitter.getToggleButton().setVisible(true);
            }
            forceLayout = true;
        } else {
            if (markPassingsSplitter != null) { // if the panel is not present, the splitter may not be found
                markPassingsPanel.setVisible(false);
                markPassingsSplitter.getToggleButton().setVisible(false);
            }
        }
        if (userService.hasPermission(SecuredDomainType.MANAGE_MARK_POSITIONS.getPermission(), /* TODO race ownership */ null, /* TODO race acl */ null)) {
            if (markPositionSplitter != null) { // if the panel is not present, the splitter may not be found
                markPositionSplitter.getToggleButton().setVisible(true);
            }
            forceLayout = true;
        } else {
            if (markPositionSplitter != null) { // if the panel is not present, the splitter may not be found
                markPositionPanel.setVisible(false);
                markPositionSplitter.getToggleButton().setVisible(false);
            }
            forceLayout();
        }
        if (forceLayout) {
            forceLayout();
        }
        mediaManagementButton.setVisible(mediaPlayerManagerComponent.getAssignedMediaTracks().stream().anyMatch(
                track->mediaPlayerManagerComponent.allowsEditing(track.dbId)));
    }
    
    /**
     * Shows/hides the text on left components toggle button by modifying CSS <code>font-size</code> property and adjust
     * the dragger position by modifying CSS <code>margin-top</code> property. 
     * 
     * @param visible
     *            <code>true</code> to show the button text, <code>false</code> to hide it
     */
    void setLeftComponentToggleButtonTextVisibilityAndDraggerPosition(final boolean visible) {
        Splitter leftScrollPanelSplitter = splitLayoutPanel.getAssociatedSplitter(leftScrollPanel);
        if (leftScrollPanelSplitter != null) {
            Style toggleButtonStyle = leftScrollPanelSplitter.getToggleButton().getElement().getStyle();
            if (visible) toggleButtonStyle.clearFontSize();
            else toggleButtonStyle.setFontSize(0, Unit.PX);
            Style drapperStyle = leftScrollPanelSplitter.getDragger().getElement().getStyle();
            if (visible) drapperStyle.clearMarginTop();
            else drapperStyle.setMarginTop(-25, Unit.PX);
        }
    }
    
    /**
     * Shows/hides the text on right components toggle button by modifying CSS <code>font-size</code> property and adjust
     * the dragger position by modifying CSS <code>margin-top</code> property. 
     * 
     * @param visible
     *            <code>true</code> to show the button text, <code>false</code> to hide it
     */
    void setRightComponentToggleButtonTextVisibilityAndDraggerPosition(final boolean visible) {
        Splitter rightFlowPanelSplitter = splitLayoutPanel.getAssociatedSplitter(rightPanel);
        if (rightFlowPanelSplitter != null) {
            Style toggleButtonStyle = rightFlowPanelSplitter.getToggleButton().getElement().getStyle();
            if (visible) toggleButtonStyle.clearFontSize();
            else toggleButtonStyle.setFontSize(0, Unit.PX);
            Style drapperStyle = rightFlowPanelSplitter.getDragger().getElement().getStyle();
            if (visible) drapperStyle.clearMarginTop();
            else drapperStyle.setMarginTop(-25, Unit.PX);
        }
    }
    
    public void setLeftComponentToggleButtonVisible(boolean visible) {
        Splitter leftScrollPanelSplitter = splitLayoutPanel.getAssociatedSplitter(leftScrollPanel);
        if (leftScrollPanelSplitter != null) {
            leftScrollPanelSplitter.getToggleButton().setVisible(visible);
        }
    }
    
    public void setRightComponentToggleButtonVisible(boolean visible) {
        Splitter rightPanelSplitter = splitLayoutPanel.getAssociatedSplitter(rightPanel);
        if (rightPanelSplitter != null) {
            rightPanelSplitter.getToggleButton().setVisible(visible);
        }
    }
    
    public ScrollPanel getLeftScrollPanel() {
        return leftScrollPanel;
    }
    
    public Panel getRightPanel() {
        return rightPanel;
    }
}