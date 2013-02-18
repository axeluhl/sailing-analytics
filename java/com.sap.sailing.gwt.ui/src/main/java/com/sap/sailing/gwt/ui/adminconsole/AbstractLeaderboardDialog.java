package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public abstract class AbstractLeaderboardDialog extends DataEntryDialog<LeaderboardDescriptor> {
    protected final StringMessages stringMessages;
    protected TextBox nameTextBox;
    protected TextBox displayNameTextBox;
    protected LeaderboardDescriptor leaderboard;
    protected ListBox sailingEventsListBox;
    protected Collection<EventDTO> existingEvents;
    protected ListBox courseAreaListBox;
    
    protected LongBox[] discardThresholdBoxes;
    protected static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;

    public AbstractLeaderboardDialog(String title, LeaderboardDescriptor leaderboardDTO, StringMessages stringConstants,
    		Collection<EventDTO> existingEvents, Validator<LeaderboardDescriptor> validator,  DialogCallback<LeaderboardDescriptor> callback) {
        super(title, null, stringConstants.ok(), stringConstants.cancel(), validator, callback);
        this.stringMessages = stringConstants;
        this.leaderboard = leaderboardDTO;
        this.existingEvents = existingEvents;
        courseAreaListBox = createListBox(false);
        courseAreaListBox.setEnabled(false);
    }
    
    @Override
    protected LeaderboardDescriptor getResult() {
        int[] discardThresholdsBoxContents = getDiscardThresholds(discardThresholdBoxes);
        leaderboard.setName(nameTextBox.getValue());
        leaderboard.setDisplayName(displayNameTextBox.getValue());
        leaderboard.setDiscardThresholds(discardThresholdsBoxContents);
        CourseAreaDTO courseArea = getSelectedCourseArea();
        if (courseArea == null) {
        	leaderboard.setCourseAreaId(null);
        } else {
        	leaderboard.setCourseAreaId(getSelectedCourseArea().id);
        }
        return leaderboard;
    }

    protected static int[] getDiscardThresholds(LongBox[] discardThresholdBoxes) {
        List<Integer> discardThresholds = new ArrayList<Integer>();
        // go backwards; starting from first non-zero element, add them; take over leading zeroes which validator shall discard
        for (int i = discardThresholdBoxes.length-1; i>=0; i--) {
            if ((discardThresholdBoxes[i].getValue() != null
                    && discardThresholdBoxes[i].getValue().toString().length() > 0) || !discardThresholds.isEmpty()) {
                if (discardThresholdBoxes[i].getValue() == null) {
                    discardThresholds.add(0, 0);
                } else {
                    discardThresholds.add(0, discardThresholdBoxes[i].getValue().intValue());
                }
            }
        }
        int[] discardThresholdsBoxContents = new int[discardThresholds.size()];
        for (int i = 0; i < discardThresholds.size(); i++) {
            discardThresholdsBoxContents[i] = discardThresholds.get(i);
        }
        return discardThresholdsBoxContents;
    }

    @Override
    public void show() {
        super.show();
        nameTextBox.setFocus(true);
    }

    protected static LongBox[] initEmptyDiscardThresholdBoxes(DataEntryDialog<?> dialog) {
        LongBox[] result = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < result.length; i++) {
            result[i] = dialog.createLongBoxWithOptionalValue(null, 2);
            result[i].setVisibleLength(2);
        }
        return result;
    }

    protected static LongBox[] initPrefilledDiscardThresholdBoxes(int[] valuesToShow, DataEntryDialog<?> dialog) {
        LongBox[] result = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < result.length; i++) {
            if (i < valuesToShow.length) {
                result[i] = dialog.createLongBox(valuesToShow[i], 2);
            } else {
                result[i] = dialog.createLongBoxWithOptionalValue(null, 2);
            }
            result[i].setVisibleLength(2);
        }
        return result;
    }    

    protected ListBox createSailingEventListBox(DataEntryDialog<?> dialog, StringMessages stringMessages) {
        ListBox eventListBox = dialog.createListBox(false);
        eventListBox.addItem("Please select a sailing event...");
        for (EventDTO event: existingEvents) {
        	eventListBox.addItem(event.name);
        }
        eventListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onEventSelectionChanged();
            }
        });
        return eventListBox;
    }
    
    protected void onEventSelectionChanged() {
		EventDTO selectedEvent = getSelectedEvent();
		courseAreaListBox.clear();
		courseAreaListBox.addItem("Please select a course area...");
		for (CourseAreaDTO courseArea : selectedEvent.venue.getCourseAreas()) {
			courseAreaListBox.addItem(courseArea.name);
		}
		courseAreaListBox.setEnabled(true);
	}

	protected static ListBox createScoringSchemeListBox(DataEntryDialog<?> dialog, StringMessages stringMessages) {
        ListBox scoringSchemeListBox2 = dialog.createListBox(false);
        for (ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
            scoringSchemeListBox2.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringMessages));
        }
        return scoringSchemeListBox2;
    }

    protected static ScoringSchemeType getSelectedScoringSchemeType(ListBox scoringSchemeListBox, StringMessages stringMessages) {
        ScoringSchemeType result = null;
        int selIndex = scoringSchemeListBox.getSelectedIndex();
        if (selIndex >= 0) { 
            String itemText = scoringSchemeListBox.getItemText(selIndex);
            for (ScoringSchemeType scoringSchemeType : ScoringSchemeType.values()) {
                if (ScoringSchemeTypeFormatter.format(scoringSchemeType, stringMessages).equals(itemText)) {
                    result = scoringSchemeType;
                    break;
                }
            }
        }
        return result;
    }
    
    public EventDTO getSelectedEvent() {
    	EventDTO result = null;
        int selIndex = sailingEventsListBox.getSelectedIndex();
        if(selIndex > 0) { // the zero index represents the 'no selection' text
            String itemText = sailingEventsListBox.getItemText(selIndex);
            for(EventDTO eventDTO: existingEvents) {
                if(eventDTO.name.equals(itemText)) {
                    result = eventDTO;
                    break;
                }
            }
        }
        return result;
    }
    
    public CourseAreaDTO getSelectedCourseArea() {
    	CourseAreaDTO result = null;
    	EventDTO event = getSelectedEvent();
        int selIndex = courseAreaListBox.getSelectedIndex();
        if(selIndex > 0) { // the zero index represents the 'no selection' text
            String itemText = courseAreaListBox.getItemText(selIndex);
            for(CourseAreaDTO courseAreaDTO: event.venue.getCourseAreas()) {
                if(courseAreaDTO.name.equals(itemText)) {
                    result = courseAreaDTO;
                    break;
                }
            }
        }
        return result;
    }
}
