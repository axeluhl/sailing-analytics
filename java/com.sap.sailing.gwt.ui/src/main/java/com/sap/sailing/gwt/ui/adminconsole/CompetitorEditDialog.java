package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.CountryCodeFactory;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
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
public class CompetitorEditDialog extends DataEntryDialog<CompetitorDTO> {
    private final CompetitorDTO competitorToEdit;
    private final TextBox name;
    private final ListBox threeLetterIocCountryCode;
    private final TextBox sailId;
    private final StringMessages stringMessages;
    
    public CompetitorEditDialog(final StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback) {
        super(stringMessages.editCompetitor(), stringMessages.editCompetitor(), stringMessages.ok(), stringMessages
                .cancel(), new Validator<CompetitorDTO>() {
                    @Override
                    public String getErrorMessage(CompetitorDTO valueToValidate) {
                        final String result;
                        if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                            result = stringMessages.pleaseEnterAName();
                        } else if (valueToValidate.getSailID() == null || valueToValidate.getSailID().isEmpty()) {
                            result = stringMessages.pleaseEnterASailNumber();
                        } else {
                            result = null;
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.stringMessages = stringMessages;
        this.competitorToEdit = competitorToEdit;
        this.name = new TextBox();
        this.name.setText(competitorToEdit.getName());
        this.threeLetterIocCountryCode = new ListBox();
        CountryCodeFactory ccf = CountryCodeFactory.INSTANCE;
        int i=0;
        for (CountryCode cc : ccf.getAll()) {
            this.threeLetterIocCountryCode.addItem(cc.getThreeLetterIOCCode()+" "+cc.getName(), cc.getThreeLetterIOCCode());
            if (cc.getThreeLetterIOCCode().equals(competitorToEdit.getThreeLetterIocCountryCode())) {
                this.threeLetterIocCountryCode.setSelectedIndex(i);
            }
        }
        this.sailId = new TextBox();
        this.sailId.setText(competitorToEdit.getSailID());
    }

    @Override
    protected CompetitorDTO getResult() {
        CompetitorDTO result = new CompetitorDTOImpl(name.getText(),
                /* twoLetterIsoCountryCode */ null,
                threeLetterIocCountryCode.getValue(threeLetterIocCountryCode.getSelectedIndex()),
                /* countryName */null, sailId.getText(), competitorToEdit.getIdAsString(),
                competitorToEdit.getBoatClass());
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(3, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, name);
        result.setWidget(1, 0, new Label(stringMessages.sailNumber()));
        result.setWidget(1, 1, sailId);
        result.setWidget(2, 0, new Label(stringMessages.nationality()));
        result.setWidget(2, 1, threeLetterIocCountryCode);
        return result;
    }

}
