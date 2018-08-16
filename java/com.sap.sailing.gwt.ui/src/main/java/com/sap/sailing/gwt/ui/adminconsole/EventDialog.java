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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.windfinder.AvailableWindFinderSpotCollections;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListInlineEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.StringConstantsListEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.filestorage.FileStorageManagementGwtServiceAsync;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;

public abstract class EventDialog extends DataEntryDialogWithDateTimeBox<EventDTO> {
    protected StringMessages stringMessages;
    protected TextBox nameEntryField;
    protected TextArea descriptionEntryField;
    protected TextBox venueEntryField;
    protected DateAndTimeInput startDateBox;
    protected DateAndTimeInput endDateBox;
    protected CheckBox isPublicCheckBox;
    protected UUID id;
    protected TextBox baseURLEntryField;
    protected CourseAreaListInlineEditorComposite courseAreaNameList;
    protected StringConstantsListEditorComposite leaderboardGroupList;
    protected StringListInlineEditorComposite windFinderSpotCollectionIdsComposite;
    protected Map<String, LeaderboardGroupDTO> availableLeaderboardGroupsByName;
    protected ImagesListComposite imagesListComposite;
    protected VideosListComposite videosListComposite;
    protected ExternalLinksComposite externalLinksComposite;
    private final MutableBoolean storageServiceAvailable = new MutableBoolean();
    
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
                    if (!courseAreaNotEmpty) {
                        break;
                    }
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
            if (startDate != null && endDate != null) {
                if (startDate.after(endDate)) {
                    datesErrorMessage = stringMessages.startDateMustBeforeEndDate(); 
                }
            } else if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
                datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate();
            }
            
            if (datesErrorMessage != null) {
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
    
    public static class MutableBoolean{
        private boolean value;
        public MutableBoolean() {
            
        }
        public void setValue(boolean value) {
            this.value = value;
        }
        public boolean getValue() {
            return value;
        }
    }

    /**
     * @param leaderboardGroupsOfEvent even though not editable in this dialog, this parameter gives an editing subclass a chance to "park" the leaderboard group
     * assignments for re-association with the new {@link EventDTO} created by the {@link #getResult} method.
     */
    public EventDialog(EventParameterValidator validator, SailingServiceAsync sailingService,
            StringMessages stringMessages, List<LeaderboardGroupDTO> availableLeaderboardGroups,
            Iterable<LeaderboardGroupDTO> leaderboardGroupsOfEvent, DialogCallback<EventDTO> callback) {
        super(stringMessages.event(), null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        testFileStorageService(sailingService);//callback: the earlier the better to improve user experience
        this.stringMessages = stringMessages;
        this.availableLeaderboardGroupsByName = new HashMap<>();
        for (final LeaderboardGroupDTO lgDTO : availableLeaderboardGroups) {
            availableLeaderboardGroupsByName.put(lgDTO.getName(), lgDTO);
        }
        getDialogBox().getWidget().setWidth("730px");
        final ValueChangeHandler<Iterable<String>> valueChangeHandler = new ValueChangeHandler<Iterable<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                validateAndUpdate();
            }
        };
        final ValueChangeHandler<Iterable<CourseAreaDTO>> courseAreaValueChangeHandler = new ValueChangeHandler<Iterable<CourseAreaDTO>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<CourseAreaDTO>> event) {
                validateAndUpdate();
            }
        };
        courseAreaNameList = new CourseAreaListInlineEditorComposite(Collections.<CourseAreaDTO> emptyList(),
                new GenericStringListInlineEditorComposite.ExpandedUi<CourseAreaDTO>(stringMessages, IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        SuggestedCourseAreaNames.suggestedCourseAreaNames, stringMessages.enterCourseAreaName(), 50));
        courseAreaNameList.addValueChangeHandler(courseAreaValueChangeHandler);
        List<String> leaderboardGroupNames = new ArrayList<>();
        for (LeaderboardGroupDTO leaderboardGroupDTO: availableLeaderboardGroups) {
            leaderboardGroupNames.add(leaderboardGroupDTO.getName());
        }
        leaderboardGroupList = new StringConstantsListEditorComposite(Collections.<String> emptyList(),
                new StringConstantsListEditorComposite.ExpandedUi(stringMessages, IconResources.INSTANCE.removeIcon(),
                        leaderboardGroupNames, stringMessages.selectALeaderboardGroup()));
        leaderboardGroupList.addValueChangeHandler(valueChangeHandler);
        imagesListComposite = new ImagesListComposite(sailingService, stringMessages,storageServiceAvailable);
        videosListComposite = new VideosListComposite(sailingService,stringMessages,storageServiceAvailable);
        externalLinksComposite = new ExternalLinksComposite(stringMessages);
        final List<String> suggestedWindFinderSpotCollections = AvailableWindFinderSpotCollections
                .getAllAvailableWindFinderSpotCollectionsInAlphabeticalOrder() == null ? Collections.emptyList()
                        : AvailableWindFinderSpotCollections
                                .getAllAvailableWindFinderSpotCollectionsInAlphabeticalOrder();
        windFinderSpotCollectionIdsComposite = new StringListInlineEditorComposite(Collections.<String> emptyList(),
                new GenericStringListEditorComposite.ExpandedUi<String>(stringMessages,
                        IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        suggestedWindFinderSpotCollections, stringMessages.enterIdOfWindFinderReviewedSpotCollection(), 35));
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
        result.setOfficialWebsiteURL(externalLinksComposite.getOfficialWebsiteURLValue());
        result.setBaseURL(baseURLEntryField.getText().trim().isEmpty() ? null : baseURLEntryField.getText().trim());
        result.setSailorsInfoWebsiteURLs(externalLinksComposite.getSailorsInfoWebsiteURLs());
        result.startDate = startDateBox.getValue();
        result.endDate = endDateBox.getValue();
        result.isPublic = isPublicCheckBox.getValue();
        result.id = id;
        List<CourseAreaDTO> courseAreas = courseAreaNameList.getValue();
        for (ImageDTO image : imagesListComposite.getAllImages()) {
            result.addImage(image);
        }
        for (VideoDTO video : videosListComposite.getAllVideos()) {
            result.addVideo(video);
        }
        result.venue = new VenueDTO(venueEntryField.getText(), courseAreas);
        result.setWindFinderReviewedSpotsCollection(windFinderSpotCollectionIdsComposite.getValue());
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
        int rowIndex = 0;
        formGrid.setWidget(rowIndex,  0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(rowIndex++, 1, nameEntryField);
        formGrid.setWidget(rowIndex,  0, new Label(stringMessages.description() + ":"));
        formGrid.setWidget(rowIndex++, 1, descriptionEntryField);
        formGrid.setWidget(rowIndex, 0, new Label(stringMessages.venue() + ":"));
        formGrid.setWidget(rowIndex++, 1, venueEntryField);
        formGrid.setWidget(rowIndex, 0, new Label(stringMessages.timeZone() + ":"));
        formGrid.setWidget(rowIndex++, 1, new Label(DateAndTimeFormatterUtil.getClientTimeZoneAsGMTString()));
        formGrid.setWidget(rowIndex, 0, new Label(stringMessages.startDate() + ":"));
        formGrid.setWidget(rowIndex++, 1, startDateBox);
        formGrid.setWidget(rowIndex, 0, new Label(stringMessages.endDate() + ":"));
        formGrid.setWidget(rowIndex++, 1, endDateBox);
        formGrid.setWidget(rowIndex, 0, new Label(stringMessages.isPublic() + ":"));
        formGrid.setWidget(rowIndex++, 1, isPublicCheckBox);
        formGrid.setWidget(rowIndex, 0, new Label(stringMessages.eventBaseURL() + ":"));
        formGrid.setWidget(rowIndex++, 1, baseURLEntryField);
        TabLayoutPanel tabPanel =  new TabLayoutPanel(30, Unit.PX);
        tabPanel.ensureDebugId("EventDialogTabs");
        tabPanel.setHeight("400px");
        panel.add(tabPanel);
        final ScrollPanel eventTab = new ScrollPanel(formGrid);
        eventTab.ensureDebugId("EventTab");
        tabPanel.add(eventTab, stringMessages.event());
        final ScrollPanel externalLinksCompositeTab = new ScrollPanel(externalLinksComposite);
        externalLinksCompositeTab.ensureDebugId("ExternalLinksCompositeTab");
        tabPanel.add(externalLinksCompositeTab, stringMessages.externalLinks());
        final ScrollPanel leaderboardGroupTab = new ScrollPanel(leaderboardGroupList);
        leaderboardGroupTab.ensureDebugId("LeaderboardGroupsTab");
        tabPanel.add(leaderboardGroupTab, stringMessages.leaderboardGroups());
        final ScrollPanel courseAreasTab = new ScrollPanel(courseAreaNameList);
        courseAreasTab.ensureDebugId("CourseAreasTab");
        tabPanel.add(courseAreasTab, stringMessages.courseAreas());
        final ScrollPanel imagesTab = new ScrollPanel(imagesListComposite);
        imagesTab.ensureDebugId("ImagesTab");
        tabPanel.add(imagesTab, stringMessages.images());
        final ScrollPanel videosTab = new ScrollPanel(videosListComposite);
        videosTab.ensureDebugId("VideosTab");
        tabPanel.add(videosTab, stringMessages.videos());
        final ScrollPanel windFinderTab = new ScrollPanel(windFinderSpotCollectionIdsComposite);
        windFinderTab.ensureDebugId("WindFinderTab");
        tabPanel.add(windFinderTab, stringMessages.windFinder());
        return panel;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameEntryField;
    }

    // used for ImageListComposite and VideoListComposite to inform user that upload is not possible without needing to
    // try it first
    private void testFileStorageService(FileStorageManagementGwtServiceAsync sailingService) {
        // double callback to test if a fileStorageService is enabled and enable upload if it is
        storageServiceAvailable.setValue(false);
        sailingService.getActiveFileStorageServiceName(new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (result != null) {
                    sailingService.testFileStorageServiceProperties(result,
                            LocaleInfo.getCurrentLocale().getLocaleName(),
                            new AsyncCallback<FileStorageServicePropertyErrorsDTO>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    Notification.notify(stringMessages.setUpStorageService(), NotificationType.ERROR);
                                }

                                @Override
                                public void onSuccess(FileStorageServicePropertyErrorsDTO result) {
                                    if (result == null) {
                                        storageServiceAvailable.setValue(true);
                                    } else {
                                        Notification.notify(stringMessages.setUpStorageService(),
                                                NotificationType.ERROR);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.setUpStorageService(), NotificationType.ERROR);
            }
        });
    }
}
