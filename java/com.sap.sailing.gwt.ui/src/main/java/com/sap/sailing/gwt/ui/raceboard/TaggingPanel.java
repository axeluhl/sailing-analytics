package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.adminconsole.ImagesBarColumn;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigImagesBarCell;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TagListProvider;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sailing.gwt.ui.raceboard.TaggingPanel.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.IconResources;
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

    public interface TagPanelResources extends ClientBundle {
        public static final TagPanelResources INSTANCE = GWT.create(TagPanelResources.class);

        // TODO image is stored in wrong folder
        @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
        ImageResource editIcon();

        @Source("tagging-panel.css")
        public TagPanelStyle style();

        public interface TagPanelStyle extends CssResource {
            String tagPanel();
            String tag();
            String tagHeading();
            String tagCreated();
            String tagComment();
            String tagImage();
            String button();
            String footerButton();
            String tagButtonTable();
            String inputPanelTag();
            String inputPanelComment();
            String inputPanelImageURL();
            String footerPanel();
            String tagPreviewPanel();
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
        @Template("<div class='{0}'><div class='{1}'>{3}</div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml cell(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag, SafeHtml createdAt,
                SafeHtml content);

        @Template("<div class='{0}'><div class='{1}'>{3}<button>X</button></div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml cellRemovable(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag,
                SafeHtml createdAt, SafeHtml content);

        @Template("<div class='{0}'><img src='{2}'/></div><div class='{1}'>{3}</div>")
        SafeHtml contentWithCommentWithImage(String styleTagImage, String styleTagComment, SafeUri imageURL,
                SafeHtml comment);

        @Template("<div class='{0}'>{1}</div>")
        SafeHtml contentWithCommentWithoutImage(String styleTagComment, SafeHtml comment);

        @Template("<div class='{0}'><img src='{1}'/></div>")
        SafeHtml contentWithoutCommentWithImage(String styleTagImage, SafeUri imageURL);
    }

    private class TagCell extends AbstractCell<TagDTO> {
        private final TagCellTemplate tagCellTemplate = GWT.create(TagCellTemplate.class);
        private final TagPanelResources tagPanelRes = GWT.create(TagPanelResources.class);
        private final TagPanelStyle tagPanelStyle = tagPanelRes.style();

        public TagCell() {
            super("click");
        }

        @Override
        public void render(Context context, TagDTO tag, SafeHtmlBuilder htmlBuilder) {
            if (tag == null) {
                return;
            }

            SafeHtml safeTag = SafeHtmlUtils.fromString(tag.getTag());
            SafeHtml safeCreated = SafeHtmlUtils.fromString(stringMessages.tagCreated(tag.getUsername(), DateTimeFormat.getFormat("E d/M/y, HH:mm").format(tag.getRaceTimepoint().asDate())));
            SafeHtml safeComment = SafeHtmlUtils.fromString(tag.getComment());
            SafeUri trustedImageURL = UriUtils.fromTrustedString(tag.getImageURL());

            SafeHtml content = SafeHtmlUtils.EMPTY_SAFE_HTML;
            if (tag.getComment().length() > 0 && tag.getImageURL().length() <= 0) {
                content = tagCellTemplate.contentWithCommentWithoutImage(tagPanelStyle.tagComment(), safeComment);
            } else if (tag.getComment().length() <= 0 && tag.getImageURL().length() > 0) {
                content = tagCellTemplate.contentWithoutCommentWithImage(tagPanelStyle.tagImage(), trustedImageURL);
            } else if (tag.getComment().length() > 0 && tag.getImageURL().length() > 0) {
                content = tagCellTemplate.contentWithCommentWithImage(tagPanelStyle.tagImage(),
                        tagPanelStyle.tagComment(), trustedImageURL, safeComment);
            }

            SafeHtml cell;
            if (userService.getCurrentUser() != null
                    && tag.getUsername().equals(userService.getCurrentUser().getName())) {
                cell = tagCellTemplate.cellRemovable(tagPanelStyle.tag(), tagPanelStyle.tagHeading(),
                        tagPanelStyle.tagCreated(), safeTag, safeCreated, content);
            } else {
                cell = tagCellTemplate.cell(tagPanelStyle.tag(), tagPanelStyle.tagHeading(), tagPanelStyle.tagCreated(),
                        safeTag, safeCreated, content);
            }
            htmlBuilder.append(cell);
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, TagDTO tag, NativeEvent event,
                ValueUpdater<TagDTO> valueUpdater) {
            super.onBrowserEvent(context, parent, tag, event, valueUpdater);
            if ("click".equals(event.getType())) {
                EventTarget eventTarget = event.getEventTarget();
                if (!Element.is(eventTarget)) {
                    return;
                }
                Element button = parent.getElementsByTagName("button").getItem(0);
                if (button != null && button.isOrHasChild(Element.as(eventTarget))) {
                    removeTagFromRaceLog(tag);
                }
            }
        }
    }

    private class EditTagButtonsImagesBarCell extends ImagesBarCell {
        public static final String ACTION_REMOVE = "ACTION_REMOVE";
        public static final String ACTION_EDIT = "ACTION_EDIT";
        private final StringMessages stringMessages;

        public EditTagButtonsImagesBarCell(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        protected Iterable<ImageSpec> getImageSpecs() {
            return Arrays.asList(
                    new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(),
                            makeImagePrototype(TagPanelResources.INSTANCE.editIcon())),
                    new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(),
                            makeImagePrototype(IconResources.INSTANCE.removeIcon())));
        }
    }

    private class TagButton extends Button {
        private String tag, imageURL, comment;

        public TagButton(String buttonName, String tag, String imageURL, String comment) {
            super(buttonName);
            this.tag = tag;
            this.imageURL = imageURL;
            this.comment = comment;
            setStyleName(TagPanelResources.INSTANCE.style().button());
            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addTagToRaceLog(getTag(), getComment(), getImageURL());
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

        public void setTag(String tag) {
            this.tag = tag;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

    }

    private class TagCreationPanel extends VerticalPanel {
        private final Panel customButtonsPanel;
        private final Panel standardButtonsPanel;
        private final TagCreationInputPanel inputPanel;

        public TagCreationPanel(StringMessages stringMessages) {
            setWidth("100%");
            inputPanel = new TagCreationInputPanel(stringMessages);
            standardButtonsPanel = new FlowPanel();
            standardButtonsPanel.setStyleName(TagPanelResources.INSTANCE.style().footerPanel());
            customButtonsPanel = new FlowPanel();
            customButtonsPanel.setStyleName(TagPanelResources.INSTANCE.style().footerPanel());
            add(inputPanel);
            add(standardButtonsPanel);
            add(customButtonsPanel);

            Button createTagFromTextBoxes = new Button(stringMessages.tagAddTag());
            createTagFromTextBoxes.setStyleName(TagPanelResources.INSTANCE.style().button());
            createTagFromTextBoxes.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isAuthorizedAndRaceLogAvailable()) {
                        addTagToRaceLog(inputPanel.getTagValue(), inputPanel.getCommentValue(),
                                inputPanel.getImageURLValue());
                        inputPanel.clearAllValues();
                    }
                }
            });
            standardButtonsPanel.add(createTagFromTextBoxes);

            Button editCustomTagButtons = new Button(stringMessages.tagEditCustomTagButtons());
            editCustomTagButtons.setStyleName(TagPanelResources.INSTANCE.style().button());
            editCustomTagButtons.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isAuthorizedAndRaceLogAvailable()) {
                        EditCustomTagButtonsDialog editCustomTagButtonsDialog = new EditCustomTagButtonsDialog(
                                customButtonsPanel);

                        // scheduler is used because otherwise DialogBox would not be centered properly
                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                            public void execute() {
                                editCustomTagButtonsDialog.show();
                                editCustomTagButtonsDialog.center();
                            }
                        });
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

    public class TagPreviewPanel extends VerticalPanel {
        private final CellList<TagDTO> tagPreviewCellList;
        private TagDTO previewTag;
        private List<TagDTO> listContainingPreviewTag;
        private final TagCreationInputPanel inputField;

        public TagPreviewPanel(TagCreationInputPanel inputField) {
            this.inputField = inputField;
            tagPreviewCellList = new CellList<TagDTO>(new TagCell(), CellListResources.INSTANCE);
            
            setStyleName(TagPanelResources.INSTANCE.style().tagPreviewPanel());
            
            add(tagPreviewCellList);
            
            inputField.getTagTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    renderPreview();
                }

            });
            inputField.getImageURLTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    renderPreview();
                }

            });
            inputField.getCommentTextArea().addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    renderPreview();
                }

            });
            renderPreview();
        }

        public void renderPreview() {
            previewTag = new TagDTO(inputField.getTagValue(), inputField.getCommentValue(), inputField.getImageURLValue(), "Author", new MillisecondsTimePoint(timer.getTime()), new MillisecondsTimePoint(timer.getTime()));
            listContainingPreviewTag = new ArrayList<TagDTO>();
            listContainingPreviewTag.add(previewTag);
            tagPreviewCellList.setRowData(listContainingPreviewTag);
        }

        @Override
        public Iterator<Widget> iterator() {
            return null;
        }

        @Override
        public boolean remove(Widget child) {
            return false;
        }

    }

    public class TagCreationInputPanel extends VerticalPanel {
        private TextBox tagTextBox, imageURLTextBox;
        private TextArea commentTextArea;

        public TagCreationInputPanel(StringMessages stringMessages) {
            setWidth("100%");

            tagTextBox = new TextBox();
            tagTextBox.setWidth("100%");
            tagTextBox.setStyleName(TagPanelResources.INSTANCE.style().inputPanelTag());
            tagTextBox.setTitle(stringMessages.tagLabelTag());
            tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
            add(tagTextBox);

            imageURLTextBox = new TextBox();
            imageURLTextBox.setWidth("100%");
            imageURLTextBox.setStyleName(TagPanelResources.INSTANCE.style().inputPanelImageURL());
            imageURLTextBox.setTitle(stringMessages.tagLabelImageURL());
            imageURLTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
            add(imageURLTextBox);

            commentTextArea = new TextArea();
            commentTextArea.setWidth("100%");
            commentTextArea.setStyleName(TagPanelResources.INSTANCE.style().inputPanelComment());
            commentTextArea.setVisibleLines(4);
            commentTextArea.setTitle(stringMessages.tagLabelComment());
            commentTextArea.getElement().setPropertyString("placeholder", stringMessages.tagLabelComment());
            add(commentTextArea);

        }

        public TextBox getTagTextBox() {
            return tagTextBox;
        }
        
        public TextBox getImageURLTextBox() {
            return imageURLTextBox;
        }
        
        public TextArea getCommentTextArea() {
            return commentTextArea;
        }

        public String getTagValue() {
            return tagTextBox.getValue();
        }

        public String getCommentValue() {
            return commentTextArea.getValue();
        }

        public String getImageURLValue() {
            return imageURLTextBox.getValue();
        }

        public void setTagValue(String tag) {
            tagTextBox.setValue(tag);
        }

        public void setCommentValue(String comment) {
            commentTextArea.setValue(comment);
        }

        public void setImageURLValue(String imageURL) {
            imageURLTextBox.setValue(imageURL);
        }

        public void clearAllValues() {
            tagTextBox.setText("");
            imageURLTextBox.setText("");
            commentTextArea.setText("");
        }
    }

    private class EditCustomTagButtonsDialog extends DialogBox {

        private final Button closeButton;
        private final Panel mainPanel;
        private final CellTable<TagButton> customTagButtonsTable;
        private final TagCreationInputPanel inputPanel;
        private final Button addCustomTagButton;

        public EditCustomTagButtonsDialog(Panel customButtonsPanel) {
            setGlassEnabled(true);
            setText(stringMessages.tagEditCustomTagButtons());

            mainPanel = new VerticalPanel();
            mainPanel.setWidth("100px");

            customTagButtonsTable = new CellTable<TagButton>();
            customTagButtonsTable.setStyleName(TagPanelResources.INSTANCE.style().tagButtonTable());
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

            ImagesBarColumn<TagButton, EditTagButtonsImagesBarCell> actionsColumn = new ImagesBarColumn<TagButton, EditTagButtonsImagesBarCell>(
                    new EditTagButtonsImagesBarCell(stringMessages));
            actionsColumn.setFieldUpdater(new FieldUpdater<TagButton, String>() {
                @Override
                public void update(int index, TagButton button, String value) {
                    if (LeaderboardConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                        customTagButtons.remove(button);
                        customButtonsPanel.remove(button);
                        customTagButtonsTable.setRowData(customTagButtons);
                    } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                        EditTagButtonDialog editCustomTagButtonDialog = new EditTagButtonDialog(button,
                                customTagButtonsTable, stringMessages);
                        // scheduler is used because otherwise DialogBox would not be centered properly

                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                            public void execute() {
                                editCustomTagButtonDialog.show();
                                editCustomTagButtonDialog.center();
                            }
                        });
                    }
                }
            });

            customTagButtonsTable.addColumn(tagColumn, stringMessages.tagLabelTag());
            customTagButtonsTable.addColumn(commentColumn, stringMessages.tagLabelComment());
            customTagButtonsTable.addColumn(imageURLColumn, stringMessages.tagLabelImageURL());
            customTagButtonsTable.addColumn(actionsColumn, "Actions");
            customTagButtonsTable.setRowData(customTagButtons);

            inputPanel = new TagCreationInputPanel(stringMessages);

            addCustomTagButton = new Button(stringMessages.tagAddCustomTagButton());
            addCustomTagButton.setStyleName(TagPanelResources.INSTANCE.style().footerButton());
            addCustomTagButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (inputPanel.getTagValue().length() > 0) {
                        TagButton tagButton = new TagButton(inputPanel.getTagValue(), inputPanel.getTagValue(),
                                inputPanel.getImageURLValue(), inputPanel.getCommentValue());
                        inputPanel.clearAllValues();
                        customTagButtons.add(tagButton);
                        customButtonsPanel.add(tagButton);
                        customTagButtonsTable.setRowData(customTagButtons);
                    } else {
                        Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
                    }
                }
            });

            closeButton = new Button(stringMessages.close());
            closeButton.setStyleName(TagPanelResources.INSTANCE.style().footerButton());

            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hideDialog();
                }
            });

            mainPanel.add(customTagButtonsTable);
            mainPanel.add(inputPanel);
            mainPanel.add(addCustomTagButton);
            mainPanel.add(closeButton);

            setWidget(mainPanel);
        }

        private void hideDialog() {
            this.hide();
        }
    }

    private final class EditTagButtonDialog extends DialogBox {
        private final HorizontalPanel mainPanel;
        private final TagPreviewPanel tagPreviewPanel;
        private final VerticalPanel leftPanel;
        private final TagCreationInputPanel inputPanel;
        private final HorizontalPanel saveAndClosePanel;
        private final Button saveButton, cancelButton;


        public EditTagButtonDialog(TagButton tagButton, CellTable<TagButton> customTagButtonsTable, StringMessages stringMessages) {
            setText(stringMessages.tagEditCustomTagButton());
            mainPanel = new HorizontalPanel();
            leftPanel = new VerticalPanel();

            inputPanel = new TagCreationInputPanel(stringMessages);
            inputPanel.setTagValue(tagButton.getTag());
            inputPanel.setCommentValue(tagButton.getComment());
            inputPanel.setImageURLValue(tagButton.getImageURL());

            saveButton = new Button(stringMessages.tagSaveCustomTagButton());
            saveButton.setStyleName(TagPanelResources.INSTANCE.style().footerButton());
            saveButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    tagButton.setTag(inputPanel.getTagValue());
                    tagButton.setComment(inputPanel.getCommentValue());
                    tagButton.setImageURL(inputPanel.getImageURLValue());
                    customTagButtonsTable.redraw();
                    hideDialog();
                }
            });

            cancelButton = new Button(stringMessages.cancel());
            cancelButton.setStyleName(TagPanelResources.INSTANCE.style().footerButton());
            cancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hideDialog();
                }
            });
            
            tagPreviewPanel = new TagPreviewPanel(inputPanel);

            saveAndClosePanel = new HorizontalPanel();
            saveAndClosePanel.setStyleName(TagPanelResources.INSTANCE.style().footerPanel());
            saveAndClosePanel.add(saveButton);
            saveAndClosePanel.add(cancelButton);

            leftPanel.add(inputPanel);
            leftPanel.add(saveAndClosePanel);
            
            mainPanel.add(leftPanel);
            mainPanel.add(tagPreviewPanel);
            setWidget(mainPanel);
        }

        private void hideDialog() {
            this.hide();
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
        userService.addUserStatusEventHandler(this);
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
                GWT.log(event.getSource().toString());
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

    private void addTagToRaceLog(String tag, String comment, String imageURL) {
        if (isAuthorizedAndRaceLogAvailable()) {
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
                Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
            }
        }

    }

    private void removeTagFromRaceLog(TagDTO tag) {
        sailingService.removeTagFromRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag,
                new AsyncCallback<SuccessInfo>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify("Could not remove tag!", NotificationType.ERROR);
                    }

                    @Override
                    public void onSuccess(SuccessInfo result) {
                        tagListProvider.getAllTags().remove(tag);
                        updateContent();
                        Notification.notify("Removed tag successfully", NotificationType.SUCCESS);
                    }
                });
    }

    private boolean isAuthorizedAndRaceLogAvailable() {
        return !(userService.getCurrentUser() == null || leaderboardName == null || raceColumn == null && fleet == null);
    }

    private void updateContent() {
        tagCreationPanel.setVisible(userService.getCurrentUser() != null);
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
    public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
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
