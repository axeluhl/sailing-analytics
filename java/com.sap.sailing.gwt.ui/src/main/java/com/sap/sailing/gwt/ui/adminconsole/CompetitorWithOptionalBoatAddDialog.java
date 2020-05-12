package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * An Add dialog for a competitor with an optional boat. If {@link #withBoatCheckBox} is checked the
 * {@link #boatDataPanel} is visible and validated
 * 
 * @author Dmitry Bilyk
 */

public class CompetitorWithOptionalBoatAddDialog extends AbstractCompetitorWithBoatDialog {

    private static CheckBox withBoatCheckBox;
    private final VerticalPanel boatDataPanel;

    /**
     * The class creates the UI-dialog to type in the Data about a competitor.
     * 
     * @param competitorToAdd
     *            The 'competitorToAdd' parameter contains the competitor which should be initialized.
     * @param boatClass
     *            The boat class is the default shown boat class for new boats. Set <code>null</code> if your boat is
     *            already initialized or you don't want a default boat class.
     */
    public CompetitorWithOptionalBoatAddDialog(StringMessages stringMessages, CompetitorWithBoatDTO competitorToAdd,
            DialogCallback<CompetitorWithBoatDTO> callback) {
        super(stringMessages.addCompetitor(), stringMessages, competitorToAdd, callback, null,
                new CompetitorWithOptionalBoatAddDialog.CompetitorWithOptionalBoatValidator(stringMessages));
        initWithBoatCheckBox();
        boatDataPanel = new VerticalPanel();
        boatDataPanel.setVisible(false);
        this.ensureDebugId("CompetitorWithBoatAddDialog");
    }

    private void initWithBoatCheckBox() {
        withBoatCheckBox = new CheckBox();
        withBoatCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boatDataPanel.setVisible(withBoatCheckBox.getValue());
                validateAndUpdate();
            }
        });
        withBoatCheckBox.setValue(false);
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel result = new VerticalPanel();
        result.add(super.getAdditionalWidget());
        result.add(createWithBoatCheckBoxPanel());
        boatDataPanel.add(createHeadlineLabel(getStringMessages().boat()));
        Grid grid = new Grid(5, 2);
        grid.setWidget(0, 0, new Label(getStringMessages().name()));
        grid.setWidget(0, 1, boatNameTextBox);
        grid.setWidget(1, 0, new Label(getStringMessages().sailNumber()));
        grid.setWidget(1, 1, sailIdTextBox);
        grid.setWidget(2, 0, new Label(getStringMessages().boatClass()));
        grid.setWidget(2, 1, boatClassNameTextBox);
        grid.setWidget(3, 0, new Label(getStringMessages().color()));
        grid.setWidget(3, 1, boatDisplayColorTextBox);
        boatDataPanel.add(grid);
        result.add(boatDataPanel);
        return result;
    }

    @Override
    protected CompetitorWithBoatDTO getResult() {
        CompetitorWithBoatDTO competitorWithOptionalBoat = super.getResult();
        if (!withBoatCheckBox.getValue()) {
            competitorWithOptionalBoat.setBoat(null);
        }
        return competitorWithOptionalBoat;
    }

    private HorizontalPanel createWithBoatCheckBoxPanel() {
        HorizontalPanel withBoatCheckBoxPanel = new HorizontalPanel();
        withBoatCheckBoxPanel.add(new Label(getStringMessages().withBoat()));
        withBoatCheckBoxPanel.getElement().getStyle().setMarginTop(10, Unit.PX);
        withBoatCheckBoxPanel.getElement().getStyle().setMarginLeft(20, Unit.PX);
        withBoatCheckBoxPanel.getElement().getStyle().setMarginBottom(10, Unit.PX);
        withBoatCheckBoxPanel.add(withBoatCheckBox);
        return withBoatCheckBoxPanel;
    }

    @Override
    protected void setBoatClassNameEnabled(SuggestBox boatClassNameTextBox, boolean enabled) {
        super.setBoatClassNameEnabled(boatClassNameTextBox, true);
    }

    protected static class CompetitorWithOptionalBoatValidator
            extends CompetitorWithoutBoatValidator<CompetitorWithBoatDTO> {
        protected final StringMessages stringMessages;

        public CompetitorWithOptionalBoatValidator(StringMessages stringMessages) {
            super(stringMessages);
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(CompetitorWithBoatDTO competitorToValidate) {
            String result = super.getErrorMessage(competitorToValidate);
            if (withBoatCheckBox.getValue()) {
                if (result == null) {
                    BoatDTO boatToValidate = competitorToValidate.getBoat();
                    boolean invalidSailId = boatToValidate.getSailId() == null || boatToValidate.getSailId().isEmpty();
                    boolean invalidName = boatToValidate.getName() == null || boatToValidate.getName().isEmpty();
                    if (invalidSailId && invalidName) {
                        result = stringMessages.pleaseEnterASailNumberOrABoatName();
                    } else if (boatToValidate.getColor() != null && boatToValidate.getColor() instanceof InvalidColor) {
                        result = boatToValidate.getColor().getAsHtml();
                    } else if (isBoatClassInvalid(boatToValidate)) {
                        result = stringMessages.pleaseEnterABoatClass();
                    }
                }
            }
            return result;
        }

        private boolean isBoatClassInvalid(BoatDTO boatToValidate) {
            return boatToValidate.getBoatClass().getName() == null || boatToValidate.getBoatClass().getName().isEmpty();
        }

    }

}
