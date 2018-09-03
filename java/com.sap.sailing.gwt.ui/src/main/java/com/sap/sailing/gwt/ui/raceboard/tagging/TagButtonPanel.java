package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.UserService;

/**
 * Displays tag buttons of current user.
 */
public class TagButtonPanel extends FlowPanel {

    private static final String USER_STORAGE_TAG_BUTTONS_KEY = "sailingAnalytics.raceBoard.tagButtons";

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private final TagFooterPanel footerPanel;
    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;
    private final UserService userService;

    private final Label heading;
    private final Panel tagButtonsPanel;

    public TagButtonPanel(TagFooterPanel footerPanel, TaggingPanel taggingPanel) {
        this.footerPanel = footerPanel;
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();
        this.userService = taggingPanel.getUserSerivce();

        heading = new Label(stringMessages.tagButtons());
        heading.setStyleName(style.tagButtonPanelHeader());
        add(heading);

        tagButtonsPanel = new FlowPanel();
        tagButtonsPanel.setStyleName(style.buttonsPanel());
        tagButtonsPanel.addStyleName(style.tagButtonPanel());
        add(tagButtonsPanel);
    }

    /**
     * Stores local copy of tag buttons from current user in user storage.
     */
    protected void storeAllTagButtons() {
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

    /**
     * Loads all tag buttons from current user from user storage and displays them.
     */
    protected void loadAllTagButtons() {
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
                            for (TagButton tagButton : deserializer.deserialize((JSONObject) value)) {
                                taggingPanel.addTagButton(tagButton);
                            }
                        }
                    }
                    recalculateHeight();
                }
            });
        } else {
            taggingPanel.getTagButtons().clear();
            recalculateHeight();
        }
    }

    /**
     * If the height of the tagButtonsPanel has changed after deleting all tag buttons (delta not equals 0 ), the
     * footerWidget of the TaggingPanel has a different height, which in this case might cause the contentWidget to be
     * to small. In case no tag buttons are available for the current user, tag button panel will be hidden.
     */
    protected void recalculateHeight() {
        if (taggingPanel.getTagButtons().size() == 0) {
            tagButtonsPanel.clear();
            footerPanel.setTagButtonsVisibility(false);
        } else {
            footerPanel.setTagButtonsVisibility(true);
            final int oldHeight = getOffsetHeight();
            tagButtonsPanel.clear();
            taggingPanel.getTagButtons().forEach(button -> {
                tagButtonsPanel.add(button);
            });
            if ((getOffsetHeight() - oldHeight) != 0) {
                taggingPanel.refreshContentPanel();
            }
        }
    }
}
