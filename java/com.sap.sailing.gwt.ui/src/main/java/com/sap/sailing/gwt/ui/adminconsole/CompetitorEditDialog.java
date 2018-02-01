package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * The competitors produced by this dialog will have a <code>null</code>
 * {@link CompetitorDTO#getTwoLetterIsoCountryCode() twoLetterIsoCountryCode} and a <code>null</code>
 * {@link CompetitorDTO#getCountryName() countryName} because all of these can be derived from a valid
 * {@link CompetitorDTO#getThreeLetterIocCountryCode() threeLetterIocCountryCode}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorEditDialog extends DataEntryDialog<CompetitorDTO> {
    private final CompetitorDTO competitorToEdit;
    private final TextBox name;
    private final SuggestBox boatClassName;
    private final TextBox displayColorTextBox;
    private final ListBox threeLetterIocCountryCode;
    private final TextBox sailId;
    private final TextBox email;
    private final TextBox searchTag;
    private final StringMessages stringMessages;
    private final URLFieldWithFileUpload flagImageURL;
    private final URLFieldWithFileUpload imageUrlAndUploadComposite;
    private final DoubleBox timeOnTimeFactor;
    private final DoubleBox timeOnDistanceAllowanceInSecondsPerNauticalMile;
    
    /**
     * The class creates the UI-dialog to type in the Data about a competitor.
     * 
     * @param competitorToEdit
     *            The 'competitorToEdit' parameter contains the competitor which should be changed or initialized.
     * @param boatClass
     *            The boat class is the default shown boat class for new competitors. Set <code>null</code> if your competitor is
     *            already initialized or you don�t want a default boat class.
     */
    public CompetitorEditDialog(final StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback, String boatClass) {
        super(stringMessages.editCompetitor(), null, stringMessages.ok(), stringMessages
                .cancel(), new Validator<CompetitorDTO>() {
                    @Override
                    public String getErrorMessage(CompetitorDTO valueToValidate) {
                        String result = null;
                        if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                            result = stringMessages.pleaseEnterAName();
                        } else if (valueToValidate.getSailID() == null || valueToValidate.getSailID().isEmpty()) {
                            result = stringMessages.pleaseEnterASailNumber();
                        } else if (valueToValidate.getColor() != null) {
                            Color displayColor = valueToValidate.getColor();
                            if (displayColor instanceof InvalidColor) {
                                result = displayColor.getAsHtml();
                            }
                        } else if (valueToValidate.getBoatClass().getName() == null || valueToValidate.getBoatClass().getName().isEmpty()) {
                            result = stringMessages.pleaseEnterABoatClass();
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("CompetitorEditDialog");
        this.stringMessages = stringMessages;
        this.competitorToEdit = competitorToEdit;
                
        this.boatClassName = createSuggestBox(new BoatClassMasterdataSuggestOracle());
        boatClassName.ensureDebugId("BoatClassNameSuggestBox");
        int i=0;
        if (competitorToEdit.getBoatClass() != null) {
            boatClassName.setValue(competitorToEdit.getBoatClass().getName());
            boatClassName.setEnabled(false);
        } else {
            boatClassName.setValue(boatClass); // widgets have to accept null values here
        }
        this.name = createTextBox(competitorToEdit.getName());
        name.ensureDebugId("NameTextBox");
        this.email = createTextBox(competitorToEdit.getEmail());
        this.searchTag = createTextBox(competitorToEdit.getSearchTag());
        this.displayColorTextBox = createTextBox(competitorToEdit.getColor() == null ? "" : competitorToEdit.getColor().getAsHtml()); 
        this.threeLetterIocCountryCode = createListBox(/* isMultipleSelect */ false);
        CountryCodeFactory ccf = CountryCodeFactory.INSTANCE;
        i=0;
        List<CountryCode> ccs = new ArrayList<CountryCode>();
        Util.addAll(ccf.getAll(), ccs);
        ccs.add(null); // representing no nationality (NONE / white flag)
        Collections.sort(ccs, new Comparator<CountryCode>() {
            @Override
            public int compare(CountryCode o1, CountryCode o2) {
                return Util.compareToWithNull(o1 == null ? null : o1.getThreeLetterIOCCode(), o2 == null ? null : o2.getThreeLetterIOCCode(), /* nullIsLess */ true);
            }
        });
        for (CountryCode cc : ccs) {
            if (cc == null) {
                this.threeLetterIocCountryCode.addItem("", ""); // the NONE country code that uses the empty, white flag
                if (competitorToEdit.getThreeLetterIocCountryCode() == null || competitorToEdit.getThreeLetterIocCountryCode().isEmpty()) {
                    this.threeLetterIocCountryCode.setSelectedIndex(i);
                }
                i++;
            } else if (cc.getThreeLetterIOCCode() != null) {
                this.threeLetterIocCountryCode.addItem(cc.getThreeLetterIOCCode() + " " + cc.getName(), cc.getThreeLetterIOCCode());
                if (cc.getThreeLetterIOCCode().equals(competitorToEdit.getThreeLetterIocCountryCode())) {
                    this.threeLetterIocCountryCode.setSelectedIndex(i);
                }
                i++;
            }
        }
        this.sailId = createTextBox(competitorToEdit.getSailID());
        sailId.ensureDebugId("SailIdTextBox");
        this.flagImageURL = new URLFieldWithFileUpload(stringMessages);
        this.flagImageURL.setURL(competitorToEdit.getFlagImageURL());
        this.imageUrlAndUploadComposite = new URLFieldWithFileUpload(stringMessages);
        this.imageUrlAndUploadComposite.setURL(competitorToEdit.getImageURL());
        this.timeOnTimeFactor = createDoubleBox(competitorToEdit.getTimeOnTimeFactor(), 10);
        this.timeOnDistanceAllowanceInSecondsPerNauticalMile = createDoubleBox(
                competitorToEdit.getTimeOnDistanceAllowancePerNauticalMile() == null ? null : competitorToEdit
                        .getTimeOnDistanceAllowancePerNauticalMile().asSeconds(), 10);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return name;
    }

    /**
     * Encodes an invalid color; can be used 
     * @author Axel Uhl (D043530)
     *
     */
    private class InvalidColor implements Color {
        private static final long serialVersionUID = 4012986110898149543L;
        private final Exception exception;
        
        protected InvalidColor(Exception exception) {
            this.exception = exception;
        }

        @Override
        public com.sap.sse.common.Util.Triple<Integer, Integer, Integer> getAsRGB() {
            return null;
        }

        @Override
        public com.sap.sse.common.Util.Triple<Float, Float, Float> getAsHSV() {
            return null;
        }

        @Override
        public String getAsHtml() {
            return stringMessages.invalidColor(exception.getMessage());
        }

        @Override
        public Color invert() {
            return null;
        }
    }

    @Override
    protected CompetitorDTO getResult() {
        Color color;
        if (displayColorTextBox.getText() == null || displayColorTextBox.getText().isEmpty()) {
            color = null;
        } else {
            try {
                color = new RGBColor(displayColorTextBox.getText());
            } catch (IllegalArgumentException iae) {
                color = new InvalidColor(iae);
            }
        }
        BoatClassDTO boatClass = new BoatClassDTO(boatClassName.getValue(), Distance.NULL, Distance.NULL);
        BoatDTO boat = new BoatDTO(name.getText(), sailId.getText());
        CompetitorDTO result = new CompetitorDTOImpl(name.getText(), color, email.getText(),
                /* twoLetterIsoCountryCode */ null,
                threeLetterIocCountryCode.getValue(threeLetterIocCountryCode.getSelectedIndex()),
                /* countryName */ null, competitorToEdit.getIdAsString(),
                imageUrlAndUploadComposite.getURL(), flagImageURL.getURL(), boat, boatClass,
                timeOnTimeFactor.getValue(),
                timeOnDistanceAllowanceInSecondsPerNauticalMile.getValue() == null ? null :
                        new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile.getValue()*1000)), searchTag.getValue());
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(11, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, name);
        result.setWidget(1, 0, new Label(stringMessages.sailNumber()));
        result.setWidget(1, 1, sailId);
        result.setWidget(2, 0, new Label(stringMessages.nationality()));
        result.setWidget(2, 1, threeLetterIocCountryCode);
        result.setWidget(3, 0, new Label(stringMessages.color()));
        result.setWidget(3, 1, displayColorTextBox);
        result.setWidget(4, 0, new Label(stringMessages.email()));
        result.setWidget(4, 1, email);
        result.setWidget(5, 0, new Label(stringMessages.searchTag()));
        result.setWidget(5, 1, searchTag);
        result.setWidget(6, 0, new Label(stringMessages.boatClass()));
        result.setWidget(6, 1, boatClassName);
        result.setWidget(7, 0, new Label(stringMessages.flagImageURL()));
        result.setWidget(7, 1, flagImageURL);
        result.setWidget(8, 0, new Label(stringMessages.imageURL()));
        result.setWidget(8, 1, imageUrlAndUploadComposite);
        result.setWidget(9, 0, new Label(stringMessages.timeOnTimeFactor()));
        result.setWidget(9, 1, timeOnTimeFactor);
        result.setWidget(10, 0, new Label(stringMessages.timeOnDistanceAllowanceInSecondsPerNauticalMile()));
        result.setWidget(10, 1, timeOnDistanceAllowanceInSecondsPerNauticalMile);
        return result;
    }

}
