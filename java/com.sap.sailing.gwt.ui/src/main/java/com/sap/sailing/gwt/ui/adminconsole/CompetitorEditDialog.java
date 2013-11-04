package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class CompetitorEditDialog extends DataEntryDialog<CompetitorDTO> {
    private final CompetitorDTO competitorToEdit;
    private final TextBox name;
    private final ListBox twoLetterIsoCountryCode;
    private final ListBox threeLetterIocCountryCode;
    private final TextBox sailId;
    
    public CompetitorEditDialog(StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback) {
        super(stringMessages.editCompetitor(), stringMessages.editCompetitor(), stringMessages.ok(), stringMessages
                .cancel(), new Validator<CompetitorDTO>() {
                    @Override
                    public String getErrorMessage(CompetitorDTO valueToValidate) {
                        // TODO Auto-generated method stub
                        return null;
                    }
                }, /* animationEnabled */true, callback);
        this.competitorToEdit = competitorToEdit;
        this.name = new TextBox();
        this.name.setText(competitorToEdit.getName());
        this.twoLetterIsoCountryCode = new ListBox();
        this.twoLetterIsoCountryCode.addItem(competitorToEdit.getTwoLetterIsoCountryCode());
        this.threeLetterIocCountryCode = new ListBox();
        this.threeLetterIocCountryCode.addItem(competitorToEdit.getThreeLetterIocCountryCode());
        this.sailId = new TextBox();
        this.sailId.setText(competitorToEdit.getSailID());
    }

    @Override
    protected CompetitorDTO getResult() {
        CompetitorDTO result = new CompetitorDTOImpl(name.getText(),
                twoLetterIsoCountryCode.getValue(twoLetterIsoCountryCode.getSelectedIndex()),
                threeLetterIocCountryCode.getValue(threeLetterIocCountryCode.getSelectedIndex()),
                /* countryName */null, sailId.getText(), competitorToEdit.getIdAsString(),
                competitorToEdit.getBoatClass());
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        // TODO Auto-generated method stub
        return super.getAdditionalWidget();
    }

}
