package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanel.State;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.UserService;

/**
 * Panel used to create tags and tag buttons in side menu of RaceBoard.
 */
public class TagCreationPanel extends FlowPanel {

    private static final String USER_STORAGE_TAG_BUTTONS_KEY = "sailingAnalytics.raceBoard.tagButtons";

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();
    private final Panel tagButtonsPanel;

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;
    private final UserService userService;

    public TagCreationPanel(TaggingPanel taggingPanel) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();
        this.userService = taggingPanel.getUserSerivce();

        setStyleName(style.tagCreationPanel());

        TagInputPanel inputPanel = new TagInputPanel(stringMessages);

        tagButtonsPanel = new FlowPanel();
        tagButtonsPanel.setStyleName(style.buttonsPanel());
        loadAllTagButtons();

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

        final TagCreationPanel INSTANCE = this;
        Button editCustomTagButtons = new Button(stringMessages.tagEditCustomTagButtons());
        editCustomTagButtons.setStyleName(style.tagDialogButton());
        editCustomTagButtons.addStyleName("gwt-Button");
        editCustomTagButtons.addClickHandler(event -> {
            if (taggingPanel.isLoggedInAndRaceLogAvailable()) {
                new TagButtonDialog(taggingPanel, INSTANCE);
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
        add(tagButtonsPanel);
    }

    public void updateButtons() {
        /*
         * If the height of the customButtonsPanel has changed after deleting (delta not equals to 0 ), the footerWidget
         * of the TaggingPanel has a different height, which in this case might cause the contentWidget to be to small.
         */
        final int oldHeight = tagButtonsPanel.getOffsetHeight();
        tagButtonsPanel.clear();
        taggingPanel.getTagButtons().forEach(button -> {
            tagButtonsPanel.add(button);
        });
        if ((tagButtonsPanel.getOffsetHeight() - oldHeight) != 0) {
            taggingPanel.refreshContentPanel();
        }
    }

    public void storeAllTagButtons() {
        TagButtonJsonDeSerializer serializer = new TagButtonJsonDeSerializer();
        JSONObject jsonObject = serializer.serialize(taggingPanel.getTagButtons());
        userService.setPreference(USER_STORAGE_TAG_BUTTONS_KEY, jsonObject.toString(), new AsyncCallback<Void>() {
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
            userService.getPreference(USER_STORAGE_TAG_BUTTONS_KEY, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                    // preference does not exist for this user in user store
                    // => user did not save any tag-buttons before
                    // => ignore error
                }

                @Override
                public void onSuccess(String result) {
                    taggingPanel.getTagButtons().clear();
                    if (result != null && !result.isEmpty()) {
                        final TagButtonJsonDeSerializer deserializer = new TagButtonJsonDeSerializer();
                        final JSONValue value = JSONParser.parseStrict(result);
                        if (value.isObject() != null) {
                            taggingPanel.getTagButtons().clear();
                            for (TagButton tagButton : deserializer.deserialize((JSONObject) value)) {
                                taggingPanel.addTagButton(tagButton);
                            }
                        }
                    }
                    updateButtons();
                }
            });
        } else {
            taggingPanel.getTagButtons().clear();
            updateButtons();
        }
    }
}
