package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.Util;
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
    private final ListBox boatClassName;
    private final TextBox displayColorTextBox;
    private final ListBox threeLetterIocCountryCode;
    private final TextBox sailId;
    private final TextBox email;
    private final StringMessages stringMessages;
    private final TextBox flagImageURL;
    private final URLFieldWithFileUpload imageUrlAndUploadComposite;
    
    public CompetitorEditDialog(final StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback) {
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
        this.stringMessages = stringMessages;
        this.competitorToEdit = competitorToEdit;
        
        this.boatClassName = createListBox(/* isMultipleSelect */ false);
        int i=0;
        List<String> boatClassNamesList = new ArrayList<String>();
        for (BoatClassMasterdata t : BoatClassMasterdata.values()) {
            boatClassNamesList.add(t.getDisplayName());
        }
        String competitorsBoatClassName = competitorToEdit.getBoatClass() != null ? competitorToEdit.getBoatClass().getName() : null;
        Collections.sort(boatClassNamesList);
        for (String name : boatClassNamesList) {
            boatClassName.addItem(name);
            if (name.equals(competitorsBoatClassName)) {
                boatClassName.setSelectedIndex(i);
            }
            i++;
        }
        if (boatClassName.getSelectedIndex() == -1 && competitorsBoatClassName != null) {
            boatClassName.addItem(competitorsBoatClassName);
            boatClassName.setSelectedIndex(i);
        }
        if (competitorToEdit.getBoatClass() != null) {
            boatClassName.setEnabled(false);
        }
        
        this.name = createTextBox(competitorToEdit.getName());
        this.email = createTextBox(competitorToEdit.getEmail());
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
        this.flagImageURL = createTextBox(competitorToEdit.getFlagImageURL(), 100);
        imageUrlAndUploadComposite = new URLFieldWithFileUpload(stringMessages);
        imageUrlAndUploadComposite.setURL(competitorToEdit.getImageURL());
    }

    
    @Override
    public void show() {
        super.show();
        name.setFocus(true);
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
        BoatClassDTO boatClass = new BoatClassDTO(boatClassName.getValue(boatClassName.getSelectedIndex()), 0);
        CompetitorDTO result = new CompetitorDTOImpl(name.getText(), color, email.getText(),
                /* twoLetterIsoCountryCode */ null,
                threeLetterIocCountryCode.getValue(threeLetterIocCountryCode.getSelectedIndex()),
                /* countryName */ null, sailId.getText(), competitorToEdit.getIdAsString(),
                imageUrlAndUploadComposite.getURL(), flagImageURL.getText(), boatClass);
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(8, 2);
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
        result.setWidget(5, 0, new Label(stringMessages.boatClass()));
        result.setWidget(5, 1, boatClassName);
        result.setWidget(6, 0, new Label(stringMessages.flagImageURL()));
        result.setWidget(6, 1, flagImageURL);
        result.setWidget(7, 0, new Label(stringMessages.imageURL()));
        result.setWidget(7, 1, imageUrlAndUploadComposite);
        return result;
    }

}
