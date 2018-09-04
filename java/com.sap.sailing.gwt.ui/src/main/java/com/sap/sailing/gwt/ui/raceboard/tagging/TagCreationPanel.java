package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanel.State;

/**
 * Panel used to create tags and {@link TagButton tag-buttons} at the {@link TaggingPanel#footerPanel footer} of the
 * {@link TaggingPanel}.
 */
public class TagCreationPanel extends FlowPanel {

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private final StringMessages stringMessages;

    /**
     * Creates panel used to create tags and {@link TagButton tag-buttons}.
     * 
     * @param taggingPanel
     *            provides reference to {@link StringMessages}
     * @param tagFooterPanel
     *            required for creation of {@link TagButton tag-buttons}
     */
    protected TagCreationPanel(TaggingPanel taggingPanel, TagFooterPanel tagFooterPanel) {
        this.stringMessages = taggingPanel.getStringMessages();

        setStyleName(style.tagCreationPanel());

        TagInputPanel inputPanel = new TagInputPanel(stringMessages);

        Button createTagFromTextBoxes = new Button(stringMessages.tagAddTag());
        createTagFromTextBoxes.setStyleName(style.tagDialogButton());
        createTagFromTextBoxes.addStyleName("gwt-Button");
        createTagFromTextBoxes.addClickHandler(event -> {
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
                new TagButtonDialog(taggingPanel, tagFooterPanel);
            }
        });

        Panel standardButtonsPanel = new FlowPanel();
        standardButtonsPanel.setStyleName(style.buttonsPanel());
        standardButtonsPanel.add(editCustomTagButtons);
        standardButtonsPanel.add(createTagFromTextBoxes);

        Panel headerPanel = new FlowPanel();
        headerPanel.setStyleName(style.tagCreationPanelHeader());
        Label heading = new Label(stringMessages.tagAddTags());
        heading.setStyleName(style.tagCreationPanelHeaderLabel());
        headerPanel.add(heading);
        Button closeFooterButton = new Button("X");
        closeFooterButton.setStyleName(style.tagCreationPanelHeaderButton());
        closeFooterButton.setTitle(stringMessages.close());
        closeFooterButton.addClickHandler(event -> {
            taggingPanel.setCurrentState(State.VIEW);
        });
        headerPanel.add(closeFooterButton);

        add(headerPanel);
        add(inputPanel);
        add(standardButtonsPanel);
    }
}
