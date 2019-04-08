package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.gwt.ui.adminconsole.ImagesBarColumn;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigImagesBarCell;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanelResources.TagPanelStyle;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.client.UserService;

/**
 * Dialog which allows users to modify their personal {@link TagButton tag-buttons}.
 */
public class TagButtonDialog extends DialogBox {

    /**
     * Preset layout for action column at the {@link TagButtonDialog#createTable tag button table} used in the
     * {@link TagButtonDialog}.
     */
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
                    new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())),
                    new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(),
                            makeImagePrototype(IconResources.INSTANCE.removeIcon())));
        }
    }

    protected interface ImageWithTextCellTemplate extends SafeHtmlTemplates {
        @Template("<div><img src='{0}' /><span style=\"vertical-align: top\">&nbsp;&nbsp;{1}</span></div>")
        SafeHtml cell(SafeUri imageUrl, SafeHtml text);
    }

    private class ImageWithTextCell extends AbstractCell<TagButton> {

        ImageWithTextCellTemplate template = GWT.create(ImageWithTextCellTemplate.class);

        @Override
        public void render(Context context, TagButton tagButton, SafeHtmlBuilder sb) {

            SafeUri trustedImageURL;
            SafeHtml safeTag = SafeHtmlUtils.fromString(tagButton.getTag());

            if (tagButton.isVisibleForPublic()) {
                trustedImageURL = resources.publicIcon().getSafeUri();
            } else {
                trustedImageURL = resources.privateIcon().getSafeUri();
            }
            sb.append(template.cell(trustedImageURL, safeTag));
        }
    }

    private final TaggingPanelResources resources = TaggingPanelResources.INSTANCE;
    private final TagPanelStyle style = resources.style();
    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;
    private final TagInputPanel inputPanel;
    private final TagPreviewPanel tagPreviewPanel;
    private final CellTable<TagButton> tagButtonTable;
    private final TagFooterPanel footerPanel;

    private TagButton selectedTagButton;

    /**
     * <b>May not be edited directly</b>!<br/>
     * When {@link #updateTagMode} is set to <code>true</code>, UI is displaying version for updating a tag button
     * (button names and click listener). When this boolean is set to <code>false</code>, UI is displaying version for
     * creation of tag buttons. Use method {@link #setButtonMode(boolean)} to change this value and the corresponding
     * UI.
     */
    private boolean updateTagMode = false;

    /**
     * Centered dialog which allows users to edit their personal {@link TagButton tag-buttons}.
     * 
     * @param taggingPanel
     *            {@link TaggingPanel} which creates this {@link TagButtonDialog}.
     * @param footerPanel
     *            footer panel of {@link TaggingPanel}
     * @param sailingService
     *            Sailing Service of {@link TaggingPanel}
     * @param stringMessages
     *            string messages of {@link TaggingPanel}
     */
    public TagButtonDialog(TaggingPanel taggingPanel, TagFooterPanel footerPanel, SailingServiceAsync sailingService,
            StringMessages stringMessages, UserService userService) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = stringMessages;
        this.footerPanel = footerPanel;
        setGlassEnabled(true);
        setText(stringMessages.tagEditCustomTagButtons());
        addStyleName(style.tagButtonDialog());
        inputPanel = new TagInputPanel(taggingPanel, sailingService, stringMessages, new DialogCallback<TagDTO>() {
            @Override
            public void ok(TagDTO editedObject) {
                if (updateTagMode) {
                    onSaveTagButtonChangesPressed();
                } else {
                    onAddTagButtonPressed();
                }
            }

            @Override
            public void cancel() {
                if (updateTagMode) {
                    onCancelTagButtonChangesPressed();
                } else {
                    onCloseButtonPressed();
                }
            }
        });
        tagPreviewPanel = new TagPreviewPanel(taggingPanel, inputPanel, stringMessages, userService);
        tagButtonTable = createTable(footerPanel, inputPanel, tagPreviewPanel);
        tagButtonTable.addRedrawHandler(() -> {
            // center dialog when content of tagButtonTable changes (table needs to be redrawn)
            center();
        });
        Panel controlButtonPanel = createButtonPanel(tagButtonTable, inputPanel, tagPreviewPanel, footerPanel);
        setButtonMode(false);
        // wrap tag buttons table to control max-height of table
        Panel tagButtonsTableWrapper = new SimplePanel();
        tagButtonsTableWrapper.setStyleName(style.tagButtonTableWrapper());
        tagButtonsTableWrapper.add(tagButtonTable);
        Panel mainPanel = new FlowPanel();
        mainPanel.setStyleName(style.tagButtonDialogPanel());
        mainPanel.add(tagButtonsTableWrapper);
        mainPanel.add(inputPanel.getStatusLabel());
        mainPanel.add(inputPanel);
        mainPanel.add(controlButtonPanel);
        mainPanel.add(tagPreviewPanel);
        setWidget(mainPanel);
        center();
    }

    /**
     * Creates table which shows tag buttons as a compact overview in form of a table.
     * 
     * @param footerPanel
     *            footer panel of {@link TaggingPanel}
     * @param inputPanel
     *            input fields of {@link TagButtonDialog} which allow to create new and modify existing {@link TagButton
     *            tag-buttons}
     * @param tagPreviewPanel
     *            renders {@link TagPreviewPanel} preview of current input fields
     * @return {@link com.google.gwt.user.cellview.client.CellTable CellTable} containing {@link TagButton tag-buttons}
     */
    private CellTable<TagButton> createTable(TagFooterPanel footerPanel, TagInputPanel inputPanel,
            TagPreviewPanel tagPreviewPanel) {
        CellTable<TagButton> tagButtonTable = new CellTable<TagButton>(15, resources);
        tagButtonTable.setStyleName(style.tagButtonTable());

        // columns
        ImageWithTextCell imageWithTextCell = new ImageWithTextCell();
        Column<TagButton, TagButton> tagColumn = new Column<TagButton, TagButton>(imageWithTextCell) {
            @Override
            public TagButton getValue(TagButton button) {
                return button;
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
                    new ConfirmationDialog(stringMessages, stringMessages.tagButtonConfirmDeletionHeading(),
                            stringMessages.tagButtonConfirmDeletion(button.getTag()), (confirmed) -> {
                                if (confirmed) {
                                    taggingPanel.getTagButtons().remove(button);
                                    footerPanel.storeAllTagButtons();
                                    setRowData(tagButtonTable, taggingPanel.getTagButtons());
                                    footerPanel.recalculateHeight();
                                }
                                center();
                            });
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    selectedTagButton = button;
                    inputPanel.setTag(button.getTag());
                    inputPanel.setImageURL(button.getImageURL());
                    inputPanel.setComment(button.getComment());
                    inputPanel.setVisibleForPublic(button.isVisibleForPublic());
                    inputPanel.validateAndUpdate();
                    tagPreviewPanel.renderPreview(inputPanel);
                    setButtonMode(true);
                    tagButtonTable.setVisible(false);
                    center();
                }
            }
        });
        tagButtonTable.addColumn(tagColumn, stringMessages.tagLabelTag());
        tagButtonTable.addColumn(imageURLColumn, stringMessages.tagLabelImage());
        tagButtonTable.addColumn(commentColumn, stringMessages.tagLabelComment());
        tagButtonTable.addColumn(actionsColumn, stringMessages.tagLabelAction());
        // set these width values manually as they are not accessable via CSS classes
        tagButtonTable.setColumnWidth(tagColumn, "25%");
        tagButtonTable.setColumnWidth(imageURLColumn, "20%");
        tagButtonTable.setColumnWidth(commentColumn, "40%");
        tagButtonTable.setColumnWidth(actionsColumn, "15%");
        setRowData(tagButtonTable, taggingPanel.getTagButtons());
        return tagButtonTable;
    }

    /**
     * Creates a {@link Panel} containing all action buttons of the {@link TagButtonDialog} to create {@link TagButton
     * tag-buttons}, save changes, discard changes and close the {@link TagButtonDialog}.
     * 
     * @param tagButtonTable
     *            {@link com.google.gwt.user.cellview.client.CellTable CellTable} containing {@link TagButton
     *            tag-buttons}
     * @param inputPanel
     *            input fields of {@link TagButtonDialog} which allow to create new and modify existing {@link TagButton
     *            tag-buttons}
     * @param tagPreviewPanel
     *            renders {@link TagPreviewPanel} preview of current input fields
     * @param footerPanel
     *            footer panel of {@link TaggingPanel}
     * @return {@link Panel} containing all action buttons
     */
    private Panel createButtonPanel(CellTable<TagButton> tagButtonTable, TagInputPanel inputPanel,
            TagPreviewPanel tagPreviewPanel, TagFooterPanel footerPanel) {
        Button okButton = inputPanel.getOkButton();
        okButton.setStyleName(style.tagDialogButton());
        okButton.addStyleName("gwt-Button");
        Button cancelButton = inputPanel.getCancelButton();
        cancelButton.setStyleName(style.tagDialogButton());
        cancelButton.addStyleName("gwt-Button");
        Panel controlButtonPanel = new FlowPanel();
        controlButtonPanel.setStyleName(style.buttonsPanel());
        controlButtonPanel.add(okButton);
        controlButtonPanel.add(cancelButton);
        return controlButtonPanel;
    }

    /**
     * Sets the given <code>buttons</code> as value for the given <code>tagButtonTable</code>. If <code>buttons</code>
     * is empty, <code>tagButtonTable</code> will be hidden.
     * 
     * @param tagButtonTable
     *            {@link com.google.gwt.user.cellview.client.CellTable CellTable} containing {@link TagButton
     *            tag-buttons}
     * @param buttons
     *            List of {@link TagButton tag-buttons} which represents the local version of the current users
     *            {@link TagButton tag-buttons}.
     */
    private void setRowData(CellTable<TagButton> tagButtonTable, List<TagButton> buttons) {
        tagButtonTable.setRowData(buttons);
        tagButtonTable.setVisible(!buttons.isEmpty());
    }

    /**
     * Button click handler for 'save' button (can only be pressed when user is currently editing a tag button).
     */
    private void onSaveTagButtonChangesPressed() {
        selectedTagButton.setText(inputPanel.getTag());
        selectedTagButton.setTag(inputPanel.getTag());
        selectedTagButton.setComment(inputPanel.getComment());
        selectedTagButton.setImageURL(inputPanel.getImageURL());
        selectedTagButton.setVisibleForPublic(inputPanel.isVisibleForPublic());
        footerPanel.storeAllTagButtons();
        inputPanel.clearAllValues();
        tagPreviewPanel.renderPreview(inputPanel);
        tagButtonTable.redraw();
        setButtonMode(false);
        selectedTagButton = null;
        setRowData(tagButtonTable, taggingPanel.getTagButtons());
        center();
    }

    /**
     * Button click handler for 'cancel' button (can only be pressed when user is currently editing a tag button).
     */
    private void onCancelTagButtonChangesPressed() {
        // ask user for confirmation to discard changes if values of input fields changed
        if (!inputPanel.compareFieldsToTagButton(selectedTagButton)) {
            new ConfirmationDialog(stringMessages, stringMessages.tagDiscardChangesHeading(),
                    stringMessages.tagDiscardChanges(), confirmed -> {
                        if (confirmed) {
                            inputPanel.clearAllValues();
                            tagPreviewPanel.renderPreview(inputPanel);
                            tagButtonTable.setVisible(true);
                            setButtonMode(false);
                            center();
                        }
                    });
        } else {
            inputPanel.clearAllValues();
            tagPreviewPanel.renderPreview(inputPanel);
            tagButtonTable.setVisible(true);
            setButtonMode(false);
            center();
        }
    }

    /**
     * Button click handler for 'add tag button' button (can only be pressed when user is currently creating a new tag
     * button).
     */
    private void onAddTagButtonPressed() {
        if (inputPanel.getTag().isEmpty()) {
            Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
        } else {
            TagButton tagButton = new TagButton(inputPanel.getTag(), inputPanel.getTag(), inputPanel.getImageURL(),
                    inputPanel.getComment(), inputPanel.isVisibleForPublic());
            inputPanel.clearAllValues();
            tagPreviewPanel.renderPreview(inputPanel);
            taggingPanel.addTagButton(tagButton);
            footerPanel.storeAllTagButtons();
            footerPanel.recalculateHeight();
            setRowData(tagButtonTable, taggingPanel.getTagButtons());
        }
        center();
    }

    /**
     * Button click handler for 'close' button (can only be pressed when user is currently creating a new tag button).
     */
    private void onCloseButtonPressed() {
        hide();
    }

    /**
     * Sets text of buttons regarding to current mode.
     * 
     * @param updateTag
     *            should be <code>true</code> when user updates a tag button, otherwise <code>false</code> (default,
     *            create new tag button)
     */
    private void setButtonMode(boolean updateTag) {
        if (updateTag) {
            inputPanel.getOkButton().setText(stringMessages.save());
            inputPanel.getCancelButton().setText(stringMessages.cancel());
            updateTagMode = true;
        } else {
            inputPanel.getOkButton().setText(stringMessages.tagAddCustomTagButton());
            inputPanel.getCancelButton().setText(stringMessages.close());
            updateTagMode = false;
        }
    }
}
