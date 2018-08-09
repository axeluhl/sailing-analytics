package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
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
import com.sap.sailing.gwt.ui.client.TagListProvider;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.raceboard.TaggingPanel.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.security.ui.client.UserService;

public class TaggingPanel extends ComponentWithoutSettings implements RaceTimesInfoProviderListener {

    public interface TagPanelResources extends ClientBundle {
        public static final TagPanelResources INSTANCE = GWT.create(TagPanelResources.class);

        @Source("tagging-panel.css")
        public TagPanelStyle style();

        public interface TagPanelStyle extends CssResource {
            String tagPanel();
            String tag();
            String tagHeading();
            String tagCreated();
            String tagComment();
            String tagImage();
        }
    }

    public interface CellListResources extends CellList.Resources {
        public static final CellListResources INSTANCE = GWT.create(CellListResources.class);

        @Override
        @Source("tagging-celllist.css")
        public CellListStyle cellListStyle();

        public interface CellListStyle extends CellList.Style {
            String cellListEventItem();
            String cellListWidget();
            String cellListEvenItem();
            String cellListOddItem();
            String cellListSelectedItem();
            String cellListKeyboardSelectedItem();
        }
    }

    public interface TagCellTemplate extends SafeHtmlTemplates {
        @Template("<div class='{0}'><div class='{1}'>{5}</div><div class='{2}'>(created by <b>{6}</b> at {7})</div><div class='{3}'><img src='{8}'/></div><div class='{4}'>{9}</div></div>")
        SafeHtml cell(String styleTag, String styleTagHeading, String styleTagCreated, String styleTagImage,
                String styleTagComment, SafeHtml tag, SafeHtml author, SafeHtml createdAt, SafeUri imageURL,
                SafeHtml comment);

        @Template("<div class='{0}'><div class='{1}'>{4}</div><div class='{2}'>(created by <b>{5}</b> at {6})</div><div class='{3}'>{7}</div></div>")
        SafeHtml cellWithCommentWithoutImage(String styleTag, String styleTagHeading, String styleTagCreated,
                String styleTagComment, SafeHtml tag, SafeHtml author, SafeHtml createdAt, SafeHtml comment);

        @Template("<div class='{0}'><div class='{1}'>{4}</div><div class='{2}'>(created by <b>{5}</b> at {6})</div><div class='{3}'><img src='{7}'/></div></div>")
        SafeHtml cellWithoutCommentWithImage(String styleTag, String styleTagHeading, String styleTagCreated,
                String styleTagImage, SafeHtml tag, SafeHtml author, SafeHtml createdAt, SafeUri imageURL);

        @Template("<div class='{0}'><div class='{1}'>{3}</div><div class='{2}'>(created by <b>{4}</b> at {5})</div></div>")
        SafeHtml cellWithoutCommentWithoutImage(String styleTag, String styleTagHeading, String styleTagCreated,
                SafeHtml tag, SafeHtml author, SafeHtml createdAt);
    }

    private class TagCell extends AbstractCell<TagDTO> {
        private final TagCellTemplate tagCellTemplate = GWT.create(TagCellTemplate.class);
        private final TagPanelResources tagPanelRes = GWT.create(TagPanelResources.class);
        private final TagPanelStyle tagPanelStyle = tagPanelRes.style();

        @Override
        public void render(Context context, TagDTO tag, SafeHtmlBuilder htmlBuilder) {
            if (tag == null) {
                return;
            }

            SafeHtml safeTag = SafeHtmlUtils.fromString(tag.getTag());
            SafeHtml safeAuthor = SafeHtmlUtils.fromString(tag.getUsername());
            SafeHtml safeCreatedAt = SafeHtmlUtils
                    .fromString(DateAndTimeFormatterUtil.shortTimeFormatter.render(tag.getRaceTimepoint().asDate()));
            SafeHtml safeComment = SafeHtmlUtils.fromString(tag.getComment());
            SafeUri trustedImageURL = UriUtils.fromTrustedString(tag.getImageURL());

            SafeHtml cell = null;
            if (tag.getComment().length() <= 0 && tag.getImageURL().length() <= 0) {
                // no comment & no image
                cell = tagCellTemplate.cellWithoutCommentWithoutImage(tagPanelStyle.tag(), tagPanelStyle.tagHeading(),
                        tagPanelStyle.tagCreated(), safeTag, safeAuthor, safeCreatedAt);
            } else if (tag.getComment().length() > 0 && tag.getImageURL().length() <= 0) {
                // comment & no image
                cell = tagCellTemplate.cellWithCommentWithoutImage(tagPanelStyle.tag(), tagPanelStyle.tagHeading(),
                        tagPanelStyle.tagCreated(), tagPanelStyle.tagComment(), safeTag, safeAuthor, safeCreatedAt,
                        safeComment);
            } else if (tag.getComment().length() <= 0 && tag.getImageURL().length() > 0) {
                // no comment & image
                cell = tagCellTemplate.cellWithoutCommentWithImage(tagPanelStyle.tag(), tagPanelStyle.tagHeading(),
                        tagPanelStyle.tagCreated(), tagPanelStyle.tagImage(), safeTag, safeAuthor, safeCreatedAt,
                        trustedImageURL);
            } else {
                // comment & image
                cell = tagCellTemplate.cell(tagPanelStyle.tag(), tagPanelStyle.tagHeading(), tagPanelStyle.tagCreated(),
                        tagPanelStyle.tagImage(), tagPanelStyle.tagComment(), safeTag, safeAuthor, safeCreatedAt,
                        trustedImageURL, safeComment);
            }
            htmlBuilder.append(cell);
        }
    }

    private class TagButton extends Button {
        private String tag, imageURL, comment;

        public TagButton(String buttonName, String tag, String imageURL, String comment) {
            super(buttonName);
            this.tag = tag;
            this.imageURL = imageURL;
            this.comment = comment;
            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addNewTag(getTag(), getComment(), getImageURL());
                }
            });
        }

        public String getTag() {
            return tag;
        }

        public String getImageURL() {
            return imageURL;
        }

        public String getComment() {
            return comment;
        }
    }

    private class TagCreationPanel extends VerticalPanel {
        private final Panel customButtonsPanel;
 
        public TagCreationPanel(StringMessages stringMessages) {    
            TagCreationInputPanel inputPanel = new TagCreationInputPanel(stringMessages);
            Panel standardButtonsPanel = new FlowPanel(); 
            customButtonsPanel = new FlowPanel(); 
            add(inputPanel);
            add(standardButtonsPanel);
            add(customButtonsPanel);
            
            Button createTagFromTextBoxes = new Button(stringMessages.tagAddButton());
            createTagFromTextBoxes.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isAuthorized()) {
                        addNewTag(inputPanel.getTagValue(), inputPanel.getCommentValue(), inputPanel.getImageURLValue());
                        inputPanel.clearAllValues();
                    }
                }
            });
            standardButtonsPanel.add(createTagFromTextBoxes);

            Button editCustomTagButtons = new Button(stringMessages.tagEditCustomTagButtons());
            editCustomTagButtons.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isAuthorized()) {
                        new EditCustomTagsDialog(customButtonsPanel).show();
                        updateButtons();
                    }
                }
            });
            standardButtonsPanel.add(editCustomTagButtons);
        }

        private void updateButtons() {
            customButtonsPanel.clear();
            customTagButtons.forEach(button -> {
                customButtonsPanel.add(button);
            });
        }
    }
    
    private class EditCustomTagsDialog extends DataEntryDialog<List<TagButton>>{
        private final Panel customButtonsPanel;
        
        public EditCustomTagsDialog(Panel customButtonsPanel) {
            super(stringMessages.tagEditCustomTagsButtonDialogHeader(), "", stringMessages.ok(), stringMessages.cancel(), null, null);
            this.customButtonsPanel = customButtonsPanel; 
        }

        @Override
        protected Widget getAdditionalWidget() {
            Panel mainPanel = new HorizontalPanel();
            Panel rightPanel = new VerticalPanel();

            CellTable<TagButton> customTagButtonsTable = new CellTable<TagButton>();
            // add table header
            TextColumn<TagButton> tagColumn = new TextColumn<TagButton>() {
                @Override
                public String getValue(TagButton button) {
                    return button.getTag();
                }
            };
            TextColumn<TagButton> commentColumn = new TextColumn<TagButton>() {
                @Override
                public String getValue(TagButton button) {
                    return button.getComment();
                }
            };
            TextColumn<TagButton> imageURLColumn = new TextColumn<TagButton>() {
                @Override
                public String getValue(TagButton button) {
                    return button.getImageURL();
                }
            };
            ButtonCell buttonCell = new ButtonCell();
            Column<TagButton, String> buttonColumn = new Column<TagButton, String>(buttonCell) {
                @Override
                public String getValue(TagButton tag) {
                    return "delete";
                }
            };
            buttonColumn.setFieldUpdater(new FieldUpdater<TagButton, String>() {
                public void update(int index, TagButton button, String value) {
                    customTagButtons.remove(button);
                    customButtonsPanel.remove(button);
                    customTagButtonsTable.setRowData(customTagButtons);
                }
              });
            customTagButtonsTable.addColumn(tagColumn, stringMessages.tagLabelTag());
            customTagButtonsTable.addColumn(commentColumn, stringMessages.tagLabelComment());
            customTagButtonsTable.addColumn(imageURLColumn, stringMessages.tagLabelImageURL());
            customTagButtonsTable.addColumn(buttonColumn, "Delete");
            customTagButtonsTable.setRowData(customTagButtons);
            mainPanel.add(rightPanel);
            mainPanel.add(customTagButtonsTable);
            
            TagCreationInputPanel inputPanel = new TagCreationInputPanel(stringMessages);
            rightPanel.add(inputPanel);
            
            Button addCustomTagButton = new Button(stringMessages.tagAddCustomTagButton());
            addCustomTagButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (inputPanel.getTagValue().length() > 0) {
                        TagButton tagButton = new TagButton(inputPanel.getTagValue(), inputPanel.getTagValue(), inputPanel.getImageURLValue(), inputPanel.getCommentValue());
                        inputPanel.clearAllValues();
                        customTagButtons.add(tagButton);
                        customButtonsPanel.add(tagButton);
                        customTagButtonsTable.setRowData(customTagButtons);
                    } else {
                        Notification.notify(stringMessages.tagNotSpecified(), NotificationType.ERROR);
                    }
                }
            });
            rightPanel.add(addCustomTagButton);

            return mainPanel;
        }

        @Override
        protected List<TagButton> getResult() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private final HeaderPanel panel;
    private final TagCreationPanel tagCreationPanel;
    private final Panel filterbarPanel;
    private final Panel contentPanel;
    private final CellList<TagDTO> tagCellList;
    private final SingleSelectionModel<TagDTO> tagSelectionModel;

    private final TagListProvider tagListProvider;
    private final List<TagButton> customTagButtons;

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final Timer timer;
    private final RaceTimesInfoProvider raceTimesInfoProvider;

    private String leaderboardName = null;
    private RaceColumnDTO raceColumn = null;
    private FleetDTO fleet = null;

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer,
            RaceTimesInfoProvider raceTimesInfoProvider) {
        super(parent, context);

        TagPanelResources.INSTANCE.style().ensureInjected();
        CellListResources.INSTANCE.cellListStyle().ensureInjected();

        tagListProvider = new TagListProvider();
        customTagButtons = new ArrayList<TagButton>();

        panel = new HeaderPanel();
        filterbarPanel = new TagFilterPanel(stringMessages, tagListProvider);
        tagCellList = new CellList<TagDTO>(new TagCell(), CellListResources.INSTANCE);
        tagSelectionModel = new SingleSelectionModel<TagDTO>();

        contentPanel = new ScrollPanel();
        tagCreationPanel = new TagCreationPanel(stringMessages);

        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.userService = userService;
        this.timer = timer;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);

        initializePanel();
    }

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer,
            RaceTimesInfoProvider raceTimesInfoProvider, String leaderboardName, RaceColumnDTO raceColumn,
            FleetDTO fleet) {
        this(parent, context, stringMessages, sailingService, userService, timer, raceTimesInfoProvider);
        updateRace(leaderboardName, raceColumn, fleet);
    }

    private void initializePanel() {
        // Panel
        panel.setStyleName(TagPanelResources.INSTANCE.style().tagPanel());

        // Searchbar
        panel.setHeaderWidget(filterbarPanel);
        panel.setFooterWidget(tagCreationPanel);

        // Content (tags)
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
        contentPanel.getElement().getStyle().setHeight(100, Unit.PCT);
        contentPanel.getElement().getStyle().setPaddingTop(10, Unit.PX);

        panel.setContentWidget(contentPanel);
        updateContent();
    }

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
    }

    private void addNewTag(String tag, String comment, String imageURL) {
        if (isAuthorized()) {
            if (tag.length() > 0) {
                sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag, comment,
                        imageURL, new MillisecondsTimePoint(timer.getTime()), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.tagNotAdded(), NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Notification.notify(stringMessages.tagAddedSuccessfully(), NotificationType.INFO);
                            }
                        });
            } else {
                Notification.notify(stringMessages.tagNotSpecified(), NotificationType.ERROR);
            }
        }

    }

    private boolean isAuthorized() {
        if (leaderboardName == null || raceColumn == null || fleet == null) {
            Notification.notify(stringMessages.tagNotAdded(), NotificationType.ERROR);
            return false;
        }
        if (userService.getCurrentUser() == null) {
            Notification.notify(stringMessages.tagNotLoggedIn(), NotificationType.WARNING);
            return false;
        }
        return true;
    }

    private void updateContent() {
        tagListProvider.updateFilteredTags();
        tagCellList.setVisibleRange(0, tagListProvider.getFilteredTagsListSize());
        tagListProvider.refresh();
    }

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        raceTimesInfo.forEach((raceIdentifier, raceInfo) -> {
            boolean addedTag = false;
            boolean updatedLatestTag = false;
            if (raceIdentifier.equals(raceInfo.getRaceIdentifier())) {
                List<TagDTO> currentTags = tagListProvider.getAllTags();
                TimePoint latestReceivedTagTime = raceTimesInfoProvider.getLatestReceivedTag(raceIdentifier);
                for (TagDTO tag : raceInfo.getTags()) {
                    if (!currentTags.contains(tag)) {
                        currentTags.add(tag);
                        addedTag = true;
                        if (latestReceivedTagTime == null || (latestReceivedTagTime != null
                                && latestReceivedTagTime.before(tag.getCreatedAt()))) {
                            latestReceivedTagTime = tag.getCreatedAt();
                            updatedLatestTag = true;
                        }
                    }
                }
                if (updatedLatestTag) {
                    raceTimesInfoProvider.setLatestReceivedTagTime(raceIdentifier, latestReceivedTagTime);
                }
                if (addedTag) {
                    updateContent();
                }
            }
        });
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
        return panel;
    }

    @Override
    public boolean isVisible() {
        return panel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        panel.setVisible(visibility);
    }

    @Override
    public String getDependentCssClassName() {
        return "tags";
    }
}
