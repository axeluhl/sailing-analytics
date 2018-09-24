package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanel.State;
import com.sap.sse.security.ui.client.UserService;

/**
 * Panel used to create and edit tags and {@link TagButton tag-buttons} at the {@link TaggingPanel#footerPanel footer}
 * of the {@link TaggingPanel}.
 */
public class TagModificationPanel extends FlowPanel {

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;

    private final Label heading;
    private final Panel editButtonsPanel, createButtonsPanel;
    private final TagInputPanel inputPanel;

    /**
     * Creates panel used to create tags and {@link TagButton tag-buttons}.
     * 
     * @param taggingPanel
     *            provides reference to {@link StringMessages}
     * @param tagFooterPanel
     *            required for creation of {@link TagButton tag-buttons}
     */
    protected TagModificationPanel(TaggingPanel taggingPanel, TagFooterPanel tagFooterPanel,
            StringMessages stringMessages, UserService userService) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = stringMessages;

        setStyleName(style.tagModificationPanel());

        inputPanel = new TagInputPanel(taggingPanel, stringMessages);

        Button createTagFromInputFields = new Button(stringMessages.tagAddTag());
        createTagFromInputFields.setStyleName(style.tagDialogButton());
        createTagFromInputFields.addStyleName("gwt-Button");
        createTagFromInputFields.addClickHandler(event -> {
            if (taggingPanel.isLoggedInAndRaceLogAvailable()) {
                taggingPanel.saveTag(inputPanel.getTag(), inputPanel.getComment(), inputPanel.getImageURL(),
                        inputPanel.isVisibleForPublic());
                inputPanel.clearAllValues();
            }
        });

        Button editCustomTagButtons = new Button(stringMessages.tagEditCustomTagButtons());
        editCustomTagButtons.setStyleName(style.tagDialogButton());
        editCustomTagButtons.addStyleName("gwt-Button");
        editCustomTagButtons.addClickHandler(event -> {
            if (taggingPanel.isLoggedInAndRaceLogAvailable()) {
                new TagButtonDialog(taggingPanel, tagFooterPanel, stringMessages, userService);
            }
        });

        createButtonsPanel = new FlowPanel();
        createButtonsPanel.setStyleName(style.buttonsPanel());
        createButtonsPanel.add(editCustomTagButtons);
        createButtonsPanel.add(createTagFromInputFields);
        createButtonsPanel.setVisible(true);

        Button saveTagChanges = new Button(stringMessages.save());
        saveTagChanges.setStyleName(style.tagDialogButton());
        saveTagChanges.addStyleName("gwt-Button");
        saveTagChanges.addClickHandler(event -> {
            taggingPanel.updateTag(taggingPanel.getSelectedTag(), inputPanel.getTag(), inputPanel.getComment(),
                    inputPanel.getImageURL(), inputPanel.isVisibleForPublic());
            resetState();
        });

        Button cancelTagChanges = new Button(stringMessages.cancel());
        cancelTagChanges.setStyleName(style.tagDialogButton());
        cancelTagChanges.addStyleName("gwt-Button");
        cancelTagChanges.addClickHandler(event -> {
            if ((taggingPanel.getCurrentState().equals(State.CREATE_TAG) && !inputPanel.isInputEmpty())
                    || (taggingPanel.getCurrentState().equals(State.EDIT_TAG)
                            && !inputPanel.compareFieldsToTag(taggingPanel.getSelectedTag()))) {
                new ConfirmationDialog(stringMessages, stringMessages.tagDiscardChangesHeading(),
                        stringMessages.tagDiscardChanges(), result -> {
                            if (result) {
                                resetState();
                            }
                        });
            } else {
                resetState();
            }
        });

        editButtonsPanel = new FlowPanel();
        editButtonsPanel.setStyleName(style.buttonsPanel());
        editButtonsPanel.add(saveTagChanges);
        editButtonsPanel.add(cancelTagChanges);
        editButtonsPanel.setVisible(false);

        Panel headerPanel = new FlowPanel();
        headerPanel.setStyleName(style.tagModificationPanelHeader());
        heading = new Label(stringMessages.tagAddTags());
        heading.setStyleName(style.tagModificationPanelHeaderLabel());
        headerPanel.add(heading);
        Button closeFooterButton = new Button("X");
        closeFooterButton.setStyleName(style.tagModificationPanelHeaderButton());
        closeFooterButton.setTitle(stringMessages.close());
        closeFooterButton.addClickHandler(event -> {
            if ((taggingPanel.getCurrentState().equals(State.CREATE_TAG) && !inputPanel.isInputEmpty())
                    || (taggingPanel.getCurrentState().equals(State.EDIT_TAG)
                            && !inputPanel.compareFieldsToTag(taggingPanel.getSelectedTag()))) {
                new ConfirmationDialog(stringMessages, stringMessages.tagDiscardChangesHeading(),
                        stringMessages.tagDiscardChanges(), result -> {
                            if (result) {
                                resetState();
                            }
                        });
            } else {
                resetState();
            }
        });
        headerPanel.add(closeFooterButton);

        add(headerPanel);
        add(inputPanel);
        add(createButtonsPanel);
        add(editButtonsPanel);
    }

    /**
     * Updates UI to match {@link TaggingPanel#getCurrentState() current state}.
     * 
     * @param state
     *            new state
     */
    protected void setCurrentStatus(State state) {
        if (state.equals(State.EDIT_TAG)) {
            heading.setText(stringMessages.tagEditTag());
            createButtonsPanel.setVisible(false);
            editButtonsPanel.setVisible(true);
            inputPanel.setTag(taggingPanel.getSelectedTag());
        } else {
            heading.setText(stringMessages.tagAddTags());
            createButtonsPanel.setVisible(true);
            editButtonsPanel.setVisible(false);
        }
        inputPanel.setCurrentStatus();
    }

    /**
     * Resets input fields and {@link TaggingPanel#setCurrentState(State) TaggingPanels currentState}.
     */
    private void resetState() {
        inputPanel.clearAllValues();
        taggingPanel.setCurrentState(State.VIEW);
    }
}
