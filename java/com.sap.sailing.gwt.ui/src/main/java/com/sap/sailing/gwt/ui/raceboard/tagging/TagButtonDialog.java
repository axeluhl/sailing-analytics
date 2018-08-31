package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.adminconsole.ImagesBarColumn;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigImagesBarCell;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * Dialog for modifying tag buttons
 */
public class TagButtonDialog extends DialogBox {
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

    private final TagPanelResources resources = TagPanelResources.INSTANCE;
    private final TagPanelStyle style = resources.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;

    private Button closeButton, saveButton, cancelButton, addTagButtonButton;
    private TagButton selectedTagButton;

    public TagButtonDialog(TaggingPanel taggingPanel, TagCreationPanel tagCreationPanel) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();

        setGlassEnabled(true);
        setText(stringMessages.tagEditCustomTagButtons());
        addStyleName(style.tagButtonDialog());

        TagInputPanel inputPanel = new TagInputPanel(stringMessages);
        TagPreviewPanel tagPreviewPanel = new TagPreviewPanel(taggingPanel, inputPanel);
        CellTable<TagButton> tagButtonsTable = createTable(tagCreationPanel, inputPanel, tagPreviewPanel);
        Panel controlButtonPanel = createButtonPanel(tagButtonsTable, inputPanel, tagPreviewPanel, tagCreationPanel);

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
                    new ConfirmationDialog(stringMessages, stringMessages.tagButtonConfirmDeletionHeading(),
                            stringMessages.tagButtonConfirmDeletion(button.getTag()), (confirmed) -> {
                                if (confirmed) {
                                    taggingPanel.getTagButtons().remove(button);
                                    tagCreationPanel.storeAllTagButtons();
                                    setRowData(tagButtonTable, taggingPanel.getTagButtons());
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

        setRowData(tagButtonTable, taggingPanel.getTagButtons());

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
        saveButton.addClickHandler(event -> {
            if (!inputPanel.getTag().isEmpty()) {
                selectedTagButton.setText(inputPanel.getTag());
                selectedTagButton.setTag(inputPanel.getTag());
                selectedTagButton.setComment(inputPanel.getComment());
                selectedTagButton.setImageURL(inputPanel.getImageURL());
                selectedTagButton.setVisibleForPublic(inputPanel.isVisibleForPublic());
                tagCreationPanel.storeAllTagButtons();
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

    private void addCancelButton(TagInputPanel inputPanel, TagPreviewPanel tagPreviewPanel) {
        cancelButton = new Button(stringMessages.cancel());
        cancelButton.setVisible(false);
        cancelButton.setStyleName(style.tagDialogButton());
        cancelButton.addStyleName("gwt-Button");
        cancelButton.addClickHandler(event -> {
            inputPanel.clearAllValues();
            tagPreviewPanel.renderPreview(inputPanel);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            closeButton.setVisible(true);
            addTagButtonButton.setVisible(true);
            center();
        });
    }

    private void addCloseButton(TagCreationPanel tagCreationPanel, TagPreviewPanel tagPreviewPanel) {
        closeButton = new Button(stringMessages.close());
        closeButton.setStyleName(style.tagDialogButton());
        closeButton.addStyleName("gwt-Button");
        closeButton.addClickHandler(event -> {
            hide();
            tagCreationPanel.updateButtons();
        });
    }

    private void addTagButtonButton(TagCreationPanel tagCreationPanel, CellTable<TagButton> tagButtonTable,
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
                tagCreationPanel.storeAllTagButtons();
                setRowData(tagButtonTable, taggingPanel.getTagButtons());
            } else {
                Notification.notify(stringMessages.tagNotSpecified(), NotificationType.WARNING);
            }
            center();
        });
    }

    private void setRowData(CellTable<TagButton> tagButtonTable, List<TagButton> buttons) {
        tagButtonTable.setRowData(buttons);
        tagButtonTable.setVisible(!buttons.isEmpty());
    }
}
