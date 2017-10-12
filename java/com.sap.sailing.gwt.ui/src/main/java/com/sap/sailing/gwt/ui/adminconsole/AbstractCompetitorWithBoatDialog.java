package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * An abstract base class for dialogs which can handle a competitor AND a boat together 
 * @author Frank Mittag
 * 
 */
public abstract class AbstractCompetitorWithBoatDialog extends CompetitorWithoutBoatEditDialog<CompetitorDTO> {
    protected final SuggestBox boatClassNameTextBox;
    protected final TextBox sailIdTextBox;
    protected final TextBox boatNameTextBox;
    protected final TextBox boatDisplayColorTextBox;
    
    protected static class CompetitorWithBoatValidator extends CompetitorWithoutBoatValidator<CompetitorDTO> {
        protected final StringMessages stringMessages;

        public CompetitorWithBoatValidator(StringMessages stringMessages) {
            super(stringMessages);
            
            this.stringMessages = stringMessages;
        }
        
        @Override
        public String getErrorMessage(CompetitorDTO competitorToValidate) {
            String result = super.getErrorMessage(competitorToValidate);
            if (result == null) {
                BoatDTO boatToValidate = competitorToValidate.getBoat();
                if (boatToValidate == null) {
                    result = "You need to specify a boat for the competitor";
                } else {
                    if (boatToValidate.getSailId() == null || boatToValidate.getSailId().isEmpty()) {
                        result = stringMessages.pleaseEnterASailNumber();
                    } else if (boatToValidate.getBoatClass().getName() == null || boatToValidate.getBoatClass().getName().isEmpty()) {
                        result = stringMessages.pleaseEnterABoatClass();
                    }
                }
            }
            return result;
        }
    }
    
    /**
     * The class creates the UI-dialog to type in the Data about a competitor.
     * 
     * @param competitorToEdit
     *            The 'competitorToEdit' parameter contains the competitor which should be changed or initialized.
     * @param boatClass
     *            The boat class is the default shown boat class for new boats. Set <code>null</code> if your boat is
     *            already initialized or you don't want a default boat class.
     */
    public AbstractCompetitorWithBoatDialog(String dialogTitle, StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback, String boatClass) {
        super(dialogTitle, stringMessages, competitorToEdit, new AbstractCompetitorWithBoatDialog.CompetitorWithBoatValidator(stringMessages), callback);
                
        final BoatDTO boatToEdit = competitorToEdit.getBoat();
        this.boatClassNameTextBox = createSuggestBox(new BoatClassMasterdataSuggestOracle());
        boatClassNameTextBox.ensureDebugId("BoatClassNameSuggestBox");
        if (boatToEdit.getBoatClass() != null) {
            boatClassNameTextBox.setValue(boatToEdit.getBoatClass().getName());
            boatClassNameTextBox.setEnabled(false);
        } else {
            boatClassNameTextBox.setValue(boatClass); // widgets have to accept null values here
        }
        this.boatNameTextBox = createTextBox(boatToEdit.getName());
        boatNameTextBox.ensureDebugId("BoatNameTextBox");
        this.boatDisplayColorTextBox = createTextBox(boatToEdit.getColor() == null ? "" : boatToEdit.getColor().getAsHtml()); 
        this.sailIdTextBox = createTextBox(boatToEdit.getSailId());
        sailIdTextBox.ensureDebugId("SailIdTextBox");
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return super.getInitialFocusWidget();
    }

    protected abstract BoatDTO getBoat();

    @Override
    protected CompetitorDTO getResult() {
        CompetitorWithoutBoatDTO c = super.getResult();
        BoatDTO boat = getBoat();
        CompetitorDTO result = new CompetitorDTOImpl(c.getName(), c.getShortName(), c.getColor(), c.getEmail(),
                c.getTwoLetterIsoCountryCode(), c.getThreeLetterIocCountryCode(), c.getCountryName(),
                getCompetitorToEdit().getIdAsString(),
                c.getImageURL(), c.getFlagImageURL(),
                c.getTimeOnTimeFactor(), c.getTimeOnDistanceAllowancePerNauticalMile(), c.getSearchTag(),
                boat);
        return result;
    }

}
