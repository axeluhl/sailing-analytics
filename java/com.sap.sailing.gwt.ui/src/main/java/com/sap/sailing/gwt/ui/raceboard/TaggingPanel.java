package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TagListProvider;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSets;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.raceboard.TaggingPanel.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.security.ui.client.UserService;

public class TaggingPanel extends ComponentWithoutSettings implements TimeListener {

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

    private final HeaderPanel panel;
    private final Panel filterbarPanel;
    private final Panel contentPanel;
    private final CellList<TagDTO> tagCellList;
    private final SingleSelectionModel<TagDTO> tagSelectionModel;
    private final Panel buttonsPanel;

    private final List<Button> buttons;
    private final TagListProvider tagListProvider;
    private final TagsFilterSets tagsFilterSet;

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final Timer timer;

    private String leaderboardName = null;
    private RaceColumnDTO raceColumn = null;
    private FleetDTO fleet = null;
    private TimePoint lastReceivedTag = null;

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer) {
        super(parent, context);

        TagPanelResources.INSTANCE.style().ensureInjected();
        CellListResources.INSTANCE.cellListStyle().ensureInjected();

        tagsFilterSet = new TagsFilterSets();
        tagListProvider = new TagListProvider(tagsFilterSet.getActiveFilterSetWithGeneralizedType());

        panel = new HeaderPanel();
        filterbarPanel = new TagFilterPanel(null, stringMessages, tagListProvider);
        tagCellList = new CellList<TagDTO>(new TagCell(), CellListResources.INSTANCE);
        tagSelectionModel = new SingleSelectionModel<TagDTO>();

        contentPanel = new ScrollPanel();
        buttonsPanel = new FlowPanel();

        buttons = new ArrayList<Button>();        

        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.userService = userService;
        this.timer = timer;
        timer.addTimeListener(this);

        initializePanel();
    }

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer, String leaderboardName,
            RaceColumnDTO raceColumn, FleetDTO fleet) {
        this(parent, context, stringMessages, sailingService, userService, timer);
        updateRace(leaderboardName, raceColumn, fleet);
    }

    private void initializePanel() {
        // Panel
        panel.setTitle(stringMessages.tagPanel());
        panel.setStyleName(TagPanelResources.INSTANCE.style().tagPanel());

        // Searchbar
        panel.setHeaderWidget(filterbarPanel);

        // Buttons
        buttons.add(new Button(stringMessages.tagAddButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addNewTagButton();
            }
        }));
        panel.setFooterWidget(buttonsPanel);

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
        updateButtons();
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

    private void addNewTagButton() {
        Button tagButton = new Button("Tag-Button");
        tagButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (leaderboardName == null || raceColumn == null || fleet == null) {
                    Notification.notify(stringMessages.tagNotAdded(), NotificationType.ERROR);
                    return;
                }
                if (userService.getCurrentUser() == null) {
                    Notification.notify(stringMessages.tagNotLoggedIn(), NotificationType.WARNING);
                    return;
                }

                TagDTO tag = new TagDTO("Super Duper Tag!", "Fancy comment...", "https://localhost:8080/image/abc.png",
                        userService.getCurrentUser().getName(), new MillisecondsTimePoint(timer.getTime()));
                sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag.getTag(),
                        tag.getComment(), tag.getImageURL(), tag.getRaceTimepoint(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.tagNotAdded(), NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Notification.notify(stringMessages.tagAddedSuccessfully(), NotificationType.INFO);
                                tagListProvider.addTag(tag);
                                updateContent();
                            }
                        });
            }
        });

        buttons.add(tagButton);
        updateButtons();
    }

    private void updateContent() {
        tagListProvider.updateFilteredTags();
        tagCellList.setVisibleRange(0, tagListProvider.getFilteredTagsListSize());
        tagListProvider.getFilteredTags().sort(new Comparator<TagDTO>() {
            @Override
            public int compare(TagDTO tag1, TagDTO tag2) {
                long time1 = tag1.getRaceTimepoint().asMillis();
                long time2 = tag2.getRaceTimepoint().asMillis();
                return time1 < time2 ? -1 : time1 == time2 ? 0 : 1;
            }
        });
        tagListProvider.refresh();
    }

    private void updateButtons() {
        buttonsPanel.clear();
        buttons.forEach(button -> {
            buttonsPanel.add(button);
        });
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (leaderboardName != null && raceColumn != null && fleet != null && panel.isAttached() && panel.isVisible()
                && oldTime != null) {
            // load tags since last received tag => decrease required bandwidth as only difference in tags will be sent
            // over network
            sailingService.getTags(leaderboardName, raceColumn.getName(), fleet.getName(), lastReceivedTag,
                    new MillisecondsTimePoint(newTime.getTime()), new AsyncCallback<List<TagDTO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.tagNotLoaded(), NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(List<TagDTO> result) {
                            if (result != null) {
                                List<TagDTO> tags = tagListProvider.getAllTags();
                                for (TagDTO tag : result) {
                                    if (!tagListProvider.getAllTags().contains(tag)) {
                                        tags.add(tag);
                                        lastReceivedTag = tag.getRaceTimepoint();
                                        updateContent();
                                    }
                                }
                            }
                        }
                    });
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
