package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.listedit.StringConstantsListEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public abstract class EventDialog extends DataEntryDialogWithBootstrap<EventDTO> {
    protected StringMessages stringMessages;
    protected TextBox nameEntryField;
    protected TextArea descriptionEntryField;
    protected TextBox venueEntryField;
    protected BetterDateTimeBox startDateBox;
    protected BetterDateTimeBox endDateBox;
    protected CheckBox isPublicCheckBox;
    protected UUID id;
    protected TextBox officialWebsiteURLEntryField;
    protected TextBox sailorsInfoWebsiteURLEntryField;
    protected StringListInlineEditorComposite courseAreaNameList;
    protected StringConstantsListEditorComposite leaderboardGroupList;
    protected Map<String, LeaderboardGroupDTO> availableLeaderboardGroupsByName;
    protected ImagesListComposite imagesListComposite; 
    protected VideosListComposite videosListComposite; 

    protected static class EventParameterValidator implements Validator<EventDTO> {

        private StringMessages stringMessages;
        private ArrayList<EventDTO> existingEvents;

        public EventParameterValidator(StringMessages stringMessages, Collection<EventDTO> existingEvents) {
            this.stringMessages = stringMessages;
            this.existingEvents = new ArrayList<EventDTO>(existingEvents);
        }

        @Override
        public String getErrorMessage(EventDTO eventToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = eventToValidate.getName() != null && eventToValidate.getName().length() > 0;
            boolean venueNotEmpty = eventToValidate.venue.getName() != null && eventToValidate.venue.getName().length() > 0;
            boolean courseAreaNotEmpty = eventToValidate.venue.getCourseAreas() != null && eventToValidate.venue.getCourseAreas().size() > 0;

            if (courseAreaNotEmpty) {
                for (CourseAreaDTO courseArea : eventToValidate.venue.getCourseAreas()) {
                    courseAreaNotEmpty = courseArea.getName() != null && courseArea.getName().length() > 0;
                    if (!courseAreaNotEmpty)
                        break;
                }
            }

            boolean unique = true;
            for (EventDTO event : existingEvents) {
                if (event.getName().equals(eventToValidate.getName())) {
                    unique = false;
                    break;
                }
            }

            Date startDate = eventToValidate.startDate;
            Date endDate = eventToValidate.endDate;
            String datesErrorMessage = null;
            // remark: startDate == null and endDate == null is valid
            if(startDate != null && endDate != null) {
                if(startDate.after(endDate)) {
                    datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate(); 
                }
            } else if((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
                datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate();
            }
            
            if(datesErrorMessage != null) {
                errorMessage = datesErrorMessage;
            } else if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
            } else if (!venueNotEmpty) {
                errorMessage = stringMessages.pleaseEnterNonEmptyVenue();
            } else if (!courseAreaNotEmpty) {
                errorMessage = stringMessages.pleaseEnterNonEmptyCourseArea();
            } else if (!unique) {
                errorMessage = stringMessages.eventWithThisNameAlreadyExists();
            }

            return errorMessage;
        }

    }

    /**
     * @param leaderboardGroupsOfEvent even though not editable in this dialog, this parameter gives an editing subclass a chance to "park" the leaderboard group
     * assignments for re-association with the new {@link EventDTO} created by the {@link #getResult} method.
     */
    public EventDialog(EventParameterValidator validator, SailingServiceAsync sailingService, StringMessages stringMessages, List<LeaderboardGroupDTO> availableLeaderboardGroups,
            Iterable<LeaderboardGroupDTO> leaderboardGroupsOfEvent, DialogCallback<EventDTO> callback) {
        super(stringMessages.event(), null, stringMessages.ok(), stringMessages.cancel(), validator,
                callback);
        this.stringMessages = stringMessages;
        this.availableLeaderboardGroupsByName = new HashMap<>();
        for (final LeaderboardGroupDTO lgDTO : availableLeaderboardGroups) {
            availableLeaderboardGroupsByName.put(lgDTO.getName(), lgDTO);
        }
        getDialogBox().getWidget().setWidth("730px");
        final ValueChangeHandler<Iterable<String>> valueChangeHandler = new ValueChangeHandler<Iterable<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                validate();
            }
        };

        courseAreaNameList = new StringListInlineEditorComposite(Collections.<String> emptyList(),
                new StringListInlineEditorComposite.ExpandedUi(stringMessages, IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        SuggestedCourseAreaNames.suggestedCourseAreaNames, stringMessages.enterCourseAreaName(), 50));
        courseAreaNameList.addValueChangeHandler(valueChangeHandler);
        List<String> leaderboardGroupNames = new ArrayList<>();
        for(LeaderboardGroupDTO leaderboardGroupDTO: availableLeaderboardGroups) {
            leaderboardGroupNames.add(leaderboardGroupDTO.getName());
        }
        leaderboardGroupList = new StringConstantsListEditorComposite(Collections.<String> emptyList(),
                new StringConstantsListEditorComposite.ExpandedUi(stringMessages, IconResources.INSTANCE.removeIcon(),
                        leaderboardGroupNames, "Select a leaderboard group..."));
        leaderboardGroupList.addValueChangeHandler(valueChangeHandler);
        
        imagesListComposite = new ImagesListComposite(sailingService, stringMessages);
        videosListComposite = new VideosListComposite(stringMessages);
    }

    @Override
    protected EventDTO getResult() {
        EventDTO result = new EventDTO();
        List<String> leaderboardGroupNames = leaderboardGroupList.getValue();
        for (final String lgName : leaderboardGroupNames) {
            final LeaderboardGroupDTO lgDTO = availableLeaderboardGroupsByName.get(lgName);
            if (lgDTO != null) {
                result.addLeaderboardGroup(lgDTO);
            }                
        }
        result.setName(nameEntryField.getText());
        result.setDescription(descriptionEntryField.getText());
        result.setOfficialWebsiteURL(officialWebsiteURLEntryField.getText().trim().isEmpty() ? null : officialWebsiteURLEntryField.getText().trim());
        result.setSailorsInfoWebsiteURL(sailorsInfoWebsiteURLEntryField.getText().trim().isEmpty() ? null : sailorsInfoWebsiteURLEntryField.getText().trim());
        result.startDate = startDateBox.getValue();
        result.endDate = endDateBox.getValue();
        result.isPublic = isPublicCheckBox.getValue();
        result.id = id;

        List<CourseAreaDTO> courseAreas = new ArrayList<CourseAreaDTO>();
        for (String courseAreaName : courseAreaNameList.getValue()) {
            CourseAreaDTO courseAreaDTO = new CourseAreaDTO();
            courseAreaDTO.setName(courseAreaName);
            courseAreas.add(courseAreaDTO);
        }
        for(ImageDTO image: imagesListComposite.getAllImages()) {
            result.addImage(image);
        }
        for(VideoDTO video: videosListComposite.getAllVideos()) {
            result.addVideo(video);
        }
        result.venue = new VenueDTO(venueEntryField.getText(), courseAreas);
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        Grid formGrid = new Grid(8, 2);
        formGrid.setWidget(0,  0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1,  0, new Label(stringMessages.description() + ":"));
        formGrid.setWidget(1, 1, descriptionEntryField);
        formGrid.setWidget(2, 0, new Label(stringMessages.venue() + ":"));
        formGrid.setWidget(2, 1, venueEntryField);
        formGrid.setWidget(3, 0, new Label(stringMessages.startDate() + ":"));
        formGrid.setWidget(3, 1, startDateBox);
        formGrid.setWidget(4, 0, new Label(stringMessages.endDate() + ":"));
        formGrid.setWidget(4, 1, endDateBox);
        formGrid.setWidget(5, 0, new Label(stringMessages.isPublic() + ":"));
        formGrid.setWidget(5, 1, isPublicCheckBox);
        formGrid.setWidget(6, 0, new Label(stringMessages.eventOfficialWebsiteURL() + ":"));
        formGrid.setWidget(6, 1, officialWebsiteURLEntryField);
        formGrid.setWidget(7, 0, new Label(stringMessages.eventSailorsInfoWebsiteURL() + ":"));
        formGrid.setWidget(7, 1, sailorsInfoWebsiteURLEntryField);

        TabLayoutPanel tabPanel =  new TabLayoutPanel(30, Unit.PX);
        tabPanel.setHeight("500px");
        panel.add(tabPanel);
        tabPanel.add(new ScrollPanel(formGrid), stringMessages.event());
        tabPanel.add(new ScrollPanel(leaderboardGroupList), stringMessages.leaderboardGroups());
        tabPanel.add(new ScrollPanel(courseAreaNameList), stringMessages.courseAreas());
        tabPanel.add(new ScrollPanel(imagesListComposite), stringMessages.images());
        tabPanel.add(new ScrollPanel(videosListComposite), stringMessages.videos());
        return panel;
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
