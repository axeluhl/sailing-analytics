package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class TaggingPanel extends ComponentWithoutSettings
        implements RaceTimesInfoProviderListener, UserStatusEventHandler {

    /**
     * Describes the current state of the Tagging-Panel.
     */
    protected enum State {
        VIEW, // default
        EDIT
    }

    // styling
    private final TagPanelStyle style;

    // required to display tags
    private final CellList<TagDTO> tagCellList;
    private final SingleSelectionModel<TagDTO> tagSelectionModel;
    private final TagListProvider tagListProvider;

    // custom tag buttons of current user
    private final List<TagButton> tagButtons;

    // UI elements
    private final HeaderPanel taggingPanel;
    private final TagFilterPanel filterbarPanel;
    private final Panel contentPanel;
    private final TagFooterPanel footerPanel;
    private final Button createTagsButton;

    // misc. elements
    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final Timer timer;
    private final RaceTimesInfoProvider raceTimesInfoProvider;

    // race log identifiers
    private String leaderboardName = null;
    private RaceColumnDTO raceColumn = null;
    private FleetDTO fleet = null;

    // current state of the Tagging-Panel
    private State currentState;

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer,
            RaceTimesInfoProvider raceTimesInfoProvider) {
        super(parent, context);

        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.userService = userService;
        this.timer = timer;
        this.raceTimesInfoProvider = raceTimesInfoProvider;

        style = TagPanelResources.INSTANCE.style();
        style.ensureInjected();
        TagCellListResources.INSTANCE.cellListStyle().ensureInjected();

        tagCellList = new CellList<TagDTO>(new TagCell(this, false), TagCellListResources.INSTANCE);
        tagSelectionModel = new SingleSelectionModel<TagDTO>();
        tagListProvider = new TagListProvider();

        tagButtons = new ArrayList<TagButton>();

        taggingPanel = new HeaderPanel();
        footerPanel = new TagFooterPanel(this);
        filterbarPanel = new TagFilterPanel(this);
        contentPanel = new FlowPanel();
        createTagsButton = new Button();

        userService.addUserStatusEventHandler(this);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);

        setCurrentState(State.VIEW);

        initializePanel();
    }

    /**
     * Initializes UI of Tagging-Panel.
     */
    private void initializePanel() {
        taggingPanel.setStyleName(style.taggingPanel());

        // header
        taggingPanel.setHeaderWidget(filterbarPanel);

        // footer
        taggingPanel.setFooterWidget(footerPanel);

        // content (tags)
        tagListProvider.addDataDisplay(tagCellList);
        tagCellList.setEmptyListWidget(new Label(stringMessages.tagNoTagsFound()));

        tagCellList.setSelectionModel(tagSelectionModel);
        tagSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                // set time slider to corresponding position
                timer.setTime(tagSelectionModel.getSelectedObject().getRaceTimepoint().asMillis());
            }
        });

        contentPanel.add(tagCellList);
        contentPanel.addStyleName(style.tagCellListPanel());

        createTagsButton.setTitle(stringMessages.tagAddTags());
        createTagsButton.setStyleName(style.toggleEditState());
        createTagsButton.addStyleName(style.imagePen());
        createTagsButton.addClickHandler(event -> {
            setCurrentState(State.EDIT);
        });
        contentPanel.add(createTagsButton);

        taggingPanel.setContentWidget(contentPanel);
        updateContent();
    }

    /**
     * Updates parameters required to save to/revoke from race log.
     */
    public void updateRace(String leaderboardName, RaceColumnDTO raceColumn, FleetDTO fleet) {
        if (leaderboardName != null && !leaderboardName.equals(this.leaderboardName)) {
            this.leaderboardName = leaderboardName;
        }
        if (fleet != null && !fleet.equals(this.fleet)) {
            this.fleet = fleet;
        }
        if (raceColumn != null && !raceColumn.equals(this.raceColumn)) {
            this.raceColumn = raceColumn;
        }
        loadAllPrivateTags(new AsyncCallback<List<TagDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(List<TagDTO> result) {
                if (result != null) {
                    result.forEach(privateTag -> tagListProvider.getAllTags().add(privateTag));
                }
            }
        });
    }

    /**
     * Sends request to SailingService to add the given tag to the race log if the parameter
     * <code>isVisibleForPublic</code> is set to true. Otherwise tag will be stored in the user storage.
     */
    protected void saveTag(String tag, String comment, String imageURL, boolean isVisibleForPublic) {
        if (!isLoggedInAndRaceLogAvailable()) {
            // User is not logged in or race can not be identifed because regatta, race column or fleet are missing.
            Notification.notify(stringMessages.tagNotAdded(), NotificationType.ERROR);

        } else if (tag.isEmpty()) {
            // Tag heading is empty. Empty tags are not allowed.
            Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);

        } else if (isVisibleForPublic) {
            // public tags are saved to race log
            sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag, comment,
                    imageURL, new MillisecondsTimePoint(timer.getTime()), new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.tagNotAddedReason(caught.toString()),
                                    NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(SuccessInfo result) {
                            if (result.isSuccessful()) {
                                Notification.notify(stringMessages.tagAddedSuccessfully(), NotificationType.INFO);
                            } else {
                                Notification.notify(stringMessages.tagNotAddedReason(result.getMessage()),
                                        NotificationType.ERROR);
                            }
                        }
                    });
        } else {
            // private tags are saved to user storage
            loadAllPrivateTags(new AsyncCallback<List<TagDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    // TODO: Add error handling
                }

                @Override
                public void onSuccess(List<TagDTO> loadedPrivateTags) {
                    removePrivateTagsFromProvider();
                    addTagsToProvider(loadedPrivateTags);

                    TagDTO newTag = new TagDTO(tag, comment, imageURL, userService.getCurrentUser().getName(), false,
                            new MillisecondsTimePoint(getTimerTime()), MillisecondsTimePoint.now());
                    loadedPrivateTags.add(newTag);

                    // store list of loaded private tags also containing the new tag
                    TagDTOJsonDeSerializer serializer = new TagDTOJsonDeSerializer();
                    JSONObject jsonObject = serializer.serialize(loadedPrivateTags);
                    userService.setPreference(serializer.createIdenticalKeyFromThreeStrings(leaderboardName,
                            raceColumn.getName(), fleet.getName()), jsonObject.toString(), new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Notification.notify(stringMessages.tagButtonNotSavable(), NotificationType.WARNING);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    Notification.notify(stringMessages.tagAddedSuccessfully(), NotificationType.INFO);
                                    tagListProvider.getAllTags().add(newTag);
                                    updateContent();
                                }
                            });
                }
            });
        }

    }

    /**
     * If the parameter <code>tag</code> is visible for the public a request to the SailingService is sent to revoke the
     * given tag in race log. Otherwise the parameter <code>tag</code> is private and so the tag must be removed from
     * the user storage.
     */
    protected void removeTag(TagDTO tagToRemove) {
        if (tagToRemove.isVisibleForPublic()) {
            // remove public tag from race log
            sailingService.removeTagFromRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tagToRemove,
                    new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.tagNotRemoved(), NotificationType.ERROR);
                            GWT.log(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(SuccessInfo result) {
                            if (result.isSuccessful()) {
                                tagListProvider.getAllTags().remove(tagToRemove);
                                updateContent();
                                Notification.notify(stringMessages.tagRemovedSuccessfully(), NotificationType.SUCCESS);
                            } else {
                                Notification.notify(stringMessages.tagNotRemoved() + " " + result.getMessage(),
                                        NotificationType.ERROR);
                            }
                        }
                    });
        } else {
            // remove private tag from user storage
            loadAllPrivateTags(new AsyncCallback<List<TagDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                }

                @Override
                public void onSuccess(List<TagDTO> privateTags) {
                    removePrivateTagsFromProvider();
                    privateTags.remove(tagToRemove);
                    addTagsToProvider(privateTags);

                    // store list in user storage
                    TagDTOJsonDeSerializer serializer = new TagDTOJsonDeSerializer();
                    JSONObject jsonObject = serializer.serialize(privateTags);
                    userService.setPreference(serializer.createIdenticalKeyFromThreeStrings(leaderboardName,
                            raceColumn.getName(), fleet.getName()), jsonObject.toString(), new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Notification.notify(stringMessages.tagNotRemoved(), NotificationType.ERROR);
                                    GWT.log(caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    removePrivateTagsFromProvider();
                                    addTagsToProvider(privateTags);
                                    updateContent();
                                    Notification.notify(stringMessages.tagRemovedSuccessfully(),
                                            NotificationType.SUCCESS);
                                }
                            });
                }
            });
        }
    }

    private void removePrivateTagsFromProvider() {
        tagListProvider.getAllTags().removeIf(tag -> !tag.isVisibleForPublic());
    }

    private void addTagsToProvider(List<TagDTO> tags) {
        tags.forEach(tag -> tagListProvider.getAllTags().add(tag));
    }

    /**
     * Checks if currentUser (read from UserService) is non-<code>null</code> and if leaderboardName, fleet and
     * raceColumn are not <code>null</code>.
     * 
     * @return true if current user, leaderboardName, raceColumn and fleet are non-<code>null</code>, otherwise false.
     */
    protected boolean isLoggedInAndRaceLogAvailable() {
        return userService.getCurrentUser() != null && leaderboardName != null && raceColumn != null && fleet != null;
    }

    /**
     * Controls the visibility of UI elements in case the content or current state changes.
     */
    protected void updateContent() {
        setFooterPanelVisibility(currentState == State.EDIT);
        setCreateTagsButtonVisibility(currentState == State.VIEW);
        tagListProvider.updateFilteredTags();
        tagCellList.setVisibleRange(0, tagListProvider.getFilteredTagsListSize());
        tagListProvider.refresh();
    }

    /**
     * Forces tagging panel to rerender content.
     */
    protected void refreshContentPanel() {
        taggingPanel.setContentWidget(contentPanel);
    }
    
    /**
     * Forces tagging panel to rerender footer.
     */
    protected void refreshFooterPanel() {
        taggingPanel.setFooterWidget(footerPanel);
    }

    protected Date getTimerTime() {
        return timer.getTime();
    }

    protected List<TagButton> getTagButtons() {
        return tagButtons;
    }

    protected void addTagButton(TagButton tagButton) {
        tagButton.addClickHandler(event -> {
            saveTag(tagButton.getTag(), tagButton.getComment(), tagButton.getImageURL(),
                    tagButton.isVisibleForPublic());
        });
        tagButtons.add(tagButton);
    }

    protected TagListProvider getTagListProvider() {
        return tagListProvider;
    }

    protected UserService getUserSerivce() {
        return userService;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    private void loadAllPrivateTags(AsyncCallback<List<TagDTO>> callback) {
        // only reload tags if user is logged in
        if (userService.getCurrentUser() != null) {
            TagDTOJsonDeSerializer tagDTODeSerializer = new TagDTOJsonDeSerializer();

            // load all private tags from user storage
            userService.getPreference(tagDTODeSerializer.createIdenticalKeyFromThreeStrings(leaderboardName,
                    raceColumn.getName(), fleet.getName()), new AsyncCallback<String>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            // TODO: Add error handling
                        }

                        @Override
                        public void onSuccess(String result) {
                            if (result != null && !result.isEmpty()) {
                                // parse and deserialize String result into List of private tags
                                final JSONValue value = JSONParser.parseStrict(result);
                                if (value.isObject() != null) {
                                    callback.onSuccess(tagDTODeSerializer.deserialize((JSONObject) value));
                                }
                            } else {
                                // no rpivate tags received from server, send empty list as callback parameter.
                                callback.onSuccess(new ArrayList<TagDTO>());
                            }
                        }
                    });
        } else {
            // should not happen
            callback.onFailure(null);
        }
    }

    /**
     * Sets the current state and updates the UI.
     * 
     * @param state
     *            new state
     */
    protected void setCurrentState(State state) {
        currentState = state;
        updateContent();
    }

    /**
     * Updates the visibility of the footer and it's components. Input fields will NOT get displayed if user is not
     * logged in, even if showInputFields is set to true! Tag buttons can't be hidden and will get displayed
     * automatically when user is logged in.
     */
    private void setFooterPanelVisibility(boolean showInputFields) {
        // Setting footerPanel.setVisible(false) is not sufficient as panel would still be
        // rendered as 20px high white space instead of being hidden.
        // Fix: remove panel completely from footer.
        if (userService.getCurrentUser() != null && (currentState == State.EDIT || getTagButtons().size() > 0)) {
            taggingPanel.setFooterWidget(footerPanel);
            footerPanel.setInputFieldsVisibility(showInputFields);
            footerPanel.setTagButtonsVisibility(true);
        } else {
            taggingPanel.setFooterWidget(null);
        }
    }

    /**
     * Updates the visibility of the "Add Tags"-button. Button will NOT get displayed if user is not logged in, even if
     * showButton is set to true!
     */
    private void setCreateTagsButtonVisibility(boolean showButton) {
        createTagsButton.setVisible(userService.getCurrentUser() != null && showButton);
    }

    /**
     * Update local list of tags when response of server gets dispatched to all listeners by RaceTimesInfoProvider.
     * Server sends only difference of tags in comparison based on the createdAt-timestamp of the latest received tag
     * event.
     */

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        if (raceTimesInfo != null) {
            raceTimesInfo.forEach((raceIdentifier, raceInfo) -> {
                // Will be true if local list of tags get modified with new received tags, otherwise false.
                boolean modifiedTags = false;
                // Will be true if latestReceivedTagTime needs to be updated in raceTimesInfoprovider, otherwise false.
                boolean updatedLatestTag = false;
                // local list of already received tags
                List<TagDTO> currentTags = tagListProvider.getAllTags();
                // createdAt or revokedAt timepoint of latest received tag
                TimePoint latestReceivedTagTime = raceTimesInfoProvider.getLatestReceivedTagTime(raceIdentifier);
                // get difference in tags since latestReceivedTagTime
                if (raceInfo.getTags() != null) {
                    for (TagDTO tag : raceInfo.getTags()) {
                        if (tag.getRevokedAt() != null) {
                            // received tag is revoked => latestReceivedTagTime will be revokedAt if revoke event
                            // occured before latestReceivedTagTime
                            currentTags.remove(tag);
                            modifiedTags = true;
                            if (latestReceivedTagTime == null || (latestReceivedTagTime != null
                                    && latestReceivedTagTime.before(tag.getRevokedAt()))) {
                                latestReceivedTagTime = tag.getRevokedAt();
                                updatedLatestTag = true;
                            }
                        } else if (!currentTags.contains(tag)) {
                            // received tag is NOT revoked => latestReceivedTagTime will be createdAt if tag event
                            // occured before latestReceivedTagTime
                            currentTags.add(tag);
                            modifiedTags = true;
                            if (latestReceivedTagTime == null || (latestReceivedTagTime != null
                                    && latestReceivedTagTime.before(tag.getCreatedAt()))) {
                                latestReceivedTagTime = tag.getCreatedAt();
                                updatedLatestTag = true;
                            }
                        }
                    }
                }
                // set new latestReceivedTagTime for next data request
                if (updatedLatestTag) {
                    raceTimesInfoProvider.setLatestReceivedTagTime(raceIdentifier, latestReceivedTagTime);
                }
                // refresh UI if tags did change
                if (modifiedTags) {
                    updateContent();
                }
            });
        }
    }

    /**
     * When the user logs in or out the content needs to be reset to hide private tags of the previous user. This gets
     * achieved by resetting the local list of tags and resetting all latest received tag times at the
     * RaceTimesInfoProvider.
     */
    @Override
    public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
        // clear list of local tags to hide private tags of previous user
        tagListProvider.getAllTags().clear();
        raceTimesInfoProvider.getRaceIdentifiers().forEach((raceIdentifier) -> {
            raceTimesInfoProvider.setLatestReceivedTagTime(raceIdentifier, null);
        });
        loadAllPrivateTags(new AsyncCallback<List<TagDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO: add error handling
            }

            @Override
            public void onSuccess(List<TagDTO> result) {
                addTagsToProvider(result);
            }
        });
        filterbarPanel.loadTagFilterSets();
        footerPanel.loadAllTagButtons();
        setCurrentState(State.VIEW);
        updateContent();
    }

    @Override
    public String getId() {
        return "TaggingPanel";
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.tagPanel();
    }

    @Override
    public Widget getEntryWidget() {
        return taggingPanel;
    }

    @Override
    public boolean isVisible() {
        return taggingPanel.isVisible();
    }

    /**
     * Only request tags from server if Tagging-Panel is visible.
     */
    @Override
    public void setVisible(boolean visible) {
        if (raceTimesInfoProvider != null) {
            if (visible) {
                raceTimesInfoProvider.enableTagRequests();
            } else {
                raceTimesInfoProvider.disableTagRequests();
            }
        }
        taggingPanel.setVisible(visible);
    }

    @Override
    public String getDependentCssClassName() {
        return "tags";
    }
}