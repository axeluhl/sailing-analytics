package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
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
import com.sap.sailing.gwt.ui.client.shared.filter.FilterUIFactory;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSets;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSetsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSetsJsonDeSerializer;
import com.sap.sailing.gwt.ui.raceboard.TaggingPanel.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
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
        
        @Source("com/sap/sailing/gwt/ui/client/images/unlock.png")
        ImageResource publicIcon();
        
        @Source("com/sap/sailing/gwt/ui/client/images/lock.png")
        ImageResource privateIcon();

        @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Clear.png")
        ImageResource clearButton();

        @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_CompetitorsFilter_INACTIVE.png")
        ImageResource filterInactiveButton();

        @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_CompetitorsFilter_ACTIVE.png")
        ImageResource filterActiveButton();

        @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Search.png")
        ImageResource searchButton();

        @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Settings.png")
        ImageResource settingsButton();

        @Source("tagging-panel.css")
        public TagPanelStyle style();

        public interface TagPanelStyle extends CssResource {
            // tags
            String tag();
            String tagPanel();
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

            // filter tags
            String tagFilterButton();
            String tagFilterHiddenButton();
            String tagFilterClearButton();
            String tagFilterSearchButton();
            String tagFilterSettingsButton();
            String tagFilterFilterButton();
            String tagFilterContainer();
            String tagFilterSearchBox();
            String tagFilterSearchInput();
            String filterInactiveButtonBackgroundImage();
            String filterActiveButtonBackgroundImage();
            String clearButtonBackgroundImage();
            String searchButtonBackgroundImage();
            String settingsButtonBackgroundImage();
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
        @Template("<div class='{0}'><div class='{1}'><img src='{6}'>{3}</div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml cell(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag, SafeHtml createdAt,
                SafeHtml content, SafeUri safeUri);

        @Template("<div class='{0}'><div class='{1}'><img src='{6}'>{3}<button>X</button></div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml cellRemovable(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag,
                SafeHtml createdAt, SafeHtml content, SafeUri safeUri);
        

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
        private final boolean isPreviewCell;

        public TagCell(boolean isPreviewCell) {
            super("click");
            this.isPreviewCell = isPreviewCell;
        }

        @Override
        public void render(Context context, TagDTO tag, SafeHtmlBuilder htmlBuilder) {
            if (tag == null) {
                return;
            }

            SafeHtml safeTag = SafeHtmlUtils.fromString(tag.getTag());
            SafeHtml safeCreated = SafeHtmlUtils.fromString(stringMessages.tagCreated(tag.getUsername(),
                    DateTimeFormat.getFormat("E d/M/y, HH:mm").format(tag.getRaceTimepoint().asDate())));
            SafeHtml safeComment = SafeHtmlUtils.fromString(tag.getComment());
            SafeUri trustedImageURL = UriUtils.fromTrustedString(tag.getImageURL());
            
            SafeUri safeIsPublicImageUri = tag.isPublic() ? TagPanelResources.INSTANCE.publicIcon().getSafeUri() : TagPanelResources.INSTANCE.privateIcon().getSafeUri();

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
            if (!isPreviewCell && userService.getCurrentUser() != null
                    && (tag.getUsername().equals(userService.getCurrentUser().getName())
                            || userService.getCurrentUser().hasRole("admin"))) {
                cell = tagCellTemplate.cellRemovable(tagPanelStyle.tag(), tagPanelStyle.tagHeading(),
                        tagPanelStyle.tagCreated(), safeTag, safeCreated, content, safeIsPublicImageUri);
            } else {
                cell = tagCellTemplate.cell(tagPanelStyle.tag(), tagPanelStyle.tagHeading(), tagPanelStyle.tagCreated(),
                        safeTag, safeCreated, content, safeIsPublicImageUri);
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
            setStyleName(TagPanelResources.INSTANCE.style().footerButton());
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

    private class TagCreationPanel extends FlowPanel {
        private final Panel customButtonsPanel;
        private final Panel standardButtonsPanel;
        private final TagCreationInputPanel inputPanel;
        private final Button createTagFromTextBoxes, editCustomTagButtons;

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

            createTagFromTextBoxes = new Button(stringMessages.tagAddTag());
            createTagFromTextBoxes.setStyleName(TagPanelResources.INSTANCE.style().footerButton());
            createTagFromTextBoxes.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isLoggedInAndRaceLogAvailable()) {
                        addTagToRaceLog(inputPanel.getTagValue(), inputPanel.getCommentValue(),
                                inputPanel.getImageURLValue());
                        inputPanel.clearAllValues();
                    }
                }
            });
            standardButtonsPanel.add(createTagFromTextBoxes);

            editCustomTagButtons = new Button(stringMessages.tagEditCustomTagButtons());
            editCustomTagButtons.setStyleName(TagPanelResources.INSTANCE.style().footerButton());
            editCustomTagButtons.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isLoggedInAndRaceLogAvailable()) {
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

    public class TagPreviewPanel extends FlowPanel {
        private final CellList<TagDTO> tagPreviewCellList;
        private TagDTO previewTag;
        private List<TagDTO> listContainingPreviewTag;
        private final TagCreationInputPanel inputField;
        private final Label previewLabel;

        public TagPreviewPanel(TagCreationInputPanel inputField) {
            this.inputField = inputField;
            tagPreviewCellList = new CellList<TagDTO>(new TagCell(true), CellListResources.INSTANCE);
            tagPreviewCellList.setVisibleRange(0, 1);
            previewLabel = new Label(stringMessages.tagPreview());
            listContainingPreviewTag = new ArrayList<TagDTO>();

            setStyleName(TagPanelResources.INSTANCE.style().tagPreviewPanel());
            add(previewLabel);
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
            listContainingPreviewTag.removeAll(listContainingPreviewTag);
            previewTag = new TagDTO(inputField.getTagValue(), inputField.getCommentValue(),
                    inputField.getImageURLValue(),  "Author", new MillisecondsTimePoint(timer.getTime()),
                    new MillisecondsTimePoint(timer.getTime()), true);
            listContainingPreviewTag.add(previewTag);
            tagPreviewCellList.setRowData(listContainingPreviewTag);

            setVisible(!inputField.getTagValue().isEmpty());
        }
    }

    public class TagCreationInputPanel extends FlowPanel {
        private TextBox tagTextBox, imageURLTextBox;
        private TextArea commentTextArea;

        public TagCreationInputPanel(StringMessages stringMessages) {
            setWidth("100%");

            tagTextBox = new TextBox();
            tagTextBox.setStyleName(TagPanelResources.INSTANCE.style().inputPanelTag());
            tagTextBox.setTitle(stringMessages.tagLabelTag());
            tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
            add(tagTextBox);

            imageURLTextBox = new TextBox();
            imageURLTextBox.setStyleName(TagPanelResources.INSTANCE.style().inputPanelImageURL());
            imageURLTextBox.setTitle(stringMessages.tagLabelImageURL());
            imageURLTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
            add(imageURLTextBox);

            commentTextArea = new TextArea();
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

        private final Button closeButton, saveChangesButton, cancelChangesButton;
        private final FlowPanel mainPanel;
        private final CellTable<TagButton> customTagButtonsTable;
        private final TagCreationInputPanel inputPanel;
        private final Button addCustomTagButton;
        private final TagPreviewPanel tagPreviewPanel;
        private final FlowPanel controlButttonPanel;
        private TagButton selectedTagButton = null;

        public EditCustomTagButtonsDialog(Panel customButtonsPanel) {
            TagPanelStyle style = TagPanelResources.INSTANCE.style();
            setGlassEnabled(true);
            setText(stringMessages.tagEditCustomTagButtons());
            setWidth("350px");

            mainPanel = new FlowPanel();
            mainPanel.setWidth("100%");

            // Table with tag buttons
            customTagButtonsTable = new CellTable<TagButton>();
            customTagButtonsTable.setStyleName(style.tagButtonTable());
            TextColumn<TagButton> tagColumn = new TextColumn<TagButton>() {
                @Override
                public String getValue(TagButton button) {
                    return button.getTag();
                }
            };
            TextColumn<TagButton> imageURLColumn = new TextColumn<TagButton>() {
                @Override
                public String getValue(TagButton button) {
                    return button.getImageURL();
                }
            };
            TextColumn<TagButton> commentColumn = new TextColumn<TagButton>() {
                @Override
                public String getValue(TagButton button) {
                    return button.getComment();
                }
            };

            ImagesBarColumn<TagButton, EditTagButtonsImagesBarCell> actionsColumn = new ImagesBarColumn<TagButton, EditTagButtonsImagesBarCell>(
                    new EditTagButtonsImagesBarCell(stringMessages));
            actionsColumn.setFieldUpdater(new FieldUpdater<TagButton, String>() {
                @Override
                public void update(int index, TagButton button, String value) {
                    if (LeaderboardConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                        final int heightOfTagButtonPanel = customButtonsPanel.getOffsetHeight();
                        
                        customTagButtons.remove(button);
                        customButtonsPanel.remove(button);
                        customTagButtonsTable.setRowData(customTagButtons);
                        
                        final int deltaHeight = customButtonsPanel.getOffsetHeight() - heightOfTagButtonPanel;
                        /* 
                         * If the height of the customButtonsPanel has changed ( delta not equals to 0 ), 
                         * the footerWidget of the TaggingPanel has a different height, 
                         * which in this case might cause the contentWidget to be to small.                     
                        */
                        if(deltaHeight != 0) { 
                            panel.setContentWidget(contentPanel);
                        }
                    } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                        selectedTagButton = button;

                        inputPanel.setTagValue(button.getTag());
                        inputPanel.setImageURLValue(button.getImageURL());
                        inputPanel.setCommentValue(button.getComment());

                        tagPreviewPanel.renderPreview();

                        saveChangesButton.setVisible(true);
                        cancelChangesButton.setVisible(true);
                        addCustomTagButton.setVisible(false);
                        customTagButtonsTable.setVisible(false);
                    }
                }
            });

            customTagButtonsTable.addColumn(tagColumn, stringMessages.tagLabelTag());
            customTagButtonsTable.addColumn(imageURLColumn, stringMessages.tagLabelImageURL());
            customTagButtonsTable.addColumn(commentColumn, stringMessages.tagLabelComment());
            customTagButtonsTable.addColumn(actionsColumn, stringMessages.tagLabelAction());
            setRowData(customTagButtons);

            // input panel
            inputPanel = new TagCreationInputPanel(stringMessages);

            // contol buttons
            controlButttonPanel = new FlowPanel();
            controlButttonPanel.setStyleName(TagPanelResources.INSTANCE.style().footerPanel());

            addCustomTagButton = new Button(stringMessages.tagAddCustomTagButton());
            addCustomTagButton.setStyleName(style.footerButton());
            addCustomTagButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (inputPanel.getTagValue().length() > 0) {
                        final int heightOfTagButtonPanel = customButtonsPanel.getOffsetHeight();
                        
                        TagButton tagButton = new TagButton(inputPanel.getTagValue(), inputPanel.getTagValue(),
                                inputPanel.getImageURLValue(), inputPanel.getCommentValue());
                        inputPanel.clearAllValues();
                        tagPreviewPanel.renderPreview();

                        customTagButtons.add(tagButton);
                        customButtonsPanel.add(tagButton);
                        setRowData(customTagButtons);
                        
                        final int deltaHeight = customButtonsPanel.getOffsetHeight() - heightOfTagButtonPanel;
                        /* 
                         * If the height of the customButtonsPanel has changed ( delta not equals to 0 ), 
                         * the footerWidget of the TaggingPanel has a different height, 
                         * which might cause the contentWidget to overlap the footerWidget.                     
                        */
                        if(deltaHeight != 0) { 
                            panel.setContentWidget(contentPanel);
                        }
                    } else {
                        Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
                    }
                }
            });

            saveChangesButton = new Button(stringMessages.save());
            saveChangesButton.setVisible(false);
            saveChangesButton.setStyleName(style.footerButton());
            saveChangesButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (inputPanel.getTagValue().length() > 0) {
                        selectedTagButton.setText(inputPanel.getTagValue());
                        selectedTagButton.setTag(inputPanel.getTagValue());
                        selectedTagButton.setComment(inputPanel.getCommentValue());
                        selectedTagButton.setImageURL(inputPanel.getImageURLValue());
                        inputPanel.clearAllValues();
                        tagPreviewPanel.renderPreview();
                        customTagButtonsTable.redraw();

                        saveChangesButton.setVisible(false);
                        cancelChangesButton.setVisible(false);
                        addCustomTagButton.setVisible(true);
                        selectedTagButton = null;
                        setRowData(customTagButtons);
                    } else {
                        Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
                    }
                }
            });

            cancelChangesButton = new Button(stringMessages.cancel());
            cancelChangesButton.setVisible(false);
            cancelChangesButton.setStyleName(style.footerButton());
            cancelChangesButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    inputPanel.clearAllValues();
                    tagPreviewPanel.renderPreview();
                    saveChangesButton.setVisible(false);
                    cancelChangesButton.setVisible(false);
                    addCustomTagButton.setVisible(true);
                    setRowData(customTagButtons);
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
            controlButttonPanel.add(addCustomTagButton);
            controlButttonPanel.add(cancelChangesButton);
            controlButttonPanel.add(saveChangesButton);

            tagPreviewPanel = new TagPreviewPanel(inputPanel);

            mainPanel.add(customTagButtonsTable);
            mainPanel.add(inputPanel);
            mainPanel.add(controlButttonPanel);
            mainPanel.add(tagPreviewPanel);
            mainPanel.add(closeButton);
            mainPanel.ensureDebugId("Test");
            getElement().getStyle().setBackgroundColor("white");
            
            setWidget(mainPanel);
        }

        private void hideDialog() {
            this.hide();
        }

        private void setRowData(List<TagButton> buttons) {
            customTagButtonsTable.setRowData(buttons);
            customTagButtonsTable.setVisible(buttons.size() > 0);
        }
    }

    private class TagFilterPanel extends FlowPanel implements KeyUpHandler, FilterWithUI<TagDTO> {
        private final static String LOCAL_STORAGE_TAGS_FILTER_SETS_KEY = "sailingAnalytics.raceBoard.tagsFilterSets";

        private final TagPanelStyle css = TagPanelResources.INSTANCE.style();
        private final TextBox searchTextBox;
        private final Button clearTextBoxButton;
        private final Button filterSettingsButton;
        private final TagsFilterSets tagFilterSets;
        private final FlowPanel searchBoxPanel;
        private final StringMessages stringMessages;
        private final TagListProvider tagProvider;
        private final AbstractListFilter<TagDTO> filter;

        private FilterSet<TagDTO, FilterWithUI<TagDTO>> lastActiveTagFilterSet;

        public TagFilterPanel(StringMessages stringMessages, TagListProvider tagProvider) {
            css.ensureInjected();
            this.stringMessages = stringMessages;
            this.tagProvider = tagProvider;
            this.setStyleName(css.tagFilterContainer());

            TagsFilterSets loadedTagsFilterSets = loadTagsFilterSets();
            if (loadedTagsFilterSets != null) {
                tagFilterSets = loadedTagsFilterSets;
                tagProvider.setTagsFilterSet(tagFilterSets.getActiveFilterSetWithGeneralizedType());
            } else {
                tagFilterSets = createAndAddDefaultTagsFilter();
                storeTagsFilterSets(tagFilterSets);
            }

            filter = new AbstractListFilter<TagDTO>() {
                @Override
                public Iterable<String> getStrings(TagDTO tag) {
                    final List<String> result = new ArrayList<>(
                            Arrays.asList(tag.getTag().toLowerCase(), tag.getComment()));
                    return result;
                }
            };

            Button submitButton = new Button();
            submitButton.setStyleName(css.tagFilterButton());
            submitButton.addStyleName(css.tagFilterSearchButton());
            submitButton.addStyleName(css.searchButtonBackgroundImage());

            searchTextBox = new TextBox();
            searchTextBox.getElement().setAttribute("placeholder", stringMessages.tagSearchTags());
            searchTextBox.addKeyUpHandler(this);
            searchTextBox.setStyleName(css.tagFilterSearchInput());

            clearTextBoxButton = new Button();
            clearTextBoxButton.setStyleName(css.tagFilterButton());
            clearTextBoxButton.addStyleName(css.tagFilterClearButton());
            clearTextBoxButton.addStyleName(css.clearButtonBackgroundImage());
            clearTextBoxButton.addStyleName(css.tagFilterHiddenButton());
            clearTextBoxButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    clearSelection();
                }
            });

            filterSettingsButton = new Button("");
            filterSettingsButton.setStyleName(css.tagFilterButton());
            filterSettingsButton.addStyleName(css.tagFilterFilterButton());
            filterSettingsButton.setTitle(stringMessages.tagsFilter());
            filterSettingsButton.addStyleName(css.filterInactiveButtonBackgroundImage());
            filterSettingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showEditTagsFiltersDialog();
                }
            });

            searchBoxPanel = new FlowPanel();
            searchBoxPanel.setStyleName(css.tagFilterSearchBox());
            searchBoxPanel.add(submitButton);
            searchBoxPanel.add(searchTextBox);
            searchBoxPanel.add(clearTextBoxButton);
            add(searchBoxPanel);
            add(filterSettingsButton);
        }

        private void showEditTagsFiltersDialog() {
            TagsFilterSetsDialog tagsFilterSetsDialog = new TagsFilterSetsDialog(tagFilterSets, stringMessages,
                    new DialogCallback<TagsFilterSets>() {
                        @Override
                        public void ok(final TagsFilterSets newTagsFilterSets) {
                            tagFilterSets.getFilterSets().clear();
                            tagFilterSets.getFilterSets().addAll(newTagsFilterSets.getFilterSets());
                            tagFilterSets.setActiveFilterSet(newTagsFilterSets.getActiveFilterSet());

                            tagProvider.setTagsFilterSet(newTagsFilterSets.getActiveFilterSetWithGeneralizedType());
                            tagProvider.updateFilteredTags();
                            tagProvider.refresh();
                            updateTagsFilterControlState(newTagsFilterSets);
                            storeTagsFilterSets(newTagsFilterSets);
                        }

                        @Override
                        public void cancel() {
                        }

                    });

            tagsFilterSetsDialog.show();
        }

        /**
         * Updates the tags filter checkbox state by setting its check mark and updating its label according to the
         * current filter selected
         */
        private void updateTagsFilterControlState(TagsFilterSets filterSets) {
            String tagsFilterTitle = stringMessages.tagsFilter();
            FilterSet<TagDTO, FilterWithUI<TagDTO>> activeFilterSet = filterSets.getActiveFilterSet();
            if (activeFilterSet != null) {
                if (lastActiveTagFilterSet == null) {
                    filterSettingsButton.removeStyleName(css.filterInactiveButtonBackgroundImage());
                    filterSettingsButton.addStyleName(css.filterActiveButtonBackgroundImage());
                }
                lastActiveTagFilterSet = activeFilterSet;
            } else {
                if (lastActiveTagFilterSet != null) {
                    filterSettingsButton.removeStyleName(css.filterActiveButtonBackgroundImage());
                    filterSettingsButton.addStyleName(css.filterInactiveButtonBackgroundImage());
                }
                lastActiveTagFilterSet = null;
            }
            if (lastActiveTagFilterSet != null) {
                filterSettingsButton.setTitle(tagsFilterTitle + " (" + lastActiveTagFilterSet.getName() + ")");
            } else {
                filterSettingsButton.setTitle(tagsFilterTitle);
            }
        }

        private TagsFilterSets loadTagsFilterSets() {
            TagsFilterSets result = null;
            Storage localStorage = Storage.getLocalStorageIfSupported();
            if (localStorage != null) {
                try {
                    String jsonAsLocalStore = localStorage.getItem(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY);
                    if (jsonAsLocalStore != null && !jsonAsLocalStore.isEmpty()) {
                        TagsFilterSetsJsonDeSerializer deserializer = new TagsFilterSetsJsonDeSerializer();
                        JSONValue value = JSONParser.parseStrict(jsonAsLocalStore);
                        if (value.isObject() != null) {
                            result = deserializer.deserialize((JSONObject) value);
                        }
                    }
                } catch (Exception e) {
                    // exception during loading of tag filters from local storage
                }
            }
            return result;
        }

        private void storeTagsFilterSets(TagsFilterSets newTagsFilterSets) {
            Storage localStorage = Storage.getLocalStorageIfSupported();
            if (localStorage != null) {
                // delete old value
                localStorage.removeItem(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY);

                // store the tags filter set
                TagsFilterSetsJsonDeSerializer serializer = new TagsFilterSetsJsonDeSerializer();
                JSONObject jsonObject = serializer.serialize(newTagsFilterSets);
                localStorage.setItem(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY, jsonObject.toString());
            }
        }

        private TagsFilterSets createAndAddDefaultTagsFilter() {
            TagsFilterSets filterSets = new TagsFilterSets();

            // TODO add standard filters here

            return filterSets;
        }

        private void clearSelection() {
            searchTextBox.setText("");
            clearTextBoxButton.addStyleName(css.tagFilterHiddenButton());
            onKeyUp(null);
        }

        @Override
        public boolean matches(TagDTO tag) {
            final Iterable<String> lowercaseKeywords = Util
                    .splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchTextBox.getText().toLowerCase());
            return !Util.isEmpty(filter.applyFilter(lowercaseKeywords, Collections.singleton(tag)));
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String validate(StringMessages stringMessages) {
            return null;
        }

        @Override
        public String getLocalizedName(StringMessages stringMessages) {
            return getName();
        }

        @Override
        public String getLocalizedDescription(StringMessages stringMessages) {
            return getName();
        }

        @Override
        public FilterWithUI<TagDTO> copy() {
            return null;
        }

        @Override
        public FilterUIFactory<TagDTO> createUIFactory() {
            return null;
        }

        /**
         * @param event
         *            ignored; may be <code>null</code>
         */
        @Override
        public void onKeyUp(KeyUpEvent event) {
            String newValue = searchTextBox.getValue();
            if (newValue.trim().isEmpty()) {
                removeSearchFilter();
                clearTextBoxButton.addStyleName(css.tagFilterHiddenButton());
            } else {
                if (newValue.length() >= 2) {
                    clearTextBoxButton.removeStyleName(css.tagFilterHiddenButton());
                    ensureSearchFilterIsSet();
                    tagProvider.setTagsFilterSet(tagProvider.getTagsFilterSet()); //
                }
            }
        }

        private void ensureSearchFilterIsSet() {
            if (tagProvider.getTagsFilterSet() == null
                    || !Util.contains(tagProvider.getTagsFilterSet().getFilters(), this)) {
                FilterSet<TagDTO, Filter<TagDTO>> newFilterSetWithThis = new FilterSet<>(getName());
                if (tagProvider.getTagsFilterSet() != null) {
                    for (Filter<TagDTO> oldFilter : tagProvider.getTagsFilterSet().getFilters()) {
                        newFilterSetWithThis.addFilter(oldFilter);
                    }
                }
                newFilterSetWithThis.addFilter(this);
                tagProvider.setTagsFilterSet(newFilterSetWithThis);
            }
        }

        private void removeSearchFilter() {
            if (tagProvider.getTagsFilterSet() != null
                    && Util.contains(tagProvider.getTagsFilterSet().getFilters(), this)) {
                FilterSet<TagDTO, Filter<TagDTO>> newFilterSetWithThis = new FilterSet<>(
                        tagProvider.getTagsFilterSet().getName());
                for (Filter<TagDTO> oldFilter : tagProvider.getTagsFilterSet().getFilters()) {
                    if (oldFilter != this) {
                        newFilterSetWithThis.addFilter(oldFilter);
                    }
                }
                tagProvider.setTagsFilterSet(newFilterSetWithThis);
            }
        }
    }

    private final HeaderPanel panel;
    private final TagCreationPanel tagCreationPanel;
    private final Panel filterbarPanel;
    private final Label currentFilterLabel;
    private final ScrollPanel contentPanel;
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

        //TODO Add this label to ui
        currentFilterLabel = new Label();
        tagListProvider = new TagListProvider(currentFilterLabel);
        customTagButtons = new ArrayList<TagButton>();

        panel = new HeaderPanel();
        filterbarPanel = new TagFilterPanel(stringMessages, tagListProvider);
        tagCellList = new CellList<TagDTO>(new TagCell(false), CellListResources.INSTANCE);
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
        
        //contentPanel.add(currentFilterLabel);
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
        if (isLoggedInAndRaceLogAvailable()) {
            if (tag.length() > 0) {
                sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag, comment,
                        imageURL, true, new MillisecondsTimePoint(timer.getTime()), new AsyncCallback<SuccessInfo>() {
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
                Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
            }
        }

    }

    private void removeTagFromRaceLog(TagDTO tag) {
        sailingService.removeTagFromRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag,
                new AsyncCallback<SuccessInfo>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(stringMessages.tagNotRemoved(), NotificationType.ERROR);
                        GWT.log(caught.toString());
                    }

                    @Override
                    public void onSuccess(SuccessInfo result) {
                        if (result.isSuccessful()) {
                            tagListProvider.getAllTags().remove(tag);
                            updateContent();
                            Notification.notify(stringMessages.tagRemovedSuccessfully(), NotificationType.SUCCESS);
                        } else {
                            Notification.notify(stringMessages.tagNotRemoved() + " " + result.getMessage(),
                                    NotificationType.ERROR);
                        }
                    }
                });
    }

    private boolean isLoggedInAndRaceLogAvailable() {
        return !(userService.getCurrentUser() == null || leaderboardName == null || raceColumn == null
                || fleet == null);
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
