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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTOImpl;
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
 * {@link CompetitorWithBoatDTO#getTwoLetterIsoCountryCode() twoLetterIsoCountryCode} and a <code>null</code>
 * {@link CompetitorWithBoatDTO#getCountryName() countryName} because all of these can be derived from a valid
 * {@link CompetitorWithBoatDTO#getThreeLetterIocCountryCode() threeLetterIocCountryCode}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class CompetitorEditDialog<CompetitorType extends CompetitorDTO> extends DataEntryDialog<CompetitorType> {
    private final CompetitorType competitorToEdit;
    private final TextBox name;
    private final TextBox shortName;
    private final TextBox displayColorTextBox;
    private final ListBox threeLetterIocCountryCode;
    private final TextBox email;
    private final TextBox searchTag;
    private final StringMessages stringMessages;

    private final URLFieldWithFileUpload flagImageURL;
    private final URLFieldWithFileUpload imageUrlAndUploadComposite;
    private final DoubleBox timeOnTimeFactor;
    private final DoubleBox timeOnDistanceAllowanceInSecondsPerNauticalMile;
    
    protected static class CompetitorWithoutBoatValidator<CompetitorType extends CompetitorDTO> implements Validator<CompetitorType> {
        protected final StringMessages stringMessages;

        public CompetitorWithoutBoatValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }
        
        @Override
        public String getErrorMessage(CompetitorType valueToValidate) {
            String result = null;
            if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                result = stringMessages.pleaseEnterAName();
            } else if (valueToValidate.getColor() != null) {
                Color displayColor = valueToValidate.getColor();
                if (displayColor instanceof CompetitorEditDialog.InvalidColor) {
                    result = displayColor.getAsHtml();
                }
            }
            return result;
        }
    }

    /**
     * Creates an edit dialog for competitors that may be competitors without a boat assigned (not instance of
     * type {@link CompetitorWithBoatDTO}.
     */
    public static CompetitorEditDialog<CompetitorDTO> create(StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback) {
        return new CompetitorEditDialog<CompetitorDTO>(stringMessages.editCompetitor(), stringMessages, competitorToEdit,
                new CompetitorEditDialog.CompetitorWithoutBoatValidator<CompetitorDTO>(stringMessages), callback) {
                    @Override
                    protected CompetitorDTO getResult() {
                        return getBaseResult();
                    }
        };
    }
    
    /**
     * The class creates the UI-dialog to type in the data of a competitor.
     * 
     * @param competitorToEdit
     *            The 'competitorToEdit' parameter contains the competitor which should be changed or initialized.
     */
    protected CompetitorEditDialog(String dialogTitle, StringMessages stringMessages, CompetitorType competitorToEdit,
            Validator<CompetitorType> validator,  DialogCallback<CompetitorType> callback) {
        super(dialogTitle, null, stringMessages.ok(), stringMessages.cancel(),
                validator, /* animationEnabled */true, callback);
        this.ensureDebugId("CompetitorEditDialog");
        this.stringMessages = stringMessages;
        this.competitorToEdit = competitorToEdit;
                        
        this.name = createTextBox(competitorToEdit.getName());
        name.ensureDebugId("NameTextBox");
        this.shortName = createTextBox(competitorToEdit.getShortName());
        shortName.ensureDebugId("ShortNameTextBox");
        this.email = createTextBox(competitorToEdit.getEmail());
        this.searchTag = createTextBox(competitorToEdit.getSearchTag());
        this.displayColorTextBox = createTextBox(competitorToEdit.getColor() == null ? "" : competitorToEdit.getColor().getAsHtml()); 
        this.threeLetterIocCountryCode = createListBox(/* isMultipleSelect */ false);
        CountryCodeFactory ccf = CountryCodeFactory.INSTANCE;
        int i=0;
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
    protected static class InvalidColor implements Color {
        private static final long serialVersionUID = 4012986110898149543L;
        private final String exceptionMessage;
        
        protected InvalidColor(Exception exception, StringMessages stringMessages) {
            this.exceptionMessage = stringMessages.invalidColor(exception.getMessage());
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
            return exceptionMessage;
        }
        
        @Override
        public Color invert() {
            return null;
        }
    }

    /**
     * Concrete sub-types need to tell which type of {@link CompetitorDTO} object they create; the {@link #getBaseResult} method
     * can be used by sub-types to obtain a {@link CompetitorWithDTO} object that has all values managed by this implementation.
     */
    protected abstract CompetitorType getResult();
    
    protected CompetitorDTO getBaseResult() { 
        Color color;
        if (displayColorTextBox.getText() == null || displayColorTextBox.getText().isEmpty()) {
            color = null;
        } else {
            try {
                color = new RGBColor(displayColorTextBox.getText());
            } catch (IllegalArgumentException iae) {
                color = new InvalidColor(iae, stringMessages);
            }
        }
        CompetitorWithBoatDTO result = new CompetitorWithBoatDTOImpl(name.getText(),
                shortName.getText().trim().isEmpty() ? null : shortName.getText(), color,
                email.getText().trim().isEmpty() ? null : email.getText(),
                /* twoLetterIsoCountryCode */ null,
                threeLetterIocCountryCode.getValue(threeLetterIocCountryCode.getSelectedIndex()),
                /* countryName */ null, competitorToEdit.getIdAsString(),
                imageUrlAndUploadComposite.getURL(), flagImageURL.getURL(),
                timeOnTimeFactor.getValue(),
                timeOnDistanceAllowanceInSecondsPerNauticalMile.getValue() == null ? null :
                        new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile.getValue()*1000)), searchTag.getValue(), null);
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(10, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, name);
        result.setWidget(1, 0, new Label(stringMessages.shortName()));
        result.setWidget(1, 1, shortName);
        result.setWidget(2, 0, new Label(stringMessages.nationality()));
        result.setWidget(2, 1, threeLetterIocCountryCode);
        result.setWidget(3, 0, new Label(stringMessages.color()));
        result.setWidget(3, 1, displayColorTextBox);
        result.setWidget(4, 0, new Label(stringMessages.email()));
        result.setWidget(4, 1, email);
        result.setWidget(5, 0, new Label(stringMessages.searchTag()));
        result.setWidget(5, 1, searchTag);
        result.setWidget(6, 0, new Label(stringMessages.flagImageURL()));
        result.setWidget(6, 1, flagImageURL);
        result.setWidget(7, 0, new Label(stringMessages.imageURL()));
        result.setWidget(7, 1, imageUrlAndUploadComposite);
        result.setWidget(8, 0, new Label(stringMessages.timeOnTimeFactor()));
        result.setWidget(8, 1, timeOnTimeFactor);
        result.setWidget(9, 0, new Label(stringMessages.timeOnDistanceAllowanceInSecondsPerNauticalMile()));
        result.setWidget(9, 1, timeOnDistanceAllowanceInSecondsPerNauticalMile);
        return result;
    }

    protected CompetitorType getCompetitorToEdit() {
        return competitorToEdit;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

}
