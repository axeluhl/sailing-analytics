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
import com.sap.sailing.gwt.ui.adminconsole.ImagesBarColumn;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigImagesBarCell;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

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

    private final TagPanelResources resources = TagPanelResources.INSTANCE;
    private final TagPanelStyle style = resources.style();
    private final TagButtonCellTableResources buttonTableResources = GWT.create(TagButtonCellTableResources.class);

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;

    private Button closeButton, saveButton, cancelButton, addTagButtonButton;
    private TagButton selectedTagButton;

    /**
     * Centered dialog wich allows users to edit their personal {@link TagButton tag-buttons}.
     * 
     * @param taggingPanel
     *            {@link TaggingPanel} which creates this {@link TagButtonDialog}.
     * @param footerPanel
     *            footer panel of {@link TaggingPanel}
     */
    public TagButtonDialog(TaggingPanel taggingPanel, TagFooterPanel footerPanel) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();

        setGlassEnabled(true);
        setText(stringMessages.tagEditCustomTagButtons());
        addStyleName(style.tagButtonDialog());

        TagInputPanel inputPanel = new TagInputPanel(stringMessages);
        TagPreviewPanel tagPreviewPanel = new TagPreviewPanel(taggingPanel, inputPanel);
        CellTable<TagButton> tagButtonsTable = createTable(footerPanel, inputPanel, tagPreviewPanel);
        Panel controlButtonPanel = createButtonPanel(tagButtonsTable, inputPanel, tagPreviewPanel, footerPanel);

        // wrap tag buttons table to control max-height of table
        Panel tagButtonsTableWrapper = new SimplePanel();
        tagButtonsTableWrapper.setStyleName(style.tagButtonTableWrapper());
        tagButtonsTableWrapper.add(tagButtonsTable);

        Panel mainPanel = new FlowPanel();
        mainPanel.setStyleName(style.tagButtonDialogPanel());
        mainPanel.add(tagButtonsTableWrapper);
        mainPanel.add(inputPanel);
        mainPanel.add(controlButtonPanel);
        mainPanel.add(tagPreviewPanel);

        setWidget(mainPanel);
        // TODO: content gets added delayed => center again when content changes.
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
        CellTable<TagButton> tagButtonTable = new CellTable<TagButton>(15, buttonTableResources);
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
        addSaveButton(footerPanel, tagButtonTable, inputPanel, tagPreviewPanel);
        addCancelButton(inputPanel, tagPreviewPanel, tagButtonTable);
        addCloseButton(footerPanel, tagPreviewPanel);
        addTagButtonButton(footerPanel, tagButtonTable, inputPanel, tagPreviewPanel);

        Panel controlButtonPanel = new FlowPanel();
        controlButtonPanel.setStyleName(style.buttonsPanel());
        controlButtonPanel.add(addTagButtonButton);
        controlButtonPanel.add(closeButton);
        controlButtonPanel.add(saveButton);
        controlButtonPanel.add(cancelButton);

        return controlButtonPanel;
    }

    /**
     * Creates a button allowing users to save their changes on a {@link TagButton}.
     * 
     * @param footerPanel
     *            footer panel of {@link TaggingPanel}
     * @param tagButtonTable
     *            {@link com.google.gwt.user.cellview.client.CellTable CellTable} containing {@link TagButton
     *            tag-buttons}
     * @param inputPanel
     *            input fields of {@link TagButtonDialog} which allow to create new and modify existing {@link TagButton
     *            tag-buttons}
     * @param tagPreviewPanel
     *            renders {@link TagPreviewPanel} preview of current input fields
     */
    private void addSaveButton(TagFooterPanel footerPanel, CellTable<TagButton> tagButtonTable,
            TagInputPanel inputPanel, TagPreviewPanel tagPreviewPanel) {
        saveButton = new Button(stringMessages.save());
        saveButton.setVisible(false);
        saveButton.setStyleName(style.tagDialogButton());
        saveButton.addStyleName("gwt-Button");
        saveButton.addClickHandler(event -> {
            if (!inputPanel.getTag().isEmpty()) {
                selectedTagButton.setText(inputPanel.getTag());
                selectedTagButton.setTag(inputPanel.getTag());
                selectedTagButton.setComment(inputPanel.getComment());
                selectedTagButton.setImageURL(inputPanel.getImageURL());
                selectedTagButton.setVisibleForPublic(inputPanel.isVisibleForPublic());
                footerPanel.storeAllTagButtons();
                inputPanel.clearAllValues();
                tagPreviewPanel.renderPreview(inputPanel);
                tagButtonTable.redraw();

                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                closeButton.setVisible(true);
                addTagButtonButton.setVisible(true);
                selectedTagButton = null;
                setRowData(tagButtonTable, taggingPanel.getTagButtons());
            } else {
                Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
            }
            center();
        });
    }

    /**
     * Creates a button allowing users to discard their changes on a {@link TagButton}.
     * 
     * @param tagButtonTable
     *            {@link com.google.gwt.user.cellview.client.CellTable CellTable} containing {@link TagButton
     *            tag-buttons}
     * @param inputPanel
     *            input fields of {@link TagButtonDialog} which allow to create new and modify existing {@link TagButton
     *            tag-buttons}
     * @param tagPreviewPanel
     *            renders {@link TagPreviewPanel} preview of current input fields
     */
    private void addCancelButton(TagInputPanel inputPanel, TagPreviewPanel tagPreviewPanel,
            CellTable<TagButton> tagButtonTable) {
        cancelButton = new Button(stringMessages.cancel());
        cancelButton.setVisible(false);
        cancelButton.setStyleName(style.tagDialogButton());
        cancelButton.addStyleName("gwt-Button");
        cancelButton.addClickHandler(event -> {

            inputPanel.clearAllValues();
            tagPreviewPanel.renderPreview(inputPanel);
            tagButtonTable.setVisible(true);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            closeButton.setVisible(true);
            addTagButtonButton.setVisible(true);
            center();
        });
    }

    /**
     * Creates a button allowing users to close the {@link TagButtonDialog}.
     * 
     * @param tagButtonTable
     *            {@link com.google.gwt.user.cellview.client.CellTable CellTable} containing {@link TagButton
     *            tag-buttons}
     * @param inputPanel
     *            input fields of {@link TagButtonDialog} which allow to create new and modify existing {@link TagButton
     *            tag-buttons}
     * @param tagPreviewPanel
     *            renders {@link TagPreviewPanel} preview of current input fields
     */
    private void addCloseButton(TagFooterPanel footerPanel, TagPreviewPanel tagPreviewPanel) {
        closeButton = new Button(stringMessages.close());
        closeButton.setStyleName(style.tagDialogButton());
        closeButton.addStyleName("gwt-Button");
        closeButton.addClickHandler(event -> hide());
    }

    /**
     * Creates a button allowing users to add a new {@link TagButton}.
     * 
     * @param footerPanel
     *            footer panel of {@link TaggingPanel}
     * @param tagButtonTable
     *            {@link com.google.gwt.user.cellview.client.CellTable CellTable} containing {@link TagButton
     *            tag-buttons}
     * @param inputPanel
     *            input fields of {@link TagButtonDialog} which allow to create new and modify existing {@link TagButton
     *            tag-buttons}
     * @param tagPreviewPanel
     *            renders {@link TagPreviewPanel} preview of current input fields
     */
    private void addTagButtonButton(TagFooterPanel footerPanel, CellTable<TagButton> tagButtonTable,
            TagInputPanel inputPanel, TagPreviewPanel tagPreviewPanel) {
        addTagButtonButton = new Button(stringMessages.tagAddCustomTagButton());
        addTagButtonButton.setStyleName(style.tagDialogButton());
        addTagButtonButton.addStyleName("gwt-Button");
        addTagButtonButton.addClickHandler(event -> {
            if (!inputPanel.getTag().isEmpty()) {
                TagButton tagButton = new TagButton(inputPanel.getTag(), inputPanel.getTag(), inputPanel.getImageURL(),
                        inputPanel.getComment(), inputPanel.isVisibleForPublic());
                inputPanel.clearAllValues();
                tagPreviewPanel.renderPreview(inputPanel);
                taggingPanel.getTagButtons().add(tagButton);
                footerPanel.storeAllTagButtons();
                footerPanel.recalculateHeight();
                setRowData(tagButtonTable, taggingPanel.getTagButtons());
            } else {
                Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
            }
            center();
        });
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
}
