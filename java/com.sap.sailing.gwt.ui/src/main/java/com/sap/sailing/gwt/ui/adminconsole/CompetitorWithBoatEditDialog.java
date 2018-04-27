package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.Distance;
import com.sap.sse.common.impl.RGBColor;

/**
 * An edit for a competitor with a boat
 */
public class CompetitorWithBoatEditDialog extends AbstractCompetitorWithBoatDialog {
    
    /**
     * The class creates the UI-dialog to type in the Data about a competitor.
     * 
     * @param competitorToEdit
     *            The 'competitorToEdit' parameter contains the competitor which should be changed or initialized.
     * @param boatClass
     *            The boat class is the default shown boat class for new boats. Set <code>null</code> if your boat is
     *            already initialized or you don't want a default boat class.
     */
    public CompetitorWithBoatEditDialog(StringMessages stringMessages, CompetitorWithBoatDTO competitorToEdit,
            DialogCallback<CompetitorWithBoatDTO> callback, String boatClass) {
        super("Edit competitor with boat", stringMessages, competitorToEdit, callback, boatClass);
        this.ensureDebugId("CompetitorWithBoatEditDialog");
    }

    @Override
    protected BoatDTO getBoat() {
        BoatDTO result = null;
        Color boatColor;
        if (boatDisplayColorTextBox.getValue() == null || boatDisplayColorTextBox.getValue().isEmpty()) {
            boatColor = null;
        } else {
            try {
                boatColor = new RGBColor(boatDisplayColorTextBox.getValue());
            } catch (IllegalArgumentException iae) {
                boatColor = new InvalidColor(iae, getStringMessages());
            }
        }

        BoatClassDTO boatClass = new BoatClassDTO(boatClassNameTextBox.getValue(), Distance.NULL, Distance.NULL);
        result = new BoatDTO(getCompetitorToEdit().getBoat().getIdAsString(), boatNameTextBox.getValue(), boatClass, sailIdTextBox.getValue(), boatColor);
        return result;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel result = new VerticalPanel();
        result.add(super.getAdditionalWidget());
        result.add(createHeadlineLabel(getStringMessages().boat()));
        Grid grid = new Grid(4, 2);
        grid.setWidget(0, 0, new Label(getStringMessages().name()));
        grid.setWidget(0, 1, boatNameTextBox);
        grid.setWidget(1, 0, new Label(getStringMessages().sailNumber()));
        grid.setWidget(1, 1, sailIdTextBox);
        grid.setWidget(2, 0, new Label(getStringMessages().boatClass()));
        grid.setWidget(2, 1, boatClassNameTextBox);
        grid.setWidget(3, 0, new Label(getStringMessages().color()));
        grid.setWidget(3, 1, boatDisplayColorTextBox);
        result.add(grid);
        return result;
    }

}
