package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.security.ui.client.UserService;

public class TaggingPanel extends ComponentWithoutSettings {

    private final Panel panel;
    private final Panel buttonsPanel;
    private final CellTable<TagDTO> content;
    private final List<Button> buttons;
    private final List<TagDTO> tags;
    private final ListDataProvider<TagDTO> tagProvider;
    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final UserService userService;

    private String leaderboardName = null;
    private RaceColumnDTO raceColumn = null;
    private FleetDTO fleet = null;

    public class TagDTO {
        String tag;
        String comment;
        String imageURL;
        String username;
        TimePoint raceTimepoint;

        public TagDTO(String tag, String comment, String imageURL, String username, TimePoint raceTimepoint) {
            this.tag = tag;
            this.comment = comment;
            this.imageURL = imageURL;
            this.username = username;
            this.raceTimepoint = raceTimepoint;
        }

        public String getTag() {
            return tag;
        }

        public String getComment() {
            return comment;
        }

        public String getImageURL() {
            return imageURL;
        }

        public String getUsername() {
            return username;
        }
        
        public TimePoint getRaceTimepoint() {
            return raceTimepoint;
        }
    }

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService) {
        super(parent, context);
        panel = new FlowPanel();
        buttonsPanel = new FlowPanel();
        buttons = new ArrayList<Button>();
        tags = new ArrayList<TagDTO>();
        tagProvider = new ListDataProvider<TagDTO>();
        content = new CellTable<TagDTO>();
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.userService = userService;
        initializePanel();
    }

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, String leaderboardName,
            RaceColumnDTO raceColumn, FleetDTO fleet) {
        this(parent, context, stringMessages, sailingService, userService);
        updateRace(leaderboardName, raceColumn, fleet);
    }

    private void initializePanel() {
        // Misc
        panel.getElement().getStyle().setMargin(6, Unit.PX);
        panel.getElement().getStyle().setMarginTop(10, Unit.PX);
        panel.add(new Label(stringMessages.tagging()));
        
        // Buttons
        panel.add(new Button("Add new Tag-Button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addNewTagButton();
            }
        }));
        panel.add(buttonsPanel);
        
        // Content
        content.addColumn(new TextColumn<TagDTO>() {
            @Override
            public String getValue(TagDTO object) {
                return object.getTag();
            }
        }, "Tag");
        content.addColumn(new TextColumn<TagDTO>() {
            @Override
            public String getValue(TagDTO object) {
                return object.getComment();
            }
        }, "Comment");
        content.addColumn(new TextColumn<TagDTO>() {
            @Override
            public String getValue(TagDTO object) {
                return object.getUsername();
            }
        }, "Created by");
        content.addColumn(new TextColumn<TagDTO>() {
            @Override
            public String getValue(TagDTO object) {
                return object.getRaceTimepoint().toString();
            }
        }, "Race Timepoint");
        tagProvider.addDataDisplay(content);
        tagProvider.setList(tags);
        panel.add(content);
        
        updateUi();
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

    private void addNewTagButton() {
        Button tagButton = new Button("Tag-Button");
        tagButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (leaderboardName == null || raceColumn == null || fleet == null) {
                    Notification.notify("Could not trigger RaceLogTagEvent, missing information!", NotificationType.ERROR);
                    return;
                }
                if(userService.getCurrentUser() == null) {
                    Notification.notify("Please log in to add new tags!", NotificationType.WARNING);
                    return;
                }
                
                TagDTO tag = new TagDTO("Super Duper Tag!", "Fancy comment...", "https://localhost:8080/image/abc.png", userService.getCurrentUser().getName(), MillisecondsTimePoint.now());
                sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag.getTag(), tag.getComment(), tag.getImageURL(), tag.getUsername(), tag.getRaceTimepoint(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not add new tag to race log!", caught);
                        Notification.notify("Could not add new tag to race log!", NotificationType.ERROR);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify("Added new tag successfully", NotificationType.INFO);
                        tags.add(tag);
                        updateContent();
                    }
                });
            }
        });
        buttons.add(tagButton);
        updateButtons();
    }

    private void updateUi() {
        updateButtons();
        updateContent();
    }
    
    private void updateContent() {
        tagProvider.refresh();
    }
    
    private void updateButtons() {
        buttonsPanel.clear();
        buttons.forEach(button -> {
            buttonsPanel.add(button);
        });
    }

    @Override
    public String getId() {
        return "TaggingPanel";
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.tagging();
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
