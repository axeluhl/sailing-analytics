package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaManagementControl;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManager;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManager.PlayerChangeListener;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerManagerComponent;
import com.sap.sailing.gwt.ui.client.media.MediaSingleSelectionControl;
import com.sap.sailing.gwt.ui.client.shared.charts.EditMarkPassingsPanel;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.ComponentViewer;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.raceboard.TouchSplitLayoutPanel.Splitter;
import com.sap.sailing.gwt.ui.usermanagement.UserRoles;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Component Viewer that uses a {@link TouchSplitLayoutPanel} to display its components.
 * 
 * TODO: Refactor to make sure it is really only performing operations that are related to view. Currently it is digging
 * too deep into components and setting titles or even creating video buttons.
 */
public class SideBySideComponentViewer implements ComponentViewer, UserStatusEventHandler {

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
                    ((RequiresResize) widget).onResize();
                }
            }
        }
    }

    private final LeaderboardPanel leftComponent;
    private final Component<?> rightComponent;
    private final List<Component<?>> components;
    private final ScrollPanel leftScrollPanel;
    private final StringMessages stringMessages;
    private final Button mediaSelectionButton;
    private final Button mediaManagementButton;
    private final EditMarkPassingsPanel markPassingsPanel;

    private LayoutPanel mainPanel;

    private TouchSplitLayoutPanel splitLayoutPanel;
    private int savedSplitPosition = -1;
    private boolean layoutForLeftComponentForcedOnce = false;

    public SideBySideComponentViewer(final LeaderboardPanel leftComponentP, final Component<?> rightComponentP,
            final MediaPlayerManagerComponent mediaPlayerManagerComponent, List<Component<?>> components,
            final StringMessages stringMessages, UserService userService, EditMarkPassingsPanel markPassingsPanel) {
        this.stringMessages = stringMessages;
        this.leftComponent = leftComponentP;
        this.rightComponent = rightComponentP;
        this.components = components;
        this.mediaSelectionButton = createMediaSelectionButton(mediaPlayerManagerComponent);
        this.mediaManagementButton = createMediaManagementButton(mediaPlayerManagerComponent);
        this.markPassingsPanel = markPassingsPanel;
        
        userService.addUserStatusEventHandler(this);

        mediaPlayerManagerComponent.setPlayerChangeListener(new PlayerChangeListener() {

            public void notifyStateChange(){
                String caption;
                String tooltip;
                switch (mediaPlayerManagerComponent.getAssignedMediaTracks().size()) {
                case 0:
                    caption = "No Videos";
                    tooltip = caption;
                    mediaSelectionButton.setVisible(false);
                    break;
                case 1:
                    mediaSelectionButton.setVisible(true);
                    if(mediaPlayerManagerComponent.isPlaying()){
                        caption = stringMessages.mediaHideVideoCaption();
                        tooltip = stringMessages.mediaHideVideoTooltip();
                    }
                    else{
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
                if (Document.get().getClientWidth() <= 1024) {
                    mediaSelectionButton.setHTML("&nbsp;");
                } else {
                    mediaSelectionButton.setText(caption);
                }
                mediaSelectionButton.setTitle(tooltip);
                mediaManagementButton.setVisible(mediaPlayerManagerComponent.allowsEditing());
            }

        });
        this.leftScrollPanel = new ScrollPanel();
        this.leftScrollPanel.add(leftComponentP.getEntryWidget());
        this.leftScrollPanel.setTitle(leftComponentP.getEntryWidget().getTitle());
        this.mainPanel = new LayoutPanel();
        this.mainPanel.setSize("100%", "100%");
        this.mainPanel.getElement().getStyle().setMarginTop(-12, Unit.PX);
        this.mainPanel.setStyleName("SideBySideComponentViewer-MainPanel");
        this.splitLayoutPanel = new TouchSplitLayoutPanel(/* horizontal splitter width */3, /* vertical splitter height */
                25);
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
        List<Pair<Button, String>> additionalVerticalButtons = new ArrayList<Pair<Button, String>>();
        additionalVerticalButtons.add(new Pair<Button, String>(mediaSelectionButton,
                mediaPlayerManagerComponent.getDependentCssClassName()));
        if (/* TODO check for correct role; was: user != null */ true) {
            additionalVerticalButtons.add(new Pair<Button, String>(mediaManagementButton,
                    "managemedia"));
        }

        // ensure that toggle buttons are positioned right
        splitLayoutPanel.lastComponentHasBeenAdded(this, panelForMapAndHorizontalToggleButtons,
                additionalVerticalButtons);
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

        // hide button initially as we defer showing the button to the asynchroneous
        // task that gets launched by the media service to get video tracks
        result.setVisible(false);
        return result;
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

    /**
     * Called whenever the layout of {@link TouchSplitLayoutPanel} or other components change. Controls the visibility
     * based on the {@link Component}s visibility. Each {@link Component} is in charge to not display any data or update
     * itself when it is not visible.
     */
    public void forceLayout() {
        if (!leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard is not visible, but the map is
            if (isWidgetInSplitPanel(leftScrollPanel)) {
                if (leftScrollPanel.getOffsetWidth() > 0) {
                    savedSplitPosition = leftScrollPanel.getOffsetWidth();
                }
                splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, /* hidden */true,
                        savedSplitPosition);
            }
        } else if (leftComponent.isVisible() && rightComponent.isVisible()) {
            // the leaderboard and the map are visible
            splitLayoutPanel.setWidgetVisibility(leftScrollPanel, leftComponent, /* hidden */false, savedSplitPosition);
        } else if (!leftComponent.isVisible() && !rightComponent.isVisible()) {
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

    public void setLeftComponentWidth(int width) {
        // TODO: The information provided by width is wrong
        // need to find a way to get the correct information
        if (!layoutForLeftComponentForcedOnce) {
            savedSplitPosition = MIN_LEADERBOARD_WIDTH;
            forceLayout();
        }
        layoutForLeftComponentForcedOnce = true;
    }

    @Override
    public void onUserStatusChange(UserDTO user) {
        if (user == null || !(user.hasRole(UserRoles.administrator.getRolename()) || user.hasRole(UserRoles.eventmanager.getRolename()))) {
            Splitter associatedSplitter = splitLayoutPanel.getAssociatedSplitter(markPassingsPanel);
            associatedSplitter.getToggleButtonsPanel().remove(associatedSplitter.getToggleButton());
        }
    }
}
