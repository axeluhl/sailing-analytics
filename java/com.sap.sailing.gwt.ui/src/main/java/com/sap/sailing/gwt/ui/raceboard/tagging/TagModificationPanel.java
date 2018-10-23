package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanel.State;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanelResources.TagPanelStyle;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.client.UserService;

/**
 * Panel used to create and edit tags and {@link TagButton tag-buttons} at the {@link TaggingPanel#footerPanel footer}
 * of the {@link TaggingPanel}.
 */
public class TagModificationPanel extends FlowPanel {

    private final TagPanelStyle style = TaggingPanelResources.INSTANCE.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;

    private final Label heading;
    private final Panel buttonsPanel;
    private final TagInputPanel inputPanel;
    private final Button editCustomTagButtons;

    /**
     * Creates panel used to create tags and {@link TagButton tag-buttons}.
     * 
     * @param taggingPanel
     *            provides reference to {@link StringMessages}
     * @param tagFooterPanel
     *            required for creation of {@link TagButton tag-buttons}
     */
    protected TagModificationPanel(TaggingPanel taggingPanel, TagFooterPanel tagFooterPanel,
            SailingServiceAsync sailingService, StringMessages stringMessages, UserService userService) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = stringMessages;
        setStyleName(style.tagModificationPanel());
        inputPanel = new TagInputPanel(taggingPanel, sailingService, stringMessages, new DialogCallback<TagDTO>() {
            @Override
            public void ok(TagDTO editedObject) {
                if (taggingPanel.getCurrentState().equals(State.EDIT_TAG)) {
                    taggingPanel.updateTag(taggingPanel.getSelectedTag(), inputPanel.getTag(), inputPanel.getComment(),
                            inputPanel.getImageURL(), inputPanel.isVisibleForPublic());
                    resetState();
                } else {
                    if (taggingPanel.isLoggedInAndRaceLogAvailable()) {
                        taggingPanel.saveTag(inputPanel.getTag(), inputPanel.getComment(), inputPanel.getImageURL(),
                                inputPanel.isVisibleForPublic());
                        // TODO: Add callback to saveTag() to clear fields only if tag got really added
                        inputPanel.clearAllValues();
                    }
                }
            }

            @Override
            public void cancel() {
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
            }
        });
        editCustomTagButtons = new Button(stringMessages.tagEditCustomTagButtons());
        editCustomTagButtons.setStyleName(style.tagDialogButton());
        editCustomTagButtons.addStyleName("gwt-Button");
        editCustomTagButtons.addClickHandler(event -> {
            if (taggingPanel.isLoggedInAndRaceLogAvailable()) {
                new TagButtonDialog(taggingPanel, tagFooterPanel, sailingService, stringMessages, userService);
            }
        });
        inputPanel.getOkButton().setText(stringMessages.tagAddTag());
        inputPanel.getOkButton().setStyleName(style.tagDialogButton());
        inputPanel.getOkButton().addStyleName("gwt-Button");
        inputPanel.getCancelButton().setText(stringMessages.cancel());
        inputPanel.getCancelButton().setStyleName(style.tagDialogButton());
        inputPanel.getCancelButton().addStyleName("gwt-Button");
        buttonsPanel = new FlowPanel();
        buttonsPanel.setStyleName(style.buttonsPanel());
        buttonsPanel.add(editCustomTagButtons);
        buttonsPanel.add(inputPanel.getOkButton());
        buttonsPanel.add(inputPanel.getCancelButton());
        buttonsPanel.setVisible(true);
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
        add(inputPanel.getStatusLabel());
        add(inputPanel);
        add(buttonsPanel);
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
            editCustomTagButtons.setVisible(false);
            inputPanel.getOkButton().setText(stringMessages.save());
            inputPanel.getCancelButton().setVisible(true);
            inputPanel.setTag(taggingPanel.getSelectedTag());
            inputPanel.validateAndUpdate();
        } else {
            heading.setText(stringMessages.tagAddTags());
            editCustomTagButtons.setVisible(true);
            inputPanel.getOkButton().setText(stringMessages.tagAddTag());
            inputPanel.getCancelButton().setVisible(false);
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
