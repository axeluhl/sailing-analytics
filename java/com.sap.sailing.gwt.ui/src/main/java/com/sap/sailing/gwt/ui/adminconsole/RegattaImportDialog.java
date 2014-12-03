package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;


public class RegattaImportDialog extends DataEntryDialog<String>{
    protected StringMessages stringMessages;
    protected TextBox nameEntryField;
    protected TextArea descriptionEntryField;
    protected TextBox venueEntryField;
    protected BetterDateTimeBox startDateBox;
    protected BetterDateTimeBox endDateBox;
    protected CheckBox isPublicCheckBox;
    protected UUID id;
    protected TextBox officialWebsiteURLEntryField;
    protected TextBox logoImageURLEntryField;
    protected StringListInlineEditorComposite courseAreaNameList;
    protected StringListInlineEditorComposite imageURLList;
    protected StringListInlineEditorComposite videoURLList;
    protected StringListInlineEditorComposite sponsorImageURLList;
    
    

    public RegattaImportDialog(StringMessages stringMessages, String message, 
            com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator<String> validator, 
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<String> callback) {
        super("Regattas"/*title*/, null/*message*/, stringMessages.ok(), stringMessages.cancel(), validator, false/*animationEnables; raus?*/, callback);
        this.stringMessages = stringMessages;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        Grid formGrid = new Grid(8, 2);
        panel.add(formGrid);

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
        formGrid.setWidget(7, 0, new Label(stringMessages.eventLogoImageURL() + ":"));
        formGrid.setWidget(7, 1, logoImageURLEntryField);

        panel.add(createHeadlineLabel(stringMessages.courseAreas()));
        panel.add(courseAreaNameList);
        panel.add(createHeadlineLabel(stringMessages.imageURLs()));
        panel.add(imageURLList);
        panel.add(createHeadlineLabel(stringMessages.videoURLs()));
        panel.add(videoURLList);
        panel.add(createHeadlineLabel(stringMessages.sponsorImageURLs()));
        panel.add(sponsorImageURLList);
        return panel;
    }
    
    protected String getResult() {
        EventDTO result = new EventDTO();
        result.setName(nameEntryField.getText());
        result.setDescription(descriptionEntryField.getText());
        result.setOfficialWebsiteURL(officialWebsiteURLEntryField.getText().trim().isEmpty() ? null : officialWebsiteURLEntryField.getText().trim());
        result.setLogoImageURL(logoImageURLEntryField.getText().trim().isEmpty() ? null : logoImageURLEntryField.getText().trim());
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
        for (String imageURL : imageURLList.getValue()) {
            result.addImageURL(imageURL);
        }
        for (String videoURL : videoURLList.getValue()) {
            result.addVideoURL(videoURL);
        }
        for (String sponsorImageURL : sponsorImageURLList.getValue()) {
            result.addSponsorImageURL(sponsorImageURL);
        }
        result.venue = new VenueDTO(venueEntryField.getText(), courseAreas);
        return "OK";
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }
}
