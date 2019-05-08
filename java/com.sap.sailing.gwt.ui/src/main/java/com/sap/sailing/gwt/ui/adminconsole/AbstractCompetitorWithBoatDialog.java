package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTOImpl;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * An abstract base class for dialogs which can handle a competitor AND a boat together 
 * @author Frank Mittag
 * 
 */
public abstract class AbstractCompetitorWithBoatDialog extends CompetitorEditDialog<CompetitorWithBoatDTO>{
    protected final SuggestBox boatClassNameTextBox;
    protected final TextBox sailIdTextBox;
    protected final TextBox boatNameTextBox;
    protected final TextBox boatDisplayColorTextBox;
    
    protected static class CompetitorWithBoatValidator extends CompetitorWithoutBoatValidator<CompetitorWithBoatDTO> {
        protected final StringMessages stringMessages;
        private final BoatDTO originalBoat;

        public CompetitorWithBoatValidator(StringMessages stringMessages, BoatDTO originalBoat) {
            super(stringMessages);
            
            this.stringMessages = stringMessages;
            this.originalBoat = originalBoat;
        }
        
        @Override
        public String getErrorMessage(CompetitorWithBoatDTO competitorToValidate) {
            String result = super.getErrorMessage(competitorToValidate);
            if (result == null) {
                BoatDTO boatToValidate = competitorToValidate.getBoat();
                boolean invalidSailId = boatToValidate.getSailId() == null || boatToValidate.getSailId().isEmpty();
                boolean invalidName = boatToValidate.getName() == null || boatToValidate.getName().isEmpty();
                if (invalidSailId && invalidName) {
                    result = stringMessages.pleaseEnterASailNumberOrABoatName();
                } else if (boatToValidate.getColor() != null && boatToValidate.getColor() instanceof InvalidColor) {
                    result = boatToValidate.getColor().getAsHtml();
                } else if (!boatClassWasChanged(boatToValidate, originalBoat) && isBoatClassValid(boatToValidate)) {
                    // only validate if boat class has changed and the originalBoat actually exists and is not to be
                    // created here
                    result = stringMessages.pleaseEnterABoatClass();
                }
            }
            return result;
        }

        private boolean isBoatClassValid(BoatDTO boatToValidate) {
            return boatToValidate.getBoatClass().getName() == null || boatToValidate.getBoatClass().getName().isEmpty();
        }

        private boolean boatClassWasChanged(BoatDTO boatToValidate, BoatDTO originalBoat) {
            return !(boatToValidate.getBoatClass() != null && originalBoat.getBoatClass() != null
                    && !originalBoat.getBoatClass().getName().equals(boatToValidate.getBoatClass().getName()));
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
    public AbstractCompetitorWithBoatDialog(String dialogTitle, StringMessages stringMessages, CompetitorWithBoatDTO competitorToEdit,
            DialogCallback<CompetitorWithBoatDTO> callback, String boatClass) {
        super(dialogTitle, stringMessages, competitorToEdit,
                new AbstractCompetitorWithBoatDialog.CompetitorWithBoatValidator(stringMessages,
                        competitorToEdit.getBoat()),
                callback);
                
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
    protected CompetitorWithBoatDTO getResult() {
        CompetitorDTO c = getBaseResult();
        BoatDTO boat = getBoat();
        CompetitorWithBoatDTO result = new CompetitorWithBoatDTOImpl(c.getName(), c.getShortName(), c.getColor(), c.getEmail(),
                c.getTwoLetterIsoCountryCode(), c.getThreeLetterIocCountryCode(), c.getCountryName(),
                getCompetitorToEdit().getIdAsString(),
                c.getImageURL(), c.getFlagImageURL(),
                c.getTimeOnTimeFactor(), c.getTimeOnDistanceAllowancePerNauticalMile(), c.getSearchTag(),
                boat);
        return result;
    }

}
