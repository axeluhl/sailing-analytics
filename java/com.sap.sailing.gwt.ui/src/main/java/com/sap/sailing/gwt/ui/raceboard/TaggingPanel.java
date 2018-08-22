package com.sap.sailing.gwt.ui.raceboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
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
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.adminconsole.ImagesBarColumn;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigImagesBarCell;
import com.sap.sailing.gwt.ui.client.GwtJsonDeSerializer;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterUIFactory;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.TagFilterSets;
import com.sap.sailing.gwt.ui.client.shared.filter.TagFilterSetsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.TagsFilterSetsJsonDeSerializer;
import com.sap.sailing.gwt.ui.raceboard.TaggingPanel.TagCellListResources.TagCellListStyle;
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

    /* Interfaces */
    public interface TagPanelResources extends ClientBundle {
        public static final TagPanelResources INSTANCE = GWT.create(TagPanelResources.class);

        // TODO image is stored in wrong folder
        @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
        ImageResource editIcon();

        @Source("com/sap/sailing/gwt/ui/client/images/lock.png")
        ImageResource privateIcon();

        @Source("com/sap/sailing/gwt/ui/client/images/unlock.png")
        ImageResource publicIcon();

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
            // general
            String hidden();
            String taggingPanel();
            String buttonsPanel();
            String confirmationDialog();
            String confirmationDialogPanel();

            // tag cells
            String tagCell();
            String tagCellHeading();
            String tagCellCreated();
            String tagCellComment();
            String tagCellImage();
            String tagCellListPanel();

            // tag-buttons
            String tagButtonDialog(); // dialog itself
            String tagButtonDialogPanel();
            String tagDialogButton(); // button in dialog
            String tagButtonTable();
            String tagPreviewPanel();

            // tag input / creation
            String tagCreationPanel();
            String tagInputPanel();
            String tagInputPanelTag();
            String tagInputPanelComment();
            String tagInputPanelImageURL();
            String tagInputPanelIsVisibleForPublic();

            // tag filtering
            String tagFilterButton();
            String tagFilterHiddenButton();
            String tagFilterClearButton();
            String tagFilterSearchButton();
            String tagFilterSettingsButton();
            String tagFilterFilterButton();
            String tagFilterPanel();
            String tagFilterSearchBox();
            String tagFilterSearchInput();
            String tagFilterCurrentSelection();

            // images
            String imageActiveFilter();
            String imageInactiveFilter();
            String imageSearch();
            String imageClearSearch();
            String imageSettings();
        }
    }

    public interface TagCellListResources extends CellList.Resources {
        public static final TagCellListResources INSTANCE = GWT.create(TagCellListResources.class);

        @Override
        @Source("tagging-celllist.css")
        public TagCellListStyle cellListStyle();

        public interface TagCellListStyle extends CellList.Style {
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

        @Template("<div class='{0}'><div class='{1}'><img src='{6}'>{3}</div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml privateCell(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag,
                SafeHtml createdAt, SafeHtml content, SafeUri safeUri);

        @Template("<div class='{0}'><div class='{1}'><img src='{6}'>{3}<button>X</button></div><div class='{2}'>{4}</div>{5}</div>")
        SafeHtml privateCellRemovable(String styleTag, String styleTagHeading, String styleTagCreated, SafeHtml tag,
                SafeHtml createdAt, SafeHtml content, SafeUri safeUri);

        @Template("<div class='{0}'><img src='{2}'/></div><div class='{1}'>{3}</div>")
        SafeHtml contentWithCommentWithImage(String styleTagImage, String styleTagComment, SafeUri imageURL,
                SafeHtml comment);

        @Template("<div class='{0}'>{1}</div>")
        SafeHtml contentWithCommentWithoutImage(String styleTagComment, SafeHtml comment);

        @Template("<div class='{0}'><img src='{1}'/></div>")
        SafeHtml contentWithoutCommentWithImage(String styleTagImage, SafeUri imageURL);
    }

    /* Misc. classes */
    /**
     * Used to display tags in various locations.
     */
    private class TagCell extends AbstractCell<TagDTO> {

        private final TagCellTemplate tagCellTemplate = GWT.create(TagCellTemplate.class);
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

            SafeUri safeIsPrivateImageUri = resources.privateIcon().getSafeUri();

            SafeHtml content = SafeHtmlUtils.EMPTY_SAFE_HTML;

            if (!tag.getComment().isEmpty() && tag.getImageURL().isEmpty()) {
                content = tagCellTemplate.contentWithCommentWithoutImage(style.tagCellComment(), safeComment);
            } else if (tag.getComment().isEmpty() && !tag.getImageURL().isEmpty()) {
                content = tagCellTemplate.contentWithoutCommentWithImage(style.tagCellImage(), trustedImageURL);
            } else if (!tag.getComment().isEmpty() && !tag.getImageURL().isEmpty()) {
                content = tagCellTemplate.contentWithCommentWithImage(style.tagCellImage(), style.tagCellComment(),
                        trustedImageURL, safeComment);
            }

            SafeHtml cell;
            if (!isPreviewCell && userService.getCurrentUser() != null
                    && (tag.getUsername().equals(userService.getCurrentUser().getName())
                            || userService.getCurrentUser().hasRole("admin"))) {
                if (tag.isVisibleForPublic()) {
                    cell = tagCellTemplate.cellRemovable(style.tagCell(), style.tagCellHeading(),
                            style.tagCellCreated(), safeTag, safeCreated, content);
                } else {
                    cell = tagCellTemplate.privateCellRemovable(style.tagCell(), style.tagCellHeading(),
                            style.tagCellCreated(), safeTag, safeCreated, content, safeIsPrivateImageUri);
                }

            } else {
                if (tag.isVisibleForPublic()) {
                    cell = tagCellTemplate.cell(style.tagCell(), style.tagCellHeading(), style.tagCellCreated(),
                            safeTag, safeCreated, content);
                } else {
                    cell = tagCellTemplate.privateCell(style.tagCell(), style.tagCellHeading(), style.tagCellCreated(),
                            safeTag, safeCreated, content, safeIsPrivateImageUri);
                }
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
                    new ConfirmationDialog(stringMessages.tagConfirmDeletionHeading(),
                            stringMessages.tagConfirmDeletion(tag.getTag()), (confirmed) -> {
                                if (confirmed) {
                                    removeTagFromRaceLog(tag);
                                }
                            });
                }
            }
        }
    }

    /**
     * Shows current selected filter.
     */
    private class TagFilterLabel extends Label {
        public TagFilterLabel() {
            addStyleName(style.tagFilterCurrentSelection());
            setText(stringMessages.tagCurrentFilter());
            tagListProvider.addObserveringLabel(this);
        }

        public void update(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
            if (tagsFilterSet != null && !tagsFilterSet.getName().isEmpty()) {
                setText(stringMessages.tagCurrentFilter() + " " + tagsFilterSet.getName());
                removeStyleName(style.hidden());
            } else {
                setText("");
                addStyleName(style.hidden());
            }
            panel.setContentWidget(contentPanel);
        }
    }

    /**
     * Used to store tag button data and creates new tag event when clicking the button.
     */
    private class TagButton extends Button implements Serializable {

        private static final long serialVersionUID = -722157125410637316L;

        private String tag, imageURL, comment;
        private boolean visibleForPublic;

        public TagButton(String buttonName, String tag, String imageURL, String comment, boolean visibleForPublic) {
            super(buttonName);
            setStyleName(style.tagDialogButton());

            this.tag = tag;
            this.imageURL = imageURL;
            this.comment = comment;
            this.visibleForPublic = visibleForPublic;

            addStyleName("gwt-Button");
            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addTagToRaceLog(getTag(), getComment(), getImageURL(), isVisibleForPublic());
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

        public boolean isVisibleForPublic() {
            return visibleForPublic;
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

    public class TagButtonsJsonDeSerializer implements GwtJsonDeSerializer<List<TagButton>> {

        private static final String FIELD_TAG_BUTTONS = "tagButtons";
        private static final String FIELD_BUTTON_NAME = "buttonName";
        private static final String FIELD_TAG = "tag";
        private static final String FIELD_COMMENT = "comment";
        private static final String FIELD_IMAGE_URL = "imageURL";
        private static final String FIELD_VISIBLE_FOR_PUBLIC = "public";

        @Override
        public JSONObject serialize(List<TagButton> tagButtons) {
            JSONObject result = new JSONObject();

            JSONArray tagButtonsArray = new JSONArray();
            result.put(FIELD_TAG_BUTTONS, tagButtonsArray);

            int i = 0;
            for (TagButton button : tagButtons) {
                JSONObject tagButtonObject = new JSONObject();
                tagButtonsArray.set(i++, tagButtonObject);
                tagButtonObject.put(FIELD_BUTTON_NAME, new JSONString(button.getText()));
                tagButtonObject.put(FIELD_TAG, new JSONString(button.getTag()));
                tagButtonObject.put(FIELD_COMMENT, new JSONString(button.getComment()));
                tagButtonObject.put(FIELD_IMAGE_URL, new JSONString(button.getImageURL()));
                tagButtonObject.put(FIELD_VISIBLE_FOR_PUBLIC, JSONBoolean.getInstance(button.isVisibleForPublic()));
            }
            return result;
        }

        @Override
        public List<TagButton> deserialize(JSONObject rootObject) {
            List<TagButton> result = null;
            if (rootObject != null) {
                result = new ArrayList<TagButton>();
                JSONArray tagButtonsArray = (JSONArray) rootObject.get(FIELD_TAG_BUTTONS);
                for (int i = 0; i < tagButtonsArray.size(); i++) {
                    JSONObject tagButtonValue = (JSONObject) tagButtonsArray.get(i);
                    JSONString tagButtonName = (JSONString) tagButtonValue.get(FIELD_BUTTON_NAME);
                    JSONString tagButtonTag = (JSONString) tagButtonValue.get(FIELD_TAG);
                    JSONString tagButtonComment = (JSONString) tagButtonValue.get(FIELD_COMMENT);
                    JSONString tagButtonImageURL = (JSONString) tagButtonValue.get(FIELD_IMAGE_URL);
                    JSONBoolean tagButtonVisibleForPublic = (JSONBoolean) tagButtonValue.get(FIELD_VISIBLE_FOR_PUBLIC);
                    result.add(new TagButton(tagButtonName.stringValue(), tagButtonTag.stringValue(),
                            tagButtonComment.stringValue(), tagButtonImageURL.stringValue(),
                            tagButtonVisibleForPublic.booleanValue()));
                }
            }
            return result;
        }

    }

    /* Panel */
    /**
     * Panel used to create tags and tag buttons in side menu of RaceBoard.
     */
    private class TagCreationPanel extends FlowPanel {

        private static final String LOCAL_STORAGE_TAG_BUTTONS_KEY = "sailingAnalytics.raceBoard.tagButtons";

        private final Panel tagButtonsPanel;

        public TagCreationPanel(StringMessages stringMessages) {
            setStyleName(style.tagCreationPanel());

            TagInputPanel inputPanel = new TagInputPanel(stringMessages);

            tagButtonsPanel = new FlowPanel();
            tagButtonsPanel.setStyleName(style.buttonsPanel());
            loadAllTagButtons();
            updateButtons();

            Button createTagFromTextBoxes = new Button(stringMessages.tagAddTag());
            createTagFromTextBoxes.setStyleName(style.tagDialogButton());
            createTagFromTextBoxes.addStyleName("gwt-Button");
            createTagFromTextBoxes.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isLoggedInAndRaceLogAvailable()) {
                        addTagToRaceLog(inputPanel.getTag(), inputPanel.getComment(), inputPanel.getImageURL(),
                                inputPanel.isVisibleForPublic());
                        inputPanel.clearAllValues();
                    }
                }
            });

            final TagCreationPanel INSTANCE = this;
            Button editCustomTagButtons = new Button(stringMessages.tagEditCustomTagButtons());
            editCustomTagButtons.setStyleName(style.tagDialogButton());
            editCustomTagButtons.addStyleName("gwt-Button");
            editCustomTagButtons.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (isLoggedInAndRaceLogAvailable()) {
                        new TagButtonDialog(INSTANCE);
                    }
                }
            });

            Panel standardButtonsPanel = new FlowPanel();
            standardButtonsPanel.setStyleName(style.buttonsPanel());
            standardButtonsPanel.add(editCustomTagButtons);
            standardButtonsPanel.add(createTagFromTextBoxes);

            add(inputPanel);
            add(standardButtonsPanel);
            add(tagButtonsPanel);
        }

        public void updateButtons() {
            /*
             * If the height of the customButtonsPanel has changed after deleting (delta not equals to 0 ), the
             * footerWidget of the TaggingPanel has a different height, which in this case might cause the contentWidget
             * to be to small.
             */
            final int oldHeight = tagButtonsPanel.getOffsetHeight();
            tagButtonsPanel.clear();
            tagButtons.forEach(button -> {
                tagButtonsPanel.add(button);
            });
            if ((tagButtonsPanel.getOffsetHeight() - oldHeight) != 0) {
                panel.setContentWidget(contentPanel);
            }
        }

        public void storeAllTagButtons() {
            TagButtonsJsonDeSerializer serializer = new TagButtonsJsonDeSerializer();
            JSONObject jsonObject = serializer.serialize(tagButtons);
            userService.setPreference(LOCAL_STORAGE_TAG_BUTTONS_KEY, jsonObject.toString(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    Notification.notify(stringMessages.tagButtonNotSavable(), NotificationType.WARNING);
                }

                @Override
                public void onSuccess(Void result) {
                }
            });
        }

        public void loadAllTagButtons() {
            tagButtonsPanel.clear();
            if (userService.getCurrentUser() != null) {
                userService.getPreference(LOCAL_STORAGE_TAG_BUTTONS_KEY, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // do nothing
                    }

                    @Override
                    public void onSuccess(String result) {
                        if (result != null && !result.isEmpty()) {
                            final TagButtonsJsonDeSerializer deserializer = new TagButtonsJsonDeSerializer();
                            final JSONValue value = JSONParser.parseStrict(result);
                            if (value.isObject() != null) {
                                tagButtons = deserializer.deserialize((JSONObject) value);
                                updateButtons();
                                return;
                            }
                        }
                        tagButtons = new ArrayList<TagButton>();
                        updateButtons();
                    }
                });
            } else {
                tagButtons = new ArrayList<TagButton>();
                updateButtons();
            }
        }
    }

    /**
     * Panel containing input fields for tag/tag button creation and modification.
     */
    private class TagInputPanel extends FlowPanel {

        // default value for "Visible for public" checkbox
        private static final boolean DEFAULT_VISIBLE_FOR_PUBLIC = true;

        private final TextBox tagTextBox, imageURLTextBox;
        private final TextArea commentTextArea;
        private final CheckBox visibleForPublicCheckBox;

        public TagInputPanel(StringMessages stringMessages) {
            setStyleName(style.tagInputPanel());

            tagTextBox = new TextBox();
            tagTextBox.setStyleName(style.tagInputPanelTag());
            tagTextBox.setTitle(stringMessages.tagLabelTag());
            tagTextBox.getElement().setAttribute("maxlength", Integer.toString(TagDTO.MAX_TAG_LENGTH));
            tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
            add(tagTextBox);

            imageURLTextBox = new TextBox();
            imageURLTextBox.setStyleName(style.tagInputPanelImageURL());
            imageURLTextBox.setTitle(stringMessages.tagLabelImageURL());
            imageURLTextBox.getElement().setAttribute("maxlength", Integer.toString(TagDTO.MAX_IMAGE_URL_LENGTH));
            imageURLTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
            add(imageURLTextBox);

            commentTextArea = new TextArea();
            commentTextArea.setStyleName(style.tagInputPanelComment());
            commentTextArea.setVisibleLines(4);
            commentTextArea.getElement().setAttribute("maxlength", Integer.toString(TagDTO.MAX_COMMENT_LENGTH));
            commentTextArea.setTitle(stringMessages.tagLabelComment());
            commentTextArea.getElement().setPropertyString("placeholder", stringMessages.tagLabelComment());
            add(commentTextArea);

            SimplePanel checkboxWrapper = new SimplePanel();
            visibleForPublicCheckBox = new CheckBox(stringMessages.tagVisibleForPublicCheckBox());
            visibleForPublicCheckBox.setValue(DEFAULT_VISIBLE_FOR_PUBLIC);
            checkboxWrapper.setStyleName(style.tagInputPanelIsVisibleForPublic());
            checkboxWrapper.setWidget(visibleForPublicCheckBox);
            add(checkboxWrapper);
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

        public CheckBox getVisibleForPublicCheckBox() {
            return visibleForPublicCheckBox;
        }

        public String getTag() {
            return tagTextBox.getValue();
        }

        public String getComment() {
            return commentTextArea.getValue();
        }

        public String getImageURL() {
            return imageURLTextBox.getValue();
        }

        public boolean isVisibleForPublic() {
            return visibleForPublicCheckBox.getValue();
        }

        public void setTag(String tag) {
            tagTextBox.setValue(tag);
        }

        public void setComment(String comment) {
            commentTextArea.setValue(comment);
        }

        public void setImageURL(String imageURL) {
            imageURLTextBox.setValue(imageURL);
        }

        public void setVisibleForPublic(boolean visibleForPublic) {
            visibleForPublicCheckBox.setValue(visibleForPublic);
        }

        public void clearAllValues() {
            tagTextBox.setText("");
            imageURLTextBox.setText("");
            commentTextArea.setText("");
            setVisibleForPublic(DEFAULT_VISIBLE_FOR_PUBLIC);
        }
    }

    /**
     * Panel used to preview a tag.
     */
    private class TagPreviewPanel extends FlowPanel {

        private final CellList<TagDTO> tagPreviewCellList;
        private List<TagDTO> listContainingPreviewTag;

        public TagPreviewPanel(TagInputPanel inputPanel) {
            setStyleName(style.tagPreviewPanel());

            tagPreviewCellList = new CellList<TagDTO>(new TagCell(true), cellResources);
            listContainingPreviewTag = new ArrayList<TagDTO>();

            add(new Label(stringMessages.tagPreview()));
            add(tagPreviewCellList);

            inputPanel.getTagTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    renderPreview(inputPanel);
                }

            });
            inputPanel.getImageURLTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    renderPreview(inputPanel);
                }

            });
            inputPanel.getCommentTextArea().addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    renderPreview(inputPanel);
                }

            });
            inputPanel.getVisibleForPublicCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    renderPreview(inputPanel);
                }
            });
            renderPreview(inputPanel);
        }

        public void renderPreview(TagInputPanel inputPanel) {
            listContainingPreviewTag.removeAll(listContainingPreviewTag);
            listContainingPreviewTag.add(new TagDTO(inputPanel.getTag(), inputPanel.getComment(),
                    inputPanel.getImageURL(), userService.getCurrentUser().getName(), inputPanel.isVisibleForPublic(),
                    new MillisecondsTimePoint(timer.getTime()), new MillisecondsTimePoint(timer.getTime())));
            tagPreviewCellList.setRowData(listContainingPreviewTag);

            setVisible(!inputPanel.getTag().isEmpty());
        }
    }

    /**
     * Panel used to select and modify tag filter.
     */
    private class TagFilterPanel extends FlowPanel implements KeyUpHandler, FilterWithUI<TagDTO> {

        private final static String LOCAL_STORAGE_TAGS_FILTER_SETS_KEY = "sailingAnalytics.raceBoard.tagsFilterSets";

        private FilterSet<TagDTO, FilterWithUI<TagDTO>> lastActiveTagFilterSet;
        private TagFilterSets tagFilterSets;
        private TextBox searchTextBox;
        private Button clearTextBoxButton, filterSettingsButton;
        private TagFilterLabel currentFilter;
        private final AbstractListFilter<TagDTO> filter;

        public TagFilterPanel() {
            setStyleName(style.tagFilterPanel());

            loadTagFilterSets();
            filter = new AbstractListFilter<TagDTO>() {
                @Override
                public Iterable<String> getStrings(TagDTO tag) {
                    final List<String> result = new ArrayList<>(
                            Arrays.asList(tag.getTag().toLowerCase(), tag.getComment()));
                    return result;
                }
            };
            initializeUI();
        }

        private void initializeUI() {
            setStyleName(style.tagFilterPanel());

            Button submitButton = new Button();
            submitButton.setStyleName(style.tagFilterButton());
            submitButton.addStyleName("gwt-Button");
            submitButton.addStyleName(style.tagFilterSearchButton());
            submitButton.addStyleName(style.imageSearch());

            searchTextBox = new TextBox();
            searchTextBox.getElement().setAttribute("placeholder", stringMessages.tagSearchTags());
            searchTextBox.addKeyUpHandler(this);
            searchTextBox.setStyleName(style.tagFilterSearchInput());

            clearTextBoxButton = new Button();
            clearTextBoxButton.setStyleName(style.tagFilterButton());
            clearTextBoxButton.addStyleName(style.tagFilterClearButton());
            clearTextBoxButton.addStyleName(style.imageClearSearch());
            clearTextBoxButton.addStyleName(style.tagFilterHiddenButton());
            clearTextBoxButton.addStyleName("gwt-Button");
            clearTextBoxButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    clearSelection();
                }
            });

            filterSettingsButton = new Button();
            filterSettingsButton.setStyleName(style.tagFilterButton());
            filterSettingsButton.addStyleName(style.tagFilterFilterButton());
            filterSettingsButton.addStyleName(style.imageInactiveFilter());
            filterSettingsButton.addStyleName("gwt-Button");
            filterSettingsButton.setTitle(stringMessages.tagsFilter());
            filterSettingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showFilterDialog();
                }
            });

            currentFilter = new TagFilterLabel();

            Panel searchBoxPanel = new FlowPanel();
            searchBoxPanel.setStyleName(style.tagFilterSearchBox());
            searchBoxPanel.add(submitButton);
            searchBoxPanel.add(searchTextBox);
            searchBoxPanel.add(clearTextBoxButton);

            add(searchBoxPanel);
            add(filterSettingsButton);
            add(currentFilter);
        }

        private void showFilterDialog() {
            TagFilterSetsDialog tagsFilterSetsDialog = new TagFilterSetsDialog(tagFilterSets, stringMessages,
                    new DialogCallback<TagFilterSets>() {
                        @Override
                        public void ok(final TagFilterSets newTagFilterSets) {
                            tagFilterSets.getFilterSets().clear();
                            tagFilterSets.getFilterSets().addAll(newTagFilterSets.getFilterSets());
                            tagFilterSets.setActiveFilterSet(newTagFilterSets.getActiveFilterSet());

                            tagListProvider.setTagsFilterSet(newTagFilterSets.getActiveFilterSetWithGeneralizedType());
                            tagListProvider.updateFilteredTags();
                            tagListProvider.refresh();

                            updateTagFilterControlState(newTagFilterSets);
                            if (userService.getCurrentUser() != null) {
                                storeTagFilterSets(newTagFilterSets);
                            }
                            updateContent();
                        }

                        @Override
                        public void cancel() {
                        }
                    }, userService);

            tagsFilterSetsDialog.show();
        }

        /**
         * Updates the tags filter checkbox state by setting its check mark and updating its label according to the
         * current filter selected
         */
        private void updateTagFilterControlState(TagFilterSets filterSets) {
            String tagsFilterTitle = stringMessages.tagsFilter();
            FilterSet<TagDTO, FilterWithUI<TagDTO>> activeFilterSet = filterSets.getActiveFilterSet();
            if (activeFilterSet != null) {
                if (lastActiveTagFilterSet == null) {
                    filterSettingsButton.removeStyleName(style.imageInactiveFilter());
                    filterSettingsButton.addStyleName(style.imageActiveFilter());
                }
                lastActiveTagFilterSet = activeFilterSet;
            } else {
                if (lastActiveTagFilterSet != null) {
                    filterSettingsButton.removeStyleName(style.imageActiveFilter());
                    filterSettingsButton.addStyleName(style.imageInactiveFilter());
                }
                lastActiveTagFilterSet = null;
            }

            if (lastActiveTagFilterSet != null) {
                filterSettingsButton.setTitle(tagsFilterTitle + " (" + lastActiveTagFilterSet.getName() + ")");
            } else {
                filterSettingsButton.setTitle(tagsFilterTitle);
            }
        }

        /*
         * loads users Tag Filter Sets from server and stores it into tagFilterSets
         */
        private void loadTagFilterSets() {
            if (userService.getCurrentUser() != null) {
                userService.getPreference(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // do nothing
                    }

                    @Override
                    public void onSuccess(String result) {
                        if (result != null && !result.isEmpty()) {
                            final TagsFilterSetsJsonDeSerializer deserializer = new TagsFilterSetsJsonDeSerializer();
                            final JSONValue value = JSONParser.parseStrict(result);
                            if (value.isObject() != null) {
                                tagFilterSets = deserializer.deserialize((JSONObject) value);
                                tagListProvider.setTagsFilterSet(tagFilterSets.getActiveFilterSetWithGeneralizedType());
                                return;
                            }
                        }
                        tagFilterSets = new TagFilterSets();
                        tagListProvider.setTagsFilterSet(null);
                    }
                });
            } else {
                tagFilterSets = new TagFilterSets();
                tagListProvider.setTagsFilterSet(null);
            }
        }

        private void storeTagFilterSets(TagFilterSets newTagsFilterSets) {
            TagsFilterSetsJsonDeSerializer serializer = new TagsFilterSetsJsonDeSerializer();
            JSONObject jsonObject = serializer.serialize(newTagsFilterSets);
            userService.setPreference(LOCAL_STORAGE_TAGS_FILTER_SETS_KEY, jsonObject.toString(),
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.tagFilterNotSavable(), NotificationType.WARNING);
                        }

                        @Override
                        public void onSuccess(Void result) {
                        }
                    });
        }

        private void clearSelection() {
            searchTextBox.setText("");
            clearTextBoxButton.addStyleName(style.tagFilterHiddenButton());
            onKeyUp(null);
        }

        private void ensureSetSearchFilter() {
            if (tagListProvider.getTagFilterSet() == null
                    || !Util.contains(tagListProvider.getTagFilterSet().getFilters(), this)) {
                FilterSet<TagDTO, Filter<TagDTO>> newFilterSetWithThis = new FilterSet<>(getName());
                if (tagListProvider.getTagFilterSet() != null) {
                    for (Filter<TagDTO> oldFilter : tagListProvider.getTagFilterSet().getFilters()) {
                        newFilterSetWithThis.addFilter(oldFilter);
                    }
                }
                newFilterSetWithThis.addFilter(this);
                tagListProvider.setTagsFilterSet(newFilterSetWithThis);
            }
        }

        private void removeSearchFilter() {
            if (tagListProvider.getTagFilterSet() != null
                    && Util.contains(tagListProvider.getTagFilterSet().getFilters(), this)) {
                FilterSet<TagDTO, Filter<TagDTO>> newFilterSetWithThis = new FilterSet<>(
                        tagListProvider.getTagFilterSet().getName());
                for (Filter<TagDTO> oldFilter : tagListProvider.getTagFilterSet().getFilters()) {
                    if (oldFilter != this) {
                        newFilterSetWithThis.addFilter(oldFilter);
                    }
                }
                tagListProvider.setTagsFilterSet(newFilterSetWithThis);
            }
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
                clearTextBoxButton.addStyleName(style.tagFilterHiddenButton());
            } else {
                if (newValue.length() >= 2) {
                    clearTextBoxButton.removeStyleName(style.tagFilterHiddenButton());
                    ensureSetSearchFilter();
                    tagListProvider.setTagsFilterSet(tagListProvider.getTagFilterSet()); //
                }
            }
        }
    }

    /* Dialogs */
    /**
     * Dialog for modifying tag buttons
     */
    private class TagButtonDialog extends DialogBox {

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
                                makeImagePrototype(resources.editIcon())),
                        new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(),
                                makeImagePrototype(IconResources.INSTANCE.removeIcon())));
            }
        }

        private Button closeButton, saveButton, cancelButton, addTagButtonButton;
        private TagButton selectedTagButton;

        public TagButtonDialog(TagCreationPanel tagCreationPanel) {
            setGlassEnabled(true);
            setText(stringMessages.tagEditCustomTagButtons());
            addStyleName(style.tagButtonDialog());

            TagInputPanel inputPanel = new TagInputPanel(stringMessages);
            TagPreviewPanel tagPreviewPanel = new TagPreviewPanel(inputPanel);
            CellTable<TagButton> tagButtonsTable = createTable(tagCreationPanel, inputPanel, tagPreviewPanel);
            Panel controlButtonPanel = createButtonPanel(tagButtonsTable, inputPanel, tagPreviewPanel,
                    tagCreationPanel);

            Panel mainPanel = new FlowPanel();
            mainPanel.setStyleName(style.tagButtonDialogPanel());
            mainPanel.add(tagButtonsTable);
            mainPanel.add(inputPanel);
            mainPanel.add(controlButtonPanel);
            mainPanel.add(tagPreviewPanel);

            setWidget(mainPanel);
            center();
        }

        private CellTable<TagButton> createTable(TagCreationPanel tagCreationPanel, TagInputPanel inputPanel,
                TagPreviewPanel tagPreviewPanel) {
            CellTable<TagButton> tagButtonTable = new CellTable<TagButton>();
            tagButtonTable.setStyleName(style.tagButtonTable());
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
            Column<TagButton, ImageResource> visibleForPublicColumn = new Column<TagButton, ImageResource>(
                    new ImageResourceCell()) {
                @Override
                public ImageResource getValue(TagButton tagButton) {
                    if (tagButton.isVisibleForPublic()) {
                        return resources.publicIcon();
                    } else {
                        return resources.privateIcon();
                    }
                }
            };
            ImagesBarColumn<TagButton, EditTagButtonsImagesBarCell> actionsColumn = new ImagesBarColumn<TagButton, EditTagButtonsImagesBarCell>(
                    new EditTagButtonsImagesBarCell(stringMessages));
            actionsColumn.setFieldUpdater(new FieldUpdater<TagButton, String>() {
                @Override
                public void update(int index, TagButton button, String value) {
                    if (LeaderboardConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                        new ConfirmationDialog(stringMessages.tagButtonConfirmDeletionHeading(),
                                stringMessages.tagButtonConfirmDeletion(button.getTag()), (confirmed) -> {
                                    if (confirmed) {
                                        tagButtons.remove(button);
                                        tagCreationPanel.storeAllTagButtons();
                                        setRowData(tagButtonTable, tagButtons);
                                        tagCreationPanel.updateButtons();
                                    }
                                    center();
                                });
                    } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                        selectedTagButton = button;

                        inputPanel.setTag(button.getTag());
                        inputPanel.setImageURL(button.getImageURL());
                        inputPanel.setComment(button.getComment());
                        inputPanel.setVisibleForPublic(button.isVisibleForPublic());

                        tagPreviewPanel.renderPreview(inputPanel);

                        saveButton.setVisible(true);
                        cancelButton.setVisible(true);
                        closeButton.setVisible(false);
                        addTagButtonButton.setVisible(false);

                        tagButtonTable.setVisible(false);
                        center();
                    }
                }
            });

            tagButtonTable.addColumn(tagColumn, stringMessages.tagLabelTag());
            tagButtonTable.addColumn(imageURLColumn, stringMessages.tagLabelImageURL());
            tagButtonTable.addColumn(commentColumn, stringMessages.tagLabelComment());
            tagButtonTable.addColumn(visibleForPublicColumn, stringMessages.tagVisibility());
            tagButtonTable.addColumn(actionsColumn, stringMessages.tagLabelAction());

            // set these width values manually as they are not accessable via CSS classes
            tagButtonTable.setColumnWidth(tagColumn, "30%");
            tagButtonTable.setColumnWidth(imageURLColumn, "20%");
            tagButtonTable.setColumnWidth(commentColumn, "30%");
            tagButtonTable.setColumnWidth(visibleForPublicColumn, "10%");
            tagButtonTable.setColumnWidth(actionsColumn, "10%");

            setRowData(tagButtonTable, tagButtons);

            return tagButtonTable;
        }

        private Panel createButtonPanel(CellTable<TagButton> tagButtonTable, TagInputPanel inputPanel,
                TagPreviewPanel tagPreviewPanel, TagCreationPanel tagCreationPanel) {
            addSaveButton(tagCreationPanel, tagButtonTable, inputPanel, tagPreviewPanel);
            addCancelButton(inputPanel, tagPreviewPanel);
            addCloseButton(tagCreationPanel, tagPreviewPanel);
            addTagButtonButton(tagCreationPanel, tagButtonTable, inputPanel, tagPreviewPanel);

            Panel controlButtonPanel = new FlowPanel();
            controlButtonPanel.setStyleName(style.buttonsPanel());
            controlButtonPanel.add(addTagButtonButton);
            controlButtonPanel.add(closeButton);
            controlButtonPanel.add(saveButton);
            controlButtonPanel.add(cancelButton);

            return controlButtonPanel;
        }

        private void addSaveButton(TagCreationPanel tagCreationPanel, CellTable<TagButton> tagButtonTable,
                TagInputPanel inputPanel, TagPreviewPanel tagPreviewPanel) {
            saveButton = new Button(stringMessages.save());
            saveButton.setVisible(false);
            saveButton.setStyleName(style.tagDialogButton());
            saveButton.addStyleName("gwt-Button");
            saveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!inputPanel.getTag().isEmpty()) {
                        selectedTagButton.setText(inputPanel.getTag());
                        selectedTagButton.setTag(inputPanel.getTag());
                        selectedTagButton.setComment(inputPanel.getComment());
                        selectedTagButton.setImageURL(inputPanel.getImageURL());
                        tagCreationPanel.storeAllTagButtons();
                        inputPanel.clearAllValues();
                        tagPreviewPanel.renderPreview(inputPanel);
                        tagButtonTable.redraw();

                        saveButton.setVisible(false);
                        cancelButton.setVisible(false);
                        closeButton.setVisible(true);
                        addTagButtonButton.setVisible(true);
                        selectedTagButton = null;
                        setRowData(tagButtonTable, tagButtons);
                    } else {
                        Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
                    }
                    center();
                }
            });
        }

        private void addCancelButton(TagInputPanel inputPanel, TagPreviewPanel tagPreviewPanel) {
            cancelButton = new Button(stringMessages.cancel());
            cancelButton.setVisible(false);
            cancelButton.setStyleName(style.tagDialogButton());
            cancelButton.addStyleName("gwt-Button");
            cancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    inputPanel.clearAllValues();
                    tagPreviewPanel.renderPreview(inputPanel);
                    saveButton.setVisible(false);
                    cancelButton.setVisible(false);
                    closeButton.setVisible(true);
                    addTagButtonButton.setVisible(true);
                    center();
                }
            });
        }

        private void addCloseButton(TagCreationPanel tagCreationPanel, TagPreviewPanel tagPreviewPanel) {
            closeButton = new Button(stringMessages.close());
            closeButton.setStyleName(style.tagDialogButton());
            closeButton.addStyleName("gwt-Button");
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                    tagCreationPanel.updateButtons();
                }
            });
        }

        private void addTagButtonButton(TagCreationPanel tagCreationPanel, CellTable<TagButton> tagButtonTable,
                TagInputPanel inputPanel, TagPreviewPanel tagPreviewPanel) {
            addTagButtonButton = new Button(stringMessages.tagAddCustomTagButton());
            addTagButtonButton.setStyleName(style.tagDialogButton());
            addTagButtonButton.addStyleName("gwt-Button");
            addTagButtonButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!inputPanel.getTag().isEmpty()) {
                        TagButton tagButton = new TagButton(inputPanel.getTag(), inputPanel.getTag(),
                                inputPanel.getImageURL(), inputPanel.getComment(), inputPanel.isVisibleForPublic());
                        inputPanel.clearAllValues();
                        tagPreviewPanel.renderPreview(inputPanel);
                        tagButtons.add(tagButton);
                        tagCreationPanel.storeAllTagButtons();
                        setRowData(tagButtonTable, tagButtons);
                    } else {
                        Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
                    }
                    center();
                }
            });
        }

        private void setRowData(CellTable<TagButton> tagButtonTable, List<TagButton> buttons) {
            tagButtonTable.setRowData(buttons);
            tagButtonTable.setVisible(!buttons.isEmpty());
        }
    }

    /**
     * Used to store tags and filter sets and to apply these filters on the tags
     */
    private class TagListProvider extends ListDataProvider<TagDTO> {

        private List<TagDTO> allTags;
        private FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet;
        private TagFilterLabel observingLabel;

        public TagListProvider() {
            allTags = new ArrayList<TagDTO>();
        }

        public void addObserveringLabel(TagFilterLabel tagFilterLabel) {
            observingLabel = tagFilterLabel;
        }

        public List<TagDTO> getAllTags() {
            return allTags;
        }

        public List<TagDTO> getFilteredTags() {
            return getList();
        }

        /**
         * filter all tags by tagsFilterSet
         */
        public void updateFilteredTags() {
            List<TagDTO> currentFilteredList = new ArrayList<TagDTO>(getAllTags());
            if (currentFilterSet != null) {
                for (Filter<TagDTO> filter : currentFilterSet.getFilters()) {
                    for (Iterator<TagDTO> i = currentFilteredList.iterator(); i.hasNext();) {
                        TagDTO tag = i.next();
                        if (!filter.matches(tag)) {
                            i.remove();
                        }
                    }
                }
            }

            currentFilteredList.sort(new Comparator<TagDTO>() {
                @Override
                public int compare(TagDTO tag1, TagDTO tag2) {
                    long time1 = tag1.getRaceTimepoint().asMillis();
                    long time2 = tag2.getRaceTimepoint().asMillis();
                    return time1 < time2 ? -1 : time1 == time2 ? 0 : 1;
                }
            });
            setList(currentFilteredList);
        }

        public FilterSet<TagDTO, Filter<TagDTO>> getTagFilterSet() {
            return currentFilterSet;
        }

        public void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
            currentFilterSet = tagsFilterSet;
            if (observingLabel != null) {
                observingLabel.update(tagsFilterSet);
            }
            updateFilteredTags();
            refresh();
        }

        public int getFilteredTagsListSize() {
            return Util.size(getFilteredTags());
        }
    }

    /**
     * Used to show generic confirmation dialog to ask user for explicit confirmation of an action.
     */
    private class ConfirmationDialog extends DialogBox {

        public ConfirmationDialog(String title, String text, Consumer<Boolean> consumer) {
            Panel mainPanel = new FlowPanel();
            mainPanel.setStyleName(style.confirmationDialogPanel());

            Label label = new Label(text);
            label.getElement().getStyle().setMarginBottom(10, Unit.PX);

            Panel buttonsPanel = new FlowPanel();
            buttonsPanel.setStyleName(style.buttonsPanel());

            Button confirm = new Button(stringMessages.confirm());
            confirm.setStyleName(style.tagDialogButton());
            confirm.addStyleName("gwt-Button");
            confirm.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    consumer.accept(true);
                    hide();
                }
            });
            buttonsPanel.add(confirm);

            Button cancel = new Button(stringMessages.cancel());
            cancel.setStyleName(style.tagDialogButton());
            cancel.addStyleName("gwt-Button");
            cancel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    consumer.accept(false);
                    hide();
                }
            });
            buttonsPanel.add(cancel);

            mainPanel.add(label);
            mainPanel.add(buttonsPanel);
            setWidget(mainPanel);

            addStyleName(style.confirmationDialog());
            setGlassEnabled(true);
            setText(title);
            center();
        }
    }

    private final TagPanelResources resources;
    private final TagPanelStyle style;

    private final TagCellListResources cellResources;
    private final TagCellListStyle cellStyle;

    private final CellList<TagDTO> tagCellList;
    private final SingleSelectionModel<TagDTO> tagSelectionModel;
    private final TagListProvider tagListProvider;

    private List<TagButton> tagButtons;

    private final HeaderPanel panel;
    private final TagCreationPanel tagCreationPanel;
    private final TagFilterPanel filterbarPanel;
    private final Panel contentPanel;

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

        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.userService = userService;
        this.timer = timer;
        this.raceTimesInfoProvider = raceTimesInfoProvider;

        resources = TagPanelResources.INSTANCE;
        style = resources.style();
        style.ensureInjected();

        cellResources = TagCellListResources.INSTANCE;
        cellStyle = cellResources.cellListStyle();
        cellStyle.ensureInjected();

        tagCellList = new CellList<TagDTO>(new TagCell(false), cellResources);
        tagSelectionModel = new SingleSelectionModel<TagDTO>();
        tagListProvider = new TagListProvider();

        tagButtons = new ArrayList<TagButton>();

        panel = new HeaderPanel();
        tagCreationPanel = new TagCreationPanel(stringMessages);
        filterbarPanel = new TagFilterPanel();
        contentPanel = new ScrollPanel();

        userService.addUserStatusEventHandler(this);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);

        initializePanel();
    }

    private void initializePanel() {
        // Panel
        panel.setStyleName(style.taggingPanel());

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
        contentPanel.addStyleName(style.tagCellListPanel());

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

    private void addTagToRaceLog(String tag, String comment, String imageURL, boolean isPublic) {
        if (isLoggedInAndRaceLogAvailable()) {
            if (!tag.isEmpty()) {
                sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag, comment,
                        imageURL, isPublic, new MillisecondsTimePoint(timer.getTime()),
                        new AsyncCallback<SuccessInfo>() {
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
        return userService.getCurrentUser() != null && leaderboardName != null && raceColumn != null && fleet != null;
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
            // Will be true if local list of tags get modified with new received tags, otherwise false.
            boolean modifiedTags = false;
            // Will be true if latestReceivedTagTime needs to be updated in raceTimesInfoprovider, otherwise false.
            boolean updatedLatestTag = false;
            // local list of already received tags
            List<TagDTO> currentTags = tagListProvider.getAllTags();
            // createdAt or revokedAt timepoint of latest received tag
            TimePoint latestReceivedTagTime = raceTimesInfoProvider.getLatestReceivedTagTime(raceIdentifier);
            // get difference in tags since latestReceivedTagTime
            for (TagDTO tag : raceInfo.getTags()) {
                if (tag.getRevokedAt() != null) {
                    // received tag is revoked => latestReceivedTagTime will be revokedAt if revoke event occured
                    // before latestReceivedTagTime
                    currentTags.remove(tag);
                    modifiedTags = true;
                    if (latestReceivedTagTime == null
                            || (latestReceivedTagTime != null && latestReceivedTagTime.before(tag.getRevokedAt()))) {
                        latestReceivedTagTime = tag.getRevokedAt();
                        updatedLatestTag = true;
                    }
                } else if (!currentTags.contains(tag)) {
                    // received tag is NOT revoked => latestReceivedTagTime will be createdAt if tag event occured
                    // before latestReceivedTagTime
                    currentTags.add(tag);
                    modifiedTags = true;
                    if (latestReceivedTagTime == null
                            || (latestReceivedTagTime != null && latestReceivedTagTime.before(tag.getCreatedAt()))) {
                        latestReceivedTagTime = tag.getCreatedAt();
                        updatedLatestTag = true;
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

    @Override
    public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
        // clear list of local tags to hide private tags of previous user
        tagListProvider.getAllTags().clear();
        raceTimesInfoProvider.getRaceIdentifiers().forEach((raceIdentifier) -> {
            raceTimesInfoProvider.setLatestReceivedTagTime(raceIdentifier, null);
        });
        filterbarPanel.loadTagFilterSets();
        tagCreationPanel.loadAllTagButtons();
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
