package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Position;
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
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSets;
import com.sap.sailing.gwt.ui.raceboard.TaggingPanel.TagResources.TagStyle;
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

    public interface TagResources extends ClientBundle, CellList.Resources {
        public static final TagResources INSTANCE = GWT.create(TagResources.class);

        @Source("tagging.css")
        public TagStyle style();

        public interface TagStyle extends CssResource, CellList.Style {
            String tag();
            String tagHeading();
            String tagCreated();
            String tagComment();
            String tagImage();

            String cellListEventItem();
            String cellListWidget();
            String cellListEvenItem();
            String cellListOddItem();
            String cellListSelectedItem();
            String cellListKeyboardSelectedItem();
        }
    }

    public interface TagCellTemplate extends SafeHtmlTemplates {
        @Template("<div class='{0}'><div class='{1}'>{5}</div><div class='{2}'>(created by <b>{6}</b> at <i>{7}</i>)</div><div class='{3}'>{8}</div><div class='{4}'><img src='{9}'/></div></div>")
        SafeHtml cell(String styleTag, String styleTagHeading, String styleTagCreated, String styleTagComment,
                String styleTagImage, SafeHtml tag, SafeHtml author, SafeHtml createdAt, SafeHtml comment,
                SafeUri imageURL);

        @Template("<div class='{0}'><div class='{1}'>{4}</div><div class='{2}'>(created by <b>{5}</b> at <i>{6}</i>)</div><div class='{3}'>{7}</div></div>")
        SafeHtml cellWithCommentWithoutImage(String styleTag, String styleTagHeading, String styleTagCreated,
                String styleTagComment, SafeHtml tag, SafeHtml author, SafeHtml createdAt, SafeHtml comment);

        @Template("<div class='{0}'><div class='{1}'>{4}</div><div class='{2}'>(created by <b>{5}</b> at <i>{6}</i>)</div><div class='{3}'><img src='{7}'/></div></div>")
        SafeHtml cellWithoutCommentWithImage(String styleTag, String styleTagHeading, String styleTagCreated,
                String styleTagImage, SafeHtml tag, SafeHtml author, SafeHtml createdAt, SafeUri imageURL);

        @Template("<div class='{0}'><div class='{1}'>{3}</div><div class='{2}'>(created by <b>{4}</b> at <i>{5}</i>)</div></div>")
        SafeHtml cellWithoutCommentWithoutImage(String styleTag, String styleTagHeading, String styleTagCreated,
                SafeHtml tag, SafeHtml author, SafeHtml createdAt);
    }

    private class TagCell extends AbstractCell<TagDTO> {
        private final TagCellTemplate tagCellTemplate = GWT.create(TagCellTemplate.class);
        private final TagResources tagRes = GWT.create(TagResources.class);
        private final TagStyle tagStyle = tagRes.style();

        @Override
        public void render(Context context, TagDTO tag, SafeHtmlBuilder htmlBuilder) {
            if (tag == null) {
                return;
            }

            SafeHtml safeTag = SafeHtmlUtils.fromString(tag.getTag());
            SafeHtml safeAuthor = SafeHtmlUtils.fromString(tag.getUsername());
            SafeHtml safeCreatedAt = SafeHtmlUtils.fromString(tag.getRaceTimepoint().toString());
            SafeHtml safeComment = SafeHtmlUtils.fromString(tag.getComment());
            SafeUri  trustedImageURL = UriUtils.fromTrustedString(tag.getImageURL());

            SafeHtml cell = null;
            if (tag.getComment().length() <= 0 && tag.getImageURL().length() <= 0) {
                // no comment & no image
                cell = tagCellTemplate.cellWithoutCommentWithoutImage(tagStyle.tag(), tagStyle.tagHeading(),
                        tagStyle.tagCreated(), safeTag, safeAuthor, safeCreatedAt);
            } else if (tag.getComment().length() > 0 && tag.getImageURL().length() <= 0) {
                // comment & no image
                cell = tagCellTemplate.cellWithCommentWithoutImage(tagStyle.tag(), tagStyle.tagHeading(),
                        tagStyle.tagCreated(), tagStyle.tagComment(), safeTag, safeAuthor, safeCreatedAt, safeComment);
            } else if (tag.getComment().length() <= 0 && tag.getImageURL().length() > 0) {
                // no comment & image
                cell = tagCellTemplate.cellWithoutCommentWithImage(tagStyle.tag(), tagStyle.tagHeading(),
                        tagStyle.tagCreated(), tagStyle.tagImage(), safeTag, safeAuthor, safeCreatedAt,
                        trustedImageURL);
            } else {
                // comment & image
                cell = tagCellTemplate.cell(tagStyle.tag(), tagStyle.tagHeading(), tagStyle.tagCreated(),
                        tagStyle.tagComment(), tagStyle.tagImage(), safeTag, safeAuthor, safeCreatedAt, safeComment,
                        trustedImageURL);
            }
            htmlBuilder.append(cell);
        }
    }

    private final HeaderPanel panel;
    private final Panel filterbarPanel;
    private final Panel contentPanel;
    private final CellList<TagDTO> tagCellList;
    private final Panel buttonsPanel;

    private final List<Button> buttons;
    private final List<TagDTO> tags;
    private final ListDataProvider<TagDTO> tagProvider;

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

        panel = new HeaderPanel();
        filterbarPanel = new TagFilterPanel(null, stringMessages, new TagsFilterSets());
        tagCellList = new CellList<TagDTO>(new TagCell(), TagResources.INSTANCE);
        contentPanel = new ScrollPanel();
        buttonsPanel = new FlowPanel();

        buttons = new ArrayList<Button>();
        tags = new ArrayList<TagDTO>();
        tagProvider = new ListDataProvider<TagDTO>();

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
        TagResources.INSTANCE.style().ensureInjected();

        // Panel
        panel.setTitle(stringMessages.tagging());
        panel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        panel.getElement().getStyle().setTop(0, Unit.PX);
        panel.getElement().getStyle().setBottom(0, Unit.PX);
        panel.getElement().getStyle().setLeft(0, Unit.PX);
        panel.getElement().getStyle().setRight(0, Unit.PX);
        panel.getElement().getStyle().setMargin(6, Unit.PX);
        panel.getElement().getStyle().setMarginTop(10, Unit.PX);

        // Searchbar
        panel.setHeaderWidget(filterbarPanel);

        // Buttons
        buttons.add(new Button("Add new Tag-Button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addNewTagButton();
            }
        }));
        panel.setFooterWidget(buttonsPanel);

        // Content (tags)
        tagProvider.addDataDisplay(tagCellList);
        tagProvider.setList(tags);
        contentPanel.add(tagCellList);
        contentPanel.getElement().getStyle().setHeight(100, Unit.PCT);
        contentPanel.getElement().getStyle().setPaddingBottom(10, Unit.PX);
        contentPanel.getElement().getStyle().setMarginTop(10, Unit.PX);

        panel.setContentWidget(contentPanel);
        updateUi();
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
                    Notification.notify("Could not trigger RaceLogTagEvent, missing information!",
                            NotificationType.ERROR);
                    return;
                }
                if (userService.getCurrentUser() == null) {
                    Notification.notify("Please log in to add new tags!", NotificationType.WARNING);
                    return;
                }

                TagDTO tag = new TagDTO("Super Duper Tag!", "Fancy comment...", "https://localhost:8080/image/abc.png",
                        userService.getCurrentUser().getName(), new MillisecondsTimePoint(timer.getTime()));
                sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag.getTag(),
                        tag.getComment(), tag.getImageURL(), tag.getRaceTimepoint(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log("Could not add new tag to race log!", caught);
                                Notification.notify("Could not add new tag to race log!", NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Notification.notify("Added new tag successfully", NotificationType.INFO);
                                tags.add(tag);
                                updateContent();
                            }
                        });
            }
        });

        buttons.add(tagButton);
        updateButtons();
    }

    private void updateUi() {
        updateButtons();
        updateContent();
    }

    private void updateContent() {
        tagCellList.setVisibleRange(0, tags.size());
        tagProvider.refresh();
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
                            Notification.notify("Could not load tags!", NotificationType.ERROR);
                            GWT.log("Could not load tags!", caught);
                        }

                        @Override
                        public void onSuccess(List<TagDTO> result) {
                            if (result != null) {
                                for (TagDTO tag : result) {
                                    if (!tags.contains(tag)) {
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
        return stringMessages.tagging();
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
