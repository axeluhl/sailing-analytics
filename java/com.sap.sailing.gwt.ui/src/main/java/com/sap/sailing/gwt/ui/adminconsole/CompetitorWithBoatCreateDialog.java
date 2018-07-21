package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Distance;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;

/**
 * A creation dialog for competitors with boats
 * The dialog let you decide if you want to create a new boat or use an existing boat
 * 
 */
public class CompetitorWithBoatCreateDialog extends AbstractCompetitorWithBoatDialog {

    private final RadioButton useExistingBoatRadioButton;
    private final RadioButton useNewBoatRadioButton;
    private final VerticalPanel newBoatPanel;
    private final VerticalPanel existingBoatPanel;
    private final BoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> existingBoatsTable;    
    
    /**
     * The class creates the UI-dialog to type in the Data about a competitor.
     * 
     * @param competitorToEdit
     *            The 'competitorToEdit' parameter contains the competitor which should be changed or initialized.
     * @param boatClass
     *            The boat class is the default shown boat class for new boats. Set <code>null</code> if your boat is
     *            already initialized or you don't want a default boat class.
     */
    public CompetitorWithBoatCreateDialog(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter, CompetitorWithBoatDTO competitorToEdit,
            DialogCallback<CompetitorWithBoatDTO> callback, String boatClass) {
        super("Add competitor with boat", stringMessages, competitorToEdit, callback, boatClass);
        this.ensureDebugId("CompetitorWithBoatCreateDialog");
                        
        this.useNewBoatRadioButton = this.createRadioButton("BoatCreationSelection", "Create new boat");
        this.useExistingBoatRadioButton = this.createRadioButton("BoatCreationSelection", "Use existing boat");
        this.useNewBoatRadioButton.setValue(true);
        useNewBoatRadioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateBoatPanels();
            }
        });
        useExistingBoatRadioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateBoatPanels();
            }
        });        
        newBoatPanel = new VerticalPanel();
        existingBoatPanel = new VerticalPanel();
        
        this.existingBoatsTable = new BoatTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ false, 
                /* enablePager */ true, /* pagingSize*/ 10, false);
        existingBoatsTable.refreshBoatList(true, /* callback */ null);
        this.existingBoatsTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                CompetitorWithBoatCreateDialog.this.validateAndUpdate();
            }
        });
    }

    private void updateBoatPanels() {
        boolean isUseExistingBoatSelected = useExistingBoatRadioButton.getValue();
        newBoatPanel.setVisible(!isUseExistingBoatSelected);
        existingBoatPanel.setVisible(isUseExistingBoatSelected);
    }

    @Override
    protected BoatDTO getBoat() {
        BoatDTO result = null;
        boolean isUseExistingBoatSelected = useExistingBoatRadioButton.getValue();
        if (isUseExistingBoatSelected) {
            Set<BoatDTO> selectedSet = existingBoatsTable.getSelectionModel().getSelectedSet();
            if (!selectedSet.isEmpty()) {
                result = selectedSet.iterator().next(); 
            }
        } else {
            BoatClassDTO boatClass = new BoatClassDTO(boatClassNameTextBox.getValue(), Distance.NULL, Distance.NULL);
            result = new BoatDTO(getCompetitorToEdit().getBoat().getIdAsString(), boatNameTextBox.getName(), boatClass, sailIdTextBox.getText());
        }
        return result;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel result = new VerticalPanel();
        result.add(super.getAdditionalWidget());
        result.add(createHeadlineLabel(getStringMessages().boat()));
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(useNewBoatRadioButton);
        hPanel.add(useExistingBoatRadioButton);
        result.add(hPanel);
        result.add(newBoatPanel);
        result.add(existingBoatPanel);
        Grid grid = new Grid(4, 2);
        grid.setWidget(0, 0, new Label(getStringMessages().name()));
        grid.setWidget(0, 1, boatNameTextBox);
        grid.setWidget(1, 0, new Label(getStringMessages().sailNumber()));
        grid.setWidget(1, 1, sailIdTextBox);
        grid.setWidget(2, 0, new Label(getStringMessages().color()));
        grid.setWidget(2, 1, boatDisplayColorTextBox);
        grid.setWidget(3, 0, new Label(getStringMessages().boatClass()));
        grid.setWidget(3, 1, boatClassNameTextBox);
        result.add(grid);
        newBoatPanel.add(grid);
        existingBoatPanel.add(existingBoatsTable);
        updateBoatPanels();
        return result;
    }

}
