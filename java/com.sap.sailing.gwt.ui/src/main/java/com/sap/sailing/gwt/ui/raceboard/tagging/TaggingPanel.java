package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.ImageResizingTaskDTO;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * A view showing tags which are connected to a specific race and allowing users to add own tags to a race. This view is
 * shown at the {@link com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel RaceBoard}. Tags consist of a title and optional
 * a comment and/or image. Tag-Buttons allow users to preset tags which are used more frequently. Public tags will be
 * stored as an {@link com.sap.sailing.domain.abstractlog.race.RaceLogEvent RaceLogEvent}, private tags will be stored
 * in the {@link com.sap.sse.security.UserStore UserStore}.
 * 
 * @author Julian Rendl, Henri Kohlberg
 */
public class TaggingPanel extends ComponentWithoutSettings
        implements RaceTimesInfoProviderListener, UserStatusEventHandler, TimeListener {

    /**
     * Describes the {@link TaggingPanel#currentState current state} of the {@link TaggingPanel}.
     */
    protected enum State {
        VIEW, // default
        CREATE_TAG,
        EDIT_TAG
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

    // Needed for sharing Tags
    private boolean tagHasNotBeenHighlightedYet = true;
    protected final TimePoint timePointToHighlight;
    protected final String tagToHighlight;

    /**
     * This boolean prevents the timer from jumping when other users create or delete tags. The timer change event is
     * called by the selection change event and the other way around. When:<br/>
     * 1) the timer is in <i>PLAY</i> mode and<br/>
     * 2) the current timer position is set after the last received tag and<br/>
     * 3) another user adds/deletes/changes any tag between the latest received tag and the current timer position<br/>
     * consecutively, the timer would jump to this new tag as the selection would change automatically as the latest tag
     * changed. This selection change would also trigger the timer to jump to the latest tag, which is not intended in
     * this case. Therefor any received changes on any tags will set this boolen to true which will ignore the time jump
     * at the selection change event and prevent this wrong behaviour.
     * 
     * @see #raceTimesInfosReceived(Map, long, Date, long)
     */
    private boolean preventTimeJumpAtSelectionChangeForOnce = false;

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer,
            RaceTimesInfoProvider raceTimesInfoProvider, TimePoint timePointToHighlight, String tagToHighlight) {
        super(parent, context);

        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.userService = userService;
        this.timer = timer;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.timePointToHighlight = timePointToHighlight;
        this.tagToHighlight = tagToHighlight;

        style = TaggingPanelResources.INSTANCE.style();
        style.ensureInjected();
        TaggingPanelResources.INSTANCE.cellListStyle().ensureInjected();
        TaggingPanelResources.INSTANCE.cellTableStyle().ensureInjected();

        tagCellList = new CellList<TagDTO>(new TagCell(this, stringMessages, userService, false),
                TaggingPanelResources.INSTANCE);
        tagSelectionModel = new SingleSelectionModel<TagDTO>();
        tagListProvider = new TagListProvider();

        tagButtons = new ArrayList<TagButton>();

        taggingPanel = new HeaderPanel();
        footerPanel = new TagFooterPanel(this, sailingService, stringMessages, userService);
        filterbarPanel = new TagFilterPanel(this, stringMessages, userService);
        contentPanel = new FlowPanel();
        createTagsButton = new Button();

        userService.addUserStatusEventHandler(this);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);

        setCurrentState(State.VIEW);
        initializePanel();
    }

    /**
     * Initializes UI of {@link TaggingPanel}.
     */
    private void initializePanel() {
        taggingPanel.setStyleName(style.taggingPanel());

        // header
        taggingPanel.setHeaderWidget(filterbarPanel);

        // footer
        taggingPanel.setFooterWidget(footerPanel);

        // content (tags)
        contentPanel.addStyleName(style.tagCellListPanel());
        contentPanel.add(tagCellList);
        contentPanel.add(createTagsButton);
        tagListProvider.addDataDisplay(tagCellList);
        tagCellList.setEmptyListWidget(new Label(stringMessages.tagNoTagsFound()));
        tagCellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        tagCellList.setSelectionModel(tagSelectionModel);
        tagSelectionModel.addSelectionChangeHandler(event -> {
            // set time slider to corresponding position
            TagDTO selectedTag = tagSelectionModel.getSelectedObject();
            if (selectedTag != null) {
                /**
                 * Do not set time of timer when {@link #preventTimeJumpAtSelectionChangeForOnce} is set to
                 * <code>true</code>. In this case set {@link #preventTimeJumpAtSelectionChangeForOnce} to
                 * <code>false</code> as selection change is ignored once.
                 * 
                 * @see #preventTimeJumpAtSelectionChangeForOnce
                 */
                if (preventTimeJumpAtSelectionChangeForOnce) {
                    preventTimeJumpAtSelectionChangeForOnce = false;
                } else {
                    // remove time change listener when manual selecting tag cells as this could end in an infinite loop
                    // of timer change -> automatic selection change -> timer change -> ...
                    timer.removeTimeListener(this);
                    timer.setTime(selectedTag.getRaceTimepoint().asMillis());
                    // adding time change listener again
                    timer.addTimeListener(this);
                }
            }
        });
        createTagsButton.setTitle(stringMessages.tagAddTags());
        createTagsButton.setStyleName(style.toggleEditState());
        createTagsButton.addStyleName(style.imagePusTransparent());
        createTagsButton.addClickHandler(event -> {
            setCurrentState(State.CREATE_TAG);
        });

        taggingPanel.setContentWidget(contentPanel);
        updateContent();
    }

    /**
     * Updates parameters required to save/revoke {@link com.sap.sailing.domain.abstractlog.race.RaceLogEvent events}
     * to/from {@link com.sap.sailing.domain.abstractlog.race.RaceLog RaceLog}.
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
        reloadPrivateTags();
    }

    /**
     * Checks if {@link UserService#getCurrentUser() current user} is non-<code>null</code> and if
     * {@link #leaderboardName}, {@link #raceColumn} and {@link #fleet} are non-<code>null</code>.
     * 
     * @return <code>true</code> if current user, {@link #leaderboardName}, {@link #raceColumn} and {@link #fleet} are
     *         non-<code>null</code>, otherwise <code>false</code>.
     */
    protected boolean isLoggedInAndRaceLogAvailable() {
        return userService.getCurrentUser() != null && leaderboardName != null && raceColumn != null && fleet != null;
    }

    /**
     * Saves tag at current timer position.
     * 
     * @see #saveTag(String, Sting, String, boolean, TimePoint, boolean)
     */
    protected void saveTag(String tag, String comment, String imageURL, boolean visibleForPublic) {
        getResizedImageURLForImageURL(imageURL, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage());
            }

            @Override
            public void onSuccess(String resizedImageURL) {
                saveTag(tag, comment, imageURL, resizedImageURL, visibleForPublic, null);
            }
        });
    }

    /**
     * Returns image URL of resized image for given image URL.
     * 
     * @param imageURL
     *            URL of image which needs to be resized
     * @param callback
     *            An asynchronous callback containing the resized image URL, <code>empty string</code> in case of empty
     *            given image URL
     */
    protected void getResizedImageURLForImageURL(String imageURL, AsyncCallback<String> callback) {
        if (imageURL == null || imageURL.isEmpty()) {
            callback.onSuccess("");
        } else {
            sailingService.resolveImageDimensions(imageURL, new AsyncCallback<Pair<Integer, Integer>>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(Pair<Integer, Integer> result) {
                    if (result == null || result.getA() == null || result.getB() == null) {
                        callback.onFailure(new IllegalArgumentException("Size of image could not be determined!"));
                    } else {
                        int imageWidth = result.getA();
                        int imageHeight = result.getB();
                        if (imageWidth < MediaTagConstants.TAGGING_IMAGE.getMinWidth()
                                || imageHeight < MediaTagConstants.TAGGING_IMAGE.getMinHeight()) {
                            callback.onFailure(new IllegalArgumentException("Image is to small for resizing!"));
                        } else {
                            if (imageWidth > MediaTagConstants.TAGGING_IMAGE.getMaxWidth()
                                    || imageHeight > MediaTagConstants.TAGGING_IMAGE.getMaxHeight()) {
                                ArrayList<MediaTagConstants> tags = new ArrayList<MediaTagConstants>();
                                tags.add(MediaTagConstants.TAGGING_IMAGE);
                                sailingService.resizeImage(new ImageResizingTaskDTO(imageURL, new Date(), tags),
                                        new AsyncCallback<Set<ImageDTO>>() {
                                            @Override
                                            public void onFailure(Throwable caught) {
                                                callback.onFailure(caught);
                                            }

                                            @Override
                                            public void onSuccess(Set<ImageDTO> result) {
                                                String resizedImageURL = null;
                                                if (result.size() != 0) {
                                                    resizedImageURL = result.iterator().next().getSourceRef();
                                                }
                                                callback.onSuccess(resizedImageURL);
                                            }
                                        });
                            } else {
                                callback.onSuccess(imageURL);
                            }
                        }
                    }

                }
            });
        }
    }

    /**
     * Sends request to {@link SailingServiceAsync SailingService} to add the given tag to the
     * {@link com.sap.sailing.domain.abstractlog.race.RaceLog RaceLog} if the parameter <code>isVisibleForPublic</code>
     * is set to <code>true</code>. Otherwise tag will be stored in the {@link com.sap.sse.security.UserStore
     * UserStore}. <br/>
     * Checks parameters for valid values and replaces optional parameters with value <code>null</code> by default
     * values: <code>comment</code> and <code>imageURL</code> will be replaced by an empty string,
     * <code>raceTimePoint</code> by current {@link #getTimerTime() timer position}.
     */
    protected void saveTag(String tag, String comment, String imageURL, String resizedImageURL,
            boolean visibleForPublic, TimePoint raceTimePoint) {
        if (tagAlreadyExists(tag, comment, imageURL, resizedImageURL, visibleForPublic, raceTimePoint)) {
            // tag does already exist
            Notification.notify(stringMessages.tagNotSavedReason(" " + stringMessages.tagAlreadyExists()),
                    NotificationType.WARNING);
        } else if (!isLoggedInAndRaceLogAvailable()) {
            // User is not logged in or race can not be identified because regatta, race column or fleet are missing.
            Notification.notify(stringMessages.tagNotSaved(), NotificationType.ERROR);
        } else if (tag.isEmpty()) {
            // Tag heading is empty. Empty tags are not allowed.
            Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
        } else {
            // replace null values with default values
            final String saveComment = (comment == null ? "" : comment);
            final TimePoint saveRaceTimePoint = (raceTimePoint == null ? new MillisecondsTimePoint(getTimerTime())
                    : raceTimePoint);
            sailingService.addTag(leaderboardName, raceColumn.getName(), fleet.getName(), tag, saveComment, imageURL,
                    resizedImageURL, visibleForPublic, saveRaceTimePoint, new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.tagNotSavedReason(caught.toString()),
                                    NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(SuccessInfo result) {
                            if (result.isSuccessful()) {
                                Notification.notify(stringMessages.tagSavedSuccessfully(), NotificationType.INFO);
                                // reload private tags if added tag is private
                                if (!visibleForPublic) {
                                    reloadPrivateTags();
                                }
                            } else {
                                Notification.notify(stringMessages.tagNotSavedReason(result.getMessage()),
                                        NotificationType.ERROR);
                            }
                        }
                    });
        }
    }

    /**
     * Removes tag in non-<code>silent</code> mode.
     * 
     * @param tag
     *            tag to remove
     * 
     * @see #removeTag(TagDTO, boolean)
     */
    protected void removeTag(TagDTO tag) {
        removeTag(tag, false);
    }

    /**
     * Sends request to {@link SailingServiceAsync SailingService} to remove the given <code>tag</code>.
     * 
     * @param tag
     *            tag to remove
     * @param silent
     *            when set to <code>true</code>, only error messages will get displayed to user
     */
    protected void removeTag(TagDTO tag, boolean silent) {
        sailingService.removeTag(leaderboardName, raceColumn.getName(), fleet.getName(), tag,
                new AsyncCallback<SuccessInfo>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(stringMessages.tagNotRemoved(), NotificationType.ERROR);
                        GWT.log(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(SuccessInfo result) {
                        if (result.isSuccessful()) {
                            tagListProvider.remove(tag);
                            updateContent();
                            if (!silent) {
                                Notification.notify(stringMessages.tagRemovedSuccessfully(), NotificationType.SUCCESS);
                            }
                        } else {
                            Notification.notify(stringMessages.tagNotRemoved() + " " + result.getMessage(),
                                    NotificationType.ERROR);
                        }
                    }
                });
    }

    /**
     * Updates given <code>tagToUpdate</code> with the given parameters <code>tag</code>, <code>comment</code>,
     * <code>imageURL</code> and <code>isPublic</code>.
     * 
     * @see TagDTO
     */
    protected void updateTag(TagDTO tagToUpdate, String tag, String comment, String imageURL,
            boolean visibleForPublic) {
        // A new resized image gets created every time a tag is updated (if tag shall contain an image)
        getResizedImageURLForImageURL(imageURL, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.tagNotSavedReason(caught.getMessage()), NotificationType.ERROR);
                GWT.log(caught.getMessage());
            }

            @Override
            public void onSuccess(String resizedImageURL) {
                sailingService.updateTag(leaderboardName, raceColumn.getName(), fleet.getName(), tagToUpdate, tag,
                        comment, imageURL, resizedImageURL, visibleForPublic, new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.tagNotSavedReason(caught.getMessage()),
                                        NotificationType.ERROR);
                                GWT.log(caught.getMessage());
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (result.isSuccessful()) {
                                    tagListProvider.remove(tagToUpdate);
                                    // If old tag was or new tag is private, reload all private tags. Otherwise just
                                    // refresh UI.
                                    if (!tagToUpdate.isVisibleForPublic() || !visibleForPublic) {
                                        reloadPrivateTags();
                                    } else {
                                        updateContent();
                                    }
                                    Notification.notify(stringMessages.tagSavedSuccessfully(),
                                            NotificationType.SUCCESS);
                                } else {
                                    Notification.notify(stringMessages.tagNotSavedReason(result.getMessage()),
                                            NotificationType.ERROR);
                                }
                            }
                        });
            }
        });
    }

    /**
     * Returns whether given tag already exists.
     * 
     * @return <code>true</code> if tag already exists (only checked client side), otherwise <code>false</code>
     */
    protected boolean tagAlreadyExists(String tag, String comment, String imageURL, String resizedImageURL,
            boolean visibleForPublic, TimePoint raceTimePoint) {
        for (TagDTO tagDTO : tagListProvider.getAllTags()) {
            if (tagDTO.equals(tag, comment, imageURL, resizedImageURL, visibleForPublic,
                    userService.getCurrentUser().getName(), new MillisecondsTimePoint(getTimerTime()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all private tags from {@link TagListProvider}, loads all private tags from {@link SailingServiceAsync
     * SailingService}, adds them to the {@link TagListProvider} and updates the UI via {@link #updateContent()}.
     */
    private void reloadPrivateTags() {
        sailingService.getPrivateTags(leaderboardName, raceColumn.getName(), fleet.getName(),
                new AsyncCallback<List<TagDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(List<TagDTO> result) {
                        tagListProvider.removePrivateTags();
                        if (result != null && !result.isEmpty()) {
                            tagListProvider.addAll(result);
                        }
                        updateContent();
                    }
                });
    }

    /**
     * Controls the visibility of UI elements in case the content or {@link #currentState} changes.
     */
    protected void updateContent() {
        ensureFooterPanelVisibility();
        createTagsButton.setVisible(userService.getCurrentUser() != null && currentState.equals(State.VIEW));
        if (currentState.equals(State.EDIT_TAG)) {
            taggingPanel.addStyleName(style.taggingPanelDisabled());
            // disable selection of tags when another tags gets edited (currentState == EDIT_TAG)
            tagCellList.setSelectionModel(new NoSelectionModel<TagDTO>());
        } else {
            taggingPanel.removeStyleName(style.taggingPanelDisabled());
            tagCellList.setSelectionModel(tagSelectionModel);
        }
        tagListProvider.updateFilteredTags();
        tagCellList.setVisibleRange(0, tagListProvider.getFilteredTags().size());
        tagListProvider.refresh();
    }

    /**
     * Forces {@link #contentPanel} to rerender.
     */
    protected void refreshContentPanel() {
        taggingPanel.setContentWidget(contentPanel);
    }

    /**
     * Adds {@link TagButton} to {@link #tagButtons list} of all {@link TagButton tag-buttons} and applies
     * {@link com.google.gwt.event.dom.client.ClickHandler ClickHandler} on it which allows saving of tags.
     */
    protected void addTagButton(TagButton tagButton) {
        tagButton.addClickHandler(event -> {
            saveTag(tagButton.getTag(), tagButton.getComment(), tagButton.getImageURL(),
                    tagButton.isVisibleForPublic());
        });
        tagButtons.add(tagButton);
    }

    /**
     * Returns time of {@link #timer}.
     */
    protected Date getTimerTime() {
        return timer.getTime();
    }

    /**
     * Returns the {@link SingleSelectionModel#getSelectedObject() current selected} {@link TagDTO tag}.
     */
    protected TagDTO getSelectedTag() {
        return tagSelectionModel.getSelectedObject();
    }

    /**
     * Returns {@link #tagButtons list} of all {@link TagButton tag-buttons} of the {@link UserService#getCurrentUser()
     * current user}.
     */
    protected List<TagButton> getTagButtons() {
        return tagButtons;
    }

    /**
     * Returns instance of {@link TagListProvider} so it does not have to be a constructor parameter of every sub
     * component of the {@link TaggingPanel}.
     */
    protected TagListProvider getTagListProvider() {
        return tagListProvider;
    }

    /**
     * Sets the {@link #currentState} to the given {@link State state} and updates the UI.
     * 
     * @param state
     *            new state
     */
    protected void setCurrentState(State state) {
        currentState = state;
        updateContent();
    }

    /**
     * Returns current state of {@link TaggingPanel}.
     * 
     * @return current state
     */
    protected State getCurrentState() {
        return currentState;
    }

    /**
     * Updates the visibility of the {@link #footerPanel} and it's components. Input fields will not get displayed if
     * user is not logged in. {@link TagButton}s can't be hidden and will get displayed automatically when
     * {@link UserService#getCurrentUser() current user} is logged in.
     */
    private void ensureFooterPanelVisibility() {
        // Setting footerPanel.setVisible(false) is not sufficient as panel would still be
        // rendered as 20px high white space instead of being hidden.
        // Fix: remove panel completely from footer.
        if (currentState != null && (!currentState.equals(State.VIEW)
                || (currentState.equals(State.VIEW) && !getTagButtons().isEmpty()))) {
            taggingPanel.setFooterWidget(footerPanel);
            footerPanel.setCurrentState(currentState);
        } else {
            taggingPanel.setFooterWidget(null);
        }
    }

    /**
     * Checks whether current user has permission to modify public tags.
     * 
     * @return <code>true</code> if user has {@link Mode#UPDATE update permissions} on {@link #leaderboardName current
     *         leaderboard}, otherwise <code>false</code>
     */
    protected boolean hasPermissionToModifyPublicTags() {
        boolean hasPermission = false;
        if (leaderboardName != null && userService.hasPermission(
                SecuredDomainType.LEADERBOARD.getPermissionForObjects(DefaultActions.UPDATE, leaderboardName))) {
            hasPermission = true;
        }
        return hasPermission;
    }

    /**
     * Clears local list of tags and reloads settings for the current user (private tags, tag buttons and filter).
     */
    protected void clearCache() {
        tagListProvider.clear();
        raceTimesInfoProvider.getRaceIdentifiers().forEach((raceIdentifier) -> {
            raceTimesInfoProvider.setLatestReceivedTagTime(raceIdentifier, null);
        });
        reloadPrivateTags();
        filterbarPanel.loadTagFilterSets();
        footerPanel.loadAllTagButtons();
    }

    /**
     * Updates {@link TagListProvider#getAllTags() local list of tags} when response of {@link SailingServiceAsync
     * SailingService} gets dispatched to all listeners by {@link RaceTimesInfoProvider}. {@link SailingServiceAsync
     * SailingService} sends only difference of tags in comparison based on the <code>createdAt</code>-timestamp of the
     * {@link RaceTimesInfoProvider#latestReceivedTagTimes latest received tag events}.
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
                // createdAt or revokedAt timepoint of latest received tag
                TimePoint latestReceivedTagTime = raceTimesInfoProvider.getLatestReceivedTagTime(raceIdentifier);
                // get difference in tags since latestReceivedTagTime
                if (raceInfo.getTags() != null) {
                    for (TagDTO tag : raceInfo.getTags()) {
                        if (tag.getRevokedAt() != null) {
                            // received tag is revoked => latestReceivedTagTime will be revokedAt if revoke event
                            // occured before latestReceivedTagTime
                            tagListProvider.remove(tag);
                            modifiedTags = true;
                            if (latestReceivedTagTime == null || (latestReceivedTagTime != null
                                    && latestReceivedTagTime.before(tag.getRevokedAt()))) {
                                latestReceivedTagTime = tag.getRevokedAt();
                                updatedLatestTag = true;
                            }
                        } else if (!tagListProvider.getAllTags().contains(tag)) {
                            // received tag is NOT revoked => latestReceivedTagTime will be createdAt if tag event
                            // occured before latestReceivedTagTime
                            tagListProvider.add(tag);
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
                    preventTimeJumpAtSelectionChangeForOnce = true;
                    updateContent();
                }
                // After tags were added for the first time, find tag which matches the URL Parameter "tag", highlight
                // it and jump to its logical timepoint
                // Loading of tags has to be enabled first, that is why raceInfo.getTags() might be null
                // and so the highlighting of tags must wait till raceInfo.getTags() is not null
                if (tagHasNotBeenHighlightedYet && raceInfo.getTags() != null) {
                    tagHasNotBeenHighlightedYet = false;
                    if (timePointToHighlight != null) {
                        timer.setTime(timePointToHighlight.asMillis());
                        if (tagToHighlight != null) {
                            TagDTO matchingTag = null;
                            for (TagDTO tag : tagListProvider.getAllTags()) {
                                if (tag.getRaceTimepoint().equals(timePointToHighlight)
                                        && tag.getTag().equals(tagToHighlight)) {
                                    matchingTag = tag;
                                    break;
                                }
                            }
                            if (matchingTag != null) {
                                tagSelectionModel.clear();
                                tagSelectionModel.setSelected(matchingTag, true);
                            } else {
                                Notification.notify(stringMessages.tagNotFound(), NotificationType.WARNING);
                            }
                        }

                    }
                }
            });
        }
    }

    /**
     * When the {@link UserService#getCurrentUser() current user} logs in or out the {@link #contentPanel content} needs
     * to be reset to hide private tags of the previous {@link UserService#getCurrentUser() current user}. This gets
     * achieved by resetting the {@link TagListProvider#getAllTags() local list of tags} and resetting all
     * {@link RaceTimesInfoProvider#latestReceivedTagTimes latest received tag events} at the
     * {@link RaceTimesInfoProvider}.
     */
    @Override
    public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
        clearCache();
        setCurrentState(State.VIEW);
    }

    /**
     * Highlights most current tag when timer changes.
     */
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        // When reopening Tagging-Panel after it was opened and closed by the user, the TouchSplitLayoutPanel will
        // set the timer to the current time and not the race time, oldTime will be null in this case. This would cause
        // a jump in time as TaggingPanel selects most current tag when time changes. When time continues while timer is
        // in "play"-mode, oldValue won't be null. This can be used as a workaround to discover this "false" time
        // change.
        // => workaround: Check if oldValue is not null to avoid jumps in time
        if (oldTime != null) {
            TagDTO toHighlight = null;
            for (TagDTO tag : tagListProvider.getAllTags()) {
                if (tag.getRaceTimepoint().asDate().getTime() <= newTime.getTime()) {
                    toHighlight = tag;
                } else if (tag.getRaceTimepoint().asDate().getTime() > newTime.getTime()) {
                    break;
                }
            }
            tagSelectionModel.clear();
            if (toHighlight != null) {
                tagSelectionModel.setSelected(toHighlight, true);
            }
        }
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
     * Requests tags from server only if {@link TaggingPanel} is visible.
     */
    @Override
    public void setVisible(boolean visible) {
        if (raceTimesInfoProvider != null) {
            if (visible) {
                raceTimesInfoProvider.enableTagRequests();
                timer.addTimeListener(this);
            } else {
                raceTimesInfoProvider.disableTagRequests();
                timer.removeTimeListener(this);
            }
        }
        taggingPanel.setVisible(visible);
    }

    @Override
    public String getDependentCssClassName() {
        return "tags";
    }

}