package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;

public class EventDetailsComposite extends Composite  {
    private EventDTO event;
    private final StringMessages stringMessages;
    private final Label eventId;
    private final Label eventName;
    private final Label venueName;
    private final Label description;
    private final Label startDate;
    private final Label endDate;
    private final Label isPublic;
    private final Anchor officialWebsiteURL;
    private final Anchor eventOverviewURL;
    private final Anchor logoImageURL;
    private final SimpleStringListComposite courseAreaNamesList;
    private final SimpleStringListComposite imageURLList;
    private final SimpleStringListComposite videoURLList;
    private final SimpleStringListComposite sponsorImageURLList;
    private final SimpleStringListComposite leaderboardGroupList;
    
    private final CaptionPanel mainPanel;

    public EventDetailsComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;

        event = null;
        mainPanel = new CaptionPanel(stringMessages.regatta());
        VerticalPanel vPanel = new VerticalPanel();
        mainPanel.add(vPanel);

        int rows = 15;
        Grid grid = new Grid(rows, 2);
        vPanel.add(grid);
        
        int currentRow = 0;
        eventId = createLabelAndValueWidget(grid, currentRow++, stringMessages.id(), "IdLabel");
        eventName = createLabelAndValueWidget(grid, currentRow++, stringMessages.eventName(), "NameLabel");
        description = createLabelAndValueWidget(grid, currentRow++, stringMessages.description(), "DescriptionLabel");
        venueName = createLabelAndValueWidget(grid, currentRow++, stringMessages.venue(), "VenueLabel");
        startDate = createLabelAndValueWidget(grid, currentRow++, stringMessages.startDate(), "StartDateLabel");
        endDate = createLabelAndValueWidget(grid, currentRow++, stringMessages.endDate(), "EndDateLabel");
        isPublic = createLabelAndValueWidget(grid, currentRow++, stringMessages.isPublic(), "IsPublicLabel");
        officialWebsiteURL = createAnchorAndValueWidget(grid, currentRow++, stringMessages.eventOfficialWebsiteURL(), "OfficialWebsiteURLLabel");
        eventOverviewURL = createAnchorAndValueWidget(grid, currentRow++, stringMessages.eventOverviewURL(), "EventOverviewURLLabel");
        logoImageURL = createAnchorAndValueWidget(grid, currentRow++, stringMessages.eventLogoImageURL(), "LogoImageURLLabel");
        courseAreaNamesList = createLableAndValueListWidget(grid, currentRow++, stringMessages.courseAreas(), "CourseAreaValueList");
        imageURLList = createLableAndValueListWidget(grid, currentRow++, stringMessages.imageURLs(), "ImageURLValueList");
        videoURLList = createLableAndValueListWidget(grid, currentRow++, stringMessages.videoURLs(), "VideoURLValueList");
        sponsorImageURLList = createLableAndValueListWidget(grid, currentRow++, stringMessages.sponsorImageURLs(), "SponsorImageURLValueList");
        leaderboardGroupList = createLableAndValueListWidget(grid, currentRow++, stringMessages.leaderboardGroups(), "LeaderboardGroupValueList");
        
        for(int i=0; i < rows; i++) {
            grid.getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_TOP);
            grid.getCellFormatter().setVerticalAlignment(i, 1, HasVerticalAlignment.ALIGN_TOP);
        }

        initWidget(mainPanel);
    }
    
    private Label createLabelAndValueWidget(Grid grid, int row, String label, String debugId) {
        Label valueLabel = new Label();
        valueLabel.ensureDebugId(debugId);
        grid.setWidget(row , 0, new Label(label + ":"));
        grid.setWidget(row , 1, valueLabel);
        return valueLabel;
    }

    private Anchor createAnchorAndValueWidget(Grid grid, int row, String label, String debugId) {
        Anchor valueAnchor = new Anchor();
        valueAnchor.setTarget("_blank");
        valueAnchor.ensureDebugId(debugId);
        grid.setWidget(row , 0, new Label(label + ":"));
        grid.setWidget(row , 1, valueAnchor);
        return valueAnchor;
    }

    private SimpleStringListComposite createLableAndValueListWidget(Grid grid, int row, String label, String debugId) {
        SimpleStringListComposite valueList = new SimpleStringListComposite();
        valueList.ensureDebugId(debugId);
        grid.setWidget(row , 0, new Label(label + ":"));
        grid.setWidget(row , 1, valueList);
        return valueList;
    }

    public EventDTO getEvent() {
        return event;
    }

    public void setEvent(EventDTO event) {
        this.event = event;
        updateEventDetails();
    }

    private void updateEventDetails() {
        if (event != null) {
            mainPanel.setCaptionText(stringMessages.event() + " " + event.getName());
            eventName.setText(event.getName());
            eventId.setText(event.id.toString());
            venueName.setText(event.venue.getName());
            description.setText(event.getDescription());
            startDate.setText(event.startDate != null ? event.startDate.toString() : "");
            endDate.setText(event.endDate != null ? event.endDate.toString() : "");
            isPublic.setText(String.valueOf(event.isPublic));
            officialWebsiteURL.setText(event.getOfficialWebsiteURL());
            officialWebsiteURL.setHref(event.getOfficialWebsiteURL());
            Map<String, String> regattaOverviewURLParameters = new HashMap<String, String>();
            regattaOverviewURLParameters.put("event", event.id.toString());
            String regattaOverviewLink = EntryPointLinkFactory.createRegattaOverviewLink(regattaOverviewURLParameters);
            eventOverviewURL.setText(regattaOverviewLink);
            eventOverviewURL.setHref(regattaOverviewLink);
            logoImageURL.setText(event.getLogoImageURL());
            logoImageURL.setHref(event.getLogoImageURL());
     
            List<String> courseAreaNames = new ArrayList<>();
            if (event.venue.getCourseAreas() != null && event.venue.getCourseAreas().size() > 0) {
                for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                    courseAreaNames.add(courseArea.getName());
                }
            }
            courseAreaNamesList.setValues(courseAreaNames);

            List<String> imageURLStringsAsList = new ArrayList<>();
            Util.addAll(event.getImageURLs(), imageURLStringsAsList);
            imageURLList.setValues(imageURLStringsAsList);
            List<String> videoURLStringsAsList = new ArrayList<>();
            Util.addAll(event.getVideoURLs(), videoURLStringsAsList);
            videoURLList.setValues(videoURLStringsAsList);
            List<String> sponsorImageURLStringsAsList = new ArrayList<>();
            Util.addAll(event.getSponsorImageURLs(), sponsorImageURLStringsAsList);
            sponsorImageURLList.setValues(sponsorImageURLStringsAsList);
            List<String> leaderboardGroupNamesAsList = new ArrayList<>();
            for(LeaderboardGroupDTO leaderboardGroupDTO: event.getLeaderboardGroups()) {
                leaderboardGroupNamesAsList.add(leaderboardGroupDTO.getName());
            }
            leaderboardGroupList.setValues(leaderboardGroupNamesAsList);
        }
    }

    private class SimpleStringListComposite extends Composite {
        private final FlowPanel panel;
            
        public SimpleStringListComposite() {
            super();
            panel = new FlowPanel();
            this.initWidget(panel);
        }
        
        public void setValues(List<String> values) {
            panel.clear();
            for(String value: values) {
                panel.add(new ListItemWidget(value));
            }
        }
    }
    
    public class ListItemWidget extends SimplePanel {
        public ListItemWidget() {
            super((Element) Document.get().createLIElement().cast());
            getElement().getStyle().setProperty("listStylePosition", "inside");
        }

        public ListItemWidget(String s) {
            this();
            getElement().setInnerText(s);
        }

        public ListItemWidget(Widget w) {
            this();
            this.add(w);
        }
    }
}
