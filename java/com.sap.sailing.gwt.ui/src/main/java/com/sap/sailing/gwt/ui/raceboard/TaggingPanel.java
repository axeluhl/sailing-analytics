package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
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
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.security.ui.client.UserService;

public class TaggingPanel extends ComponentWithoutSettings implements TimeListener {

    private final Panel panel;
    private final Panel buttonsPanel;
    private final CellTable<TagDTO> content;
    private final List<Button> buttons;
    private final List<TagDTO> tags;
    private final ListDataProvider<TagDTO> tagProvider;
    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final Timer timer;

    private String leaderboardName = null;
    private RaceColumnDTO raceColumn = null;
    private FleetDTO fleet = null;
    private TimePoint lastReceivedTag = null;

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer) {
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
        this.timer = timer;
        timer.addTimeListener(this);
        initializePanel();
    }

    public TaggingPanel(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            SailingServiceAsync sailingService, UserService userService, Timer timer, String leaderboardName,
            RaceColumnDTO raceColumn, FleetDTO fleet) {
        this(parent, context, stringMessages, sailingService, userService, timer);
        updateRace(leaderboardName, raceColumn, fleet);
    }

    private void initializePanel() {
        // Misc
        panel.setTitle(stringMessages.tagging());
        panel.getElement().getStyle().setMargin(6, Unit.PX);
        panel.getElement().getStyle().setMarginTop(10, Unit.PX);

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
                    Notification.notify("Could not trigger RaceLogTagEvent, missing information!",
                            NotificationType.ERROR);
                    return;
                }
                if (userService.getCurrentUser() == null) {
                    Notification.notify("Please log in to add new tags!", NotificationType.WARNING);
                    return;
                }

                TagDTO tag = new TagDTO("Super Duper Tag!", "Fancy comment...", "https://localhost:8080/image/abc.png",
                        userService.getCurrentUser().getName(), new MillisecondsTimePoint(timer.getTime()));
                sailingService.addTagToRaceLog(leaderboardName, raceColumn.getName(), fleet.getName(), tag.getTag(),
                        tag.getComment(), tag.getImageURL(), tag.getUsername(), tag.getRaceTimepoint(),
                        new AsyncCallback<Void>() {
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
        content.setVisibleRange(0, tags.size());
        tagProvider.refresh();
    }

    private void updateButtons() {
        buttonsPanel.clear();
        buttons.forEach(button -> {
            buttonsPanel.add(button);
        });
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if(leaderboardName != null && raceColumn != null && fleet != null && panel.isAttached() && panel.isVisible() && oldTime != null) {
            // load tags since last received tag => decrease required bandwidth as only difference in tags will be sent over network
            sailingService.getTags(leaderboardName, raceColumn.getName(), fleet.getName(), lastReceivedTag, new MillisecondsTimePoint(newTime.getTime()), new AsyncCallback<List<TagDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Notification.notify("Could not load tags!", NotificationType.ERROR);
                    GWT.log("Could not load tags!", caught);
                }

                @Override
                public void onSuccess(List<TagDTO> result) {
                    if(result != null) {
                        for(TagDTO tag : result) {
                            if(!tags.contains(tag)) {                                
                                tags.add(tag);
                                lastReceivedTag = tag.getRaceTimepoint();
                                updateContent();
                            }
                        }
                    }
                }
            });
        }
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
