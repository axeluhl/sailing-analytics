package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * The competitors produced by this dialog will have a <code>null</code>
 * {@link CompetitorDTO#getTwoLetterIsoCountryCode() twoLetterIsoCountryCode} and a <code>null</code>
 * {@link CompetitorDTO#getCountryName() countryName} because all of these can be derived from a valid
 * {@link CompetitorDTO#getThreeLetterIocCountryCode() threeLetterIocCountryCode}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorEditDialog extends CompetitorWithoutBoatEditDialog<CompetitorDTO> {
    private final SuggestBox boatClassName;
    private final TextBox sailId;

    protected static class CompetitorValidator extends CompetitorWithoutBoatValidator<CompetitorDTO> {
        protected final StringMessages stringMessages;

        public CompetitorValidator(StringMessages stringMessages) {
            super(stringMessages);
            
            this.stringMessages = stringMessages;
        }
        
        @Override
        public String getErrorMessage(CompetitorDTO valueToValidate) {
            String result = super.getErrorMessage(valueToValidate);
            if (result == null) {
                if (valueToValidate.getSailID() == null || valueToValidate.getSailID().isEmpty()) {
                    result = stringMessages.pleaseEnterASailNumber();
                } else if (valueToValidate.getBoatClass().getName() == null || valueToValidate.getBoatClass().getName().isEmpty()) {
                    result = stringMessages.pleaseEnterABoatClass();
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
     *            The boat class is the default shown boat class for new competitors. Set <code>null</code> if your competitor is
     *            already initialized or you don't want a default boat class.
     */
    public CompetitorEditDialog(final StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback, String boatClass) {
        super(stringMessages, competitorToEdit, new CompetitorEditDialog.CompetitorValidator(stringMessages), callback);
        this.ensureDebugId("CompetitorEditDialog");
                
        this.boatClassName = createSuggestBox(new BoatClassMasterdataSuggestOracle());
        boatClassName.ensureDebugId("BoatClassNameSuggestBox");
        if (competitorToEdit.getBoatClass() != null) {
            boatClassName.setValue(competitorToEdit.getBoatClass().getName());
            boatClassName.setEnabled(false);
        } else {
            boatClassName.setValue(boatClass); // widgets have to accept null values here
        }
        this.sailId = createTextBox(competitorToEdit.getSailID());
        sailId.ensureDebugId("SailIdTextBox");
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return super.getInitialFocusWidget();
    }

    @Override
    protected CompetitorDTO getResult() {
        CompetitorWithoutBoatDTO c = super.getResult();
        BoatClassDTO boatClass = new BoatClassDTO(boatClassName.getValue(), Distance.NULL, Distance.NULL);
        // Use temporary competitorID as boat Id -> boat will be removed from competitor later on anyway
        BoatDTO boat = new BoatDTO(getCompetitorToEdit().getIdAsString(), c.getName(), boatClass, sailId.getText());
        CompetitorDTO result = new CompetitorDTOImpl(c.getName(), c.getShortName(), c.getColor(), c.getEmail(),
                c.getTwoLetterIsoCountryCode(), c.getThreeLetterIocCountryCode(), c.getCountryName(),
                getCompetitorToEdit().getIdAsString(),
                c.getImageURL(), c.getFlagImageURL(),
                c.getTimeOnTimeFactor(), c.getTimeOnDistanceAllowancePerNauticalMile(), c.getSearchTag(),
                boat);
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel result = new VerticalPanel();
        result.add(super.getAdditionalWidget());
        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, new Label(getStringMessages().sailNumber()));
        grid.setWidget(0, 1, sailId);
        grid.setWidget(1, 0, new Label(getStringMessages().boatClass()));
        grid.setWidget(1, 1, boatClassName);
        result.add(grid);
        return result;
    }

}
